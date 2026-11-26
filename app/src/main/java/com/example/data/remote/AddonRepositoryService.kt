package com.example.data.remote

import com.example.data.local.AddonRepositoryEntity
import com.example.data.local.MediaItemEntity
import com.example.data.model.EpisodeItem
import com.example.data.model.LanguageTrack
import com.example.data.model.MediaCategory
import com.example.data.model.RepoManifestDto
import com.example.data.model.RepoMediaItemDto
import com.example.data.model.SupportedLanguages
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ParsedRepositoryResult(
    val repository: AddonRepositoryEntity,
    val items: List<MediaItemEntity>
)

class AddonRepositoryService(
    private val moshi: Moshi
) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val manifestAdapter = moshi.adapter(RepoManifestDto::class.java)
    private val itemListAdapter = moshi.adapter<List<RepoMediaItemDto>>(
        Types.newParameterizedType(List::class.java, RepoMediaItemDto::class.java)
    )
    private val epAdapter = moshi.adapter<List<EpisodeItem>>(
        Types.newParameterizedType(List::class.java, EpisodeItem::class.java)
    )
    private val langAdapter = moshi.adapter<List<LanguageTrack>>(
        Types.newParameterizedType(List::class.java, LanguageTrack::class.java)
    )

    suspend fun fetchFromUrl(
        rawUrl: String,
        existingRepoId: String? = null,
        customName: String? = null
    ): Result<ParsedRepositoryResult> = withContext(Dispatchers.IO) {
        val url = rawUrl.trim()
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            return@withContext Result.failure(IllegalArgumentException("Enter a valid http:// or https:// repository URL."))
        }

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "CineGlass/1.0 (Android; OpenCatalog)")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IllegalStateException("Repository returned HTTP ${response.code}")
                    )
                }
                val body = response.body?.string().orEmpty().trim()
                if (body.isBlank()) {
                    return@withContext Result.failure(IllegalStateException("Repository returned an empty response."))
                }

                // 1. Check if it's an M3U / M3U8 playlist
                if (body.startsWith("#EXTM3U", ignoreCase = true) || url.endsWith(".m3u", true) || url.endsWith(".m3u8", true)) {
                    val parsed = parseM3uPlaylist(
                        content = body,
                        sourceUrl = url,
                        repoId = existingRepoId ?: "repo_${UUID.randomUUID().toString().take(8)}",
                        customName = customName
                    )
                    return@withContext Result.success(parsed)
                }

                // 2. Otherwise parse JSON manifest or JSON item array
                parseJsonManifest(
                    rawJson = body,
                    manifestUrl = url,
                    repoId = existingRepoId ?: "repo_${UUID.randomUUID().toString().take(8)}",
                    customName = customName
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseJsonManifest(
        rawJson: String,
        manifestUrl: String = "local://json-import",
        repoId: String = "repo_${UUID.randomUUID().toString().take(8)}",
        customName: String? = null
    ): Result<ParsedRepositoryResult> {
        val trimmed = rawJson.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("JSON manifest cannot be empty."))
        }

        return try {
            val hostFallback = runCatching { URI(manifestUrl).host }.getOrNull()?.removePrefix("www.") ?: "Custom Repo"
            val dtoList: List<RepoMediaItemDto>
            val repoName: String
            val repoDesc: String
            val repoAuthor: String
            val siteUrl: String
            val badge: String

            if (trimmed.startsWith("[")) {
                dtoList = itemListAdapter.fromJson(trimmed).orEmpty()
                repoName = customName?.takeIf { it.isNotBlank() } ?: hostFallback
                repoDesc = "Imported JSON streaming catalog from $hostFallback"
                repoAuthor = hostFallback
                siteUrl = manifestUrl
                badge = "JSON"
            } else {
                val manifest = manifestAdapter.fromJson(trimmed)
                    ?: return Result.failure(IllegalArgumentException("Could not parse JSON repository schema."))
                dtoList = manifest.items ?: manifest.catalog ?: manifest.movies ?: emptyList()
                repoName = customName?.takeIf { it.isNotBlank() } ?: manifest.name?.takeIf { it.isNotBlank() } ?: hostFallback
                repoDesc = manifest.description?.takeIf { it.isNotBlank() }
                    ?: "Declarative streaming catalog synced from $hostFallback"
                repoAuthor = manifest.author?.takeIf { it.isNotBlank() } ?: hostFallback
                siteUrl = manifest.siteUrl?.takeIf { it.isNotBlank() } ?: manifestUrl
                badge = manifest.iconBadge?.takeIf { it.isNotBlank() }?.take(4)?.uppercase() ?: "REPO"
            }

            if (dtoList.isEmpty()) {
                return Result.failure(IllegalArgumentException("Manifest has no playable items in 'items', 'catalog', or 'movies'."))
            }

            val now = System.currentTimeMillis()
            val mappedItems = dtoList.mapIndexedNotNull { idx, dto ->
                val playable = (dto.streamUrl ?: dto.url ?: dto.hlsUrl)?.trim().orEmpty()
                if (playable.isBlank()) return@mapIndexedNotNull null
                val category = MediaCategory.fromKey(dto.category)
                val fallbackKey = when (category) {
                    MediaCategory.MOVIES -> "movies_hero"
                    MediaCategory.WEB_SERIES -> "series_hero"
                    MediaCategory.ANIME -> "anime_hero"
                }
                val isHls = playable.contains(".m3u8", ignoreCase = true)
                val langs = SupportedLanguages.fromNames(dto.languages ?: listOf("English", "Hindi"))
                MediaItemEntity(
                    id = dto.id?.takeIf { it.isNotBlank() } ?: "${repoId}_item_${idx}_${UUID.randomUUID().toString().take(5)}",
                    title = dto.title?.takeIf { it.isNotBlank() } ?: "Stream #${idx + 1}",
                    category = category.key,
                    synopsis = dto.synopsis ?: dto.description ?: "Streamed live via $repoName repository add-on.",
                    year = dto.year ?: 2026,
                    rating = (dto.rating ?: 8.4).coerceIn(1.0, 10.0),
                    durationLabel = dto.duration?.takeIf { it.isNotBlank() } ?: "${dto.maxQuality ?: "1080p"} • Stream",
                    genresCsv = dto.genres?.joinToString(",")?.ifBlank { "Featured" } ?: "Featured",
                    posterUrl = dto.posterUrl.orEmpty(),
                    localPosterKey = fallbackKey,
                    backdropUrl = dto.backdropUrl ?: dto.posterUrl.orEmpty(),
                    streamUrl = if (!isHls) playable else (dto.streamUrl ?: playable),
                    hlsUrl = dto.hlsUrl ?: if (isHls) playable else "",
                    maxQuality = dto.maxQuality?.takeIf { it.isNotBlank() } ?: "1080p",
                    languagesJson = langAdapter.toJson(langs),
                    episodesJson = epAdapter.toJson(dto.episodes.orEmpty()),
                    repoId = repoId,
                    repoName = repoName,
                    isUserUploaded = false,
                    createdAt = now - idx * 1000L
                )
            }

            if (mappedItems.isEmpty()) {
                return Result.failure(IllegalArgumentException("No valid stream URLs found inside repository items."))
            }

            val categoriesCsv = mappedItems.map { it.category }.distinct().joinToString(",")
            val repo = AddonRepositoryEntity(
                id = repoId,
                name = repoName,
                description = repoDesc,
                manifestUrl = manifestUrl,
                siteUrl = siteUrl,
                author = repoAuthor,
                iconBadge = badge,
                categoriesCsv = categoriesCsv,
                isEnabled = true,
                isBuiltIn = false,
                itemCount = mappedItems.size,
                lastSyncedAt = now,
                lastError = null
            )
            Result.success(ParsedRepositoryResult(repo, mappedItems))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Invalid JSON format: ${e.message}"))
        }
    }

    private fun parseM3uPlaylist(
        content: String,
        sourceUrl: String,
        repoId: String,
        customName: String?
    ): ParsedRepositoryResult {
        val host = runCatching { URI(sourceUrl).host }.getOrNull()?.removePrefix("www.") ?: "HLS Feed"
        val repoName = customName?.takeIf { it.isNotBlank() } ?: "$host Playlist"
        val lines = content.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val now = System.currentTimeMillis()
        val parsedItems = mutableListOf<MediaItemEntity>()

        // Detect master HLS playlist (contains EXT-X-STREAM-INF -> treat the URL itself as a single adaptive stream)
        val isMasterHls = lines.any { it.startsWith("#EXT-X-STREAM-INF", ignoreCase = true) || it.startsWith("#EXT-X-TARGETDURATION", ignoreCase = true) }
        if (isMasterHls) {
            parsedItems += MediaItemEntity(
                id = "${repoId}_hls_master",
                title = customName?.takeIf { it.isNotBlank() } ?: "Adaptive HLS Stream ($host)",
                category = MediaCategory.MOVIES.key,
                synopsis = "Adaptive bitrate HLS stream linked directly from $sourceUrl.",
                year = 2026,
                rating = 8.6,
                durationLabel = "HLS • Adaptive HD",
                genresCsv = "HLS,Live Stream",
                posterUrl = "",
                localPosterKey = "movies_hero",
                backdropUrl = "",
                streamUrl = sourceUrl,
                hlsUrl = sourceUrl,
                maxQuality = "4K",
                languagesJson = langAdapter.toJson(SupportedLanguages.fromNames(listOf("English", "Hindi", "Japanese", "Spanish"))),
                episodesJson = "[]",
                repoId = repoId,
                repoName = repoName,
                createdAt = now
            )
        } else {
            var pendingTitle: String? = null
            var pendingCategory = MediaCategory.MOVIES
            var pendingPoster = ""
            lines.forEach { line ->
                if (line.startsWith("#EXTINF", ignoreCase = true)) {
                    val commaIdx = line.lastIndexOf(',')
                    pendingTitle = if (commaIdx >= 0) line.substring(commaIdx + 1).trim() else "Stream"
                    val lower = line.lowercase()
                    pendingCategory = when {
                        lower.contains("anime") -> MediaCategory.ANIME
                        lower.contains("series") || lower.contains("show") -> MediaCategory.WEB_SERIES
                        else -> MediaCategory.MOVIES
                    }
                    val logoMatch = Regex("""tvg-logo="([^"]+)"""").find(line)
                    pendingPoster = logoMatch?.groupValues?.getOrNull(1).orEmpty()
                } else if (line.startsWith("http://", true) || line.startsWith("https://", true)) {
                    val idx = parsedItems.size
                    val isHls = line.contains(".m3u8", true)
                    val fallbackKey = when (pendingCategory) {
                        MediaCategory.MOVIES -> "movies_hero"
                        MediaCategory.WEB_SERIES -> "series_hero"
                        MediaCategory.ANIME -> "anime_hero"
                    }
                    parsedItems += MediaItemEntity(
                        id = "${repoId}_m3u_$idx",
                        title = pendingTitle?.takeIf { it.isNotBlank() } ?: "Channel Stream #${idx + 1}",
                        category = pendingCategory.key,
                        synopsis = "Imported from M3U playlist ($host).",
                        year = 2026,
                        rating = 8.3,
                        durationLabel = if (isHls) "HLS • HD" else "HD Stream",
                        genresCsv = "Playlist,Stream",
                        posterUrl = pendingPoster,
                        localPosterKey = fallbackKey,
                        backdropUrl = pendingPoster,
                        streamUrl = line,
                        hlsUrl = if (isHls) line else "",
                        maxQuality = "1080p",
                        languagesJson = langAdapter.toJson(SupportedLanguages.fromNames(listOf("English", "Hindi", "Japanese"))),
                        episodesJson = "[]",
                        repoId = repoId,
                        repoName = repoName,
                        createdAt = now - idx * 500L
                    )
                    pendingTitle = null
                    pendingPoster = ""
                }
            }
        }

        val repo = AddonRepositoryEntity(
            id = repoId,
            name = repoName,
            description = "M3U/HLS streaming playlist linked from $host",
            manifestUrl = sourceUrl,
            siteUrl = sourceUrl,
            author = host,
            iconBadge = "M3U",
            categoriesCsv = parsedItems.map { it.category }.distinct().joinToString(",").ifBlank { "MOVIES" },
            isEnabled = true,
            isBuiltIn = false,
            itemCount = parsedItems.size,
            lastSyncedAt = now,
            lastError = null
        )
        return ParsedRepositoryResult(repo, parsedItems)
    }
}
