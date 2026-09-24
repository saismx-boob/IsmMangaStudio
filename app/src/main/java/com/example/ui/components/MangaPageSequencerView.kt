package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.data.model.BackgroundProfile
import com.example.data.model.CharacterProfile
import com.example.data.model.GridPageLayout
import com.example.data.model.MangaPage
import com.example.data.model.MangaPanel
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

enum class PageSequencerGridMode(val title: String, val columns: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TWO_COLUMN("Grille 2x2", 2, Icons.Default.ViewModule),
    SINGLE_COLUMN("Webtoon 1 Col", 1, Icons.Default.ViewDay),
    COMPACT_THREE("Grille 3 Col", 3, Icons.Default.ViewCompact),
    MANGA_SHEET("Planche Manga", 0, Icons.Default.ViewAgenda)
}

/**
 * Interactive Grid-based layout for sequencing generated panels into a full page view.
 * Supports:
 * - Drag-and-drop reordering with fluid visual feedback & hover target indicators
 * - Direct Up/Down sequential step reordering
 * - Full page preview simulating authentic Japanese Manga paper layout & margins
 * - Insertion of new panels / sub-panels at any sequence position
 * - Direct regeneration & inspection
 * - Multi-column grid modes (2 columns, 1 column webtoon flow, 3 columns compact, authentic sheet)
 */
@Composable
fun MangaPageSequencerView(
    page: MangaPage?,
    panels: List<MangaPanel>,
    characters: List<CharacterProfile>,
    backgrounds: List<BackgroundProfile>,
    onReorderPanels: (List<MangaPanel>) -> Unit,
    onSelectPanel: (MangaPanel, Int) -> Unit,
    onGeneratePanel: (Int) -> Unit,
    onAddPanel: () -> Unit,
    onSplitPanel: (MangaPanel) -> Unit,
    onDeletePanel: (MangaPanel) -> Unit,
    modifier: Modifier = Modifier
) {
    var gridMode by remember { mutableStateOf(PageSequencerGridMode.TWO_COLUMN) }
    var showFullSheetDialog by remember { mutableStateOf(false) }

    // Local sequence state to enable smooth optimistic drag reordering
    val sequencedPanels = remember { mutableStateListOf<MangaPanel>() }
    LaunchedEffect(panels) {
        sequencedPanels.clear()
        sequencedPanels.addAll(panels.sortedBy { it.panelIndex })
    }

    // Drag-and-drop state
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragGlobalPosition by remember { mutableStateOf(Offset.Zero) }
    var targetHoverIndex by remember { mutableStateOf<Int?>(null) }

    // Global position bounds of each grid slot
    val slotBounds = remember { mutableStateMapOf<Int, Rect>() }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1017)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, ManhuaCyan.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("page_sequencer_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ManhuaCyan.copy(alpha = 0.15f))
                            .border(1.dp, ManhuaCyan, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = null,
                            tint = ManhuaCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SÉQUENCEUR DE PLANCHE",
                                color = ManhuaCyan,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = QiGold,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "DRAG & DROP",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Page #${page?.pageNumber ?: 1} • ${sequencedPanels.size} cases • Réorganisez par glisser-déposer",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Fullsheet preview button
                    OutlinedButton(
                        onClick = { showFullSheetDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MangaPaperWhite),
                        border = BorderStroke(1.dp, InkBorder),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("full_page_preview_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = ManhuaCyan
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Aperçu Planche", fontSize = 11.sp, color = MangaPaperWhite)
                    }

                    // Add panel button
                    Button(
                        onClick = onAddPanel,
                        colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("sequencer_add_panel_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "+ Case", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid View Selector Chips (2 columns, 1 column, 3 columns, Sheet)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF07090E), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PageSequencerGridMode.values().forEach { mode ->
                    val isSelected = gridMode == mode
                    Surface(
                        color = if (isSelected) ManhuaCyan else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { gridMode = mode }
                            .testTag("sequencer_mode_${mode.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = mode.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = mode.title,
                                color = if (isSelected) Color.Black else TextSecondary,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Drag & drop hint indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DragIndicator,
                        contentDescription = null,
                        tint = QiGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Maintenez appuyé sur une case pour la déplacer, ou utilisez ↑ / ↓",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                if (draggedIndex != null) {
                    Surface(
                        color = QiGold,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "DÉPLACEMENT ACTIF (CASE #${(draggedIndex ?: 0) + 1})",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Grid or Full Manga Page View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF080A10), RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, InkBorder), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                if (sequencedPanels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Aucune case dans cette planche.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                            Button(
                                onClick = onAddPanel,
                                colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson)
                            ) {
                                Text("+ Ajouter la première case")
                            }
                        }
                    }
                } else if (gridMode == PageSequencerGridMode.MANGA_SHEET) {
                    // Authentic Full Manga Page Sheet View
                    MangaFullSheetPageLayout(
                        panels = sequencedPanels,
                        characters = characters,
                        backgrounds = backgrounds,
                        pageNumber = page?.pageNumber ?: 1,
                        onPanelClicked = { p, idx -> onSelectPanel(p, idx) },
                        onMoveUp = { idx ->
                            if (idx > 0) {
                                val temp = sequencedPanels.toMutableList()
                                val item = temp.removeAt(idx)
                                temp.add(idx - 1, item)
                                onReorderPanels(temp)
                            }
                        },
                        onMoveDown = { idx ->
                            if (idx < sequencedPanels.size - 1) {
                                val temp = sequencedPanels.toMutableList()
                                val item = temp.removeAt(idx)
                                temp.add(idx + 1, item)
                                onReorderPanels(temp)
                            }
                        }
                    )
                } else {
                    // Multi-Column Grid Layout with Drag-and-Drop Sequencing
                    val columnCount = gridMode.columns
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columnCount),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 520.dp)
                            .testTag("sequencer_grid_view")
                    ) {
                        itemsIndexed(
                            items = sequencedPanels,
                            key = { _, panel -> panel.id.takeIf { it != 0L } ?: panel.panelIndex }
                        ) { index, panel ->
                            val isDragged = draggedIndex == index
                            val isHoverTarget = targetHoverIndex == index && draggedIndex != index
                            val character = characters.firstOrNull { it.id == panel.characterId }
                            val bg = backgrounds.firstOrNull { it.id == panel.backgroundId }

                            SequencerPanelGridItem(
                                panel = panel,
                                displayIndex = index,
                                totalCount = sequencedPanels.size,
                                isDragged = isDragged,
                                isHoverTarget = isHoverTarget,
                                character = character,
                                background = bg,
                                onSlotGloballyPositioned = { rect ->
                                    slotBounds[index] = rect
                                },
                                onDragStart = { startOffset ->
                                    draggedIndex = index
                                    dragGlobalPosition = startOffset
                                },
                                onDrag = { dragDelta ->
                                    dragGlobalPosition += dragDelta
                                    // Hit-test other slots
                                    var newHover: Int? = null
                                    for ((i, rect) in slotBounds) {
                                        if (rect.contains(dragGlobalPosition)) {
                                            newHover = i
                                            break
                                        }
                                    }
                                    targetHoverIndex = newHover
                                },
                                onDragEnd = {
                                    val from = draggedIndex
                                    val to = targetHoverIndex
                                    if (from != null && to != null && from != to) {
                                        val updatedList = sequencedPanels.toMutableList()
                                        val movedItem = updatedList.removeAt(from)
                                        updatedList.add(to, movedItem)
                                        onReorderPanels(updatedList)
                                    }
                                    draggedIndex = null
                                    targetHoverIndex = null
                                },
                                onSelect = { onSelectPanel(panel, index) },
                                onGenerate = { onGeneratePanel(index) },
                                onSplit = { onSplitPanel(panel) },
                                onDelete = { onDeletePanel(panel) },
                                onMoveUp = {
                                    if (index > 0) {
                                        val updated = sequencedPanels.toMutableList()
                                        val moved = updated.removeAt(index)
                                        updated.add(index - 1, moved)
                                        onReorderPanels(updated)
                                    }
                                },
                                onMoveDown = {
                                    if (index < sequencedPanels.size - 1) {
                                        val updated = sequencedPanels.toMutableList()
                                        val moved = updated.removeAt(index)
                                        updated.add(index + 1, moved)
                                        onReorderPanels(updated)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Fullscreen Page Sheet Dialog
    if (showFullSheetDialog) {
        Dialog(
            onDismissRequest = { showFullSheetDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PLANCHE MANGA COMPLÈTE • PAGE #${page?.pageNumber ?: 1}",
                            color = MangaPaperWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        IconButton(onClick = { showFullSheetDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        MangaFullSheetPageLayout(
                            panels = sequencedPanels,
                            characters = characters,
                            backgrounds = backgrounds,
                            pageNumber = page?.pageNumber ?: 1,
                            onPanelClicked = { p, idx ->
                                showFullSheetDialog = false
                                onSelectPanel(p, idx)
                            },
                            onMoveUp = { idx ->
                                if (idx > 0) {
                                    val temp = sequencedPanels.toMutableList()
                                    val item = temp.removeAt(idx)
                                    temp.add(idx - 1, item)
                                    onReorderPanels(temp)
                                }
                            },
                            onMoveDown = { idx ->
                                if (idx < sequencedPanels.size - 1) {
                                    val temp = sequencedPanels.toMutableList()
                                    val item = temp.removeAt(idx)
                                    temp.add(idx + 1, item)
                                    onReorderPanels(temp)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * An individual draggable and reorderable item inside the page sequencer grid.
 */
@Composable
fun SequencerPanelGridItem(
    panel: MangaPanel,
    displayIndex: Int,
    totalCount: Int,
    isDragged: Boolean,
    isHoverTarget: Boolean,
    character: CharacterProfile?,
    background: BackgroundProfile?,
    onSlotGloballyPositioned: (Rect) -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onSelect: () -> Unit,
    onGenerate: () -> Unit,
    onSplit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isDragged) 1.05f else if (isHoverTarget) 0.97f else 1.0f,
        animationSpec = spring(),
        label = "scale"
    )

    val borderColor = when {
        isDragged -> QiGold
        isHoverTarget -> ManhuaCyan
        panel.imagePath != null -> MangaCrimson.copy(alpha = 0.6f)
        else -> InkBorder
    }

    Box(
        modifier = modifier
            .scale(scale)
            .onGloballyPositioned { coords ->
                onSlotGloballyPositioned(coords.boundsInRoot())
            }
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHoverTarget) ManhuaCyan.copy(alpha = 0.15f) else Color(0xFF131722))
            .border(BorderStroke(if (isDragged || isHoverTarget) 2.dp else 1.dp, borderColor), RoundedCornerShape(8.dp))
            .testTag("sequencer_panel_slot_$displayIndex")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Panel Preview Media Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
                    .background(Color(0xFF0A0D14))
                    .clickable { onSelect() }
                    .pointerInput(displayIndex) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                onDragStart(offset)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount)
                            },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() }
                        )
                    }
            ) {
                if (panel.imagePath != null && File(panel.imagePath).exists()) {
                    AsyncImage(
                        model = File(panel.imagePath),
                        contentDescription = "Case #${displayIndex + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Blueprint Placeholder
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Non générée",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Top Left: Sequence Index Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(bottomEnd = 6.dp),
                    border = BorderStroke(0.8.dp, InkBorder),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragIndicator,
                            contentDescription = "Glisser",
                            tint = QiGold,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "#${displayIndex + 1}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Top Right: Character badge if assigned
                if (character != null) {
                    Surface(
                        color = MangaCrimson.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(bottomStart = 6.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = character.name,
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Hover target message overlay
                if (isHoverTarget) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ManhuaCyan.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DÉPOSER EN #${displayIndex + 1}",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Description / Action Prompt snippet
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = panel.userPrompt.ifBlank { "Action non renseignée" },
                    color = MangaPaperWhite,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Footer Controls: Reorder Up/Down arrows & quick actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sequential Move buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = displayIndex > 0,
                            modifier = Modifier
                                .size(22.dp)
                                .testTag("seq_up_$displayIndex")
                        ) {
                            Icon(
                                imageVector = Icons.Default.North,
                                contentDescription = "Monter dans la séquence",
                                tint = if (displayIndex > 0) ManhuaCyan else TextMuted.copy(alpha = 0.3f),
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        IconButton(
                            onClick = onMoveDown,
                            enabled = displayIndex < totalCount - 1,
                            modifier = Modifier
                                .size(22.dp)
                                .testTag("seq_down_$displayIndex")
                        ) {
                            Icon(
                                imageVector = Icons.Default.South,
                                contentDescription = "Descendre dans la séquence",
                                tint = if (displayIndex < totalCount - 1) ManhuaCyan else TextMuted.copy(alpha = 0.3f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Action buttons (Split, Generate)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Split button
                        Surface(
                            color = InkMidnight,
                            shape = RoundedCornerShape(3.dp),
                            border = BorderStroke(0.6.dp, QiGold.copy(alpha = 0.6f)),
                            modifier = Modifier.clickable { onSplit() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Splitscreen,
                                contentDescription = "Diviser",
                                tint = QiGold,
                                modifier = Modifier
                                    .padding(3.dp)
                                    .size(12.dp)
                            )
                        }

                        // Generate / Regenerate
                        Surface(
                            color = MangaCrimson,
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier.clickable { onGenerate() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = if (panel.imagePath != null) "Refaire" else "Créer",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
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
 * Authentic Japanese Manga paper page layout representation (B4 paper standard with standard bleed & margins).
 * Renders panels sequentially into a coherent page composition.
 */
@Composable
fun MangaFullSheetPageLayout(
    panels: List<MangaPanel>,
    characters: List<CharacterProfile>,
    backgrounds: List<BackgroundProfile>,
    pageNumber: Int,
    onPanelClicked: (MangaPanel, Int) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF07090E)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(2.dp, InkBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .testTag("manga_full_sheet_layout")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Manga Bleed & Meta Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "◄ SENS DE LECTURE MANGA • PAGE $pageNumber",
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${panels.size} CASES ENCHAÎNÉES",
                    color = ManhuaCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Sequential Flow based on panel count (Dynamic Grid Partitioning)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF030406), RoundedCornerShape(6.dp))
                    .border(BorderStroke(1.dp, Color(0xFF1E2433)), RoundedCornerShape(6.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (panels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aucune case dans cette planche.", color = TextMuted, fontSize = 12.sp)
                    }
                } else if (panels.size == 1) {
                    // Full Splash Page
                    FullSheetSlot(
                        panel = panels[0],
                        index = 0,
                        total = 1,
                        character = characters.firstOrNull { it.id == panels[0].characterId },
                        onClick = { onPanelClicked(panels[0], 0) },
                        onMoveUp = { onMoveUp(0) },
                        onMoveDown = { onMoveDown(0) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    )
                } else if (panels.size == 2) {
                    // 2 stacked vertical panels
                    panels.forEachIndexed { idx, p ->
                        FullSheetSlot(
                            panel = p,
                            index = idx,
                            total = 2,
                            character = characters.firstOrNull { it.id == p.characterId },
                            onClick = { onPanelClicked(p, idx) },
                            onMoveUp = { onMoveUp(idx) },
                            onMoveDown = { onMoveDown(idx) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )
                    }
                } else if (panels.size == 3) {
                    // 1 Top Wide Panel, 2 Bottom Side-by-side
                    FullSheetSlot(
                        panel = panels[0],
                        index = 0,
                        total = 3,
                        character = characters.firstOrNull { it.id == panels[0].characterId },
                        onClick = { onPanelClicked(panels[0], 0) },
                        onMoveUp = { onMoveUp(0) },
                        onMoveDown = { onMoveDown(0) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 1..2) {
                            val p = panels.getOrNull(i)
                            if (p != null) {
                                FullSheetSlot(
                                    panel = p,
                                    index = i,
                                    total = 3,
                                    character = characters.firstOrNull { it.id == p.characterId },
                                    onClick = { onPanelClicked(p, i) },
                                    onMoveUp = { onMoveUp(i) },
                                    onMoveDown = { onMoveDown(i) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                        }
                    }
                } else {
                    // 4 or more panels: 2x2 or sequential row layout
                    val chunked = panels.chunked(2)
                    chunked.forEachIndexed { rowIndex, rowPanels ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowPanels.forEachIndexed { colIndex, p ->
                                val actualIdx = rowIndex * 2 + colIndex
                                FullSheetSlot(
                                    panel = p,
                                    index = actualIdx,
                                    total = panels.size,
                                    character = characters.firstOrNull { it.id == p.characterId },
                                    onClick = { onPanelClicked(p, actualIdx) },
                                    onMoveUp = { onMoveUp(actualIdx) },
                                    onMoveDown = { onMoveDown(actualIdx) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                            if (rowPanels.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullSheetSlot(
    panel: MangaPanel,
    index: Int,
    total: Int,
    character: CharacterProfile?,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF10141E))
            .border(BorderStroke(1.dp, InkBorder), RoundedCornerShape(4.dp))
            .clickable { onClick() }
    ) {
        if (panel.imagePath != null && File(panel.imagePath).exists()) {
            AsyncImage(
                model = File(panel.imagePath),
                contentDescription = "Case #${index + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Case #${index + 1}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                if (panel.userPrompt.isNotBlank()) {
                    Text(
                        text = panel.userPrompt,
                        color = TextSecondary,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Top Left Index
        Surface(
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(bottomEnd = 4.dp),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Text(
                text = "#${index + 1}",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }

        // Top Right Reorder quick arrows
        Surface(
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(bottomStart = 4.dp),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Row(modifier = Modifier.padding(1.dp)) {
                if (index > 0) {
                    IconButton(
                        onClick = onMoveUp,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            Icons.Default.North,
                            contentDescription = "Monter",
                            tint = ManhuaCyan,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                if (index < total - 1) {
                    IconButton(
                        onClick = onMoveDown,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            Icons.Default.South,
                            contentDescription = "Descendre",
                            tint = ManhuaCyan,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}
