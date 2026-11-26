package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.download.OfflineDownloadManager
import com.example.data.local.AddonRepositoryEntity
import com.example.data.local.CineGlassDatabase
import com.example.data.local.DownloadEntity
import com.example.data.local.MediaItemEntity
import com.example.data.model.EpisodeItem
import com.example.data.model.LanguageTrack
import com.example.data.model.MediaCategory
import com.example.data.model.MediaItemUi
import com.example.data.model.SupportedLanguages
import com.example.data.remote.AddonRepositoryService
import com.example.data.seed.OpenCinemaCatalog
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.util.UUID

class CineGlassRepository(
    private val context: Context
) {
    private val db = CineGlassDatabase.getInstance(context)
    private val mediaDao = db.mediaItemDao()
    private val repoDao = db.addonRepositoryDao()
    private val downloadDao = db.downloadDao()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val epAdapter = moshi.adapter<List<EpisodeItem>>(
        Types.newParameterizedType(List::class.java, EpisodeItem::class.java)
    )
    private val langAdapter = moshi.adapter<List<LanguageTrack>>(
        Types.newParameterizedType(List::class.java, LanguageTrack::class.java)
    )

    private val addonService = AddonRepositoryService(moshi)
    private val downloadManager = OfflineDownloadManager(context, downloadDao)

    val activeCatalog: Flow<List<MediaItemUi>> = mediaDao.observeActiveCatalog().map { list ->
        list.map { it.toUiModel() }
    }

    val userUploads: Flow<List<MediaItemUi>> = mediaDao.observeUserUploads().map { list ->
        list.map { it.toUiModel() }
    }

    val repositories: Flow<List<AddonRepositoryEntity>> = repoDao.observeRepositories()

    val downloads: Flow<List<DownloadEntity>> = downloadDao.observeDownloads()

    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        if (mediaDao.countAll() == 0) {
            repoDao.upsertAll(OpenCinemaCatalog.starterRepositories())
            mediaDao.upsertAll(OpenCinemaCatalog.starterMedia(moshi))
        }
    }

    suspend fun toggleBookmark(mediaId: String, bookmarked: Boolean) {
        mediaDao.setBookmarked(mediaId, bookmarked)
    }

    suspend fun saveWatchProgress(mediaId: String, positionMs: Long, durationMs: Long) {
        if (durationMs > 0L) {
            mediaDao.updateWatchProgress(mediaId, positionMs, durationMs)
        }
    }

    suspend fun toggleRepository(repoId: String, enabled: Boolean) {
        repoDao.setEnabled(repoId, enabled)
    }

    suspend fun deleteRepository(repoId: String) {
        mediaDao.deleteByRepoId(repoId)
        repoDao.deleteById(repoId)
    }

    suspend fun addRepositoryFromUrl(url: String, customName: String?): Result<String> {
        val result = addonService.fetchFromUrl(rawUrl = url, customName = customName)
        return result.map { parsed ->
            repoDao.upsert(parsed.repository)
            mediaDao.upsertAll(parsed.items)
            "Added ${parsed.repository.name} (${parsed.items.size} streams)"
        }
    }

    suspend fun importRepositoryJson(rawJson: String, customName: String?): Result<String> {
        val result = addonService.parseJsonManifest(rawJson = rawJson, customName = customName)
        return result.map { parsed ->
            repoDao.upsert(parsed.repository)
            mediaDao.upsertAll(parsed.items)
            "Imported ${parsed.repository.name} (${parsed.items.size} titles)"
        }
    }

    suspend fun syncRepository(repo: AddonRepositoryEntity): Result<String> {
        if (repo.isBuiltIn) {
            val now = System.currentTimeMillis()
            repoDao.updateSyncMeta(repo.id, repo.itemCount, now, null)
            return Result.success("${repo.name} is up to date")
        }
        if (repo.manifestUrl.startsWith("local://")) {
            val now = System.currentTimeMillis()
            repoDao.updateSyncMeta(repo.id, repo.itemCount, now, null)
            return Result.success("${repo.name} local manifest verified")
        }
        val result = addonService.fetchFromUrl(
            rawUrl = repo.manifestUrl,
            existingRepoId = repo.id,
            customName = repo.name
        )
        return result.fold(
            onSuccess = { parsed ->
                repoDao.upsert(parsed.repository)
                mediaDao.deleteByRepoId(repo.id)
                mediaDao.upsertAll(parsed.items)
                Result.success("Synced ${parsed.repository.name}: ${parsed.items.size} streams")
            },
            onFailure = { err ->
                repoDao.updateSyncMeta(repo.id, repo.itemCount, System.currentTimeMillis(), err.message)
                Result.failure(err)
            }
        )
    }

    suspend fun linkDirectStreamingSource(
        siteName: String,
        title: String,
        streamUrl: String,
        category: MediaCategory,
        quality: String,
        languages: List<String>,
        synopsis: String
    ): Result<MediaItemUi> = withContext(Dispatchers.IO) {
        val trimmedUrl = streamUrl.trim()
        if (!trimmedUrl.startsWith("http://", true) && !trimmedUrl.startsWith("https://", true)) {
            return@withContext Result.failure(IllegalArgumentException("Enter a valid http:// or https:// stream URL."))
        }
        val cleanTitle = title.trim().ifBlank { "Linked Stream" }
        val host = runCatching { URI(trimmedUrl).host }.getOrNull()?.removePrefix("www.") ?: "Direct Link"
        val cleanSite = siteName.trim().ifBlank { host }
        val repoId = OpenCinemaCatalog.REPO_CUSTOM_LINKS
        val now = System.currentTimeMillis()

        val existingRepo = repoDao.getById(repoId)
        val newCount = (existingRepo?.itemCount ?: 0) + 1
        repoDao.upsert(
            AddonRepositoryEntity(
                id = repoId,
                name = existingRepo?.name ?: "Linked Streaming Sites",
                description = "Direct streaming links & custom site endpoints added by you.",
                manifestUrl = "local://direct-links",
                siteUrl = trimmedUrl,
                author = "Your Add-ons",
                iconBadge = "LINK",
                categoriesCsv = "MOVIES,WEB_SERIES,ANIME",
                isEnabled = true,
                isBuiltIn = false,
                itemCount = newCount,
                lastSyncedAt = now
            )
        )

        val isHls = trimmedUrl.contains(".m3u8", ignoreCase = true)
        val fallbackKey = when (category) {
            MediaCategory.MOVIES -> "movies_hero"
            MediaCategory.WEB_SERIES -> "series_hero"
            MediaCategory.ANIME -> "anime_hero"
        }

        val entity = MediaItemEntity(
            id = "link_${UUID.randomUUID().toString().take(8)}",
            title = cleanTitle,
            category = category.key,
            synopsis = synopsis.trim().ifBlank { "Directly linked from $cleanSite for instant HD playback." },
            year = 2026,
            rating = 8.7,
            durationLabel = "$quality • $cleanSite",
            genresCsv = "Linked,$cleanSite",
            posterUrl = "",
            localPosterKey = fallbackKey,
            backdropUrl = "",
            streamUrl = trimmedUrl,
            hlsUrl = if (isHls) trimmedUrl else "",
            maxQuality = quality,
            languagesJson = langAdapter.toJson(SupportedLanguages.fromNames(languages)),
            episodesJson = "[]",
            repoId = repoId,
            repoName = cleanSite,
            isUserUploaded = false,
            createdAt = now
        )
        mediaDao.upsert(entity)
        Result.success(entity.toUiModel())
    }

    suspend fun uploadUserMedia(
        title: String,
        category: MediaCategory,
        genre: String,
        year: Int,
        quality: String,
        languages: List<String>,
        synopsis: String,
        videoUri: Uri?,
        remoteStreamUrl: String,
        posterUri: Uri?,
        episodeTitle: String? = null
    ): Result<MediaItemUi> = withContext(Dispatchers.IO) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a title."))
        }

        val uploadsDir = File(context.filesDir, "uploads").apply { if (!exists()) mkdirs() }
        val uploadId = "usr_${UUID.randomUUID().toString().take(8)}"

        // Copy picked video from Photo Picker into app-private storage so URI persists across restarts
        val resolvedVideoPath: String = when {
            videoUri != null -> {
                val dest = File(uploadsDir, "${uploadId}_video.mp4")
                val copied = copyUriToFile(videoUri, dest)
                if (!copied) {
                    return@withContext Result.failure(IllegalStateException("Unable to read selected video file."))
                }
                Uri.fromFile(dest).toString()
            }
            remoteStreamUrl.trim().startsWith("http", ignoreCase = true) -> remoteStreamUrl.trim()
            else -> return@withContext Result.failure(
                IllegalArgumentException("Select a video using Photo Picker or paste a valid stream URL.")
            )
        }

        // Copy optional poster image into app-private storage
        val resolvedPosterKey: String = if (posterUri != null) {
            val posterFile = File(uploadsDir, "${uploadId}_poster.jpg")
            if (copyUriToFile(posterUri, posterFile)) {
                Uri.fromFile(posterFile).toString()
            } else {
                defaultLocalPosterKey(category)
            }
        } else {
            defaultLocalPosterKey(category)
        }

        val isHls = resolvedVideoPath.contains(".m3u8", ignoreCase = true)
        val episodes = if (category != MediaCategory.MOVIES) {
            listOf(
                EpisodeItem(
                    index = 1,
                    season = 1,
                    title = episodeTitle?.trim()?.ifBlank { "Episode 1: $cleanTitle" } ?: "Episode 1: $cleanTitle",
                    duration = quality,
                    synopsis = synopsis.trim().ifBlank { "Community uploaded episode." },
                    streamUrl = resolvedVideoPath
                )
            )
        } else {
            emptyList()
        }

        val entity = MediaItemEntity(
            id = uploadId,
            title = cleanTitle,
            category = category.key,
            synopsis = synopsis.trim().ifBlank { "Uploaded directly to your CineGlass Creator Studio." },
            year = year.coerceIn(1920, 2030),
            rating = 9.0,
            durationLabel = if (category == MediaCategory.MOVIES) "$quality • Upload" else "S1 • 1 Episode",
            genresCsv = genre.trim().ifBlank { "Community" },
            posterUrl = if (resolvedPosterKey.startsWith("file://")) resolvedPosterKey else "",
            localPosterKey = resolvedPosterKey,
            backdropUrl = if (resolvedPosterKey.startsWith("file://")) resolvedPosterKey else "",
            streamUrl = resolvedVideoPath,
            hlsUrl = if (isHls) resolvedVideoPath else "",
            maxQuality = quality,
            languagesJson = langAdapter.toJson(SupportedLanguages.fromNames(languages)),
            episodesJson = epAdapter.toJson(episodes),
            repoId = OpenCinemaCatalog.REPO_USER_UPLOADS,
            repoName = "Creator Uploads",
            isUserUploaded = true,
            createdAt = System.currentTimeMillis()
        )
        mediaDao.upsert(entity)
        Result.success(entity.toUiModel())
    }

    suspend fun deleteUserUpload(media: MediaItemUi) = withContext(Dispatchers.IO) {
        if (media.streamUrl.startsWith("file://")) {
            File(media.streamUrl.removePrefix("file://")).takeIf { it.exists() }?.delete()
        }
        if (media.localPosterKey.startsWith("file://")) {
            File(media.localPosterKey.removePrefix("file://")).takeIf { it.exists() }?.delete()
        }
        mediaDao.deleteById(media.id)
    }

    fun startOfflineDownload(
        media: MediaItemUi,
        episodeIndex: Int? = null,
        episodeTitle: String? = null,
        streamUrl: String,
        qualityLabel: String,
        languageLabel: String
    ) {
        downloadManager.startDownload(
            media = media,
            episodeIndex = episodeIndex,
            episodeTitle = episodeTitle,
            streamUrl = streamUrl,
            qualityLabel = qualityLabel,
            languageLabel = languageLabel
        )
    }

    fun cancelDownload(downloadId: String) {
        downloadManager.cancelDownload(downloadId)
    }

    suspend fun deleteDownload(downloadId: String) {
        downloadManager.deleteDownload(downloadId)
    }

    private fun copyUriToFile(uri: Uri, destFile: File): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return false
            destFile.length() > 0L
        } catch (_: Exception) {
            false
        }
    }

    private fun defaultLocalPosterKey(category: MediaCategory): String = when (category) {
        MediaCategory.MOVIES -> "movies_hero"
        MediaCategory.WEB_SERIES -> "series_hero"
        MediaCategory.ANIME -> "anime_hero"
    }

    private fun MediaItemEntity.toUiModel(): MediaItemUi {
        val parsedLanguages = runCatching { langAdapter.fromJson(languagesJson) }.getOrNull().orEmpty()
            .ifEmpty { listOf(SupportedLanguages.all.first()) }
        val parsedEpisodes = runCatching { epAdapter.fromJson(episodesJson) }.getOrNull().orEmpty()
        return MediaItemUi(
            id = id,
            title = title,
            category = MediaCategory.fromKey(category),
            synopsis = synopsis,
            year = year,
            rating = rating,
            durationLabel = durationLabel,
            genres = genresCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
            posterUrl = posterUrl,
            localPosterKey = localPosterKey,
            backdropUrl = backdropUrl,
            streamUrl = streamUrl,
            hlsUrl = hlsUrl,
            maxQuality = maxQuality,
            languages = parsedLanguages,
            episodes = parsedEpisodes,
            repoId = repoId,
            repoName = repoName,
            isUserUploaded = isUserUploaded,
            isBookmarked = isBookmarked,
            watchProgressMs = watchProgressMs,
            totalDurationMs = totalDurationMs,
            createdAt = createdAt
        )
    }
}
