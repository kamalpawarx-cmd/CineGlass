package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.model.MediaCategory
import com.example.data.model.MediaItemUi
import com.example.ui.components.EmptyGlassState
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.components.MediaArtwork
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.AuroraViolet
import com.example.ui.theme.CinemaCoral
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPure
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianElevated

@Composable
fun DiscoverScreen(
    category: MediaCategory,
    items: List<MediaItemUi>,
    allCatalog: List<MediaItemUi>,
    continueWatching: List<MediaItemUi>,
    bookmarks: List<MediaItemUi>,
    searchQuery: String,
    isSearchActive: Boolean,
    selectedLanguage: String,
    selectedGenre: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    onSelectGenre: (String) -> Unit,
    onItemClick: (MediaItemUi) -> Unit,
    onQuickPlay: (MediaItemUi) -> Unit,
    onQuickDownload: (MediaItemUi) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (category) {
        MediaCategory.MOVIES -> NeonCyan
        MediaCategory.WEB_SERIES -> AuroraViolet
        MediaCategory.ANIME -> CinemaCoral
    }

    val availableLanguages = remember(items) {
        listOf("All") + items.flatMap { it.languages.map { lang -> lang.name } }.distinct().take(8)
    }

    val availableGenres = remember(items) {
        listOf("All") + items.flatMap { it.genres }.distinct().take(10)
    }

    val filteredItems = remember(items, allCatalog, isSearchActive, searchQuery, selectedLanguage, selectedGenre) {
        val base = if (isSearchActive && searchQuery.isNotBlank()) allCatalog else items
        base.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.genres.any { it.contains(searchQuery, ignoreCase = true) } ||
                item.repoName.contains(searchQuery, ignoreCase = true) ||
                item.languages.any { it.name.contains(searchQuery, ignoreCase = true) }
            val matchesLang = selectedLanguage == "All" ||
                item.languages.any { it.name.equals(selectedLanguage, ignoreCase = true) }
            val matchesGenre = selectedGenre == "All" ||
                item.genres.any { it.equals(selectedGenre, ignoreCase = true) }
            matchesQuery && matchesLang && matchesGenre
        }
    }

    val heroItem = remember(filteredItems, items) {
        filteredItems.firstOrNull() ?: items.firstOrNull()
    }

    val categoryContinue = remember(continueWatching, category) {
        continueWatching.filter { it.category == category }
    }

    val categoryUploads = remember(filteredItems) {
        filteredItems.filter { it.isUserUploaded }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Inline Search Bar when active
        if (isSearchActive) {
            item(key = "search_bar") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("catalog_search_input"),
                    placeholder = {
                        Text(
                            text = "Search movies, series, anime, languages, repos...",
                            color = InkMuted
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = NeonCyan)
                    },
                    trailingIcon = {
                        IconButton(onClick = onCloseSearch) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.cd_close),
                                tint = InkSecondary
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorderBright,
                        focusedContainerColor = Color(0xCC121A30),
                        unfocusedContainerColor = Color(0xB3121A30),
                        focusedTextColor = InkPure,
                        unfocusedTextColor = InkPure
                    )
                )
            }
        }

        // Featured Hero Banner (hidden while typing search query)
        if (!isSearchActive && heroItem != null) {
            item(key = "hero_${heroItem.id}") {
                HeroShowcaseCard(
                    item = heroItem,
                    category = category,
                    accentColor = accentColor,
                    onPlay = { onQuickPlay(heroItem) },
                    onDetails = { onItemClick(heroItem) },
                    onDownload = { onQuickDownload(heroItem) }
                )
            }
        }

        // Language Filter Rail ("Stream in any language")
        item(key = "language_filter_rail") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Language,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "AUDIO & SUBTITLE LANGUAGES",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkSecondary
                    )
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableLanguages) { lang ->
                        val isSelected = lang == selectedLanguage
                        GlassPill(
                            text = lang,
                            containerColor = if (isSelected) accentColor.copy(alpha = 0.28f) else Color(0x66151F38),
                            borderColor = if (isSelected) accentColor else GlassBorderSubtle,
                            contentColor = if (isSelected) InkPure else InkSecondary,
                            onClick = { onSelectLanguage(lang) },
                            modifier = Modifier.testTag("lang_chip_$lang")
                        )
                    }
                }
            }
        }

        // Genre Filter Chips
        if (availableGenres.size > 1) {
            item(key = "genre_filter_rail") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableGenres) { genre ->
                        val isSelected = genre == selectedGenre
                        GlassPill(
                            text = genre,
                            containerColor = if (isSelected) Color(0x3DFFFFFF) else Color(0x4410182B),
                            borderColor = if (isSelected) Color(0x80FFFFFF) else GlassBorderSubtle,
                            contentColor = if (isSelected) InkPure else InkMuted,
                            onClick = { onSelectGenre(genre) }
                        )
                    }
                }
            }
        }

        // Continue Watching Rail
        if (categoryContinue.isNotEmpty() && !isSearchActive) {
            item(key = "continue_watching_section") {
                SectionHeader(
                    title = "Continue Watching",
                    badge = "${categoryContinue.size} IN PROGRESS",
                    accentColor = accentColor
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(categoryContinue, key = { "cw_${it.id}" }) { item ->
                        MediaPosterCard(
                            item = item,
                            onClick = { onQuickPlay(item) },
                            showProgress = true
                        )
                    }
                }
            }
        }

        // Trending Horizontal Rail
        if (filteredItems.isNotEmpty() && !isSearchActive) {
            item(key = "trending_rail") {
                SectionHeader(
                    title = when (category) {
                        MediaCategory.MOVIES -> "Trending 4K Movies"
                        MediaCategory.WEB_SERIES -> "Binge-Worthy Web Series"
                        MediaCategory.ANIME -> "Top Sub & Dub Anime"
                    },
                    badge = "${filteredItems.size} STREAMS",
                    accentColor = accentColor
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredItems, key = { "trend_${it.id}" }) { item ->
                        MediaPosterCard(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }

        // Watchlist Section (if user bookmarked items)
        val catBookmarks = bookmarks.filter { it.category == category }
        if (catBookmarks.isNotEmpty() && !isSearchActive) {
            item(key = "watchlist_rail") {
                SectionHeader(
                    title = "My Watchlist",
                    badge = "${catBookmarks.size} SAVED",
                    accentColor = CinemaGold
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(catBookmarks, key = { "bm_${it.id}" }) { item ->
                        MediaPosterCard(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }

        // Creator Uploads Rail
        if (categoryUploads.isNotEmpty() && !isSearchActive) {
            item(key = "uploads_rail") {
                SectionHeader(
                    title = "Community & Your Uploads",
                    badge = "CREATOR HUB",
                    accentColor = CinemaGold
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(categoryUploads, key = { "up_${it.id}" }) { item ->
                        MediaPosterCard(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }

        // All Matching Catalog Grid (2-column pairs for smooth vertical scrolling)
        item(key = "all_catalog_header") {
            SectionHeader(
                title = if (isSearchActive && searchQuery.isNotBlank()) {
                    "Search Results"
                } else {
                    "All ${category.displayLabel}"
                },
                badge = "${filteredItems.size} TITLES",
                accentColor = accentColor
            )
        }

        if (filteredItems.isEmpty()) {
            item(key = "empty_catalog") {
                EmptyGlassState(
                    icon = Icons.Filled.MovieFilter,
                    title = "No matching streams",
                    message = "Try switching language/genre filters, enabling more Add-on repositories, or uploading your own video in the Upload tab."
                )
            }
        } else {
            val rows = filteredItems.chunked(2)
            items(rows, key = { pair -> pair.joinToString("_") { it.id } }) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { item ->
                        MediaPosterCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            width = null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroShowcaseCard(
    item: MediaItemUi,
    category: MediaCategory,
    accentColor: Color,
    onPlay: () -> Unit,
    onDetails: () -> Unit,
    onDownload: () -> Unit
) {
    val heroShape = RoundedCornerShape(26.dp)
    val heroKey = when (category) {
        MediaCategory.MOVIES -> "movies_hero"
        MediaCategory.WEB_SERIES -> "series_hero"
        MediaCategory.ANIME -> "anime_hero"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
            .clip(heroShape)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(accentColor.copy(alpha = 0.65f), GlassBorderSubtle, Color(0x40FFFFFF))
                ),
                shape = heroShape
            )
            .clickable(onClick = onDetails)
            .testTag("hero_showcase_${item.id}")
    ) {
        // Use the bespoke generated hero backdrop for rich cinematic polish
        MediaArtwork(
            posterUrl = "",
            localPosterKey = heroKey,
            contentDescription = item.title,
            modifier = Modifier.fillMaxSize()
        )

        // Cinematic dark vignette gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33060812),
                            Color(0x66060812),
                            Color(0xF0060812)
                        )
                    )
                )
        )

        // Top badge row
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassPill(
                text = "FEATURED • ${item.maxQuality}",
                containerColor = Color(0xAA0A1122),
                borderColor = accentColor.copy(alpha = 0.7f),
                contentColor = accentColor
            )
            GlassPill(
                text = String.format("%.1f", item.rating),
                leadingIcon = Icons.Filled.Star,
                containerColor = Color(0xAA0A1122),
                borderColor = CinemaGold.copy(alpha = 0.6f),
                contentColor = CinemaGold
            )
        }

        // Bottom frosted metadata + CTAs
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.repoName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = accentColor
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineLarge,
                color = InkPure,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.year} • ${item.genres.take(3).joinToString(" / ")} • ${item.languages.size} Languages",
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPlay,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("hero_play_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = ObsidianDeep
                    )
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Stream Now", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDetails,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("hero_info_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GlassBorderBright),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0x7A121B31),
                        contentColor = InkPure
                    )
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Details")
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("hero_download_button")
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x7A121B31))
                        .border(1.dp, GlassBorderBright, RoundedCornerShape(16.dp))
                        .clickable(onClick = onDownload),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = stringResource(R.string.cd_download_item),
                        tint = InkPure,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    badge: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = InkPure
        )
        Text(
            text = badge,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor
        )
    }
}
