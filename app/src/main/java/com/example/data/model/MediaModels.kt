package com.example.data.model

import com.squareup.moshi.JsonClass

enum class MediaCategory(val key: String, val displayLabel: String) {
    MOVIES("MOVIES", "Movies"),
    WEB_SERIES("WEB_SERIES", "Web Series"),
    ANIME("ANIME", "Anime");

    companion object {
        fun fromKey(raw: String?): MediaCategory {
            val normalized = raw?.trim()?.uppercase() ?: return MOVIES
            return when {
                normalized.contains("SERIES") || normalized.contains("SHOW") || normalized.contains("TV") -> WEB_SERIES
                normalized.contains("ANIME") -> ANIME
                else -> MOVIES
            }
        }
    }
}

enum class StreamQuality(val label: String, val maxWidth: Int, val maxHeight: Int, val badge: String) {
    AUTO("Auto (Adaptive)", Int.MAX_VALUE, Int.MAX_VALUE, "AUTO"),
    UHD_4K("2160p 4K Ultra HD", 3840, 2160, "4K"),
    FHD_1080("1080p Full HD", 1920, 1080, "1080p"),
    HD_720("720p HD", 1280, 720, "720p"),
    SD_480("480p Data Saver", 854, 480, "480p");

    companion object {
        fun fromBadge(raw: String): StreamQuality {
            return entries.firstOrNull {
                it.badge.equals(raw, ignoreCase = true) || it.label.contains(raw, ignoreCase = true)
            } ?: FHD_1080
        }
    }
}

enum class DownloadStatus(val key: String) {
    QUEUED("QUEUED"),
    DOWNLOADING("DOWNLOADING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED");

    companion object {
        fun fromKey(key: String): DownloadStatus =
            entries.firstOrNull { it.key == key } ?: QUEUED
    }
}

@JsonClass(generateAdapter = true)
data class EpisodeItem(
    val index: Int = 1,
    val season: Int = 1,
    val title: String = "Episode 1",
    val duration: String = "24m",
    val synopsis: String = "",
    val streamUrl: String = "",
    val thumbnailUrl: String = ""
)

@JsonClass(generateAdapter = true)
data class LanguageTrack(
    val code: String = "en",
    val name: String = "English",
    val type: String = "Audio + Sub",
    val streamUrlOverride: String? = null
)

@JsonClass(generateAdapter = true)
data class RepoManifestDto(
    val name: String? = null,
    val description: String? = null,
    val author: String? = null,
    val siteUrl: String? = null,
    val iconBadge: String? = null,
    val items: List<RepoMediaItemDto>? = null,
    val catalog: List<RepoMediaItemDto>? = null,
    val movies: List<RepoMediaItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class RepoMediaItemDto(
    val id: String? = null,
    val title: String? = null,
    val category: String? = null,
    val synopsis: String? = null,
    val description: String? = null,
    val year: Int? = null,
    val rating: Double? = null,
    val duration: String? = null,
    val genres: List<String>? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val streamUrl: String? = null,
    val url: String? = null,
    val hlsUrl: String? = null,
    val maxQuality: String? = null,
    val languages: List<String>? = null,
    val episodes: List<EpisodeItem>? = null
)

data class MediaItemUi(
    val id: String,
    val title: String,
    val category: MediaCategory,
    val synopsis: String,
    val year: Int,
    val rating: Double,
    val durationLabel: String,
    val genres: List<String>,
    val posterUrl: String,
    val localPosterKey: String,
    val backdropUrl: String,
    val streamUrl: String,
    val hlsUrl: String,
    val maxQuality: String,
    val languages: List<LanguageTrack>,
    val episodes: List<EpisodeItem>,
    val repoId: String,
    val repoName: String,
    val isUserUploaded: Boolean,
    val isBookmarked: Boolean,
    val watchProgressMs: Long,
    val totalDurationMs: Long,
    val createdAt: Long
) {
    val progressFraction: Float
        get() = if (totalDurationMs > 0L) (watchProgressMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val primaryPlayableUrl: String
        get() = hlsUrl.ifBlank { streamUrl }

    val downloadableUrl: String
        get() = streamUrl.ifBlank { hlsUrl }
}

data class ActivePlaybackSession(
    val media: MediaItemUi,
    val episodeIndex: Int? = null,
    val episodeTitle: String? = null,
    val streamUrl: String,
    val isOfflineFile: Boolean = false,
    val initialQuality: StreamQuality = StreamQuality.AUTO,
    val initialLanguage: String = "English"
)

object SupportedLanguages {
    val all = listOf(
        LanguageTrack("en", "English", "Dolby Audio + CC"),
        LanguageTrack("hi", "Hindi", "Dubbed + CC"),
        LanguageTrack("ja", "Japanese", "Original + Sub"),
        LanguageTrack("es", "Spanish", "Audio + Sub"),
        LanguageTrack("fr", "French", "Audio + Sub"),
        LanguageTrack("ko", "Korean", "Audio + Sub"),
        LanguageTrack("de", "German", "Audio + Sub"),
        LanguageTrack("ar", "Arabic", "Subtitles")
    )

    fun fromNames(names: List<String>): List<LanguageTrack> {
        if (names.isEmpty()) return listOf(all.first())
        return names.map { name ->
            all.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) || it.code.equals(name.trim(), ignoreCase = true) }
                ?: LanguageTrack(
                    code = name.trim().take(2).lowercase().ifBlank { "en" },
                    name = name.trim(),
                    type = "Audio + Sub"
                )
        }.distinctBy { it.name }
    }
}
