package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DownloadEntity
import com.example.data.model.DownloadStatus
import com.example.ui.components.EmptyGlassState
import com.example.ui.components.GlassCard
import com.example.ui.components.MediaArtwork
import com.example.ui.theme.CinemaCoral
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPure
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianDeep

@Composable
fun DownloadsScreen(
    downloads: List<DownloadEntity>,
    onBack: () -> Unit,
    onPlayOffline: (DownloadEntity) -> Unit,
    onCancelDownload: (String) -> Unit,
    onDeleteDownload: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val activeDownloads = remember(downloads) {
        downloads.filter {
            it.status == DownloadStatus.DOWNLOADING.key || it.status == DownloadStatus.QUEUED.key
        }
    }
    val completedDownloads = remember(downloads) {
        downloads.filter { it.status == DownloadStatus.COMPLETED.key }
    }
    val failedDownloads = remember(downloads) {
        downloads.filter { it.status == DownloadStatus.FAILED.key }
    }
    val totalBytes = remember(completedDownloads) {
        completedDownloads.sumOf { it.totalBytes.coerceAtLeast(it.bytesDownloaded) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDeep)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .testTag("downloads_back_button")
                    .clip(CircleShape)
                    .background(Color(0xAA141E36))
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
            Column {
                Text(
                    text = "Offline Downloads",
                    style = MaterialTheme.typography.headlineMedium,
                    color = InkPure
                )
                Text(
                    text = "${completedDownloads.size} saved • ${formatBytes(totalBytes)} on device",
                    style = MaterialTheme.typography.labelSmall,
                    color = MintEmerald
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (activeDownloads.isNotEmpty()) {
                item(key = "active_dl_header") {
                    Text(
                        text = "Downloading Now",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonCyan
                    )
                }
                items(activeDownloads, key = { it.id }) { dl ->
                    GlassCard(
                        accentTint = NeonCyan,
                        borderAccent = NeonCyan.copy(alpha = 0.5f),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MediaArtwork(
                                posterUrl = dl.posterUrl,
                                localPosterKey = dl.localPosterKey,
                                contentDescription = dl.title,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(dl.title, style = MaterialTheme.typography.titleSmall, color = InkPure, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = "${dl.subtitleLabel} • ${dl.progress}% (${formatBytes(dl.bytesDownloaded)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan
                                )
                                LinearProgressIndicator(
                                    progress = { dl.progress / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = NeonCyan,
                                    trackColor = Color(0x33FFFFFF)
                                )
                            }
                            IconButton(onClick = { onCancelDownload(dl.id) }) {
                                Icon(Icons.Filled.Cancel, contentDescription = "Cancel download", tint = CinemaCoral)
                            }
                        }
                    }
                }
            }

            if (completedDownloads.isNotEmpty()) {
                item(key = "ready_dl_header") {
                    Text(
                        text = "Ready to Watch Offline",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkPure
                    )
                }
                items(completedDownloads, key = { it.id }) { dl ->
                    GlassCard(
                        onClick = { onPlayOffline(dl) },
                        accentTint = MintEmerald,
                        borderAccent = MintEmerald.copy(alpha = 0.45f),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MediaArtwork(
                                posterUrl = dl.posterUrl,
                                localPosterKey = dl.localPosterKey,
                                contentDescription = dl.title,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(dl.title, style = MaterialTheme.typography.titleMedium, color = InkPure, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(dl.subtitleLabel, style = MaterialTheme.typography.bodySmall, color = InkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.OfflinePin, contentDescription = null, tint = MintEmerald, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "OFFLINE • ${formatBytes(dl.totalBytes)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MintEmerald
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MintEmerald)
                                    .clickable { onPlayOffline(dl) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.cd_play), tint = ObsidianDeep)
                            }
                            IconButton(onClick = { onDeleteDownload(dl.id) }) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.cd_delete), tint = CinemaCoral)
                            }
                        }
                    }
                }
            }

            if (failedDownloads.isNotEmpty()) {
                item(key = "failed_dl_header") {
                    Text(
                        text = "Interrupted",
                        style = MaterialTheme.typography.titleMedium,
                        color = CinemaCoral
                    )
                }
                items(failedDownloads, key = { it.id }) { dl ->
                    GlassCard(contentPadding = PaddingValues(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(dl.title, style = MaterialTheme.typography.titleSmall, color = InkPure)
                                Text("Download failed. Remove and retry from details.", style = MaterialTheme.typography.bodySmall, color = InkMuted)
                            }
                            IconButton(onClick = { onDeleteDownload(dl.id) }) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.cd_delete), tint = CinemaCoral)
                            }
                        }
                    }
                }
            }

            if (downloads.isEmpty()) {
                item(key = "empty_downloads") {
                    EmptyGlassState(
                        icon = Icons.Filled.DownloadDone,
                        title = "No offline downloads yet",
                        message = "Tap Download on any Movie, Web Series episode, or Anime to save it locally in HD and watch anytime without internet."
                    )
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1024.0) {
        String.format("%.2f GB", mb / 1024.0)
    } else {
        String.format("%.1f MB", mb)
    }
}
