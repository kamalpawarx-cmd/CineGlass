package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // MOVIES, WEB_SERIES, ANIME
    val synopsis: String,
    val year: Int,
    val rating: Double,
    val durationLabel: String,
    val genresCsv: String,
    val posterUrl: String,
    val localPosterKey: String, // movies_hero, series_hero, anime_hero, upload_studio, or file:// path
    val backdropUrl: String,
    val streamUrl: String,
    val hlsUrl: String,
    val maxQuality: String, // 4K, 1080p, 720p
    val languagesJson: String,
    val episodesJson: String,
    val repoId: String,
    val repoName: String,
    val isUserUploaded: Boolean = false,
    val isBookmarked: Boolean = false,
    val watchProgressMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "addon_repositories")
data class AddonRepositoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val manifestUrl: String,
    val siteUrl: String,
    val author: String,
    val iconBadge: String,
    val categoriesCsv: String,
    val isEnabled: Boolean = true,
    val isBuiltIn: Boolean = false,
    val itemCount: Int = 0,
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val lastError: String? = null
)

@Entity(tableName = "offline_downloads")
data class DownloadEntity(
    @PrimaryKey val id: String, // "${mediaId}_${episodeIndex ?: 0}"
    val mediaId: String,
    val episodeIndex: Int? = null,
    val title: String,
    val subtitleLabel: String,
    val category: String,
    val posterUrl: String,
    val localPosterKey: String,
    val sourceUrl: String,
    val localFilePath: String,
    val qualityLabel: String,
    val languageLabel: String,
    val status: String, // QUEUED, DOWNLOADING, COMPLETED, FAILED, CANCELLED
    val progress: Int = 0,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)
