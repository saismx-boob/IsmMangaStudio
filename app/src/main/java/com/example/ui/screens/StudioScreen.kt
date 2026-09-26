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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import com.example.ai.provider.AiProvider
import com.example.ui.components.AiProviderSettingsDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.MangaPanel
import com.example.ui.MangaStudioViewModel
import com.example.ui.components.ArtStyleSelectorRow
import com.example.ui.components.BackgroundSelectorDialog
import com.example.ui.components.CameraAngleSelector
import com.example.ui.components.CharacterSelectorDialog
import com.example.ui.components.ColorAndLineStyleSelector
import com.example.ui.components.ConsistencyDNASummaryCard
import com.example.ui.components.DialogueBubbleEditorDialog
import com.example.ui.components.ExportPanelDialog
import com.example.ui.components.MangaArtAndInkControlSection
import com.example.ui.components.MangaPanelCanvas
import com.example.ui.components.PromptBuilderCard
import com.example.ui.components.RealTimePageCompositionPreview
import com.example.util.MangaPanelExporter
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.ui.theme.InkBorder
import com.example.ui.theme.InkMidnight
import com.example.ui.theme.InkSurface
import com.example.ui.theme.InkSurfaceVariant
import com.example.ui.theme.MangaCrimson
import com.example.ui.theme.MangaPaperWhite
import com.example.ui.theme.ManhuaCyan
import com.example.ui.theme.QiGold
import com.example.ui.theme.TextSecondary
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: MangaStudioViewModel,
    onNavigateToCharacters: () -> Unit,
    onNavigateToBackgrounds: () -> Unit,
    onNavigateToReader: () -> Unit,
    onNavigateToStoryboard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val editorState by viewModel.editorState.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    val pages by viewModel.currentPages.collectAsState()
    val selectedPage by viewModel.selectedPage.collectAsState()
    val panels by viewModel.currentPanels.collectAsState()
    val characters by viewModel.allCharacters.collectAsState()
    val backgrounds by viewModel.allBackgrounds.collectAsState()
    val activeProvider by viewModel.activeAiProvider.collectAsState()
    val activeAiConfig by viewModel.activeAiConfig.collectAsState()

    var showAiSettingsDialog by remember { mutableStateOf(false) }
    var showCharacterDialog by remember { mutableStateOf(false) }
    var showBackgroundDialog by remember { mutableStateOf(false) }
    var showBubbleDialog by remember { mutableStateOf(false) }
    var editingPanel by remember { mutableStateOf<MangaPanel?>(null) }
    var exportingPanel by remember { mutableStateOf<Pair<MangaPanel, Int>?>(null) }
    var exportPreviewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isExportingToGallery by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val activeCharacter = characters.firstOrNull { it.id == editorState.selectedCharacterId }
    val activeBackground = backgrounds.firstOrNull { it.id == editorState.selectedBackgroundId }

    // Google Play compliant zero-permission Photo Picker for reference images
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val targetFile = File(context.filesDir, "ref_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                viewModel.setReferenceImagePath(targetFile.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(InkMidnight)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, InkBorder, RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_banner_art),
                    contentDescription = "Studio Manga Art Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay for typography
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.88f), Color.Black.copy(alpha = 0.45f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MangaCrimson,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "STUDIO IA",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedProject?.title ?: "Manga Studio AI",
                            color = MangaPaperWhite,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Création de planches avec cohérence de personnages & décors",
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Top Right Action buttons (Storyboard Grille & Reader)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Canvas Layout button
                    Surface(
                        color = Color(0xFF1E2433),
                        border = BorderStroke(1.dp, QiGold.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { onNavigateToStoryboard() }
                            .testTag("canvas_layout_quick_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = QiGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Layout & Bulles",
                                color = QiGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Storyboard Grid button
                    Surface(
                        color = Color(0xFF1E2433),
                        border = BorderStroke(1.dp, ManhuaCyan.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { onNavigateToStoryboard() }
                            .testTag("storyboard_quick_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = ManhuaCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Storyboard",
                                color = ManhuaCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Reader button
                    Surface(
                        color = MangaCrimson,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { onNavigateToReader() }
                            .testTag("reader_quick_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Lire BD",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Page Navigation Bar (Page 1/N, Nouvelle Page, Ajouter une Case)
        item {
            Surface(
                color = InkSurface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, InkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PLANCHE #${selectedPage?.pageNumber ?: 1}",
                            color = MangaCrimson,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${panels.size} case${if (panels.size > 1) "s" else ""})",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.addPanelToCurrentPage() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ManhuaCyan),
                            border = BorderStroke(1.dp, ManhuaCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("add_panel_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Case", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.addNewPage() },
                            colors = ButtonDefaults.buttonColors(containerColor = InkSurfaceVariant),
                            modifier = Modifier.testTag("add_page_btn")
                        ) {
                            Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Page", fontSize = 12.sp, color = MangaPaperWhite)
                        }
                    }
                }
            }
        }

        // Live Real-Time Page Composition Preview (displays full manga page while editing each panel)
        item {
            RealTimePageCompositionPreview(
                page = selectedPage,
                panels = panels,
                currentEditingIndex = editorState.activePanelIndex,
                isGenerating = editorState.isGenerating,
                characters = characters,
                backgrounds = backgrounds,
                onSelectPanelToEdit = { idx ->
                    viewModel.selectPanelForEditing(idx)
                }
            )
        }

        // Visual Consistency DNA Anchor Card
        item {
            ConsistencyDNASummaryCard(
                activeCharacter = activeCharacter,
                activeBackground = activeBackground,
                artStyle = editorState.selectedArtStyle,
                onSelectCharacterClick = { showCharacterDialog = true },
                onSelectBackgroundClick = { showBackgroundDialog = true }
            )
        }

        // Manga Panel Previews
        itemsIndexed(panels) { index, panel ->
            val charForPanel = characters.firstOrNull { it.id == panel.characterId }
            val bgForPanel = backgrounds.firstOrNull { it.id == panel.backgroundId }
            val isFirstAnchor = charForPanel != null && (
                (panel.imagePath != null && panel.imagePath == charForPanel.firstAppearanceImagePath) ||
                (panel.id != 0L && panel.id == charForPanel.firstAppearancePanelId)
            )

            MangaPanelCanvas(
                panel = panel,
                panelNumber = index + 1,
                characterName = charForPanel?.name,
                backgroundName = bgForPanel?.name,
                isGenerating = editorState.isGenerating,
                isFirstAppearanceAnchor = isFirstAnchor,
                onSetAsFirstAppearanceAnchor = if (charForPanel != null && panel.imagePath != null) {
                    { viewModel.lockFirstAppearanceAnchor(charForPanel.id, panel.imagePath, panel.id) }
                } else null,
                onRegenerate = { viewModel.generatePanel(index) },
                onEditBubble = {
                    editingPanel = panel
                    showBubbleDialog = true
                },
                onBubblePositionChanged = { newX, newY ->
                    viewModel.updateBubblePosition(panel, newX, newY)
                },
                onExport = {
                    val panelNumber = index + 1
                    exportingPanel = Pair(panel, panelNumber)
                    exportPreviewBitmap = null
                    coroutineScope.launch {
                        val bmp = MangaPanelExporter.renderHighResBitmap(
                            context = context,
                            panel = panel,
                            panelNumber = panelNumber
                        )
                        exportPreviewBitmap = bmp
                    }
                }
            )
        }

        // Prompt Builder Component (Art styles, character traits, scene settings)
        item {
            PromptBuilderCard(
                currentPrompt = editorState.activePrompt,
                selectedArtStyle = editorState.selectedArtStyle,
                characters = characters,
                backgrounds = backgrounds,
                selectedCharacterId = activeCharacter?.id,
                selectedBackgroundId = activeBackground?.id,
                selectedCamera = editorState.selectedCamera,
                selectedColorMode = editorState.selectedColorMode,
                selectedLineStyle = editorState.selectedLineStyle,
                onPromptConstructed = { constructedPrompt ->
                    viewModel.updatePrompt(constructedPrompt)
                },
                onArtStyleSelected = { style ->
                    viewModel.selectPresetArtStyle(style, false)
                },
                onCharacterSelected = { charId ->
                    viewModel.selectCharacter(charId)
                },
                onBackgroundSelected = { bgId ->
                    viewModel.selectBackground(bgId)
                },
                onCameraSelected = { cam ->
                    viewModel.updateCamera(cam)
                },
                onColorModeSelected = { colorMode ->
                    viewModel.updateColorMode(colorMode)
                },
                onLineStyleSelected = { lineStyle ->
                    viewModel.updateLineStyle(lineStyle)
                }
            )
        }

        // Panel Prompt & Generation Controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = InkSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, InkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // AI Generation Engine & Key Configuration Banner
                    Surface(
                        color = InkMidnight,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(activeProvider.badgeColor).copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAiSettingsDialog = true }
                            .testTag("open_ai_settings_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(activeProvider.badgeColor))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "MOTEUR IA : ",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = activeProvider.displayName,
                                        color = MangaPaperWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Modèle : ${activeAiConfig.selectedModel} • ${if (!activeProvider.requiresKey) "Mode Gratuit Actif ✓" else if (activeAiConfig.apiKey.isNotBlank()) "Clé configurée ✓" else "Clé API requise ⚠️"}",
                                    color = if (!activeProvider.requiresKey || activeAiConfig.apiKey.isNotBlank()) ManhuaCyan else QiGold,
                                    fontSize = 11.sp
                                )
                            }

                            Surface(
                                color = Color(activeProvider.badgeColor).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(activeProvider.badgeColor).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = MangaPaperWhite,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Changer d'IA",
                                        color = MangaPaperWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    if (activeProvider.requiresKey && activeAiConfig.apiKey.isBlank()) {
                        Surface(
                            color = QiGold.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, QiGold.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAiSettingsDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚠️ Clé API non renseignée pour ${activeProvider.shortName}. Touchez pour la saisir ou basculer en Mode Gratuit sans clé.",
                                    color = QiGold,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Configurer",
                                    color = MangaPaperWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "DESCRIPTION DE LA CASE #${editorState.activePanelIndex + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MangaCrimson,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        // AI Storyboard Assistant button
                        Surface(
                            color = QiGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, QiGold.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clickable { viewModel.enhancePromptWithAI() }
                                .testTag("ai_enhance_prompt_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = QiGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Storyboarder IA",
                                    color = QiGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editorState.activePrompt,
                        onValueChange = { viewModel.updatePrompt(it) },
                        placeholder = { Text("Ex: Ren dégaine son katana en sautant du toit, regard déterminé...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MangaCrimson,
                            unfocusedBorderColor = InkBorder,
                            focusedTextColor = MangaPaperWhite,
                            unfocusedTextColor = MangaPaperWhite,
                            cursorColor = MangaCrimson
                        ),
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prompt_input_field")
                    )

                    // Reference image preview / picker
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (editorState.referenceImagePath != null) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, ManhuaCyan, RoundedCornerShape(6.dp))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(File(editorState.referenceImagePath!!))
                                        .build(),
                                    contentDescription = "Image de référence",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Image de référence active",
                                    color = ManhuaCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Injectée dans Gemini pour la cohérence",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            IconButton(
                                onClick = { viewModel.setReferenceImagePath(null) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Supprimer", tint = TextSecondary)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MangaPaperWhite),
                                border = BorderStroke(1.dp, InkBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pick_reference_image_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = ManhuaCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ajouter une image de référence (visage/pose)")
                            }
                        }
                    }

                    // Predefined Artistic Styles & Adjustable Inking Controls (Shonen, Seinen, Shojo, N&B Classique)
                    MangaArtAndInkControlSection(
                        selectedStyle = editorState.selectedArtStyle,
                        strokeWeight = editorState.lineThickness,
                        contrastLevel = editorState.contrastLevel,
                        onStyleSelected = { style, applyDefaults ->
                            viewModel.selectPresetArtStyle(style, applyDefaults)
                        },
                        onStrokeWeightChange = { viewModel.updateLineThickness(it) },
                        onContrastChange = { viewModel.updateContrastLevel(it) }
                    )

                    // Color & Line Style
                    ColorAndLineStyleSelector(
                        selectedColor = editorState.selectedColorMode,
                        onColorSelected = { viewModel.updateColorMode(it) },
                        selectedLine = editorState.selectedLineStyle,
                        onLineSelected = { viewModel.updateLineStyle(it) }
                    )

                    // Camera Framing
                    CameraAngleSelector(
                        selectedCamera = editorState.selectedCamera,
                        onCameraSelected = { viewModel.updateCamera(it) }
                    )

                    // Dialogue bubble shortcut
                    Surface(
                        color = InkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, InkBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBubbleDialog = true }
                            .testTag("open_bubble_editor")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = MangaCrimson,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Bulle de dialogue ou Onomatopée",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = editorState.dialogueText.ifBlank { "Aucune bulle définie (Toucher pour ajouter)" },
                                    color = MangaPaperWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Environment description input & Gemini prompt helper
                    var environmentInput by remember { mutableStateOf("") }
                    var showCoherentPromptPreview by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = environmentInput,
                        onValueChange = { environmentInput = it },
                        placeholder = {
                            Text(
                                text = if (activeBackground != null) "Décor actif: ${activeBackground.name} (ajoutez des précisions d'environnement...)" else "Description de l'environnement (ex: Forêt brumeuse avec ruines anciennes sous la pluie...)",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        },
                        label = { Text("Environnement & Décor de la Scène", color = ManhuaCyan, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ManhuaCyan,
                            unfocusedBorderColor = InkBorder,
                            focusedTextColor = MangaPaperWhite,
                            unfocusedTextColor = MangaPaperWhite,
                            cursorColor = ManhuaCyan
                        ),
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scene_environment_input_field")
                    )

                    // Helper button: Inspect Coherent Scene Prompt for Gemini
                    OutlinedButton(
                        onClick = { showCoherentPromptPreview = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = QiGold),
                        border = BorderStroke(1.dp, QiGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preview_coherent_scene_prompt_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = QiGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Aperçu Prompt Scène Cohérente Gemini (Personnage + Décor)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (showCoherentPromptPreview) {
                        val coherentPrompt = remember(activeCharacter, environmentInput, editorState.activePrompt, editorState.selectedArtStyle, activeBackground) {
                            val envText = environmentInput.ifBlank {
                                activeBackground?.let { "${it.name}, ${it.architectureDetails}, ambiance ${it.lightingMood}" } ?: "Environnement manga détaillé"
                            }
                            viewModel.buildCoherentScenePrompt(
                                character = activeCharacter,
                                environmentDescription = envText,
                                userAction = editorState.activePrompt,
                                artStyle = editorState.selectedArtStyle,
                                colorMode = editorState.selectedColorMode,
                                lineStyle = editorState.selectedLineStyle,
                                camera = editorState.selectedCamera,
                                backgroundProfile = activeBackground
                            )
                        }
                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

                        AlertDialog(
                            onDismissRequest = { showCoherentPromptPreview = false },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = QiGold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Prompt Scène Cohérente Gemini", color = MangaPaperWhite, fontSize = 16.sp)
                                }
                            },
                            text = {
                                Column {
                                    Text(
                                        text = "Combinaison structurée du personnage sélectionné, de l'environnement fourni et des paramètres stylistiques pour l'API Gemini :",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Surface(
                                        color = InkMidnight,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, InkBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(230.dp)
                                    ) {
                                        LazyColumn(modifier = Modifier.padding(10.dp)) {
                                            item {
                                                Text(
                                                    text = coherentPrompt,
                                                    color = QiGold,
                                                    fontSize = 11.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(coherentPrompt))
                                        showCoherentPromptPreview = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson)
                                ) {
                                    Text("Copier le Prompt", color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showCoherentPromptPreview = false }) {
                                    Text("Fermer", color = TextSecondary)
                                }
                            },
                            containerColor = InkSurface,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Generate Button
                    Button(
                        onClick = { viewModel.generatePanel(editorState.activePanelIndex) },
                        enabled = !editorState.isGenerating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MangaCrimson,
                            disabledContainerColor = MangaCrimson.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_panel_action_btn")
                    ) {
                        if (editorState.isGenerating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = editorState.generationStage.ifBlank { "Génération en cours..." },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GÉNÉRER AVEC ${activeProvider.shortName.uppercase()} (#${editorState.activePanelIndex + 1})",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Status / Error message
                    if (editorState.statusMessage != null) {
                        Text(
                            text = "✓ ${editorState.statusMessage}",
                            color = Color(0xFF34D399),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (editorState.errorMessage != null) {
                        Surface(
                            color = Color(0xFF7F1D1D).copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "⚠ ${editorState.errorMessage}",
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { showAiSettingsDialog = true },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                                    ) {
                                        Text("Paramètres IA / Clés", fontSize = 11.sp)
                                    }
                                    if (activeProvider != AiProvider.POLLINATIONS_FREE) {
                                        Button(
                                            onClick = {
                                                viewModel.setActiveAiProvider(AiProvider.POLLINATIONS_FREE)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                        ) {
                                            Text("Passer au Mode Gratuit", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialogs
    if (showCharacterDialog) {
        CharacterSelectorDialog(
            characters = characters,
            selectedCharacterId = editorState.selectedCharacterId,
            onSelectCharacter = { viewModel.selectCharacter(it) },
            onOpenCharacterVault = onNavigateToCharacters,
            onDismiss = { showCharacterDialog = false }
        )
    }

    if (showBackgroundDialog) {
        BackgroundSelectorDialog(
            backgrounds = backgrounds,
            selectedBackgroundId = editorState.selectedBackgroundId,
            onSelectBackground = { viewModel.selectBackground(it) },
            onOpenBackgroundVault = onNavigateToBackgrounds,
            onDismiss = { showBackgroundDialog = false }
        )
    }

    if (showBubbleDialog) {
        val targetPanel = editingPanel
        DialogueBubbleEditorDialog(
            initialText = targetPanel?.dialogueText ?: editorState.dialogueText,
            initialType = targetPanel?.bubbleType ?: editorState.bubbleType,
            initialFont = targetPanel?.bubbleFont ?: editorState.bubbleFont,
            initialFontSize = targetPanel?.bubbleFontSize ?: editorState.bubbleFontSize,
            initialTailDirection = targetPanel?.bubbleTailDirection ?: editorState.bubbleTailDirection,
            initialBgColor = targetPanel?.bubbleBgColor ?: editorState.bubbleBgColor,
            initialTextColor = targetPanel?.bubbleTextColor ?: editorState.bubbleTextColor,
            initialBorderColor = targetPanel?.bubbleBorderColor ?: editorState.bubbleBorderColor,
            onSave = { text, type, font, fontSize, tailDir, bgCol, textCol, borderCol ->
                if (targetPanel != null) {
                    viewModel.updatePanelSpeechBubble(
                        panel = targetPanel,
                        text = text,
                        type = type,
                        font = font,
                        fontSize = fontSize,
                        tailDirection = tailDir,
                        bgColor = bgCol,
                        textColor = textCol,
                        borderColor = borderCol
                    )
                }
                viewModel.updateDialogue(
                    text = text,
                    bubbleType = type,
                    font = font,
                    fontSize = fontSize,
                    tailDirection = tailDir,
                    bgColor = bgCol,
                    textColor = textCol,
                    borderColor = borderCol
                )
            },
            onDismiss = {
                showBubbleDialog = false
                editingPanel = null
            }
        )
    }

    if (exportingPanel != null) {
        val (expPanel, expPanelNumber) = exportingPanel!!
        ExportPanelDialog(
            panelNumber = expPanelNumber,
            previewBitmap = exportPreviewBitmap,
            isExporting = isExportingToGallery,
            onSaveToGallery = {
                val bmp = exportPreviewBitmap
                if (bmp != null) {
                    isExportingToGallery = true
                    coroutineScope.launch {
                        val result = MangaPanelExporter.saveToGallery(
                            context = context,
                            bitmap = bmp,
                            filenamePrefix = "manga_case_${expPanelNumber}"
                        )
                        isExportingToGallery = false
                        if (result.isSuccess) {
                            Toast.makeText(context, "Case #$expPanelNumber enregistrée dans Pictures/MangaStudio !", Toast.LENGTH_LONG).show()
                            exportingPanel = null
                        } else {
                            Toast.makeText(context, "Erreur d'enregistrement : ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onShare = {
                val bmp = exportPreviewBitmap
                if (bmp != null) {
                    coroutineScope.launch {
                        val result = MangaPanelExporter.createShareIntent(
                            context = context,
                            bitmap = bmp,
                            panelNumber = expPanelNumber
                        )
                        if (result.isSuccess) {
                            val chooser = result.getOrNull()
                            if (chooser != null) {
                                context.startActivity(chooser)
                            }
                        } else {
                            Toast.makeText(context, "Erreur de partage : ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onDismiss = {
                exportingPanel = null
                exportPreviewBitmap = null
            }
        )
    }

    if (showAiSettingsDialog) {
        AiProviderSettingsDialog(
            preferencesManager = viewModel.aiPreferencesManager,
            multiAiService = viewModel.multiAiService,
            activeProvider = activeProvider,
            onProviderChanged = { newProvider ->
                viewModel.setActiveAiProvider(newProvider)
            },
            onDismiss = { showAiSettingsDialog = false }
        )
    }
}
