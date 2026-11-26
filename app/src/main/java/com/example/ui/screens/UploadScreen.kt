package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MediaCategory
import com.example.data.model.MediaItemUi
import com.example.data.model.SupportedLanguages
import com.example.ui.components.EmptyGlassState
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

@Composable
fun UploadScreen(
    userUploads: List<MediaItemUi>,
    isUploading: Boolean,
    onUploadSubmit: (
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
    ) -> Unit,
    onPlayUpload: (MediaItemUi) -> Unit,
    onOpenDetail: (MediaItemUi) -> Unit,
    onDeleteUpload: (MediaItemUi) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MediaCategory.MOVIES) }
    var genre by remember { mutableStateOf("Sci-Fi") }
    var yearText by remember { mutableStateOf("2026") }
    var quality by remember { mutableStateOf("1080p") }
    var synopsis by remember { mutableStateOf("") }
    var episodeTitle by remember { mutableStateOf("") }
    var remoteStreamUrl by remember { mutableStateOf("") }
    var pickedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var pickedPosterUri by remember { mutableStateOf<Uri?>(null) }
    var selectedLanguages by remember { mutableStateOf(setOf("English", "Hindi")) }

    // Zero-permission Android Photo Picker for Video & Poster artwork (Google Play compliant)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedVideoUri = uri
        }
    }

    val posterPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedPosterUri = uri
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Studio Hero Banner
        item(key = "upload_hero") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, CinemaGold.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_upload_studio),
                    contentDescription = "Creator Upload Studio",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0x55060812), Color(0xE6060812))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GlassPill(
                        text = "CREATOR STUDIO • LOCAL LIBRARY",
                        containerColor = Color(0xAA0C1324),
                        borderColor = CinemaGold.copy(alpha = 0.7f),
                        contentColor = CinemaGold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Upload Movies, Series & Anime",
                        style = MaterialTheme.typography.headlineMedium,
                        color = InkPure
                    )
                    Text(
                        text = "Pick videos directly from your gallery or publish a stream URL right into your catalog.",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
            }
        }

        // Upload Form Glass Card
        item(key = "upload_form") {
            GlassCard(
                accentTint = CinemaGold,
                borderAccent = GlassBorderBright,
                contentPadding = PaddingValues(18.dp)
            ) {
                Text(
                    text = "1. Select Video Source",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkPure
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("upload_pick_video_button"),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            if (pickedVideoUri != null) MintEmerald else NeonCyan
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (pickedVideoUri != null) MintEmerald.copy(alpha = 0.18f) else Color(0x66122138),
                            contentColor = if (pickedVideoUri != null) MintEmerald else NeonCyan
                        )
                    ) {
                        Icon(
                            imageVector = if (pickedVideoUri != null) Icons.Filled.CheckCircle else Icons.Filled.VideoFile,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (pickedVideoUri != null) "Video Ready" else "Pick Video",
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            posterPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("upload_pick_poster_button"),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            if (pickedPosterUri != null) MintEmerald else GlassBorderBright
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x55121B31),
                            contentColor = if (pickedPosterUri != null) MintEmerald else InkPure
                        )
                    ) {
                        Icon(
                            imageVector = if (pickedPosterUri != null) Icons.Filled.CheckCircle else Icons.Filled.Image,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (pickedPosterUri != null) "Poster Set" else "Cover Art",
                            maxLines = 1
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = remoteStreamUrl,
                    onValueChange = { remoteStreamUrl = it },
                    label = { Text("Or Paste Stream URL (.mp4 / .m3u8)") },
                    placeholder = { Text("https://example.com/my-film.mp4") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_url_input"),
                    colors = glassTextFieldColors()
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "2. Metadata & Category",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkPure
                )
                Spacer(Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MediaCategory.entries) { cat ->
                        val active = cat == category
                        GlassPill(
                            text = cat.displayLabel,
                            containerColor = if (active) CinemaGold.copy(alpha = 0.28f) else Color(0x55121A30),
                            borderColor = if (active) CinemaGold else GlassBorderSubtle,
                            contentColor = if (active) InkPure else InkSecondary,
                            onClick = { category = cat },
                            modifier = Modifier.testTag("upload_cat_${cat.key}")
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    placeholder = { Text("e.g. Cyber Horizon") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_title_input"),
                    colors = glassTextFieldColors()
                )

                if (category != MediaCategory.MOVIES) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = episodeTitle,
                        onValueChange = { episodeTitle = it },
                        label = { Text("Episode 1 Title (Optional)") },
                        placeholder = { Text("e.g. Pilot: Neon Awakening") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = glassTextFieldColors()
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("Genre") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f),
                        colors = glassTextFieldColors()
                    )
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { yearText = it.filter { ch -> ch.isDigit() }.take(4) },
                        label = { Text("Year") },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f),
                        colors = glassTextFieldColors()
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text("Quality Tag", style = MaterialTheme.typography.labelLarge, color = InkSecondary)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("4K", "1080p", "720p")) { q ->
                        val active = q == quality
                        GlassPill(
                            text = q,
                            containerColor = if (active) NeonCyan.copy(alpha = 0.25f) else Color(0x55121A30),
                            borderColor = if (active) NeonCyan else GlassBorderSubtle,
                            contentColor = if (active) InkPure else InkSecondary,
                            onClick = { quality = q }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text("Audio / Subtitle Languages", style = MaterialTheme.typography.labelLarge, color = InkSecondary)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SupportedLanguages.all) { lang ->
                        val active = lang.name in selectedLanguages
                        GlassPill(
                            text = lang.name,
                            containerColor = if (active) CinemaCoral.copy(alpha = 0.25f) else Color(0x55121A30),
                            borderColor = if (active) CinemaCoral else GlassBorderSubtle,
                            contentColor = if (active) InkPure else InkSecondary,
                            onClick = {
                                selectedLanguages = if (active && selectedLanguages.size > 1) {
                                    selectedLanguages - lang.name
                                } else {
                                    selectedLanguages + lang.name
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = synopsis,
                    onValueChange = { synopsis = it },
                    label = { Text("Synopsis") },
                    placeholder = { Text("Write a short description for viewers...") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = glassTextFieldColors()
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        onUploadSubmit(
                            title,
                            category,
                            genre,
                            yearText.toIntOrNull() ?: 2026,
                            quality,
                            selectedLanguages.toList(),
                            synopsis,
                            pickedVideoUri,
                            remoteStreamUrl,
                            pickedPosterUri,
                            episodeTitle.takeIf { it.isNotBlank() }
                        ) {
                            title = ""
                            synopsis = ""
                            episodeTitle = ""
                            remoteStreamUrl = ""
                            pickedVideoUri = null
                            pickedPosterUri = null
                        }
                    },
                    enabled = !isUploading && title.isNotBlank() && (pickedVideoUri != null || remoteStreamUrl.isNotBlank()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("upload_publish_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaGold,
                        contentColor = ObsidianDeep
                    )
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            color = ObsidianDeep,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Publish to ${category.displayLabel}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // User Uploaded Library
        item(key = "your_uploads_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Uploads",
                    style = MaterialTheme.typography.headlineSmall,
                    color = InkPure
                )
                Text(
                    text = "${userUploads.size} PUBLISHED",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold
                )
            }
        }

        if (userUploads.isEmpty()) {
            item(key = "empty_uploads") {
                EmptyGlassState(
                    icon = Icons.Filled.CloudUpload,
                    title = "No uploads yet",
                    message = "Videos you publish appear here and automatically show up inside the Movies, Web Series, or Anime tabs."
                )
            }
        } else {
            items(userUploads, key = { "upload_item_${it.id}" }) { item ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onOpenDetail(item) },
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MediaArtwork(
                            posterUrl = item.posterUrl,
                            localPosterKey = item.localPosterKey,
                            contentDescription = item.title,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = InkPure,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.category.displayLabel} • ${item.maxQuality} • ${item.languages.joinToString { it.code.uppercase() }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeonCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(NeonCyan)
                                .clickable { onPlayUpload(item) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = stringResource(R.string.cd_play),
                                tint = ObsidianDeep
                            )
                        }
                        IconButton(onClick = { onDeleteUpload(item) }) {
                            Icon(
                                Icons.Filled.DeleteOutline,
                                contentDescription = stringResource(R.string.cd_delete),
                                tint = CinemaCoral
                            )
                        }
                    }
                }
            }
        }
    }
}
