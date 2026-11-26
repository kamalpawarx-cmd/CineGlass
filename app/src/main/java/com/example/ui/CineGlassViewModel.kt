package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.download.OfflineDownloadManager
import com.example.data.local.AddonRepositoryEntity
import com.example.data.local.DownloadEntity
import com.example.data.model.ActivePlaybackSession
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaCategory
import com.example.data.model.MediaItemUi
import com.example.data.model.StreamQuality
import com.example.data.repository.CineGlassRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class BottomTab(val route: String, val label: String) {
    MOVIES("movies", "Movies"),
    WEB_SERIES("series", "Web Series"),
    ANIME("anime", "Anime"),
    ADDONS("addons", "Add-ons"),
    UPLOAD("upload", "Upload")
}

data class CineGlassUiState(
    val selectedTab: BottomTab = BottomTab.MOVIES,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedLanguageFilter: String = "All",
    val selectedGenreFilter: String = "All",
    val movies: List<MediaItemUi> = emptyList(),
    val webSeries: List<MediaItemUi> = emptyList(),
    val anime: List<MediaItemUi> = emptyList(),
    val continueWatching: List<MediaItemUi> = emptyList(),
    val bookmarks: List<MediaItemUi> = emptyList(),
    val userUploads: List<MediaItemUi> = emptyList(),
    val repositories: List<AddonRepositoryEntity> = emptyList(),
    val downloads: List<DownloadEntity> = emptyList(),
    val selectedMediaId: String? = null,
    val showDownloadsScreen: Boolean = false,
    val activePlayback: ActivePlaybackSession? = null,
    val isSyncingRepo: Boolean = false,
    val isUploading: Boolean = false,
    val statusMessage: String? = null
) {
    val allCatalog: List<MediaItemUi>
        get() = movies + webSeries + anime

    val selectedMedia: MediaItemUi?
        get() = selectedMediaId?.let { id -> allCatalog.firstOrNull { it.id == id } ?: userUploads.firstOrNull { it.id == id } }

    val activeDownloadCount: Int
        get() = downloads.count {
            it.status == DownloadStatus.DOWNLOADING.key || it.status == DownloadStatus.QUEUED.key
        }

    val completedDownloadCount: Int
        get() = downloads.count { it.status == DownloadStatus.COMPLETED.key }
}

class CineGlassViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CineGlassRepository(application.applicationContext)

    private val _selectedTab = MutableStateFlow(BottomTab.MOVIES)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    private val _languageFilter = MutableStateFlow("All")
    private val _genreFilter = MutableStateFlow("All")
    private val _selectedMediaId = MutableStateFlow<String?>(null)
    private val _showDownloadsScreen = MutableStateFlow(false)
    private val _activePlayback = MutableStateFlow<ActivePlaybackSession?>(null)
    private val _isSyncingRepo = MutableStateFlow(false)
    private val _isUploading = MutableStateFlow(false)
    private val _statusMessage = MutableStateFlow<String?>(null)

    private val filterFlow = combine(
        _selectedTab,
        _searchQuery,
        _isSearchActive,
        _languageFilter,
        _genreFilter
    ) { tab, query, searchActive, lang, genre ->
        FilterBundle(tab, query, searchActive, lang, genre)
    }

    private val overlayFlow = combine(
        _selectedMediaId,
        _showDownloadsScreen,
        _activePlayback,
        _isSyncingRepo,
        _isUploading
    ) { selectedId, showDownloads, playback, syncing, uploading ->
        OverlayBundle(selectedId, showDownloads, playback, syncing, uploading)
    }

    private val dataFlow = combine(
        repository.activeCatalog,
        repository.userUploads,
        repository.repositories,
        repository.downloads
    ) { catalog, uploads, repos, downloads ->
        DataBundle(catalog, uploads, repos, downloads)
    }

    val uiState: StateFlow<CineGlassUiState> = combine(
        filterFlow,
        overlayFlow,
        dataFlow,
        _statusMessage
    ) { filters, overlays, data, message ->
        val movies = data.catalog.filter { it.category == MediaCategory.MOVIES }
        val series = data.catalog.filter { it.category == MediaCategory.WEB_SERIES }
        val anime = data.catalog.filter { it.category == MediaCategory.ANIME }
        val continueWatching = data.catalog
            .filter { it.watchProgressMs > 3_000L && it.progressFraction < 0.96f }
            .sortedByDescending { it.watchProgressMs }
        val bookmarks = data.catalog.filter { it.isBookmarked }

        CineGlassUiState(
            selectedTab = filters.tab,
            searchQuery = filters.searchQuery,
            isSearchActive = filters.isSearchActive,
            selectedLanguageFilter = filters.languageFilter,
            selectedGenreFilter = filters.genreFilter,
            movies = movies,
            webSeries = series,
            anime = anime,
            continueWatching = continueWatching,
            bookmarks = bookmarks,
            userUploads = data.uploads,
            repositories = data.repos,
            downloads = data.downloads,
            selectedMediaId = overlays.selectedMediaId,
            showDownloadsScreen = overlays.showDownloadsScreen,
            activePlayback = overlays.activePlayback,
            isSyncingRepo = overlays.isSyncingRepo,
            isUploading = overlays.isUploading,
            statusMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = CineGlassUiState()
    )

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
        }
    }

    fun selectTab(tab: BottomTab) {
        _selectedTab.value = tab
        _genreFilter.value = "All"
        _showDownloadsScreen.value = false
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectLanguageFilter(language: String) {
        _languageFilter.value = language
    }

    fun selectGenreFilter(genre: String) {
        _genreFilter.value = genre
    }

    fun openMediaDetail(mediaId: String) {
        _selectedMediaId.value = mediaId
    }

    fun closeMediaDetail() {
        _selectedMediaId.value = null
    }

    fun setShowDownloads(show: Boolean) {
        _showDownloadsScreen.value = show
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun toggleBookmark(media: MediaItemUi) {
        viewModelScope.launch {
            val next = !media.isBookmarked
            repository.toggleBookmark(media.id, next)
            _statusMessage.value = if (next) "Saved '${media.title}' to Watchlist" else "Removed from Watchlist"
        }
    }

    fun playMedia(
        media: MediaItemUi,
        episodeIndex: Int? = null,
        preferredQuality: StreamQuality = StreamQuality.AUTO,
        preferredLanguage: String = media.languages.firstOrNull()?.name ?: "English"
    ) {
        val episode = episodeIndex?.let { idx -> media.episodes.firstOrNull { it.index == idx } }
        val downloadId = OfflineDownloadManager.buildDownloadId(media.id, episode?.index)
        val completedDownload = uiState.value.downloads.firstOrNull {
            it.id == downloadId && it.status == DownloadStatus.COMPLETED.key && File(it.localFilePath).exists()
        }

        val resolvedUrl = when {
            completedDownload != null -> Uri.fromFile(File(completedDownload.localFilePath)).toString()
            episode != null && episode.streamUrl.isNotBlank() -> episode.streamUrl
            else -> media.primaryPlayableUrl
        }

        _activePlayback.value = ActivePlaybackSession(
            media = media,
            episodeIndex = episode?.index,
            episodeTitle = episode?.title,
            streamUrl = resolvedUrl,
            isOfflineFile = completedDownload != null || resolvedUrl.startsWith("file://"),
            initialQuality = preferredQuality,
            initialLanguage = preferredLanguage
        )
    }

    fun playOfflineDownload(download: DownloadEntity) {
        val file = File(download.localFilePath)
        if (!file.exists()) {
            _statusMessage.value = "Offline file no longer exists on device."
            return
        }
        val media = uiState.value.allCatalog.firstOrNull { it.id == download.mediaId }
            ?: MediaItemUi(
                id = download.mediaId,
                title = download.title,
                category = MediaCategory.fromKey(download.category),
                synopsis = "Playing offline download (${download.qualityLabel} • ${download.languageLabel}).",
                year = 2026,
                rating = 9.0,
                durationLabel = download.qualityLabel,
                genres = listOf("Offline"),
                posterUrl = download.posterUrl,
                localPosterKey = download.localPosterKey,
                backdropUrl = download.posterUrl,
                streamUrl = Uri.fromFile(file).toString(),
                hlsUrl = "",
                maxQuality = download.qualityLabel,
                languages = listOf(com.example.data.model.LanguageTrack("en", download.languageLabel, "Offline")),
                episodes = emptyList(),
                repoId = "offline",
                repoName = "Offline Storage",
                isUserUploaded = false,
                isBookmarked = false,
                watchProgressMs = 0L,
                totalDurationMs = 0L,
                createdAt = download.updatedAt
            )

        _activePlayback.value = ActivePlaybackSession(
            media = media,
            episodeIndex = download.episodeIndex,
            episodeTitle = download.subtitleLabel,
            streamUrl = Uri.fromFile(file).toString(),
            isOfflineFile = true,
            initialQuality = StreamQuality.fromBadge(download.qualityLabel),
            initialLanguage = download.languageLabel
        )
    }

    fun closePlayer() {
        _activePlayback.value = null
    }

    fun saveWatchProgress(mediaId: String, positionMs: Long, durationMs: Long) {
        viewModelScope.launch {
            repository.saveWatchProgress(mediaId, positionMs, durationMs)
        }
    }

    fun downloadMedia(
        media: MediaItemUi,
        episodeIndex: Int? = null,
        quality: StreamQuality = StreamQuality.FHD_1080,
        language: String = media.languages.firstOrNull()?.name ?: "English"
    ) {
        val episode = episodeIndex?.let { idx -> media.episodes.firstOrNull { it.index == idx } }
        val url = episode?.streamUrl?.takeIf { it.isNotBlank() } ?: media.downloadableUrl
        repository.startOfflineDownload(
            media = media,
            episodeIndex = episode?.index,
            episodeTitle = episode?.title,
            streamUrl = url,
            qualityLabel = quality.badge,
            languageLabel = language
        )
        _statusMessage.value = "Downloading ${media.title} (${quality.badge}, $language)..."
    }

    fun cancelDownload(downloadId: String) {
        repository.cancelDownload(downloadId)
        _statusMessage.value = "Download cancelled"
    }

    fun deleteDownload(downloadId: String) {
        viewModelScope.launch {
            repository.deleteDownload(downloadId)
            _statusMessage.value = "Removed offline download"
        }
    }

    fun toggleRepository(repoId: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleRepository(repoId, enabled)
        }
    }

    fun syncRepository(repo: AddonRepositoryEntity) {
        viewModelScope.launch {
            _isSyncingRepo.value = true
            val res = repository.syncRepository(repo)
            _isSyncingRepo.value = false
            _statusMessage.value = res.fold(
                onSuccess = { it },
                onFailure = { "Sync failed: ${it.message}" }
            )
        }
    }

    fun addRepositoryUrl(url: String, customName: String) {
        viewModelScope.launch {
            _isSyncingRepo.value = true
            val res = repository.addRepositoryFromUrl(url, customName.takeIf { it.isNotBlank() })
            _isSyncingRepo.value = false
            _statusMessage.value = res.fold(
                onSuccess = { it },
                onFailure = { it.message ?: "Failed to add repository" }
            )
        }
    }

    fun importRepositoryJson(rawJson: String, customName: String) {
        viewModelScope.launch {
            _isSyncingRepo.value = true
            val res = repository.importRepositoryJson(rawJson, customName.takeIf { it.isNotBlank() })
            _isSyncingRepo.value = false
            _statusMessage.value = res.fold(
                onSuccess = { it },
                onFailure = { it.message ?: "Failed to parse JSON manifest" }
            )
        }
    }

    fun linkStreamingSite(
        siteName: String,
        title: String,
        streamUrl: String,
        category: MediaCategory,
        quality: String,
        languages: List<String>,
        synopsis: String
    ) {
        viewModelScope.launch {
            val res = repository.linkDirectStreamingSource(
                siteName = siteName,
                title = title,
                streamUrl = streamUrl,
                category = category,
                quality = quality,
                languages = languages,
                synopsis = synopsis
            )
            _statusMessage.value = res.fold(
                onSuccess = { "Linked '${it.title}' to ${it.category.displayLabel}" },
                onFailure = { it.message ?: "Could not link stream" }
            )
        }
    }

    fun deleteRepository(repoId: String) {
        viewModelScope.launch {
            repository.deleteRepository(repoId)
            _statusMessage.value = "Repository removed"
        }
    }

    fun uploadUserMedia(
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
        episodeTitle: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            val res = repository.uploadUserMedia(
                title = title,
                category = category,
                genre = genre,
                year = year,
                quality = quality,
                languages = languages,
                synopsis = synopsis,
                videoUri = videoUri,
                remoteStreamUrl = remoteStreamUrl,
                posterUri = posterUri,
                episodeTitle = episodeTitle
            )
            _isUploading.value = false
            res.fold(
                onSuccess = {
                    _statusMessage.value = "Published '${it.title}' to ${it.category.displayLabel}!"
                    onSuccess()
                },
                onFailure = {
                    _statusMessage.value = it.message ?: "Upload failed"
                }
            )
        }
    }

    fun deleteUserUpload(media: MediaItemUi) {
        viewModelScope.launch {
            repository.deleteUserUpload(media)
            _statusMessage.value = "Deleted '${media.title}'"
        }
    }

    private data class FilterBundle(
        val tab: BottomTab,
        val searchQuery: String,
        val isSearchActive: Boolean,
        val languageFilter: String,
        val genreFilter: String
    )

    private data class OverlayBundle(
        val selectedMediaId: String?,
        val showDownloadsScreen: Boolean,
        val activePlayback: ActivePlaybackSession?,
        val isSyncingRepo: Boolean,
        val isUploading: Boolean
    )

    private data class DataBundle(
        val catalog: List<MediaItemUi>,
        val uploads: List<MediaItemUi>,
        val repos: List<AddonRepositoryEntity>,
        val downloads: List<DownloadEntity>
    )
}
