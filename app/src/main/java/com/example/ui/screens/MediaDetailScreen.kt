package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.download.OfflineDownloadManager
import com.example.data.local.DownloadEntity
import com.example.data.model.DownloadStatus
import com.example.data.model.EpisodeItem
import com.example.data.model.MediaItemUi
import com.example.data.model.StreamQuality
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.components.MediaArtwork
import com.example.ui.theme.CinemaCoral
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPure
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface

@Composable
fun MediaDetailScreen(
    media: MediaItemUi,
    downloads: List<DownloadEntity>,
    onBack: () -> Unit,
    onPlay: (episodeIndex: Int?, quality: StreamQuality, language: String) -> Unit,
    onDownload: (episodeIndex: Int?, quality: StreamQuality, language: String) -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var selectedQuality by remember(media.id) { mutableStateOf(StreamQuality.AUTO) }
    var selectedLanguage by remember(media.id) {
        mutableStateOf(media.languages.firstOrNull()?.name ?: "English")
    }
    var pendingDownloadEpisode by remember { mutableStateOf<EpisodeItem?>(null) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    val mainDownloadId = OfflineDownloadManager.buildDownloadId(media.id, media.episodes.firstOrNull()?.index)
    val mainDownload = downloads.firstOrNull { it.id == mainDownloadId }
    val isMainOfflineReady = mainDownload?.status == DownloadStatus.COMPLETED.key

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDeep)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            // Backdrop Hero
            item(key = "detail_backdrop") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(330.dp)
                ) {
                    MediaArtwork(
                        posterUrl = media.backdropUrl.ifBlank { media.posterUrl },
                        localPosterKey = media.localPosterKey,
                        contentDescription = media.title,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x88060812),
                                        Color(0x44060812),
                                        ObsidianDeep
                                    )
                                )
                            )
                    )

                    // Top Navigation Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("detail_back_button")
                                .clip(CircleShape)
                                .background(Color(0xAA0A1122))
                                .border(1.dp, GlassBorderBright, CircleShape)
                                .clickable(onClick = onBack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                                tint = InkPure
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("detail_bookmark_button")
                                .clip(CircleShape)
                                .background(Color(0xAA0A1122))
                                .border(
                                    1.dp,
                                    if (media.isBookmarked) CinemaGold else GlassBorderBright,
                                    CircleShape
                                )
                                .clickable(onClick = onToggleBookmark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (media.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = stringResource(R.string.cd_bookmark),
                                tint = if (media.isBookmarked) CinemaGold else InkPure
                            )
                        }
                    }

                    // Title Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassPill(
                                text = media.category.displayLabel.uppercase(),
                                containerColor = NeonCyan.copy(alpha = 0.22f),
                                borderColor = NeonCyan.copy(alpha = 0.65f),
                                contentColor = NeonCyan
                            )
                            GlassPill(
                                text = media.maxQuality,
                                containerColor = Color(0x99121B31),
                                borderColor = GlassBorderBright,
                                contentColor = InkPure
                            )
                            GlassPill(
                                text = String.format("%.1f", media.rating),
                                leadingIcon = Icons.Filled.Star,
                                containerColor = Color(0x99121B31),
                                borderColor = CinemaGold.copy(alpha = 0.55f),
                                contentColor = CinemaGold
                            )
                        }
                        Text(
                            text = media.title,
                            style = MaterialTheme.typography.displaySmall,
                            color = InkPure
                        )
                        Text(
                            text = "${media.year} • ${media.durationLabel} • ${media.genres.joinToString(" · ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }
            }

            // Primary Actions + Source Repo
            item(key = "detail_actions") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                onPlay(media.episodes.firstOrNull()?.index, selectedQuality, selectedLanguage)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("detail_play_button"),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMainOfflineReady) MintEmerald else NeonCyan,
                                contentColor = ObsidianDeep
                            )
                        ) {
                            Icon(
                                imageVector = if (isMainOfflineReady) Icons.Filled.OfflinePin else Icons.Filled.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isMainOfflineReady) "Play Offline" else "Stream HD",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                pendingDownloadEpisode = media.episodes.firstOrNull()
                                showDownloadDialog = true
                            },
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("detail_download_button"),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, if (isMainOfflineReady) MintEmerald else GlassBorderBright),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0x88141E36),
                                contentColor = if (isMainOfflineReady) MintEmerald else InkPure
                            )
                        ) {
                            Icon(
                                imageVector = if (isMainOfflineReady) Icons.Filled.CheckCircle else Icons.Filled.Download,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (isMainOfflineReady) "Saved" else "Download")
                        }
                    }

                    if (mainDownload != null && mainDownload.status == DownloadStatus.DOWNLOADING.key) {
                        GlassCard(contentPadding = PaddingValues(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Downloading offline...", style = MaterialTheme.typography.labelLarge, color = NeonCyan)
                                Text("${mainDownload.progress}%", style = MaterialTheme.typography.labelMedium, color = InkPure)
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { mainDownload.progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = NeonCyan,
                                trackColor = Color(0x33FFFFFF)
                            )
                        }
                    }

                    // Synopsis + Add-on Source Badge
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Extension,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "SOURCE: ${media.repoName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonCyan
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = media.synopsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkSecondary
                        )
                    }

                    // Preferred Quality Picker
                    GlassCard(contentPadding = PaddingValues(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.HighQuality, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            Text("Stream Quality", style = MaterialTheme.typography.titleMedium, color = InkPure)
                        }
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(StreamQuality.entries) { quality ->
                                val isSel = quality == selectedQuality
                                GlassPill(
                                    text = quality.badge,
                                    containerColor = if (isSel) NeonCyan.copy(alpha = 0.25f) else Color(0x55121A30),
                                    borderColor = if (isSel) NeonCyan else GlassBorderSubtle,
                                    contentColor = if (isSel) InkPure else InkSecondary,
                                    onClick = { selectedQuality = quality }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Language, contentDescription = null, tint = CinemaCoral, modifier = Modifier.size(18.dp))
                            Text("Audio & Subtitle Language", style = MaterialTheme.typography.titleMedium, color = InkPure)
                        }
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(media.languages) { track ->
                                val isSel = track.name == selectedLanguage
                                GlassPill(
                                    text = "${track.name} (${track.code.uppercase()})",
                                    containerColor = if (isSel) CinemaCoral.copy(alpha = 0.25f) else Color(0x55121A30),
                                    borderColor = if (isSel) CinemaCoral else GlassBorderSubtle,
                                    contentColor = if (isSel) InkPure else InkSecondary,
                                    onClick = { selectedLanguage = track.name }
                                )
                            }
                        }
                    }
                }
            }

            // Episodes List (for Web Series & Anime)
            if (media.episodes.isNotEmpty()) {
                item(key = "episodes_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Episodes",
                            style = MaterialTheme.typography.headlineSmall,
                            color = InkPure
                        )
                        Text(
                            text = "${media.episodes.size} AVAILABLE",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan
                        )
                    }
                }

                items(media.episodes, key = { "ep_${media.id}_${it.index}" }) { episode ->
                    val epDownloadId = OfflineDownloadManager.buildDownloadId(media.id, episode.index)
                    val epDownload = downloads.firstOrNull { it.id == epDownloadId }
                    val isEpReady = epDownload?.status == DownloadStatus.COMPLETED.key
                    val isEpDownloading = epDownload?.status == DownloadStatus.DOWNLOADING.key

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        onClick = { onPlay(episode.index, selectedQuality, selectedLanguage) },
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(NeonCyan.copy(alpha = 0.18f))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = stringResource(R.string.cd_play),
                                    tint = NeonCyan
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "S${episode.season} E${episode.index} • ${episode.title}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = InkPure,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = episode.synopsis.ifBlank { episode.duration },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkMuted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x6616213B))
                                    .border(
                                        1.dp,
                                        if (isEpReady) MintEmerald else GlassBorderSubtle,
                                        CircleShape
                                    )
                                    .clickable {
                                        pendingDownloadEpisode = episode
                                        showDownloadDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isEpReady) Icons.Filled.CheckCircle else Icons.Filled.Download,
                                    contentDescription = stringResource(R.string.cd_download_item),
                                    tint = if (isEpReady) MintEmerald else InkPure,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (isEpDownloading && epDownload != null) {
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { epDownload.progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = NeonCyan,
                                trackColor = Color(0x33FFFFFF)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }

    if (showDownloadDialog) {
        var dlQuality by remember { mutableStateOf(StreamQuality.FHD_1080) }
        var dlLanguage by remember { mutableStateOf(selectedLanguage) }

        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            containerColor = ObsidianSurface,
            titleContentColor = InkPure,
            textContentColor = InkSecondary,
            title = {
                Text(
                    text = "Offline Download",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = buildString {
                            append(media.title)
                            pendingDownloadEpisode?.let { append(" • S${it.season}E${it.index}") }
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonCyan
                    )
                    Text("Select Quality", style = MaterialTheme.typography.labelLarge, color = InkPure)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(StreamQuality.UHD_4K, StreamQuality.FHD_1080, StreamQuality.HD_720, StreamQuality.SD_480)) { q ->
                            val active = q == dlQuality
                            GlassPill(
                                text = q.badge,
                                containerColor = if (active) NeonCyan.copy(alpha = 0.28f) else Color(0x55121A30),
                                borderColor = if (active) NeonCyan else GlassBorderSubtle,
                                contentColor = if (active) InkPure else InkSecondary,
                                onClick = { dlQuality = q }
                            )
                        }
                    }
                    Text("Select Language", style = MaterialTheme.typography.labelLarge, color = InkPure)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(media.languages) { track ->
                            val active = track.name == dlLanguage
                            GlassPill(
                                text = track.name,
                                containerColor = if (active) CinemaCoral.copy(alpha = 0.28f) else Color(0x55121A30),
                                borderColor = if (active) CinemaCoral else GlassBorderSubtle,
                                contentColor = if (active) InkPure else InkSecondary,
                                onClick = { dlLanguage = track.name }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDownload(pendingDownloadEpisode?.index, dlQuality, dlLanguage)
                        showDownloadDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ObsidianDeep)
                ) {
                    Text("Download Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text("Cancel", color = InkSecondary)
                }
            }
        )
    }
}
