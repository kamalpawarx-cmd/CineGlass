package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.MediaItemUi
import com.example.ui.BottomTab
import com.example.ui.theme.AuroraViolet
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
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import java.io.File

@DrawableRes
fun resolveFallbackDrawable(localKey: String): Int = when (localKey) {
    "series_hero" -> R.drawable.img_hero_series
    "anime_hero" -> R.drawable.img_hero_anime
    "upload_studio" -> R.drawable.img_upload_studio
    else -> R.drawable.img_hero_movies
}

@Composable
fun MediaArtwork(
    posterUrl: String,
    localPosterKey: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val fallbackRes = remember(localPosterKey) { resolveFallbackDrawable(localPosterKey) }
    val modelData: Any? = remember(posterUrl, localPosterKey) {
        when {
            localPosterKey.startsWith("file://") -> {
                val f = File(localPosterKey.removePrefix("file://"))
                if (f.exists()) f else null
            }
            posterUrl.startsWith("file://") -> {
                val f = File(posterUrl.removePrefix("file://"))
                if (f.exists()) f else null
            }
            posterUrl.startsWith("http", ignoreCase = true) -> posterUrl
            else -> null
        }
    }

    if (modelData == null) {
        Image(
            painter = painterResource(id = fallbackRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(modelData)
                .crossfade(true)
                .build(),
            placeholder = painterResource(id = fallbackRes),
            error = painterResource(id = fallbackRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
fun AmbientCinemaBackdrop(
    activeTab: BottomTab,
    modifier: Modifier = Modifier
) {
    val primaryGlow by animateColorAsState(
        targetValue = when (activeTab) {
            BottomTab.MOVIES -> NeonCyan.copy(alpha = 0.22f)
            BottomTab.WEB_SERIES -> AuroraViolet.copy(alpha = 0.24f)
            BottomTab.ANIME -> CinemaCoral.copy(alpha = 0.24f)
            BottomTab.ADDONS -> MintEmerald.copy(alpha = 0.20f)
            BottomTab.UPLOAD -> CinemaGold.copy(alpha = 0.20f)
        },
        label = "primary_glow"
    )
    val secondaryGlow by animateColorAsState(
        targetValue = when (activeTab) {
            BottomTab.MOVIES -> CinemaCoral.copy(alpha = 0.14f)
            BottomTab.WEB_SERIES -> NeonCyan.copy(alpha = 0.16f)
            BottomTab.ANIME -> AuroraViolet.copy(alpha = 0.18f)
            BottomTab.ADDONS -> NeonCyan.copy(alpha = 0.16f)
            BottomTab.UPLOAD -> CinemaCoral.copy(alpha = 0.16f)
        },
        label = "secondary_glow"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(ObsidianSurface, ObsidianDeep, Color(0xFF04060D))
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryGlow, Color.Transparent),
                center = Offset(size.width * 0.18f, size.height * 0.10f),
                radius = size.maxDimension * 0.58f
            ),
            radius = size.maxDimension * 0.58f,
            center = Offset(size.width * 0.18f, size.height * 0.10f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondaryGlow, Color.Transparent),
                center = Offset(size.width * 0.88f, size.height * 0.42f),
                radius = size.maxDimension * 0.48f
            ),
            radius = size.maxDimension * 0.48f,
            center = Offset(size.width * 0.88f, size.height * 0.42f)
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    accentTint: Color = Color.White.copy(alpha = 0.08f),
    borderAccent: Color = GlassBorderBright,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val bgBrush = remember(accentTint) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xCC161F36),
                Color(0xB50E1527),
                accentTint.copy(alpha = 0.18f)
            )
        )
    }
    val borderBrush = remember(borderAccent) {
        Brush.linearGradient(
            colors = listOf(
                borderAccent,
                GlassBorderSubtle,
                NeonCyan.copy(alpha = 0.22f)
            )
        )
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = NeonCyan),
            onClick = onClick
        )
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(bgBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .then(clickableModifier)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun GlassPill(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0x441B2642),
    borderColor: Color = GlassBorderBright,
    contentColor: Color = InkPure,
    leadingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(50)
    val baseModifier = modifier
        .clip(shape)
        .background(containerColor)
        .border(1.dp, borderColor, shape)
        .let { mod ->
            if (onClick != null) {
                mod
                    .minimumInteractiveComponentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = NeonCyan),
                        onClick = onClick
                    )
            } else {
                mod
            }
        }
        .padding(horizontal = 12.dp, vertical = 6.dp)

    Row(
        modifier = baseModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun GlassTopBar(
    title: String,
    subtitle: String,
    activeDownloadCount: Int,
    completedDownloadCount: Int,
    onSearchClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.25f), CinemaCoral.copy(alpha = 0.28f)))
                    )
                    .border(1.dp, GlassBorderBright, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_launcher_emblem),
                    contentDescription = stringResource(R.string.app_name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = InkPure,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search glass button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .testTag("top_search_button")
                    .clip(CircleShape)
                    .background(Color(0x99162038))
                    .border(1.dp, GlassBorderBright, CircleShape)
                    .clickable(onClick = onSearchClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.cd_search),
                    tint = InkPure,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Offline Downloads glass button with badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .testTag("top_downloads_button")
                    .clip(CircleShape)
                    .background(
                        if (activeDownloadCount > 0) NeonCyan.copy(alpha = 0.22f) else Color(0x99162038)
                    )
                    .border(
                        1.dp,
                        if (activeDownloadCount > 0) NeonCyan else GlassBorderBright,
                        CircleShape
                    )
                    .clickable(onClick = onDownloadsClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = stringResource(R.string.cd_downloads),
                    tint = if (activeDownloadCount > 0) NeonCyan else InkPure,
                    modifier = Modifier.size(22.dp)
                )
                val badgeCount = if (activeDownloadCount > 0) activeDownloadCount else completedDownloadCount
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (activeDownloadCount > 0) NeonCyan else CinemaCoral),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount.coerceAtMost(9).toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = ObsidianDeep,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun tabIcons(tab: BottomTab): Pair<ImageVector, ImageVector> = when (tab) {
    BottomTab.MOVIES -> Icons.Filled.Movie to Icons.Outlined.Movie
    BottomTab.WEB_SERIES -> Icons.Filled.LiveTv to Icons.Outlined.LiveTv
    BottomTab.ANIME -> Icons.Filled.Whatshot to Icons.Outlined.Whatshot
    BottomTab.ADDONS -> Icons.Filled.Extension to Icons.Outlined.Extension
    BottomTab.UPLOAD -> Icons.Filled.CloudUpload to Icons.Outlined.CloudUpload
}

private fun tabAccentColor(tab: BottomTab): Color = when (tab) {
    BottomTab.MOVIES -> NeonCyan
    BottomTab.WEB_SERIES -> AuroraViolet
    BottomTab.ANIME -> CinemaCoral
    BottomTab.ADDONS -> MintEmerald
    BottomTab.UPLOAD -> CinemaGold
}

@Composable
fun FloatingGlassNavBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        val barShape = RoundedCornerShape(28.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(barShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xE6141D35),
                            Color(0xDC0C1224),
                            Color(0xE617223D)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x52FFFFFF),
                            Color(0x2238E8FF),
                            Color(0x38FF3D68)
                        )
                    ),
                    shape = barShape
                )
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                val accent = tabAccentColor(tab)
                val (filledIcon, outlinedIcon) = tabIcons(tab)
                val pillColor by animateColorAsState(
                    targetValue = if (selected) accent.copy(alpha = 0.22f) else Color.Transparent,
                    label = "tab_pill_color"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (selected) accent else InkSecondary,
                    label = "tab_content_color"
                )
                val horizontalPad by animateDpAsState(
                    targetValue = if (selected) 10.dp else 6.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "tab_pad"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("bottom_tab_${tab.route}")
                        .clip(RoundedCornerShape(22.dp))
                        .background(pillColor)
                        .border(
                            width = if (selected) 1.dp else 0.dp,
                            color = if (selected) accent.copy(alpha = 0.55f) else Color.Transparent,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = accent),
                            onClick = { onTabSelected(tab) }
                        )
                        .padding(horizontal = horizontalPad, vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = if (selected) filledIcon else outlinedIcon,
                        contentDescription = tab.label,
                        tint = contentColor,
                        modifier = Modifier.size(21.dp)
                    )
                    Text(
                        text = if (tab == BottomTab.WEB_SERIES) "Series" else tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) InkPure else InkMuted,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun GlassNavigationRail(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val railShape = RoundedCornerShape(26.dp)
    Column(
        modifier = modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
            .width(88.dp)
            .clip(railShape)
            .background(Color(0xE011192E))
            .border(1.dp, GlassBorderBright, railShape)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BottomTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            val accent = tabAccentColor(tab)
            val (filledIcon, outlinedIcon) = tabIcons(tab)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("rail_tab_${tab.route}")
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) accent.copy(alpha = 0.20f) else Color.Transparent)
                    .border(
                        width = if (selected) 1.dp else 0.dp,
                        color = if (selected) accent.copy(alpha = 0.55f) else Color.Transparent,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (selected) filledIcon else outlinedIcon,
                    contentDescription = tab.label,
                    tint = if (selected) accent else InkSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = if (tab == BottomTab.WEB_SERIES) "Series" else tab.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) InkPure else InkMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun MediaPosterCard(
    item: MediaItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = 152.dp,
    showProgress: Boolean = false
) {
    val cardShape = RoundedCornerShape(20.dp)
    val sizedModifier = if (width != null) modifier.width(width) else modifier.fillMaxWidth()

    Surface(
        onClick = onClick,
        modifier = sizedModifier.testTag("media_card_${item.id}"),
        shape = cardShape,
        color = Color(0xB8121B31),
        border = BorderStroke(1.dp, GlassBorderSubtle)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                MediaArtwork(
                    posterUrl = item.posterUrl,
                    localPosterKey = item.localPosterKey,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x55060812),
                                    Color.Transparent,
                                    Color(0xCC060812)
                                )
                            )
                        )
                )

                // Top-left Quality Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xBF091021))
                        .border(1.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.maxQuality,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Top-right Rating
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xBF091021))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = String.format("%.1f", item.rating),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkPure
                    )
                }

                // Bottom overlay: Language count + Play glyph
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (item.languages.size > 1) "${item.languages.size} LANGS" else item.languages.firstOrNull()?.name?.uppercase().orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSecondary
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = ObsidianDeep,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (showProgress && item.progressFraction > 0.01f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color(0x55FFFFFF))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(item.progressFraction)
                                .fillMaxHeight()
                                .background(NeonCyan)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = InkPure,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.year} • ${item.genres.firstOrNull() ?: item.category.displayLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyGlassState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ObsidianElevated)
                    .border(1.dp, GlassBorderBright, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = InkPure
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary
            )
        }
    }
}
