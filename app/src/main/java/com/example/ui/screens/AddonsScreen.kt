package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AddonRepositoryEntity
import com.example.data.model.MediaCategory
import com.example.data.model.SupportedLanguages
import com.example.data.seed.OpenCinemaCatalog
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.theme.AuroraViolet
import com.example.ui.theme.CinemaCoral
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddonsScreen(
    repositories: List<AddonRepositoryEntity>,
    totalIndexedItems: Int,
    isSyncing: Boolean,
    onToggleRepo: (repoId: String, enabled: Boolean) -> Unit,
    onSyncRepo: (AddonRepositoryEntity) -> Unit,
    onDeleteRepo: (repoId: String) -> Unit,
    onAddRepoUrl: (url: String, customName: String) -> Unit,
    onImportRepoJson: (rawJson: String, customName: String) -> Unit,
    onLinkStreamingSite: (
        siteName: String,
        title: String,
        streamUrl: String,
        category: MediaCategory,
        quality: String,
        languages: List<String>,
        synopsis: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddUrlDialog by remember { mutableStateOf(false) }
    var showLinkSiteDialog by remember { mutableStateOf(false) }
    var showJsonDialog by remember { mutableStateOf(false) }

    val activeCount = remember(repositories) { repositories.count { it.isEnabled } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overview Hero Glass Card
        item(key = "addons_overview") {
            GlassCard(
                accentTint = MintEmerald,
                borderAccent = MintEmerald.copy(alpha = 0.5f),
                contentPadding = PaddingValues(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Repository & Add-on Engine",
                            style = MaterialTheme.typography.headlineMedium,
                            color = InkPure
                        )
                        Text(
                            text = "Connect JSON catalog repositories, M3U8 playlists, or link direct streaming URLs into Movies, Series & Anime.",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = MintEmerald,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RepoStatTile(
                        label = "ACTIVE REPOS",
                        value = "$activeCount/${repositories.size}",
                        accent = MintEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    RepoStatTile(
                        label = "STREAMS INDEXED",
                        value = totalIndexedItems.toString(),
                        accent = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Primary Add-on Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showAddUrlDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("addons_add_repo_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintEmerald,
                            contentColor = ObsidianDeep
                        )
                    ) {
                        Icon(Icons.Filled.AddLink, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Repo URL", fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = { showLinkSiteDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("addons_link_site_button"),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x66122138),
                            contentColor = NeonCyan
                        )
                    ) {
                        Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Link Stream", fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showJsonDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("addons_import_json_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GlassBorderBright),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0x4410182B),
                        contentColor = InkPure
                    )
                ) {
                    Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Import Custom JSON Repository Manifest")
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = MintEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Declarative JSON/M3U catalog engine • Zero unsafe code execution",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkMuted
                    )
                }
            }
        }

        // Section Header
        item(key = "installed_repos_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Installed Repositories",
                    style = MaterialTheme.typography.headlineSmall,
                    color = InkPure
                )
                Text(
                    text = "${repositories.size} SOURCES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MintEmerald
                )
            }
        }

        items(repositories, key = { it.id }) { repo ->
            RepositoryCard(
                repo = repo,
                onToggle = { enabled -> onToggleRepo(repo.id, enabled) },
                onSync = { onSyncRepo(repo) },
                onDelete = if (!repo.isBuiltIn) { { onDeleteRepo(repo.id) } } else null
            )
        }
    }

    if (showAddUrlDialog) {
        AddRepoUrlDialog(
            onDismiss = { showAddUrlDialog = false },
            onConfirm = { url, name ->
                onAddRepoUrl(url, name)
                showAddUrlDialog = false
            }
        )
    }

    if (showLinkSiteDialog) {
        LinkStreamDialog(
            onDismiss = { showLinkSiteDialog = false },
            onConfirm = { site, title, url, cat, qual, langs, syn ->
                onLinkStreamingSite(site, title, url, cat, qual, langs, syn)
                showLinkSiteDialog = false
            }
        )
    }

    if (showJsonDialog) {
        ImportJsonDialog(
            onDismiss = { showJsonDialog = false },
            onConfirm = { json, name ->
                onImportRepoJson(json, name)
                showJsonDialog = false
            }
        )
    }
}

@Composable
private fun RepoStatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x660B1120))
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = InkMuted)
        Text(text = value, style = MaterialTheme.typography.headlineMedium, color = accent, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RepositoryCard(
    repo: AddonRepositoryEntity,
    onToggle: (Boolean) -> Unit,
    onSync: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val timeLabel = remember(repo.lastSyncedAt) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(repo.lastSyncedAt))
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("repo_card_${repo.id}"),
        accentTint = if (repo.isEnabled) NeonCyan else ObsidianElevated,
        borderAccent = if (repo.isEnabled) NeonCyan.copy(alpha = 0.4f) else GlassBorderSubtle,
        contentPadding = PaddingValues(16.dp)
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
                    .background(if (repo.isEnabled) MintEmerald.copy(alpha = 0.2f) else ObsidianElevated)
                    .border(1.dp, if (repo.isEnabled) MintEmerald else GlassBorderSubtle, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = repo.iconBadge.take(4),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (repo.isEnabled) MintEmerald else InkMuted,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = InkPure,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${repo.author} • ${repo.itemCount} titles • Synced $timeLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Switch(
                checked = repo.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ObsidianDeep,
                    checkedTrackColor = MintEmerald,
                    uncheckedThumbColor = InkSecondary,
                    uncheckedTrackColor = ObsidianElevated
                )
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = repo.description,
            style = MaterialTheme.typography.bodySmall,
            color = InkSecondary
        )

        if (!repo.lastError.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Last sync error: ${repo.lastError}",
                style = MaterialTheme.typography.bodySmall,
                color = CinemaCoral
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = repo.manifestUrl,
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSync) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.cd_sync_repo),
                        tint = NeonCyan
                    )
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = stringResource(R.string.cd_delete),
                            tint = CinemaCoral
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddRepoUrlDialog(
    onDismiss: () -> Unit,
    onConfirm: (url: String, name: String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        title = { Text("Add Streaming Repository URL", color = InkPure) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Paste a CloudStream-style JSON catalog URL or an M3U/M3U8 playlist URL. CineGlass fetches and indexes all streams automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Repository Name (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = glassTextFieldColors()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("JSON or M3U8 Repository URL") },
                    placeholder = { Text("https://example.com/catalog.json") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repo_url_input"),
                    colors = glassTextFieldColors()
                )
                TextButton(
                    onClick = {
                        name = "Mux Adaptive HLS Demo"
                        url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
                    }
                ) {
                    Text("Fill live test HLS feed URL", color = NeonCyan)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(url, name) },
                enabled = url.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MintEmerald, contentColor = ObsidianDeep)
            ) {
                Text("Fetch & Add", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = InkSecondary) }
        }
    )
}

@Composable
private fun LinkStreamDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        siteName: String,
        title: String,
        streamUrl: String,
        category: MediaCategory,
        quality: String,
        languages: List<String>,
        synopsis: String
    ) -> Unit
) {
    var siteName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var streamUrl by remember { mutableStateOf("") }
    var synopsis by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MediaCategory.MOVIES) }
    var quality by remember { mutableStateOf("1080p") }
    var selectedLangs by remember { mutableStateOf(setOf("English", "Hindi")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        title = { Text("Link Streaming Site / Source", color = InkPure) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = siteName,
                    onValueChange = { siteName = it },
                    label = { Text("Site / Provider Name") },
                    placeholder = { Text("e.g. OpenArchive HD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = glassTextFieldColors()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Movie / Series / Anime Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = glassTextFieldColors()
                )
                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { streamUrl = it },
                    label = { Text("Direct Stream URL (.mp4 / .m3u8 / .mpd)") },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = glassTextFieldColors()
                )
                Text("Category", style = MaterialTheme.typography.labelLarge, color = InkPure)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MediaCategory.entries) { cat ->
                        val active = cat == category
                        GlassPill(
                            text = cat.displayLabel,
                            containerColor = if (active) NeonCyan.copy(alpha = 0.25f) else Color(0x55121A30),
                            borderColor = if (active) NeonCyan else GlassBorderSubtle,
                            contentColor = if (active) InkPure else InkSecondary,
                            onClick = { category = cat }
                        )
                    }
                }
                Text("Max Quality", style = MaterialTheme.typography.labelLarge, color = InkPure)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("4K", "1080p", "720p")) { q ->
                        val active = q == quality
                        GlassPill(
                            text = q,
                            containerColor = if (active) MintEmerald.copy(alpha = 0.25f) else Color(0x55121A30),
                            borderColor = if (active) MintEmerald else GlassBorderSubtle,
                            contentColor = if (active) InkPure else InkSecondary,
                            onClick = { quality = q }
                        )
                    }
                }
                Text("Languages", style = MaterialTheme.typography.labelLarge, color = InkPure)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SupportedLanguages.all.take(6)) { lang ->
                        val active = lang.name in selectedLangs
                        GlassPill(
                            text = lang.name,
                            containerColor = if (active) CinemaCoral.copy(alpha = 0.25f) else Color(0x55121A30),
                            borderColor = if (active) CinemaCoral else GlassBorderSubtle,
                            contentColor = if (active) InkPure else InkSecondary,
                            onClick = {
                                selectedLangs = if (active && selectedLangs.size > 1) {
                                    selectedLangs - lang.name
                                } else {
                                    selectedLangs + lang.name
                                }
                            }
                        )
                    }
                }
                OutlinedTextField(
                    value = synopsis,
                    onValueChange = { synopsis = it },
                    label = { Text("Description (Optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = glassTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(siteName, title, streamUrl, category, quality, selectedLangs.toList(), synopsis)
                },
                enabled = title.isNotBlank() && streamUrl.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ObsidianDeep)
            ) {
                Text("Link & Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = InkSecondary) }
        }
    )
}

@Composable
private fun ImportJsonDialog(
    onDismiss: () -> Unit,
    onConfirm: (rawJson: String, customName: String) -> Unit
) {
    var customName by remember { mutableStateOf("") }
    var rawJson by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        title = { Text("Import JSON Repository", color = InkPure) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Paste a CineGlass JSON repository manifest to add new movies, web series, and anime sources immediately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
                TextButton(
                    onClick = {
                        customName = "Community Open Cinema Pack"
                        rawJson = OpenCinemaCatalog.sampleRepoJsonTemplate
                    }
                ) {
                    Text("Load Sample Repository JSON", color = NeonCyan)
                }
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Repo Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = glassTextFieldColors()
                )
                OutlinedTextField(
                    value = rawJson,
                    onValueChange = { rawJson = it },
                    label = { Text("Repository JSON") },
                    minLines = 5,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repo_json_input"),
                    colors = glassTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(rawJson, customName) },
                enabled = rawJson.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ObsidianDeep)
            ) {
                Text("Import Repo", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = InkSecondary) }
        }
    )
}

@Composable
fun glassTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = GlassBorderSubtle,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = InkMuted,
    focusedTextColor = InkPure,
    unfocusedTextColor = InkPure,
    focusedContainerColor = Color(0x8810182B),
    unfocusedContainerColor = Color(0x6610182B),
    cursorColor = NeonCyan
)
