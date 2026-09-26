package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ai.MangaArtStyle
import com.example.data.model.CharacterProfile
import com.example.data.model.MangaPanel
import com.example.data.model.MangaProject
import com.example.ui.MangaStudioViewModel
import com.example.ui.theme.InkBorder
import com.example.ui.theme.InkMidnight
import com.example.ui.theme.InkSurface
import com.example.ui.theme.InkSurfaceVariant
import com.example.ui.theme.MangaCrimson
import com.example.ui.theme.MangaPaperWhite
import com.example.ui.theme.ManhuaCyan
import com.example.ui.theme.QiGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * GeneratedMangaPanelsGallery:
 * A rich, responsive gallery component that displays generated manga panels using Coil.
 * Allows users to inspect, save high-resolution artwork to local device storage,
 * export/share panels via Android system intents, and delete generated artwork locally.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeneratedMangaPanelsGallery(
    viewModel: MangaStudioViewModel,
    onOpenInStudio: (MangaPanel) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val allGeneratedPanels by viewModel.allGeneratedPanels.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val allCharacters by viewModel.allCharacters.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterProject by remember { mutableStateOf<Long?>(null) } // null = All
    var selectedCharacterFilter by remember { mutableStateOf<Long?>(null) }
    var onlyWithDialogue by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }

    // Dialog states
    var inspectedPanel by remember { mutableStateOf<MangaPanel?>(null) }
    var panelToDelete by remember { mutableStateOf<MangaPanel?>(null) }
    var isSavingArtworkId by remember { mutableStateOf<Long?>(null) }
    var isExportingArtworkId by remember { mutableStateOf<Long?>(null) }

    // Filtered panels list
    val filteredPanels = remember(
        allGeneratedPanels,
        searchQuery,
        selectedFilterProject,
        selectedCharacterFilter,
        onlyWithDialogue
    ) {
        allGeneratedPanels.filter { panel ->
            val hasValidImage = !panel.imagePath.isNullOrBlank() && File(panel.imagePath).exists()
            if (!hasValidImage) return@filter false

            // Search query filter
            val matchesQuery = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase(Locale.ROOT)
                panel.userPrompt.lowercase(Locale.ROOT).contains(q) ||
                    panel.enrichedPrompt.lowercase(Locale.ROOT).contains(q) ||
                    (panel.dialogueText?.lowercase(Locale.ROOT)?.contains(q) == true) ||
                    (panel.characterId != null && allCharacters.any { it.id == panel.characterId && it.name.lowercase(Locale.ROOT).contains(q) })
            }

            // Character filter
            val matchesChar = if (selectedCharacterFilter == null) true else panel.characterId == selectedCharacterFilter

            // Dialogue filter
            val matchesDialogue = if (!onlyWithDialogue) true else !panel.dialogueText.isNullOrBlank()

            matchesQuery && matchesChar && matchesDialogue
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("generated_manga_panels_gallery")
    ) {
        // --- 1. Gallery Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = null,
                        tint = MangaCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GALERIE D'ARTWORK MANGA",
                        color = MangaCrimson,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "${filteredPanels.size} case(s) générée(s) par l'IA avec Coil",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // View Mode Toggle (Grid vs List)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(InkSurface)
                    .border(1.dp, InkBorder, RoundedCornerShape(8.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = { isGridView = true },
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (isGridView) MangaCrimson else Color.Transparent, RoundedCornerShape(6.dp))
                        .testTag("gallery_view_grid_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Vue Grille",
                        tint = if (isGridView) Color.White else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { isGridView = false },
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (!isGridView) MangaCrimson else Color.Transparent, RoundedCornerShape(6.dp))
                        .testTag("gallery_view_list_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewAgenda,
                        contentDescription = "Vue Liste Détaillée",
                        tint = if (!isGridView) Color.White else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 2. Search Field ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher par prompt, dialogue, personnage...", fontSize = 12.sp, color = TextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MangaCrimson, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Effacer", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = InkSurface,
                unfocusedContainerColor = InkSurface,
                focusedBorderColor = MangaCrimson,
                unfocusedBorderColor = InkBorder,
                focusedTextColor = MangaPaperWhite,
                unfocusedTextColor = MangaPaperWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gallery_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // --- 3. Filter Chips Horizontal Reel ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chip: All panels
            FilterChip(
                selected = selectedCharacterFilter == null && !onlyWithDialogue,
                onClick = {
                    selectedCharacterFilter = null
                    onlyWithDialogue = false
                },
                label = { Text("Toutes (${allGeneratedPanels.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MangaCrimson.copy(alpha = 0.25f),
                    selectedLabelColor = MangaCrimson,
                    containerColor = InkSurface,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (selectedCharacterFilter == null && !onlyWithDialogue) MangaCrimson else InkBorder,
                    enabled = true,
                    selected = selectedCharacterFilter == null && !onlyWithDialogue
                )
            )

            // Chip: With speech bubbles
            FilterChip(
                selected = onlyWithDialogue,
                onClick = { onlyWithDialogue = !onlyWithDialogue },
                leadingIcon = {
                    Icon(
                        Icons.Default.ChatBubble,
                        contentDescription = null,
                        tint = if (onlyWithDialogue) ManhuaCyan else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = { Text("Avec bulles (${allGeneratedPanels.count { !it.dialogueText.isNullOrBlank() }})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ManhuaCyan.copy(alpha = 0.2f),
                    selectedLabelColor = ManhuaCyan,
                    containerColor = InkSurface,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (onlyWithDialogue) ManhuaCyan else InkBorder,
                    enabled = true,
                    selected = onlyWithDialogue
                )
            )

            // Character specific filter chips
            allCharacters.forEach { char ->
                val charCount = allGeneratedPanels.count { it.characterId == char.id }
                if (charCount > 0) {
                    val isSelected = selectedCharacterFilter == char.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCharacterFilter = if (isSelected) null else char.id
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isSelected) QiGold else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        label = { Text("${char.name} ($charCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = QiGold.copy(alpha = 0.2f),
                            selectedLabelColor = QiGold,
                            containerColor = InkSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) QiGold else InkBorder,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 4. Panels Content (Empty State or Grid/List) ---
        if (filteredPanels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(InkSurface)
                    .border(1.dp, InkBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = if (allGeneratedPanels.isEmpty())
                            "Aucune case générée pour le moment."
                        else
                            "Aucune case ne correspond aux filtres actuels.",
                        color = MangaPaperWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (allGeneratedPanels.isEmpty())
                            "Générez des cases dans le Studio ou le Storyboard pour voir apparaître vos créations ici avec Coil."
                        else
                            "Essayez d'effacer vos critères de recherche ou réinitialisez les filtres.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    if (searchQuery.isNotEmpty() || selectedCharacterFilter != null || onlyWithDialogue) {
                        OutlinedButton(
                            onClick = {
                                searchQuery = ""
                                selectedCharacterFilter = null
                                onlyWithDialogue = false
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ManhuaCyan),
                            border = BorderStroke(1.dp, ManhuaCyan)
                        ) {
                            Text("Réinitialiser les filtres", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // Panels Display: Either Adaptive 2-Column Grid or Rich List
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(560.dp)
                        .testTag("gallery_panels_grid")
                ) {
                    items(filteredPanels, key = { it.id }) { panel ->
                        val character = allCharacters.firstOrNull { it.id == panel.characterId }
                        GeneratedPanelGridCard(
                            panel = panel,
                            character = character,
                            isSaving = isSavingArtworkId == panel.id,
                            isExporting = isExportingArtworkId == panel.id,
                            onInspect = { inspectedPanel = panel },
                            onSaveLocally = {
                                isSavingArtworkId = panel.id
                                viewModel.savePanelToDeviceGallery(context, panel) { success, msg ->
                                    isSavingArtworkId = null
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onExportArtwork = {
                                isExportingArtworkId = panel.id
                                viewModel.exportPanelArtwork(context, panel) { _, _ ->
                                    isExportingArtworkId = null
                                }
                            },
                            onDelete = { panelToDelete = panel }
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gallery_panels_list")
                ) {
                    filteredPanels.forEach { panel ->
                        val character = allCharacters.firstOrNull { it.id == panel.characterId }
                        GeneratedPanelListCard(
                            panel = panel,
                            character = character,
                            isSaving = isSavingArtworkId == panel.id,
                            isExporting = isExportingArtworkId == panel.id,
                            onInspect = { inspectedPanel = panel },
                            onOpenInStudio = { onOpenInStudio(panel) },
                            onSaveLocally = {
                                isSavingArtworkId = panel.id
                                viewModel.savePanelToDeviceGallery(context, panel) { success, msg ->
                                    isSavingArtworkId = null
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onExportArtwork = {
                                isExportingArtworkId = panel.id
                                viewModel.exportPanelArtwork(context, panel) { _, _ ->
                                    isExportingArtworkId = null
                                }
                            },
                            onDelete = { panelToDelete = panel }
                        )
                    }
                }
            }
        }
    }

    // --- 5. Fullscreen Lightbox / Panel Inspection Dialog ---
    if (inspectedPanel != null) {
        val targetPanel = inspectedPanel!!
        val character = allCharacters.firstOrNull { it.id == targetPanel.characterId }
        val associatedProject = allProjects.firstOrNull { it.id == selectedProject?.id }

        PanelInspectionDialog(
            panel = targetPanel,
            character = character,
            project = associatedProject,
            isSaving = isSavingArtworkId == targetPanel.id,
            isExporting = isExportingArtworkId == targetPanel.id,
            onDismiss = { inspectedPanel = null },
            onSaveLocally = {
                isSavingArtworkId = targetPanel.id
                viewModel.savePanelToDeviceGallery(context, targetPanel) { success, msg ->
                    isSavingArtworkId = null
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onExportArtwork = {
                isExportingArtworkId = targetPanel.id
                viewModel.exportPanelArtwork(context, targetPanel) { _, _ ->
                    isExportingArtworkId = null
                }
            },
            onOpenInStudio = {
                onOpenInStudio(targetPanel)
                inspectedPanel = null
            },
            onDelete = {
                panelToDelete = targetPanel
                inspectedPanel = null
            }
        )
    }

    // --- 6. Delete Artwork Confirmation Dialog ---
    if (panelToDelete != null) {
        val targetToDelete = panelToDelete!!
        AlertDialog(
            onDismissRequest = { panelToDelete = null },
            containerColor = InkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MangaCrimson,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Supprimer l'œuvre locale ?",
                        color = MangaPaperWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Voulez-vous supprimer l'illustration générée pour la case #${targetToDelete.panelIndex + 1} ?",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    if (targetToDelete.imagePath != null) {
                        Text(
                            text = "Fichier local : ${File(targetToDelete.imagePath).name}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGeneratedPanelArtwork(targetToDelete, deleteEntirePanel = true)
                        panelToDelete = null
                        Toast.makeText(context, "Case supprimée", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                    modifier = Modifier.testTag("confirm_delete_panel_btn")
                ) {
                    Text("Supprimer la case", color = Color.White)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.deleteGeneratedPanelArtwork(targetToDelete, deleteEntirePanel = false)
                            panelToDelete = null
                            Toast.makeText(context, "Illustration effacée", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Garder la case (effacer l'image)", color = ManhuaCyan, fontSize = 11.sp)
                    }
                    TextButton(onClick = { panelToDelete = null }) {
                        Text("Annuler", color = TextSecondary)
                    }
                }
            }
        )
    }
}

/**
 * Grid Card: Compact Manga Panel Presentation with Coil AsyncImage, badges, and quick actions.
 */
@Composable
fun GeneratedPanelGridCard(
    panel: MangaPanel,
    character: CharacterProfile?,
    isSaving: Boolean,
    isExporting: Boolean,
    onInspect: () -> Unit,
    onSaveLocally: () -> Unit,
    onExportArtwork: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageFile = remember(panel.imagePath) { panel.imagePath?.let { File(it) } }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = InkSurface),
        border = BorderStroke(1.dp, InkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onInspect() }
            .testTag("gallery_panel_grid_card_${panel.id}")
    ) {
        Column {
            // Coil AsyncImage Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.12f)
                    .background(Color.Black)
            ) {
                if (imageFile != null && imageFile.exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageFile)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Case #${panel.panelIndex + 1}: ${panel.userPrompt}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Top Badge: Panel Index
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(bottomEnd = 6.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "Case #${panel.panelIndex + 1}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Top Right Badge: Speech Bubble indicator
                if (!panel.dialogueText.isNullOrBlank()) {
                    Surface(
                        color = ManhuaCyan.copy(alpha = 0.9f),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Bulle présente",
                            tint = Color.Black,
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                }

                // Bottom Gradient Overlay with prompt snippet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                ) {
                    Text(
                        text = panel.userPrompt.ifBlank { "Illustration sans prompt" },
                        color = Color.White,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            // Character DNA badge if linked
            if (character != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141724))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = QiGold,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = character.name,
                        color = QiGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Action Buttons Bar: Save, Export, Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InkSurface)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Save locally
                IconButton(
                    onClick = onSaveLocally,
                    enabled = !isSaving,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("save_panel_btn_${panel.id}")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = ManhuaCyan)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Sauvegarder dans la galerie",
                            tint = ManhuaCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Export / Share
                IconButton(
                    onClick = onExportArtwork,
                    enabled = !isExporting,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("export_panel_btn_${panel.id}")
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = MangaPaperWhite)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Partager ou exporter",
                            tint = MangaPaperWhite,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Zoom / Inspect
                IconButton(
                    onClick = onInspect,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("inspect_panel_btn_${panel.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Aperçu détaillé",
                        tint = QiGold,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_panel_btn_${panel.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer l'œuvre",
                        tint = TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/**
 * List Card: Detailed Manga Panel Presentation with larger horizontal layout,
 * full prompt preview, dialogue bubble details, and primary action affordances.
 */
@Composable
fun GeneratedPanelListCard(
    panel: MangaPanel,
    character: CharacterProfile?,
    isSaving: Boolean,
    isExporting: Boolean,
    onInspect: () -> Unit,
    onOpenInStudio: () -> Unit,
    onSaveLocally: () -> Unit,
    onExportArtwork: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageFile = remember(panel.imagePath) { panel.imagePath?.let { File(it) } }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = InkSurface),
        border = BorderStroke(1.dp, InkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onInspect() }
            .testTag("gallery_panel_list_card_${panel.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Coil Thumbnail Box
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 98.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .border(1.dp, InkBorder, RoundedCornerShape(8.dp))
                ) {
                    if (imageFile != null && imageFile.exists()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageFile)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Aperçu case",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted)
                        }
                    }

                    // Index badge
                    Surface(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(bottomEnd = 4.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "#${panel.panelIndex + 1}",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Metadata Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CASE #${panel.panelIndex + 1}",
                            color = MangaCrimson,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )

                        if (character != null) {
                            Surface(
                                color = QiGold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, QiGold.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = QiGold, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(character.name, color = QiGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = panel.userPrompt.ifBlank { "Prompt non renseigné" },
                        color = MangaPaperWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Dialogue quote preview if present
                    if (!panel.dialogueText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = null,
                                tint = ManhuaCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "« ${panel.dialogueText} »",
                                color = ManhuaCyan,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Save locally
                OutlinedButton(
                    onClick = onSaveLocally,
                    enabled = !isSaving,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ManhuaCyan),
                    border = BorderStroke(1.dp, ManhuaCyan.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = ManhuaCyan)
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sauvegarder", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Export / Share
                OutlinedButton(
                    onClick = onExportArtwork,
                    enabled = !isExporting,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MangaPaperWhite),
                    border = BorderStroke(1.dp, InkBorder),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = MangaPaperWhite)
                    } else {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exporter", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Open in Studio
                Button(
                    onClick = onOpenInStudio,
                    colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Studio", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = TextMuted, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

/**
 * PanelInspectionDialog:
 * Lightbox dialog displaying full-screen preview with Coil, complete metadata,
 * prompt breakdown, character visual anchor details, and direct action triggers.
 */
@Composable
fun PanelInspectionDialog(
    panel: MangaPanel,
    character: CharacterProfile?,
    project: MangaProject?,
    isSaving: Boolean,
    isExporting: Boolean,
    onDismiss: () -> Unit,
    onSaveLocally: () -> Unit,
    onExportArtwork: () -> Unit,
    onOpenInStudio: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val imageFile = remember(panel.imagePath) { panel.imagePath?.let { File(it) } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = InkMidnight,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, InkBorder, RoundedCornerShape(14.dp))
                .testTag("panel_inspection_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CASE MANGA #${panel.panelIndex + 1}",
                            color = MangaCrimson,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (project != null) "Projet : ${project.title}" else "Aperçu de planche",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // High-Res Coil AsyncImage Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black)
                        .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageFile != null && imageFile.exists()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageFile)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Illustration HD de la case",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                            Text("Illustration introuvable", color = TextMuted, fontSize = 12.sp)
                        }
                    }

                    // Render Speech Bubble overlay if dialogue exists
                    if (!panel.dialogueText.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(14.dp)
                        ) {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(2.dp, Color.Black),
                                shadowElevation = 4.dp
                            ) {
                                Text(
                                    text = panel.dialogueText,
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata Details Box
                Surface(
                    color = InkSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, InkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "PROMPT DE LA CASE :",
                            color = ManhuaCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = panel.userPrompt.ifBlank { "Aucun prompt utilisateur" },
                            color = MangaPaperWhite,
                            fontSize = 12.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (character != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = QiGold, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Personnage lié : ${character.name} (#${character.visualUid})",
                                    color = QiGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons: Save, Export, Open in Studio, Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Save Locally
                    Button(
                        onClick = onSaveLocally,
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = ManhuaCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("dialog_save_locally_btn")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sauvegarder", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Export / Share
                    OutlinedButton(
                        onClick = onExportArtwork,
                        enabled = !isExporting,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, InkBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("dialog_export_btn")
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exporter", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Open in Studio
                    Button(
                        onClick = onOpenInStudio,
                        colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("dialog_open_studio_btn")
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Delete
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(42.dp)
                            .background(InkSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, InkBorder, RoundedCornerShape(8.dp))
                            .testTag("dialog_delete_panel_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MangaCrimson, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
