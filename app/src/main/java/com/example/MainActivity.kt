package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.MediaCategory
import com.example.ui.BottomTab
import com.example.ui.CineGlassViewModel
import com.example.ui.components.AmbientCinemaBackdrop
import com.example.ui.components.FloatingGlassNavBar
import com.example.ui.components.GlassNavigationRail
import com.example.ui.components.GlassTopBar
import com.example.ui.screens.AddonsScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.MediaDetailScreen
import com.example.ui.screens.UploadScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.CineGlassTheme
import com.example.ui.theme.InkPure
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianSurface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            CineGlassTheme {
                CineGlassApp()
            }
        }
    }
}

@Composable
fun CineGlassApp(
    viewModel: CineGlassViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
        viewModel.clearStatusMessage()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 720.dp

        // Layer 1: Main App Shell with Ambient Glass Backdrop
        AmbientCinemaBackdrop(activeTab = uiState.selectedTab)

        Row(modifier = Modifier.fillMaxSize()) {
            if (useNavigationRail) {
                GlassNavigationRail(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = viewModel::selectTab
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    val subtitle = when (uiState.selectedTab) {
                        BottomTab.MOVIES -> "4K HDR • MULTI-LANGUAGE CINEMA"
                        BottomTab.WEB_SERIES -> "BINGE EPISODES • ALL LANGUAGES"
                        BottomTab.ANIME -> "SUB & DUB • HIGH-BITRATE ANIME"
                        BottomTab.ADDONS -> "REPOSITORIES & LINKED SOURCES"
                        BottomTab.UPLOAD -> "CREATOR STUDIO • PUBLISH VIDEO"
                    }

                    GlassTopBar(
                        title = "CineGlass",
                        subtitle = subtitle,
                        activeDownloadCount = uiState.activeDownloadCount,
                        completedDownloadCount = uiState.completedDownloadCount,
                        onSearchClick = {
                            if (uiState.selectedTab == BottomTab.ADDONS || uiState.selectedTab == BottomTab.UPLOAD) {
                                viewModel.selectTab(BottomTab.MOVIES)
                            }
                            viewModel.setSearchActive(!uiState.isSearchActive)
                        },
                        onDownloadsClick = { viewModel.setShowDownloads(true) }
                    )

                    AnimatedContent(
                        targetState = uiState.selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "main_tab_transition",
                        modifier = Modifier.weight(1f)
                    ) { tab ->
                        when (tab) {
                            BottomTab.MOVIES -> DiscoverScreen(
                                category = MediaCategory.MOVIES,
                                items = uiState.movies,
                                allCatalog = uiState.allCatalog,
                                continueWatching = uiState.continueWatching,
                                bookmarks = uiState.bookmarks,
                                searchQuery = uiState.searchQuery,
                                isSearchActive = uiState.isSearchActive,
                                selectedLanguage = uiState.selectedLanguageFilter,
                                selectedGenre = uiState.selectedGenreFilter,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onCloseSearch = { viewModel.setSearchActive(false) },
                                onSelectLanguage = viewModel::selectLanguageFilter,
                                onSelectGenre = viewModel::selectGenreFilter,
                                onItemClick = { viewModel.openMediaDetail(it.id) },
                                onQuickPlay = { viewModel.playMedia(it) },
                                onQuickDownload = { viewModel.downloadMedia(it) }
                            )

                            BottomTab.WEB_SERIES -> DiscoverScreen(
                                category = MediaCategory.WEB_SERIES,
                                items = uiState.webSeries,
                                allCatalog = uiState.allCatalog,
                                continueWatching = uiState.continueWatching,
                                bookmarks = uiState.bookmarks,
                                searchQuery = uiState.searchQuery,
                                isSearchActive = uiState.isSearchActive,
                                selectedLanguage = uiState.selectedLanguageFilter,
                                selectedGenre = uiState.selectedGenreFilter,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onCloseSearch = { viewModel.setSearchActive(false) },
                                onSelectLanguage = viewModel::selectLanguageFilter,
                                onSelectGenre = viewModel::selectGenreFilter,
                                onItemClick = { viewModel.openMediaDetail(it.id) },
                                onQuickPlay = { viewModel.playMedia(it, it.episodes.firstOrNull()?.index) },
                                onQuickDownload = { viewModel.downloadMedia(it, it.episodes.firstOrNull()?.index) }
                            )

                            BottomTab.ANIME -> DiscoverScreen(
                                category = MediaCategory.ANIME,
                                items = uiState.anime,
                                allCatalog = uiState.allCatalog,
                                continueWatching = uiState.continueWatching,
                                bookmarks = uiState.bookmarks,
                                searchQuery = uiState.searchQuery,
                                isSearchActive = uiState.isSearchActive,
                                selectedLanguage = uiState.selectedLanguageFilter,
                                selectedGenre = uiState.selectedGenreFilter,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onCloseSearch = { viewModel.setSearchActive(false) },
                                onSelectLanguage = viewModel::selectLanguageFilter,
                                onSelectGenre = viewModel::selectGenreFilter,
                                onItemClick = { viewModel.openMediaDetail(it.id) },
                                onQuickPlay = { viewModel.playMedia(it, it.episodes.firstOrNull()?.index) },
                                onQuickDownload = { viewModel.downloadMedia(it, it.episodes.firstOrNull()?.index) }
                            )

                            BottomTab.ADDONS -> AddonsScreen(
                                repositories = uiState.repositories,
                                totalIndexedItems = uiState.allCatalog.size,
                                isSyncing = uiState.isSyncingRepo,
                                onToggleRepo = viewModel::toggleRepository,
                                onSyncRepo = viewModel::syncRepository,
                                onDeleteRepo = viewModel::deleteRepository,
                                onAddRepoUrl = viewModel::addRepositoryUrl,
                                onImportRepoJson = viewModel::importRepositoryJson,
                                onLinkStreamingSite = viewModel::linkStreamingSite
                            )

                            BottomTab.UPLOAD -> UploadScreen(
                                userUploads = uiState.userUploads,
                                isUploading = uiState.isUploading,
                                onUploadSubmit = viewModel::uploadUserMedia,
                                onPlayUpload = { viewModel.playMedia(it) },
                                onOpenDetail = { viewModel.openMediaDetail(it.id) },
                                onDeleteUpload = viewModel::deleteUserUpload
                            )
                        }
                    }
                }

                // Floating Glass Bottom Navigation Bar on compact/medium screens
                if (!useNavigationRail) {
                    FloatingGlassNavBar(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = viewModel::selectTab,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        // Layer 2: Offline Downloads Fullscreen Overlay
        if (uiState.showDownloadsScreen) {
            DownloadsScreen(
                downloads = uiState.downloads,
                onBack = { viewModel.setShowDownloads(false) },
                onPlayOffline = viewModel::playOfflineDownload,
                onCancelDownload = viewModel::cancelDownload,
                onDeleteDownload = viewModel::deleteDownload
            )
        }

        // Layer 3: Media Detail Overlay
        val selectedMedia = uiState.selectedMedia
        if (selectedMedia != null) {
            MediaDetailScreen(
                media = selectedMedia,
                downloads = uiState.downloads,
                onBack = viewModel::closeMediaDetail,
                onPlay = { epIdx, qual, lang ->
                    viewModel.playMedia(selectedMedia, epIdx, qual, lang)
                },
                onDownload = { epIdx, qual, lang ->
                    viewModel.downloadMedia(selectedMedia, epIdx, qual, lang)
                },
                onToggleBookmark = { viewModel.toggleBookmark(selectedMedia) }
            )
        }

        // Layer 4: Fullscreen Media3 Video Player Overlay
        val playback = uiState.activePlayback
        if (playback != null) {
            VideoPlayerScreen(
                session = playback,
                onClose = viewModel::closePlayer,
                onSaveProgress = viewModel::saveWatchProgress,
                onPlayNextEpisode = { nextEpIndex ->
                    viewModel.playMedia(
                        media = playback.media,
                        episodeIndex = nextEpIndex,
                        preferredQuality = playback.initialQuality,
                        preferredLanguage = playback.initialLanguage
                    )
                }
            )
        }

        // Floating Glass Snackbar Feedback
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 96.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = ObsidianSurface.copy(alpha = 0.95f),
                contentColor = InkPure,
                actionColor = NeonCyan,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            )
        }
    }
}
