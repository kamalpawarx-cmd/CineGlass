package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.data.model.ActivePlaybackSession
import com.example.data.model.StreamQuality
import com.example.data.model.SupportedLanguages
import com.example.ui.components.GlassPill
import com.example.ui.theme.CinemaCoral
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPure
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    session: ActivePlaybackSession,
    onClose: () -> Unit,
    onSaveProgress: (mediaId: String, positionMs: Long, durationMs: Long) -> Unit,
    onPlayNextEpisode: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var isControlsVisible by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var isLandscape by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var currentPositionMs by remember { mutableLongStateOf(session.media.watchProgressMs) }
    var durationMs by remember { mutableLongStateOf(session.media.totalDurationMs.coerceAtLeast(1L)) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekSliderPosition by remember { mutableFloatStateOf(0f) }

    var selectedQuality by remember(session) { mutableStateOf(session.initialQuality) }
    var selectedLanguage by remember(session) { mutableStateOf(session.initialLanguage) }
    var subtitlesEnabled by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    var showQualityDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    val trackSelector = remember { DefaultTrackSelector(context) }

    val exoPlayer = remember(session.streamUrl) {
        ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()
            .apply {
                val mediaItem = MediaItem.fromUri(Uri.parse(session.streamUrl))
                setMediaItem(mediaItem)
                if (session.media.watchProgressMs > 3_000L && session.episodeIndex == null) {
                    seekTo(session.media.watchProgressMs)
                }
                playWhenReady = true
                prepare()
            }
    }

    // Apply quality & language track selection parameters
    LaunchedEffect(selectedQuality, selectedLanguage, subtitlesEnabled) {
        val langCode = SupportedLanguages.all.firstOrNull {
            it.name.equals(selectedLanguage, ignoreCase = true)
        }?.code ?: "en"

        val paramsBuilder = exoPlayer.trackSelectionParameters.buildUpon()
            .setPreferredAudioLanguage(langCode)
            .setPreferredTextLanguage(if (subtitlesEnabled) langCode else null)
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !subtitlesEnabled)

        if (selectedQuality == StreamQuality.AUTO) {
            paramsBuilder.clearVideoSizeConstraints()
        } else {
            paramsBuilder.setMaxVideoSize(selectedQuality.maxWidth, selectedQuality.maxHeight)
        }
        exoPlayer.trackSelectionParameters = paramsBuilder.build()
    }

    LaunchedEffect(playbackSpeed) {
        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
    }

    // Player listener & lifecycle cleanup
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    errorMessage = null
                    durationMs = exoPlayer.duration.coerceAtLeast(1L)
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                errorMessage = "Stream error (${error.errorCodeName}). Tap Retry or check stream link."
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            onSaveProgress(session.media.id, exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0L))
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Poll progress cleanly
    LaunchedEffect(exoPlayer, isSeeking) {
        while (true) {
            if (!isSeeking) {
                currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                val dur = exoPlayer.duration
                if (dur > 0L) durationMs = dur
                seekSliderPosition = (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
            }
            delay(500L)
        }
    }

    // Auto-hide HUD while playing
    LaunchedEffect(isControlsVisible, isPlaying, isLocked) {
        if (isControlsVisible && isPlaying && !isLocked) {
            delay(4_500L)
            isControlsVisible = false
        }
    }

    BackHandler {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onClose()
    }

    val nextEpisodeIndex = remember(session) {
        val currentIdx = session.episodeIndex ?: return@remember null
        session.media.episodes.firstOrNull { it.index > currentIdx }?.index
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isControlsVisible = !isControlsVisible
            }
    ) {
        // Real Media3 Video Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                if (view.player != exoPlayer) {
                    view.player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering spinner
        if (isBuffering && errorMessage == null) {
            CircularProgressIndicator(
                color = NeonCyan,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }

        // Error fallback overlay
        if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xE610172A))
                    .border(1.dp, CinemaCoral, RoundedCornerShape(22.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkPure,
                    textAlign = TextAlign.Center
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassPill(
                        text = "Retry Stream",
                        containerColor = NeonCyan.copy(alpha = 0.25f),
                        borderColor = NeonCyan,
                        contentColor = NeonCyan,
                        onClick = {
                            errorMessage = null
                            isBuffering = true
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        }
                    )
                    GlassPill(
                        text = "Close",
                        containerColor = Color(0x44FFFFFF),
                        borderColor = GlassBorderBright,
                        onClick = onClose
                    )
                }
            }
        }

        // Subtitle Track Status Ribbon (when CC enabled and HUD hidden)
        if (subtitlesEnabled && !isControlsVisible && errorMessage == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 26.dp, start = 24.dp, end = 24.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x99050812))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "CC • $selectedLanguage • ${selectedQuality.badge}",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkSecondary
                )
            }
        }

        // Custom Glass Player HUD
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xCC060812),
                                Color(0x33060812),
                                Color(0xDD060812)
                            )
                        )
                    )
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!isLocked) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("player_back_button")
                                    .clip(CircleShape)
                                    .background(Color(0x99162038))
                                    .border(1.dp, GlassBorderBright, CircleShape)
                                    .clickable {
                                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                        onClose()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_back),
                                    tint = InkPure
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.media.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = InkPure,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (session.isOfflineFile) {
                                        Icon(
                                            Icons.Filled.OfflinePin,
                                            contentDescription = null,
                                            tint = MintEmerald,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Text(
                                        text = buildString {
                                            if (session.episodeTitle != null) {
                                                append(session.episodeTitle)
                                                append(" • ")
                                            }
                                            append(if (session.isOfflineFile) "OFFLINE" else selectedQuality.badge)
                                            append(" • ")
                                            append(selectedLanguage)
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (session.isOfflineFile) MintEmerald else NeonCyan,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }

                    // Lock Controls Toggle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isLocked) CinemaCoral.copy(alpha = 0.28f) else Color(0x99162038))
                            .border(1.dp, if (isLocked) CinemaCoral else GlassBorderBright, CircleShape)
                            .clickable { isLocked = !isLocked },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            contentDescription = "Lock controls",
                            tint = if (isLocked) CinemaCoral else InkPure,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Center Transport Controls
                if (!isLocked) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0x88141E36))
                                .border(1.dp, GlassBorderBright, CircleShape)
                                .clickable {
                                    exoPlayer.seekTo((exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Replay10, contentDescription = "Rewind 10 seconds", tint = InkPure, modifier = Modifier.size(28.dp))
                        }

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .testTag("player_play_pause_button")
                                .clip(CircleShape)
                                .background(NeonCyan)
                                .clickable {
                                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) stringResource(R.string.cd_pause) else stringResource(R.string.cd_play),
                                tint = ObsidianDeep,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0x88141E36))
                                .border(1.dp, GlassBorderBright, CircleShape)
                                .clickable {
                                    exoPlayer.seekTo((exoPlayer.currentPosition + 10_000L).coerceAtMost(durationMs))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Forward10, contentDescription = "Forward 10 seconds", tint = InkPure, modifier = Modifier.size(28.dp))
                        }
                    }

                    // Bottom Scrubber & Glass Control Pills
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = formatTimeMs(currentPositionMs),
                                style = MaterialTheme.typography.labelMedium,
                                color = InkPure
                            )
                            Slider(
                                value = seekSliderPosition,
                                onValueChange = { fraction ->
                                    isSeeking = true
                                    seekSliderPosition = fraction
                                    currentPositionMs = (fraction * durationMs).toLong()
                                },
                                onValueChangeFinished = {
                                    exoPlayer.seekTo((seekSliderPosition * durationMs).toLong())
                                    isSeeking = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonCyan,
                                    inactiveTrackColor = Color(0x44FFFFFF)
                                )
                            )
                            Text(
                                text = formatTimeMs(durationMs),
                                style = MaterialTheme.typography.labelMedium,
                                color = InkSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GlassPill(
                                    text = selectedQuality.badge,
                                    leadingIcon = Icons.Filled.HighQuality,
                                    containerColor = Color(0xAA141E36),
                                    borderColor = NeonCyan.copy(alpha = 0.6f),
                                    contentColor = NeonCyan,
                                    onClick = { showQualityDialog = true },
                                    modifier = Modifier.testTag("player_quality_button")
                                )
                                GlassPill(
                                    text = selectedLanguage,
                                    leadingIcon = Icons.Filled.Language,
                                    containerColor = Color(0xAA141E36),
                                    borderColor = CinemaCoral.copy(alpha = 0.6f),
                                    contentColor = InkPure,
                                    onClick = { showLanguageDialog = true },
                                    modifier = Modifier.testTag("player_language_button")
                                )
                                GlassPill(
                                    text = if (subtitlesEnabled) "CC ON" else "CC OFF",
                                    leadingIcon = Icons.Filled.ClosedCaption,
                                    containerColor = if (subtitlesEnabled) NeonCyan.copy(alpha = 0.22f) else Color(0x88141E36),
                                    borderColor = if (subtitlesEnabled) NeonCyan else GlassBorderSubtle,
                                    contentColor = if (subtitlesEnabled) NeonCyan else InkMuted,
                                    onClick = { subtitlesEnabled = !subtitlesEnabled }
                                )
                                GlassPill(
                                    text = "${playbackSpeed}x",
                                    leadingIcon = Icons.Filled.Speed,
                                    containerColor = Color(0xAA141E36),
                                    borderColor = GlassBorderSubtle,
                                    contentColor = InkSecondary,
                                    onClick = { showSpeedDialog = true }
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (nextEpisodeIndex != null && onPlayNextEpisode != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xAA141E36))
                                            .border(1.dp, NeonCyan, CircleShape)
                                            .clickable { onPlayNextEpisode(nextEpisodeIndex) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.SkipNext, contentDescription = "Next Episode", tint = NeonCyan)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("player_fullscreen_button")
                                        .clip(CircleShape)
                                        .background(Color(0xAA141E36))
                                        .border(1.dp, GlassBorderBright, CircleShape)
                                        .clickable {
                                            isLandscape = !isLandscape
                                            activity?.requestedOrientation = if (isLandscape) {
                                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                            } else {
                                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isLandscape) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                                        contentDescription = stringResource(R.string.cd_fullscreen),
                                        tint = InkPure
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            containerColor = ObsidianSurface,
            title = { Text("Video Quality", color = InkPure) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StreamQuality.entries.forEach { quality ->
                        val active = quality == selectedQuality
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (active) NeonCyan.copy(alpha = 0.2f) else Color(0x441B2540))
                                .border(1.dp, if (active) NeonCyan else GlassBorderSubtle, RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedQuality = quality
                                    showQualityDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(quality.label, color = InkPure, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                            Text(quality.badge, color = if (active) NeonCyan else InkMuted, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("Done", color = NeonCyan)
                }
            }
        )
    }

    if (showLanguageDialog) {
        val languageChoices = remember(session) {
            (session.media.languages + SupportedLanguages.all).distinctBy { it.name }
        }
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = ObsidianSurface,
            title = { Text("Audio & Subtitle Language", color = InkPure) },
            text = {
                LazyColumn(
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(languageChoices) { track ->
                        val active = track.name.equals(selectedLanguage, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (active) CinemaCoral.copy(alpha = 0.22f) else Color(0x441B2540))
                                .border(1.dp, if (active) CinemaCoral else GlassBorderSubtle, RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedLanguage = track.name
                                    showLanguageDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(track.name, color = InkPure, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                                Text(track.type, color = InkMuted, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(track.code.uppercase(), color = if (active) CinemaCoral else InkSecondary, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Done", color = NeonCyan)
                }
            }
        )
    }

    if (showSpeedDialog) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            containerColor = ObsidianSurface,
            title = { Text("Playback Speed", color = InkPure) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    speeds.forEach { spd ->
                        val active = spd == playbackSpeed
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (active) NeonCyan.copy(alpha = 0.2f) else Color(0x441B2540))
                                .clickable {
                                    playbackSpeed = spd
                                    showSpeedDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${spd}x", color = InkPure, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                            if (spd == 1.0f) Text("Normal", color = InkMuted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Close", color = NeonCyan)
                }
            }
        )
    }
}

private fun formatTimeMs(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val hrs = totalSec / 3600L
    val mins = (totalSec % 3600L) / 60L
    val secs = totalSec % 60L
    return if (hrs > 0L) {
        String.format("%d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}
