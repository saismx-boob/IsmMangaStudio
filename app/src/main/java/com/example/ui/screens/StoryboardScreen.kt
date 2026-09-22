package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.ai.CameraPerspective
import com.example.ai.MangaArtStyle
import com.example.data.model.BackgroundProfile
import com.example.data.model.CharacterProfile
import com.example.data.model.GridPageLayout
import com.example.data.model.MangaPage
import com.example.data.model.MangaPanel
import com.example.ui.MangaStudioViewModel
import com.example.ui.theme.InkBorder
import com.example.ui.theme.InkMidnight
import com.example.ui.theme.InkSurface
import com.example.ui.theme.InkSurfaceVariant
import com.example.ui.theme.MangaCrimson
import com.example.ui.theme.MangaPaperWhite
import com.example.ui.theme.MangaScreentoneGray
import com.example.ui.theme.ManhuaCyan
import com.example.ui.theme.QiGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File
import kotlin.math.roundToInt

/**
 * Types of draggable assets that can be dropped into manga grid slots.
 */
sealed class StoryboardAsset {
    data class CharacterAsset(val character: CharacterProfile) : StoryboardAsset()
    data class PoseAsset(val pose: PosePreset) : StoryboardAsset()
    data class BackgroundAsset(val background: BackgroundProfile) : StoryboardAsset()
}

/**
 * Predefined dynamic reference poses for manga characters.
 */
data class PosePreset(
    val id: String,
    val title: String,
    val category: String, // Combat, Dialogue, Émotion, Tranche de vie
    val iconEmoji: String,
    val camera: CameraPerspective,
    val actionPrompt: String,
    val description: String
)

val POSE_PRESETS = listOf(
    PosePreset(
        id = "pose_strike",
        title = "Frappe d'énergie",
        category = "Combat",
        iconEmoji = "⚡",
        camera = CameraPerspective.DUTCH_ANGLE,
        actionPrompt = "exécutant une frappe de paume foudroyante en avant, aura d'énergie tourbillonnante, pose de combat dynamique",
        description = "Attaque martiale avec projection d'aura et lignes de vitesse."
    ),
    PosePreset(
        id = "pose_sword",
        title = "Dégainement d'épée",
        category = "Combat",
        iconEmoji = "⚔️",
        camera = CameraPerspective.DUTCH_ANGLE,
        actionPrompt = "dégainant une lame brillante d'un geste fluide, étincelles tranchantes, regard déterminé",
        description = "Pose iconique de bretteur avec contre-plongée dramatique."
    ),
    PosePreset(
        id = "pose_confrontation",
        title = "Confrontation intense",
        category = "Dialogue",
        iconEmoji = "👁️",
        camera = CameraPerspective.CLOSE_UP,
        actionPrompt = "regard perçant en gros plan, ombre marquée sous les yeux, expression de détermination glaciale",
        description = "Gros plan dramatique focalisé sur le regard et l'intensité psychologique."
    ),
    PosePreset(
        id = "pose_flight",
        title = "Bond aérien",
        category = "Combat",
        iconEmoji = "🌪️",
        camera = CameraPerspective.WIDE_ESTABLISHING,
        actionPrompt = "en plein saut aérien au-dessus du sol, vêtements flottant au vent, prêt à s'abattre sur l'adversaire",
        description = "Saut spectaculaire avec vue panoramique et sensation de gravité zéro."
    ),
    PosePreset(
        id = "pose_thinking",
        title = "Réflexion tactique",
        category = "Dialogue",
        iconEmoji = "🤔",
        camera = CameraPerspective.MEDIUM_SHOT,
        actionPrompt = "main sous le menton, analysant la situation avec concentration, pose calme et posée",
        description = "Plan moyen décontracté parfait pour l'élaboration de stratégies."
    ),
    PosePreset(
        id = "pose_shock",
        title = "Stupeur & Révélation",
        category = "Émotion",
        iconEmoji = "💥",
        camera = CameraPerspective.CLOSE_UP,
        actionPrompt = "yeux écarquillés par le choc, reculant d'un pas face à une vérité inattendue, effet de vitesse radial",
        description = "Réaction émotionnelle forte avec lignes d'impact centrées."
    ),
    PosePreset(
        id = "pose_victory",
        title = "Pose victorieuse",
        category = "Combat",
        iconEmoji = "🏆",
        camera = CameraPerspective.LOW_ANGLE,
        actionPrompt = "se redressant fièrement, cheveux flottant, cape au vent, silhouette héroïque imposante",
        description = "Contre-plongée héroïque magnifiant le personnage triomphant."
    ),
    PosePreset(
        id = "pose_walk",
        title = "Marche résolue",
        category = "Tranche de vie",
        iconEmoji = "🚶",
        camera = CameraPerspective.MEDIUM_SHOT,
        actionPrompt = "avançant à grands pas résolus, dos à la caméra puis tournant la tête, allure assurée",
        description = "Plan pied montrant la démarche et l'élégance de la tenue."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryboardScreen(
    viewModel: MangaStudioViewModel,
    onNavigateBack: () -> Unit,
    onGenerateFromStoryboard: (panelIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val editorState by viewModel.editorState.collectAsState()
    val pages by viewModel.currentPages.collectAsState()
    val selectedPage by viewModel.selectedPage.collectAsState()
    val panels by viewModel.currentPanels.collectAsState()
    val characters by viewModel.allCharacters.collectAsState()
    val backgrounds by viewModel.allBackgrounds.collectAsState()

    // Active selected asset tab in drawer
    var activeAssetTab by remember { mutableIntStateOf(0) } // 0: Personnages, 1: Poses, 2: Décors
    var selectedPoseCategory by remember { mutableStateOf("Tous") }

    // Tap-to-assign fallback state when not dragging
    var selectedPaletteAsset by remember { mutableStateOf<StoryboardAsset?>(null) }

    // Drag-and-drop global state
    var isDragging by remember { mutableStateOf(false) }
    var draggedAsset by remember { mutableStateOf<StoryboardAsset?>(null) }
    var dragGlobalPosition by remember { mutableStateOf(Offset.Zero) }
    var activeHoverSlotIndex by remember { mutableStateOf<Int?>(null) }

    // Slot bounds registry (panelIndex -> Rect in root coordinates)
    val slotBounds = remember { mutableStateMapOf<Int, Rect>() }

    // Dynamic panel split dialog state
    var showSplitDialog by remember { mutableStateOf(false) }
    var panelToSplit by remember { mutableStateOf<MangaPanel?>(null) }

    val currentLayout = GridPageLayout.values().firstOrNull { it.id == (selectedPage?.layoutType ?: "TWO_PANELS_VERTICAL") }
        ?: GridPageLayout.TWO_VERTICAL

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(InkMidnight)
            .testTag("storyboard_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header Bar
            StoryboardTopBar(
                pageNumber = selectedPage?.pageNumber ?: 1,
                layout = currentLayout,
                hasPanels = panels.isNotEmpty(),
                onSelectLayout = { newLayout ->
                    viewModel.applyLayoutToCurrentPage(newLayout.id, newLayout.panelCount)
                },
                onNavigateBack = onNavigateBack,
                onAddPanel = { viewModel.addPanelToCurrentPage() },
                onAddPage = { viewModel.addNewPage() },
                onSplitPanel = {
                    panelToSplit = panels.firstOrNull()
                    showSplitDialog = true
                }
            )

            // Status notice or feedback
            editorState.statusMessage?.let { msg ->
                Surface(
                    color = ManhuaCyan.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ManhuaCyan.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ManhuaCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = msg, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearStatusMessage() }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Quick instruction banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .background(InkSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, InkBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DragIndicator, contentDescription = null, tint = ManhuaCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Glissez-déposez ou sélectionnez un héros / pose ci-dessous, puis touchez une case pour pré-visualiser la mise en page !",
                    color = MangaScreentoneGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                if (selectedPaletteAsset != null) {
                    Surface(
                        color = MangaCrimson.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, MangaCrimson),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Actif", color = MangaCrimson, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Désélectionner",
                                tint = MangaCrimson,
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable { selectedPaletteAsset = null }
                            )
                        }
                    }
                }
            }

            // Blank Page Grid (Main Workspace)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                MangaPageGridSheet(
                    layout = currentLayout,
                    panels = panels,
                    characters = characters,
                    backgrounds = backgrounds,
                    activeHoverIndex = activeHoverSlotIndex,
                    selectedPaletteAsset = selectedPaletteAsset,
                    onSlotGloballyPositioned = { index, rect ->
                        slotBounds[index] = rect
                    },
                    onSlotClicked = { slotPanel ->
                        val asset = selectedPaletteAsset
                        if (asset != null) {
                            when (asset) {
                                is StoryboardAsset.CharacterAsset -> {
                                    viewModel.applyCharacterAndPoseToPanel(
                                        panel = slotPanel,
                                        character = asset.character,
                                        poseAction = null
                                    )
                                }
                                is StoryboardAsset.PoseAsset -> {
                                    val currentChar = characters.firstOrNull { it.id == slotPanel.characterId }
                                    viewModel.applyCharacterAndPoseToPanel(
                                        panel = slotPanel,
                                        character = currentChar,
                                        poseAction = asset.pose.actionPrompt,
                                        camera = asset.pose.camera
                                    )
                                }
                                is StoryboardAsset.BackgroundAsset -> {
                                    viewModel.applyCharacterAndPoseToPanel(
                                        panel = slotPanel,
                                        character = characters.firstOrNull { it.id == slotPanel.characterId },
                                        poseAction = null,
                                        background = asset.background
                                    )
                                }
                            }
                            selectedPaletteAsset = null
                        }
                    },
                    onClearPanel = { panel ->
                        viewModel.clearPanelImage(panel)
                    },
                    onGeneratePanel = { panelIndex ->
                        onGenerateFromStoryboard(panelIndex)
                    },
                    onSplitPanel = { panel ->
                        panelToSplit = panel
                        showSplitDialog = true
                    }
                )
            }

            // Bottom Palette Drawer: Characters & Dynamic Reference Poses
            StoryboardAssetPalette(
                activeTab = activeAssetTab,
                onTabSelected = { activeAssetTab = it },
                characters = characters,
                poses = POSE_PRESETS,
                backgrounds = backgrounds,
                selectedCategory = selectedPoseCategory,
                onCategorySelected = { selectedPoseCategory = it },
                selectedAsset = selectedPaletteAsset,
                onAssetClicked = { asset ->
                    selectedPaletteAsset = if (selectedPaletteAsset == asset) null else asset
                },
                onDragStart = { asset, startOffset ->
                    isDragging = true
                    draggedAsset = asset
                    dragGlobalPosition = startOffset
                },
                onDrag = { dragDelta ->
                    dragGlobalPosition += dragDelta
                    // Check if hovering over any panel slot
                    val hoverIndex = slotBounds.entries.firstOrNull { (_, rect) ->
                        rect.contains(dragGlobalPosition)
                    }?.key
                    activeHoverSlotIndex = hoverIndex
                },
                onDragEnd = {
                    val dropTargetIndex = activeHoverSlotIndex
                    val asset = draggedAsset
                    if (dropTargetIndex != null && asset != null) {
                        val targetPanel = panels.getOrNull(dropTargetIndex)
                        if (targetPanel != null) {
                            when (asset) {
                                is StoryboardAsset.CharacterAsset -> {
                                    viewModel.applyCharacterAndPoseToPanel(
                                        panel = targetPanel,
                                        character = asset.character,
                                        poseAction = null
                                    )
                                }
                                is StoryboardAsset.PoseAsset -> {
                                    val currentChar = characters.firstOrNull { it.id == targetPanel.characterId }
                                    viewModel.applyCharacterAndPoseToPanel(
                                        panel = targetPanel,
                                        character = currentChar,
                                        poseAction = asset.pose.actionPrompt,
                                        camera = asset.pose.camera
                                    )
                                }
                                is StoryboardAsset.BackgroundAsset -> {
                                    viewModel.applyCharacterAndPoseToPanel(
                                        panel = targetPanel,
                                        character = characters.firstOrNull { it.id == targetPanel.characterId },
                                        poseAction = null,
                                        background = asset.background
                                    )
                                }
                            }
                        }
                    }
                    isDragging = false
                    draggedAsset = null
                    activeHoverSlotIndex = null
                }
            )
        }

        // Floating Drag Overlay Representation
        if (isDragging && draggedAsset != null) {
            DraggingAssetFloatingPreview(
                asset = draggedAsset!!,
                position = dragGlobalPosition
            )
        }

        // Sequential Sub-Panel Split Dialog
        if (showSplitDialog && panels.isNotEmpty()) {
            SplitPanelDialog(
                panels = panels,
                initialPanel = panelToSplit,
                onDismiss = { showSplitDialog = false },
                onConfirmSplit = { target, count, customPrompt ->
                    val updatedTarget = if (customPrompt.isNotBlank()) target.copy(userPrompt = customPrompt) else target
                    viewModel.splitPanelIntoSubPanels(updatedTarget, count)
                    showSplitDialog = false
                }
            )
        }
    }
}

/**
 * Top bar for the Storyboard interface with layout switchers and quick actions.
 */
@Composable
fun StoryboardTopBar(
    pageNumber: Int,
    layout: GridPageLayout,
    hasPanels: Boolean,
    onSelectLayout: (GridPageLayout) -> Unit,
    onNavigateBack: () -> Unit,
    onAddPanel: () -> Unit,
    onAddPage: () -> Unit,
    onSplitPanel: () -> Unit
) {
    Surface(
        color = InkSurface,
        border = BorderStroke(1.dp, InkBorder),
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("storyboard_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour Studio",
                            tint = MangaPaperWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = ManhuaCyan,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "STORYBOARD",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Page $pageNumber",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            text = "Grille de mise en page vierge & composition",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (hasPanels) {
                        OutlinedButton(
                            onClick = onSplitPanel,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = QiGold),
                            border = BorderStroke(1.dp, QiGold.copy(alpha = 0.7f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("topbar_split_panel_btn")
                        ) {
                            Icon(
                                Icons.Default.Splitscreen,
                                contentDescription = "Diviser la case en sous-cases",
                                modifier = Modifier.size(12.dp),
                                tint = QiGold
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Diviser", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = QiGold)
                        }
                    }
                    OutlinedButton(
                        onClick = onAddPanel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ManhuaCyan),
                        border = BorderStroke(1.dp, ManhuaCyan.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+ Case", fontSize = 11.sp)
                    }
                    Button(
                        onClick = onAddPage,
                        colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+ Page", fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Layout Preset Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gabarit :",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                GridPageLayout.values().forEach { preset ->
                    val isSelected = preset == layout
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectLayout(preset) },
                        label = {
                            Text(
                                text = preset.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = ManhuaCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ManhuaCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ManhuaCyan,
                            containerColor = InkMidnight,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) ManhuaCyan else InkBorder,
                            enabled = true,
                            selected = isSelected
                        ),
                        modifier = Modifier.testTag("layout_chip_${preset.name}")
                    )
                }
            }
        }
    }
}

/**
 * The blank manga paper page grid simulating authentic manga paper margins and slots.
 */
@Composable
fun MangaPageGridSheet(
    layout: GridPageLayout,
    panels: List<MangaPanel>,
    characters: List<CharacterProfile>,
    backgrounds: List<BackgroundProfile>,
    activeHoverIndex: Int?,
    selectedPaletteAsset: StoryboardAsset?,
    onSlotGloballyPositioned: (Int, Rect) -> Unit,
    onSlotClicked: (MangaPanel) -> Unit,
    onClearPanel: (MangaPanel) -> Unit,
    onGeneratePanel: (Int) -> Unit,
    onSplitPanel: (MangaPanel) -> Unit
) {
    // Authentic Manga Paper Sheet representation (ratio ~ 1 : 1.414 / B4 standard)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F121A)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.5.dp, InkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("manga_page_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Page Header with bleed indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MANGA PROOF / B4 BLEED SAFE",
                    color = TextMuted.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${panels.size} CASE(S) ACTIVÉE(S)",
                    color = ManhuaCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Grid Content based on layout
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF07090E), RoundedCornerShape(6.dp))
                    .border(BorderStroke(1.dp, Color(0xFF1F2433)), RoundedCornerShape(6.dp))
                    .padding(6.dp)
            ) {
                if (panels.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucune case définie pour cette page.\nAppuyez sur '+ Case' pour commencer !",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    when (layout) {
                        GridPageLayout.SPLASH_FULL -> {
                            // 1 single full-height splash panel
                            val panel = panels.first()
                            MangaGridSlot(
                                panel = panel,
                                slotIndex = 0,
                                isHovered = activeHoverIndex == 0,
                                isSelectedAssetPending = selectedPaletteAsset != null,
                                character = characters.firstOrNull { it.id == panel.characterId },
                                background = backgrounds.firstOrNull { it.id == panel.backgroundId },
                                onSlotGloballyPositioned = onSlotGloballyPositioned,
                                onClick = { onSlotClicked(panel) },
                                onClear = { onClearPanel(panel) },
                                onGenerate = { onGeneratePanel(0) },
                                onSplit = { onSplitPanel(panel) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        GridPageLayout.TWO_VERTICAL -> {
                            // 2 stacked vertical panels
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (i in 0..1) {
                                    val panel = panels.getOrNull(i)
                                    if (panel != null) {
                                        MangaGridSlot(
                                            panel = panel,
                                            slotIndex = i,
                                            isHovered = activeHoverIndex == i,
                                            isSelectedAssetPending = selectedPaletteAsset != null,
                                            character = characters.firstOrNull { it.id == panel.characterId },
                                            background = backgrounds.firstOrNull { it.id == panel.backgroundId },
                                            onSlotGloballyPositioned = onSlotGloballyPositioned,
                                            onClick = { onSlotClicked(panel) },
                                            onClear = { onClearPanel(panel) },
                                            onGenerate = { onGeneratePanel(i) },
                                            onSplit = { onSplitPanel(panel) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                        )
                                    } else {
                                        EmptyGridSlotPlaceholder(
                                            slotIndex = i,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        GridPageLayout.THREE_MANGA -> {
                            // 3 panels: 1 top large panel, 2 bottom side-by-side panels
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Top Hero Panel
                                val topPanel = panels.getOrNull(0)
                                if (topPanel != null) {
                                    MangaGridSlot(
                                        panel = topPanel,
                                        slotIndex = 0,
                                        isHovered = activeHoverIndex == 0,
                                        isSelectedAssetPending = selectedPaletteAsset != null,
                                        character = characters.firstOrNull { it.id == topPanel.characterId },
                                        background = backgrounds.firstOrNull { it.id == topPanel.backgroundId },
                                        onSlotGloballyPositioned = onSlotGloballyPositioned,
                                        onClick = { onSlotClicked(topPanel) },
                                        onClear = { onClearPanel(topPanel) },
                                        onGenerate = { onGeneratePanel(0) },
                                        onSplit = { onSplitPanel(topPanel) },
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .fillMaxWidth()
                                    )
                                }

                                // Bottom 2 panels side-by-side
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (i in 1..2) {
                                        val subPanel = panels.getOrNull(i)
                                        if (subPanel != null) {
                                            MangaGridSlot(
                                                panel = subPanel,
                                                slotIndex = i,
                                                isHovered = activeHoverIndex == i,
                                                isSelectedAssetPending = selectedPaletteAsset != null,
                                                character = characters.firstOrNull { it.id == subPanel.characterId },
                                                background = backgrounds.firstOrNull { it.id == subPanel.backgroundId },
                                                onSlotGloballyPositioned = onSlotGloballyPositioned,
                                                onClick = { onSlotClicked(subPanel) },
                                                onClear = { onClearPanel(subPanel) },
                                                onGenerate = { onGeneratePanel(i) },
                                                onSplit = { onSplitPanel(subPanel) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                            )
                                        } else {
                                            EmptyGridSlotPlaceholder(
                                                slotIndex = i,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        GridPageLayout.FOUR_YONKOMA -> {
                            // Stacked panels vertically (with scrolling support if more than 4 panels)
                            val totalSlots = maxOf(4, panels.size)
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(if (totalSlots > 4) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (i in 0 until totalSlots) {
                                    val panel = panels.getOrNull(i)
                                    if (panel != null) {
                                        MangaGridSlot(
                                            panel = panel,
                                            slotIndex = i,
                                            isHovered = activeHoverIndex == i,
                                            isSelectedAssetPending = selectedPaletteAsset != null,
                                            character = characters.firstOrNull { it.id == panel.characterId },
                                            background = backgrounds.firstOrNull { it.id == panel.backgroundId },
                                            onSlotGloballyPositioned = onSlotGloballyPositioned,
                                            onClick = { onSlotClicked(panel) },
                                            onClear = { onClearPanel(panel) },
                                            onGenerate = { onGeneratePanel(i) },
                                            onSplit = { onSplitPanel(panel) },
                                            modifier = if (totalSlots > 4) Modifier.height(140.dp).fillMaxWidth() else Modifier.weight(1f).fillMaxWidth()
                                        )
                                    } else {
                                        EmptyGridSlotPlaceholder(
                                            slotIndex = i,
                                            modifier = if (totalSlots > 4) Modifier.height(140.dp).fillMaxWidth() else Modifier.weight(1f).fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * An individual slot on the manga page grid.
 */
@Composable
fun MangaGridSlot(
    panel: MangaPanel,
    slotIndex: Int,
    isHovered: Boolean,
    isSelectedAssetPending: Boolean,
    character: CharacterProfile?,
    background: BackgroundProfile?,
    onSlotGloballyPositioned: (Int, Rect) -> Unit,
    onClick: () -> Unit,
    onClear: () -> Unit,
    onGenerate: () -> Unit,
    onSplit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isHovered -> QiGold
        isSelectedAssetPending -> ManhuaCyan.copy(alpha = 0.8f)
        character != null -> MangaCrimson.copy(alpha = 0.8f)
        else -> InkBorder
    }

    val borderWidth = if (isHovered || isSelectedAssetPending) 2.dp else 1.dp

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                onSlotGloballyPositioned(slotIndex, coordinates.boundsInRoot())
            }
            .clip(RoundedCornerShape(6.dp))
            .background(if (isHovered) QiGold.copy(alpha = 0.12f) else Color(0xFF131722))
            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .testTag("manga_slot_$slotIndex")
    ) {
        // Background Artwork preview if generated
        if (panel.imagePath != null && File(panel.imagePath).exists()) {
            AsyncImage(
                model = File(panel.imagePath),
                contentDescription = "Rendu Case ${slotIndex + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Subtle dark overlay to keep storyboard labels legible
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
        }

        // Slot Content Blueprint Information
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Slot badge & character badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Panel number indicator
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, InkBorder)
                ) {
                    Text(
                        text = "#${slotIndex + 1}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Drop target feedback badge
                if (isHovered) {
                    Surface(
                        color = QiGold,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "DÉPOSER ICI !",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (character != null) {
                    Surface(
                        color = MangaCrimson.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = character.name,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Center Blueprint representation (Character avatar / pose cue / prompt)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (character != null) {
                    // Character reference badge & DNA
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val refFile = character.firstAppearanceImagePath?.let { File(it) }
                        if (refFile != null && refFile.exists()) {
                            AsyncImage(
                                model = refFile,
                                contentDescription = character.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, ManhuaCyan, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Column {
                            Text(
                                text = character.visualUid,
                                color = ManhuaCyan,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = character.clothingDescription,
                                color = MangaPaperWhite,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Action / Pose prompt
                if (panel.userPrompt.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "« ${panel.userPrompt} »",
                        color = if (character != null) QiGold else MangaScreentoneGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Glissez un personnage ou une pose ici",
                        color = TextMuted,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }

                if (background != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Décor : ${background.name}",
                        color = ManhuaCyan,
                        fontSize = 9.sp
                    )
                }
            }

            // Bottom action row: Quick trigger to generate AI panel or split wide panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (panel.imagePath != null) {
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Effacer l'image pour revoir le storyboard",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Button to split wide panel into sequential sub-panels
                    Surface(
                        color = InkSurfaceVariant.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.8.dp, QiGold.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .clickable { onSplit() }
                            .testTag("slot_split_btn_$slotIndex")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Splitscreen,
                                contentDescription = "Diviser en sous-cases",
                                tint = QiGold,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Diviser",
                                color = QiGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Surface(
                    color = MangaCrimson,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .clickable { onGenerate() }
                        .testTag("slot_generate_btn_$slotIndex")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (panel.imagePath != null) "Régénérer" else "Générer",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Placeholder for empty grid slot.
 */
@Composable
fun EmptyGridSlotPlaceholder(slotIndex: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0E111A))
            .border(BorderStroke(1.dp, InkBorder.copy(alpha = 0.4f)), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Case #${slotIndex + 1} (Non allouée)",
            color = TextMuted.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
    }
}

/**
 * Dialog for automatically splitting a panel into dynamic sequential sub-panels.
 * Allows choosing target panel, division count (2, 3, 4), and sequential narrative direction.
 */
@Composable
fun SplitPanelDialog(
    panels: List<MangaPanel>,
    initialPanel: MangaPanel?,
    onDismiss: () -> Unit,
    onConfirmSplit: (target: MangaPanel, count: Int, customPrompt: String) -> Unit
) {
    var selectedPanel by remember(initialPanel) {
        mutableStateOf(initialPanel ?: panels.firstOrNull())
    }
    var subCount by remember { mutableIntStateOf(2) }
    var actionPrompt by remember(selectedPanel) {
        mutableStateOf(selectedPanel?.userPrompt ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = InkSurface,
        titleContentColor = MangaPaperWhite,
        textContentColor = TextSecondary,
        icon = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(QiGold.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, QiGold.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Splitscreen,
                    contentDescription = null,
                    tint = QiGold,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = "Division Séquentielle de Case",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MangaPaperWhite
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Divisez automatiquement une case large en sous-cases dynamiques pour dynamiser le rythme de lecture et la narration séquentielle.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                // Panel selector if multiple panels exist
                if (panels.size > 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Case cible à diviser :",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ManhuaCyan
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            panels.forEach { p ->
                                val isCurSelected = selectedPanel?.id == p.id
                                FilterChip(
                                    selected = isCurSelected,
                                    onClick = {
                                        selectedPanel = p
                                        actionPrompt = p.userPrompt
                                    },
                                    label = {
                                        Text(
                                            text = "Case #${p.panelIndex + 1}",
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = QiGold.copy(alpha = 0.2f),
                                        selectedLabelColor = QiGold,
                                        containerColor = InkMidnight,
                                        labelColor = TextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (isCurSelected) QiGold else InkBorder,
                                        enabled = true,
                                        selected = isCurSelected
                                    ),
                                    modifier = Modifier.testTag("split_panel_chip_${p.panelIndex}")
                                )
                            }
                        }
                    }
                }

                // Number of sub-panels
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Nombre de sous-cases :",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ManhuaCyan
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2 to "2 sous-cases (Duo)", 3 to "3 sous-cases (Trio)", 4 to "4 sous-cases (Action)").forEach { (count, label) ->
                            val isSel = subCount == count
                            Surface(
                                color = if (isSel) QiGold.copy(alpha = 0.18f) else InkMidnight,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isSel) QiGold else InkBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { subCount = count }
                                    .testTag("split_count_${count}_btn")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$count",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = if (isSel) QiGold else MangaPaperWhite
                                    )
                                    Text(
                                        text = label.substringBefore(" ("),
                                        fontSize = 9.sp,
                                        color = if (isSel) QiGold else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                // Sequential Action Context prompt
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Description / Intention séquentielle (optionnel) :",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    OutlinedTextField(
                        value = actionPrompt,
                        onValueChange = { actionPrompt = it },
                        placeholder = {
                            Text(
                                "ex: Coup d'œil rapide, puis choc en gros plan...",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 12.sp,
                            color = MangaPaperWhite
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = QiGold,
                            unfocusedBorderColor = InkBorder,
                            focusedContainerColor = InkMidnight,
                            unfocusedContainerColor = InkMidnight,
                            cursorColor = QiGold
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("split_prompt_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = selectedPanel ?: panels.firstOrNull()
                    if (target != null) {
                        onConfirmSplit(target, subCount, actionPrompt)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = QiGold),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("split_confirm_button")
            ) {
                Icon(
                    Icons.Default.Splitscreen,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Diviser en $subCount",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = BorderStroke(1.dp, InkBorder)
            ) {
                Text(text = "Annuler", fontSize = 12.sp)
            }
        }
    )
}

/**
 * Bottom asset palette containing Character profiles, Reference Poses, and Backgrounds.
 */
@Composable
fun StoryboardAssetPalette(
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    characters: List<CharacterProfile>,
    poses: List<PosePreset>,
    backgrounds: List<BackgroundProfile>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    selectedAsset: StoryboardAsset?,
    onAssetClicked: (StoryboardAsset) -> Unit,
    onDragStart: (StoryboardAsset, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    Surface(
        color = InkSurface,
        border = BorderStroke(1.dp, InkBorder),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("storyboard_palette")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            // Tabs: Personnages | Poses Référence | Décors
            val tabs = listOf("Fiches Héros (${characters.size})", "Poses Référence (${poses.size})", "Décors (${backgrounds.size})")
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = InkSurface,
                contentColor = MangaPaperWhite,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = ManhuaCyan,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { onTabSelected(index) },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeTab == index) ManhuaCyan else TextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (activeTab) {
                0 -> {
                    // Character Profiles horizontal list
                    if (characters.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucune fiche personnage enregistrée.", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.height(100.dp)
                        ) {
                            items(characters) { char ->
                                val asset = StoryboardAsset.CharacterAsset(char)
                                val isSelected = selectedAsset == asset
                                DraggableCharacterCard(
                                    character = char,
                                    isSelected = isSelected,
                                    onClick = { onAssetClicked(asset) },
                                    onDragStart = { offset -> onDragStart(asset, offset) },
                                    onDrag = onDrag,
                                    onDragEnd = onDragEnd
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // Pose presets categorized horizontal row
                    val categories = listOf("Tous", "Combat", "Dialogue", "Émotion", "Tranche de vie")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 2.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isCatSelected = selectedCategory == cat
                            Surface(
                                color = if (isCatSelected) QiGold.copy(alpha = 0.2f) else InkMidnight,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isCatSelected) QiGold else InkBorder),
                                modifier = Modifier.clickable { onCategorySelected(cat) }
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isCatSelected) QiGold else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val filteredPoses = if (selectedCategory == "Tous") poses else poses.filter { it.category == selectedCategory }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(95.dp)
                    ) {
                        items(filteredPoses) { pose ->
                            val asset = StoryboardAsset.PoseAsset(pose)
                            val isSelected = selectedAsset == asset
                            DraggablePoseCard(
                                pose = pose,
                                isSelected = isSelected,
                                onClick = { onAssetClicked(asset) },
                                onDragStart = { offset -> onDragStart(asset, offset) },
                                onDrag = onDrag,
                                onDragEnd = onDragEnd
                            )
                        }
                    }
                }

                2 -> {
                    // Backgrounds horizontal list
                    if (backgrounds.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucun décor enregistré dans la base.", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.height(100.dp)
                        ) {
                            items(backgrounds) { bg ->
                                val asset = StoryboardAsset.BackgroundAsset(bg)
                                val isSelected = selectedAsset == asset
                                DraggableBackgroundCard(
                                    background = bg,
                                    isSelected = isSelected,
                                    onClick = { onAssetClicked(asset) },
                                    onDragStart = { offset -> onDragStart(asset, offset) },
                                    onDrag = onDrag,
                                    onDragEnd = onDragEnd
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Character card supporting drag gesture and click selection.
 */
@Composable
fun DraggableCharacterCard(
    character: CharacterProfile,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    var globalPos by remember { mutableStateOf(Offset.Zero) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MangaCrimson.copy(alpha = 0.2f) else InkSurfaceVariant
        ),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) MangaCrimson else InkBorder
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(135.dp)
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                globalPos = coordinates.boundsInRoot().center
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        onDragStart(globalPos)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd
                )
            }
            .clickable { onClick() }
            .testTag("character_asset_${character.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val refFile = character.firstAppearanceImagePath?.let { File(it) }
                if (refFile != null && refFile.exists()) {
                    AsyncImage(
                        model = refFile,
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(1.dp, ManhuaCyan, CircleShape)
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MangaCrimson,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(character.name.take(1), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = character.name,
                        color = MangaPaperWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = character.visualUid,
                        color = ManhuaCyan,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = "${character.hairStyleColor}, ${character.clothingDescription}",
                color = TextSecondary,
                fontSize = 9.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 11.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Glisser ➔",
                    color = QiGold,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Interactive Reference Pose card supporting drag gesture and click selection.
 */
@Composable
fun DraggablePoseCard(
    pose: PosePreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    var globalPos by remember { mutableStateOf(Offset.Zero) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) QiGold.copy(alpha = 0.2f) else InkSurfaceVariant
        ),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) QiGold else InkBorder
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(135.dp)
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                globalPos = coordinates.boundsInRoot().center
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        onDragStart(globalPos)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd
                )
            }
            .clickable { onClick() }
            .testTag("pose_asset_${pose.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = pose.iconEmoji, fontSize = 14.sp)
                Surface(
                    color = QiGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text(
                        text = pose.category,
                        color = QiGold,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Text(
                text = pose.title,
                color = MangaPaperWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = pose.camera.displayName,
                color = ManhuaCyan,
                fontSize = 8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Glisser ➔",
                    color = QiGold,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Interactive Background card for the asset palette.
 */
@Composable
fun DraggableBackgroundCard(
    background: BackgroundProfile,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    var globalPos by remember { mutableStateOf(Offset.Zero) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ManhuaCyan.copy(alpha = 0.2f) else InkSurfaceVariant
        ),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) ManhuaCyan else InkBorder
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(135.dp)
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                globalPos = coordinates.boundsInRoot().center
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        onDragStart(globalPos)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd
                )
            }
            .clickable { onClick() }
            .testTag("background_asset_${background.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val refFile = background.referenceImagePath?.let { File(it) }
                if (refFile != null && refFile.exists()) {
                    AsyncImage(
                        model = refFile,
                        contentDescription = background.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ManhuaCyan.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = ManhuaCyan, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = background.name,
                    color = MangaPaperWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${background.category} • ${background.lightingMood}",
                color = TextSecondary,
                fontSize = 9.sp,
                maxLines = 1
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Glisser ➔",
                    color = ManhuaCyan,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Floating preview avatar following touch coordinates during drag-and-drop.
 */
@Composable
fun DraggingAssetFloatingPreview(
    asset: StoryboardAsset,
    position: Offset
) {
    val density = LocalDensity.current
    val xDp = with(density) { position.x.toDp() }
    val yDp = with(density) { position.y.toDp() }

    Box(
        modifier = Modifier
            .offset(x = xDp - 45.dp, y = yDp - 45.dp)
            .size(90.dp)
            .zIndex(999f)
            .shadow(16.dp, RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .border(2.dp, QiGold, RoundedCornerShape(12.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (asset) {
                is StoryboardAsset.CharacterAsset -> {
                    val refFile = asset.character.firstAppearanceImagePath?.let { File(it) }
                    if (refFile != null && refFile.exists()) {
                        AsyncImage(
                            model = refFile,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MangaCrimson, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = asset.character.name,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                is StoryboardAsset.PoseAsset -> {
                    Text(text = asset.pose.iconEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = asset.pose.title,
                        color = QiGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                is StoryboardAsset.BackgroundAsset -> {
                    Icon(Icons.Default.Image, contentDescription = null, tint = ManhuaCyan, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = asset.background.name,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
