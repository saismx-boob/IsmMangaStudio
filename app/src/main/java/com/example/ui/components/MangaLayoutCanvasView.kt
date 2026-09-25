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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.WidthFull
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
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
import coil.request.ImageRequest
import com.example.data.model.BackgroundProfile
import com.example.data.model.CharacterProfile
import com.example.data.model.MangaPage
import com.example.data.model.MangaPanel
import com.example.ui.theme.BubbleFontOption
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
 * Predefined manga speech bubble placeholder types available in the drag-and-drop toolbox.
 */
data class SpeechBubblePreset(
    val id: String,
    val type: String, // SPEECH, SHOUT, THOUGHT, WHISPER, NARRATION, ONOMATOPOEIA
    val label: String,
    val description: String,
    val defaultText: String,
    val iconEmoji: String,
    val defaultTail: String = "BOTTOM_LEFT",
    val defaultFont: String = "COMIC_NEUE",
    val defaultFontSize: Int = 14
)

val SPEECH_BUBBLE_PRESETS = listOf(
    SpeechBubblePreset(
        id = "bubble_speech",
        type = "SPEECH",
        label = "Dialogue",
        description = "Bulle ovale classique avec queue directionnelle",
        defaultText = "Dialogue du personnage...",
        iconEmoji = "💬",
        defaultTail = "BOTTOM_LEFT",
        defaultFont = "COMIC_NEUE",
        defaultFontSize = 14
    ),
    SpeechBubblePreset(
        id = "bubble_shout",
        type = "SHOUT",
        label = "Cri / Action",
        description = "Explosion dentée shonen pour attaques et révélations",
        defaultText = "KAMEHAMEHA !!",
        iconEmoji = "💥",
        defaultTail = "NONE",
        defaultFont = "BANGERS",
        defaultFontSize = 18
    ),
    SpeechBubblePreset(
        id = "bubble_thought",
        type = "THOUGHT",
        label = "Pensée",
        description = "Nuage doux avec petites bulles descendantes",
        defaultText = "(Il a esquivé mon coup... Mais comment ?)",
        iconEmoji = "💭",
        defaultTail = "BOTTOM_LEFT",
        defaultFont = "CAVEAT",
        defaultFontSize = 14
    ),
    SpeechBubblePreset(
        id = "bubble_whisper",
        type = "WHISPER",
        label = "Chuchotement",
        description = "Contour pointillé pour voix basse et confidence",
        defaultText = "Regarde derrière toi...",
        iconEmoji = "🤫",
        defaultTail = "BOTTOM_LEFT",
        defaultFont = "COMIC_NEUE",
        defaultFontSize = 13
    ),
    SpeechBubblePreset(
        id = "bubble_narration",
        type = "NARRATION",
        label = "Narration",
        description = "Cartouche rectangulaire sobre pour récit",
        defaultText = "Pendant ce temps, aux confins du royaume...",
        iconEmoji = "📜",
        defaultTail = "NONE",
        defaultFont = "COMIC_NEUE",
        defaultFontSize = 13
    ),
    SpeechBubblePreset(
        id = "bubble_sfx",
        type = "ONOMATOPOEIA",
        label = "SFX Onomatopée",
        description = "Bannière dynamique manga (BAM, DODODO, SLASH)",
        defaultText = "DODODODO !!",
        iconEmoji = "⚡",
        defaultTail = "NONE",
        defaultFont = "BANGERS",
        defaultFontSize = 20
    )
)

/**
 * Predefined page layout template options for quick start.
 */
data class MangaPageLayoutTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val panelBlueprints: List<PanelBlueprint>
)

data class PanelBlueprint(
    val widthFraction: Float, // 1.0f = full width, 0.5f = half, 0.67f = 2/3, 0.33f = 1/3
    val heightDp: Int,
    val defaultPrompt: String,
    val defaultBubble: SpeechBubblePreset? = null,
    val bubbleNormX: Float = 0.5f,
    val bubbleNormY: Float = 0.3f
)

val MANGA_LAYOUT_TEMPLATES = listOf(
    MangaPageLayoutTemplate(
        id = "tpl_shonen_climax",
        title = "Shonen Climax",
        subtitle = "Bandeau intro + 2 demi-cases + Grande case d'impact",
        icon = Icons.Default.ViewModule,
        panelBlueprints = listOf(
            PanelBlueprint(1.0f, 130, "Vue d'ensemble de la situation - Tension", SPEECH_BUBBLE_PRESETS[4], 0.25f, 0.25f),
            PanelBlueprint(0.5f, 180, "Gros plan sur le héros déterminé", SPEECH_BUBBLE_PRESETS[0], 0.45f, 0.35f),
            PanelBlueprint(0.5f, 180, "Réaction surprise de l'antagoniste", SPEECH_BUBBLE_PRESETS[2], 0.55f, 0.35f),
            PanelBlueprint(1.0f, 290, "Climax monumental : Frappe décisive d'énergie", SPEECH_BUBBLE_PRESETS[1], 0.5f, 0.6f)
        )
    ),
    MangaPageLayoutTemplate(
        id = "tpl_action_dynamique",
        title = "Combat & Vitesse",
        subtitle = "Grande case 2/3 + 2 petites d'action 1/3 + Bandeau fuite",
        icon = Icons.Default.ViewColumn,
        panelBlueprints = listOf(
            PanelBlueprint(0.67f, 220, "Héros exécutant un bond tranchant katana au clair", SPEECH_BUBBLE_PRESETS[5], 0.35f, 0.25f),
            PanelBlueprint(0.33f, 105, "Détail : étincelle de l'impact", null, 0.5f, 0.5f),
            PanelBlueprint(0.33f, 105, "Détail : regard tranchant", SPEECH_BUBBLE_PRESETS[3], 0.5f, 0.4f),
            PanelBlueprint(1.0f, 150, "Poussière et traînée d'aura à travers le champ de bataille", SPEECH_BUBBLE_PRESETS[0], 0.7f, 0.3f)
        )
    ),
    MangaPageLayoutTemplate(
        id = "tpl_dialogue_psychologique",
        title = "Dialogue & Tension",
        subtitle = "Grande case pensée + 2 demi-cases champ/contre-champ",
        icon = Icons.Default.ViewAgenda,
        panelBlueprints = listOf(
            PanelBlueprint(1.0f, 200, "Ambiance lourde sous la pluie, réflexion tactique", SPEECH_BUBBLE_PRESETS[2], 0.3f, 0.3f),
            PanelBlueprint(0.5f, 180, "Champ : Le protagoniste annonce sa décision", SPEECH_BUBBLE_PRESETS[0], 0.5f, 0.35f),
            PanelBlueprint(0.5f, 180, "Contre-champ : Le mentor sourit avec fierté", SPEECH_BUBBLE_PRESETS[0], 0.5f, 0.35f)
        )
    ),
    MangaPageLayoutTemplate(
        id = "tpl_yonkoma_4koma",
        title = "4-Koma Rythmé",
        subtitle = "4 cases horizontales pour cadence narrative",
        icon = Icons.Default.ViewDay,
        panelBlueprints = listOf(
            PanelBlueprint(1.0f, 130, "Ki (Introduction) : Situation de départ", SPEECH_BUBBLE_PRESETS[0], 0.5f, 0.3f),
            PanelBlueprint(1.0f, 130, "Sho (Développement) : L'événement s'installe", SPEECH_BUBBLE_PRESETS[0], 0.5f, 0.3f),
            PanelBlueprint(1.0f, 130, "Ten (Rebondissement) : Twist inattendu !", SPEECH_BUBBLE_PRESETS[1], 0.5f, 0.3f),
            PanelBlueprint(1.0f, 130, "Ketsu (Chute) : Conclusion comique ou dramatique", SPEECH_BUBBLE_PRESETS[2], 0.5f, 0.3f)
        )
    ),
    MangaPageLayoutTemplate(
        id = "tpl_splash_epic",
        title = "Splash Page Épique",
        subtitle = "1 immense case pleine page pour révélation majeure",
        icon = Icons.Default.Fullscreen,
        panelBlueprints = listOf(
            PanelBlueprint(1.0f, 420, "Révélation monumentale du dragon céleste surplombant la cité", SPEECH_BUBBLE_PRESETS[1], 0.5f, 0.75f)
        )
    )
)

/**
 * Drag-and-drop canvas UI component that allows users to define manga page layouts,
 * including variable panel sizes and speech bubble placement placeholders.
 */
@Composable
fun MangaLayoutCanvasView(
    page: MangaPage?,
    panels: List<MangaPanel>,
    characters: List<CharacterProfile>,
    backgrounds: List<BackgroundProfile>,
    onApplyLayout: (List<MangaPanel>, String) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // In-memory working layout state allowing full live manipulation before applying
    val layoutPanels = remember { mutableStateListOf<MangaPanel>() }

    LaunchedEffect(panels) {
        layoutPanels.clear()
        if (panels.isNotEmpty()) {
            layoutPanels.addAll(panels.sortedBy { it.panelIndex })
        } else {
            // Default 3-panel dynamic layout if empty
            layoutPanels.addAll(
                listOf(
                    MangaPanel(pageId = page?.id ?: 0, panelIndex = 0, userPrompt = "Case d'ouverture", widthFraction = 1.0f, heightDp = 150),
                    MangaPanel(pageId = page?.id ?: 0, panelIndex = 1, userPrompt = "Action gauche", widthFraction = 0.5f, heightDp = 180),
                    MangaPanel(pageId = page?.id ?: 0, panelIndex = 2, userPrompt = "Action droite", widthFraction = 0.5f, heightDp = 180)
                )
            )
        }
    }

    // Selected panel for fine-grained editing
    var selectedPanelIndex by remember { mutableIntStateOf(0) }

    // Bubble being actively edited in dialog
    var activeEditingBubblePanelIndex by remember { mutableStateOf<Int?>(null) }

    // Global drag-and-drop state for Speech Bubble Placeholders
    var isDraggingBubblePreset by remember { mutableStateOf(false) }
    var draggedBubblePreset by remember { mutableStateOf<SpeechBubblePreset?>(null) }
    var dragBubblePosition by remember { mutableStateOf(Offset.Zero) }
    var hoveredPanelIndex by remember { mutableStateOf<Int?>(null) }

    // Registry of Panel bounds on screen for drop collision
    val panelScreenBounds = remember { mutableStateMapOf<Int, Rect>() }

    // Templates menu dialog
    var showTemplatesDialog by remember { mutableStateOf(false) }

    // Fullsheet preview dialog
    var showProofPreviewDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090C12)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, ManhuaCyan.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("manga_layout_canvas_view")
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
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = ManhuaCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CANVAS MISE EN PAGE & BULLES",
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MangaCrimson,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "DRAG & DROP",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Ajustez les tailles de cases et glissez les bulles vectorielles",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Templates button
                    OutlinedButton(
                        onClick = { showTemplatesDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = QiGold),
                        border = BorderStroke(1.dp, QiGold.copy(alpha = 0.8f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("canvas_templates_btn")
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(13.dp), tint = QiGold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gabarits", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = QiGold)
                    }

                    // Apply layout button
                    Button(
                        onClick = {
                            onApplyLayout(layoutPanels.toList(), "CUSTOM_CANVAS")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ManhuaCyan),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("canvas_apply_layout_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Appliquer", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Speech Bubble Drawer (Draggable Placeholders Tray)
            Surface(
                color = Color(0xFF131824),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, InkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ChatBubble, contentDescription = null, tint = MangaCrimson, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PLACEHOLDERS DE BULLES (GLISSER SUR UNE CASE)",
                                color = MangaPaperWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "Touchez ou Glissez-déposez",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row of draggable bubble badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SPEECH_BUBBLE_PRESETS.forEach { preset ->
                            DraggableBubblePresetBadge(
                                preset = preset,
                                onDragStart = { startOffset ->
                                    isDraggingBubblePreset = true
                                    draggedBubblePreset = preset
                                    dragBubblePosition = startOffset
                                },
                                onDrag = { delta ->
                                    dragBubblePosition += delta
                                    // Hit-test with panel bounds
                                    val hitIndex = panelScreenBounds.entries.firstOrNull { (_, rect) ->
                                        rect.contains(dragBubblePosition)
                                    }?.key
                                    hoveredPanelIndex = hitIndex
                                },
                                onDragEnd = {
                                    val targetIndex = hoveredPanelIndex
                                    val targetPreset = draggedBubblePreset
                                    if (targetIndex != null && targetPreset != null && targetIndex in layoutPanels.indices) {
                                        val panel = layoutPanels[targetIndex]
                                        val bounds = panelScreenBounds[targetIndex]
                                        val normX = if (bounds != null && bounds.width > 0f) {
                                            ((dragBubblePosition.x - bounds.left) / bounds.width).coerceIn(0.1f, 0.9f)
                                        } else 0.5f
                                        val normY = if (bounds != null && bounds.height > 0f) {
                                            ((dragBubblePosition.y - bounds.top) / bounds.height).coerceIn(0.1f, 0.9f)
                                        } else 0.4f

                                        layoutPanels[targetIndex] = panel.copy(
                                            dialogueText = targetPreset.defaultText,
                                            bubbleType = targetPreset.type,
                                            bubbleFont = targetPreset.defaultFont,
                                            bubbleFontSize = targetPreset.defaultFontSize,
                                            bubbleTailDirection = targetPreset.defaultTail,
                                            bubbleNormalizedX = normX,
                                            bubbleNormalizedY = normY
                                        )
                                        selectedPanelIndex = targetIndex
                                    }
                                    isDraggingBubblePreset = false
                                    draggedBubblePreset = null
                                    hoveredPanelIndex = null
                                },
                                onTap = {
                                    // Tap to apply to currently selected panel
                                    if (selectedPanelIndex in layoutPanels.indices) {
                                        val panel = layoutPanels[selectedPanelIndex]
                                        layoutPanels[selectedPanelIndex] = panel.copy(
                                            dialogueText = preset.defaultText,
                                            bubbleType = preset.type,
                                            bubbleFont = preset.defaultFont,
                                            bubbleFontSize = preset.defaultFontSize,
                                            bubbleTailDirection = preset.defaultTail
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Canvas Area: Japanese Manga B4 Manuscript Paper simulation
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                MangaPageDraftingBoard(
                    panels = layoutPanels,
                    selectedIndex = selectedPanelIndex,
                    hoveredIndex = hoveredPanelIndex,
                    characters = characters,
                    backgrounds = backgrounds,
                    onSelectPanel = { selectedPanelIndex = it },
                    onPanelBoundsPositioned = { index, rect ->
                        panelScreenBounds[index] = rect
                    },
                    onPanelHeightChanged = { index, newHeightDp ->
                        if (index in layoutPanels.indices) {
                            layoutPanels[index] = layoutPanels[index].copy(heightDp = newHeightDp.coerceIn(90, 420))
                        }
                    },
                    onPanelWidthChanged = { index, newFraction ->
                        if (index in layoutPanels.indices) {
                            layoutPanels[index] = layoutPanels[index].copy(widthFraction = newFraction)
                        }
                    },
                    onBubblePositionChanged = { index, normX, normY ->
                        if (index in layoutPanels.indices) {
                            val p = layoutPanels[index]
                            layoutPanels[index] = p.copy(
                                bubbleNormalizedX = normX.coerceIn(0.08f, 0.92f),
                                bubbleNormalizedY = normY.coerceIn(0.08f, 0.92f)
                            )
                        }
                    },
                    onEditBubble = { index ->
                        activeEditingBubblePanelIndex = index
                    },
                    onRemoveBubble = { index ->
                        if (index in layoutPanels.indices) {
                            layoutPanels[index] = layoutPanels[index].copy(dialogueText = null)
                        }
                    },
                    onMovePanelUp = { index ->
                        if (index > 0 && index in layoutPanels.indices) {
                            val item = layoutPanels.removeAt(index)
                            layoutPanels.add(index - 1, item)
                            selectedPanelIndex = index - 1
                        }
                    },
                    onMovePanelDown = { index ->
                        if (index < layoutPanels.size - 1 && index in layoutPanels.indices) {
                            val item = layoutPanels.removeAt(index)
                            layoutPanels.add(index + 1, item)
                            selectedPanelIndex = index + 1
                        }
                    },
                    onDeletePanel = { index ->
                        if (layoutPanels.size > 1 && index in layoutPanels.indices) {
                            layoutPanels.removeAt(index)
                            selectedPanelIndex = (index - 1).coerceAtLeast(0)
                        }
                    },
                    onAddPanel = {
                        val nextIdx = layoutPanels.size
                        layoutPanels.add(
                            MangaPanel(
                                pageId = page?.id ?: 0,
                                panelIndex = nextIdx,
                                userPrompt = "Nouvelle case $nextIdx",
                                widthFraction = 1.0f,
                                heightDp = 180
                            )
                        )
                        selectedPanelIndex = nextIdx
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Selected Panel Variable Size & Bubble Controls Toolbar
            if (selectedPanelIndex in layoutPanels.indices) {
                val curPanel = layoutPanels[selectedPanelIndex]
                SelectedPanelControlsBar(
                    panel = curPanel,
                    panelIndex = selectedPanelIndex,
                    totalPanels = layoutPanels.size,
                    onWidthChanged = { fraction ->
                        layoutPanels[selectedPanelIndex] = curPanel.copy(widthFraction = fraction)
                    },
                    onHeightChanged = { heightDp ->
                        layoutPanels[selectedPanelIndex] = curPanel.copy(heightDp = heightDp)
                    },
                    onAddBubble = {
                        layoutPanels[selectedPanelIndex] = curPanel.copy(
                            dialogueText = curPanel.dialogueText ?: "Dialogue...",
                            bubbleType = curPanel.bubbleType.ifBlank { "SPEECH" }
                        )
                        activeEditingBubblePanelIndex = selectedPanelIndex
                    },
                    onEditBubble = {
                        activeEditingBubblePanelIndex = selectedPanelIndex
                    },
                    onDeletePanel = {
                        if (layoutPanels.size > 1) {
                            layoutPanels.removeAt(selectedPanelIndex)
                            selectedPanelIndex = (selectedPanelIndex - 1).coerceAtLeast(0)
                        }
                    }
                )
            }
        }
    }

    // Floating Bubble Dragging Preview
    if (isDraggingBubblePreset && draggedBubblePreset != null) {
        FloatingBubbleDragPreview(
            preset = draggedBubblePreset!!,
            position = dragBubblePosition
        )
    }

    // Bubble Editor Dialog
    if (activeEditingBubblePanelIndex != null && activeEditingBubblePanelIndex!! in layoutPanels.indices) {
        val targetPanel = layoutPanels[activeEditingBubblePanelIndex!!]
        BubblePlaceholderEditorDialog(
            panel = targetPanel,
            panelNumber = activeEditingBubblePanelIndex!! + 1,
            onDismiss = { activeEditingBubblePanelIndex = null },
            onSave = { updatedText, updatedType, updatedFont, updatedFontSize, updatedTail ->
                val p = layoutPanels[activeEditingBubblePanelIndex!!]
                layoutPanels[activeEditingBubblePanelIndex!!] = p.copy(
                    dialogueText = updatedText.ifBlank { null },
                    bubbleType = updatedType,
                    bubbleFont = updatedFont,
                    bubbleFontSize = updatedFontSize,
                    bubbleTailDirection = updatedTail
                )
                activeEditingBubblePanelIndex = null
            },
            onDelete = {
                val p = layoutPanels[activeEditingBubblePanelIndex!!]
                layoutPanels[activeEditingBubblePanelIndex!!] = p.copy(dialogueText = null)
                activeEditingBubblePanelIndex = null
            }
        )
    }

    // Templates Selection Dialog
    if (showTemplatesDialog) {
        TemplatesSelectionDialog(
            templates = MANGA_LAYOUT_TEMPLATES,
            onDismiss = { showTemplatesDialog = false },
            onSelectTemplate = { tpl ->
                layoutPanels.clear()
                tpl.panelBlueprints.forEachIndexed { i, bp ->
                    layoutPanels.add(
                        MangaPanel(
                            pageId = page?.id ?: 0,
                            panelIndex = i,
                            userPrompt = bp.defaultPrompt,
                            widthFraction = bp.widthFraction,
                            heightDp = bp.heightDp,
                            dialogueText = bp.defaultBubble?.defaultText,
                            bubbleType = bp.defaultBubble?.type ?: "SPEECH",
                            bubbleFont = bp.defaultBubble?.defaultFont ?: "COMIC_NEUE",
                            bubbleFontSize = bp.defaultBubble?.defaultFontSize ?: 14,
                            bubbleTailDirection = bp.defaultBubble?.defaultTail ?: "BOTTOM_LEFT",
                            bubbleNormalizedX = bp.bubbleNormX,
                            bubbleNormalizedY = bp.bubbleNormY
                        )
                    )
                }
                selectedPanelIndex = 0
                showTemplatesDialog = false
            }
        )
    }
}

/**
 * Draggable Bubble Preset Badge in the top drawer.
 */
@Composable
fun DraggableBubblePresetBadge(
    preset: SpeechBubblePreset,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onTap: () -> Unit
) {
    var itemRootOffset by remember { mutableStateOf(Offset.Zero) }

    Surface(
        color = InkMidnight,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF2B3347)),
        modifier = Modifier
            .onGloballyPositioned { coords ->
                itemRootOffset = coords.boundsInRoot().topLeft
            }
            .pointerInput(preset.id) {
                detectDragGestures(
                    onDragStart = { localOffset ->
                        onDragStart(itemRootOffset + localOffset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
            .clickable { onTap() }
            .testTag("bubble_preset_${preset.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(preset.iconEmoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = preset.label,
                    color = MangaPaperWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = preset.type,
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.DragIndicator,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * Floating drag preview following pointer when dragging speech bubble placeholder.
 */
@Composable
fun FloatingBubbleDragPreview(
    preset: SpeechBubblePreset,
    position: Offset
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(9999f)
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (position.x - 70).roundToInt(),
                        (position.y - 40).roundToInt()
                    )
                }
                .shadow(16.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(2.dp, MangaCrimson, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(preset.iconEmoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = preset.label,
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Relâchez sur une case",
                        color = MangaCrimson,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Manga Page Drafting Board: Visualizing the manuscript page with safe margins and variable panels.
 */
@Composable
fun MangaPageDraftingBoard(
    panels: List<MangaPanel>,
    selectedIndex: Int,
    hoveredIndex: Int?,
    characters: List<CharacterProfile>,
    backgrounds: List<BackgroundProfile>,
    onSelectPanel: (Int) -> Unit,
    onPanelBoundsPositioned: (Int, Rect) -> Unit,
    onPanelHeightChanged: (Int, Int) -> Unit,
    onPanelWidthChanged: (Int, Float) -> Unit,
    onBubblePositionChanged: (Int, Float, Float) -> Unit,
    onEditBubble: (Int) -> Unit,
    onRemoveBubble: (Int) -> Unit,
    onMovePanelUp: (Int) -> Unit,
    onMovePanelDown: (Int) -> Unit,
    onDeletePanel: (Int) -> Unit,
    onAddPanel: () -> Unit
) {
    // Manuscript Board Container
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F5F0)), // Authentic manga manuscript paper tone
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(2.dp, Color(0xFFD4CEBA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("manuscript_drafting_board")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Draw authentic Japanese manuscript lines (Non-reproducible cyan/blue lines)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val blueGuide = Color(0xFF70B0E0).copy(alpha = 0.35f)
                        val margin = 16.dp.toPx()
                        val stroke = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f))

                        // Safe outer bleed border
                        drawRect(
                            color = blueGuide,
                            topLeft = Offset(margin, margin),
                            size = androidx.compose.ui.geometry.Size(size.width - margin * 2, size.height - margin * 2),
                            style = stroke
                        )

                        // Center spine / fold line
                        drawLine(
                            color = blueGuide,
                            start = Offset(size.width / 2f, 0f),
                            end = Offset(size.width / 2f, size.height),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }
            )

            // Header of Manuscript
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📐 PLANCHE MANGA B4 • CADRE D'IMPRESSION DIRECT",
                    color = Color(0xFF5A728A),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${panels.size} CASES DÉFINIES",
                        color = MangaCrimson,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Scrollable Panel Flow inside manuscript safe area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Group panels into rows based on widthFraction
                var currentPanelIndex = 0
                while (currentPanelIndex < panels.size) {
                    val firstInRow = panels[currentPanelIndex]
                    val firstIndex = currentPanelIndex

                    if (firstInRow.widthFraction >= 0.9f) {
                        // Full width panel (1.0f)
                        CanvasPanelFrame(
                            panel = firstInRow,
                            panelIndex = firstIndex,
                            isSelected = selectedIndex == firstIndex,
                            isHovered = hoveredIndex == firstIndex,
                            character = characters.firstOrNull { it.id == firstInRow.characterId },
                            background = backgrounds.firstOrNull { it.id == firstInRow.backgroundId },
                            onSelect = { onSelectPanel(firstIndex) },
                            onBoundsPositioned = { r -> onPanelBoundsPositioned(firstIndex, r) },
                            onHeightChanged = { h -> onPanelHeightChanged(firstIndex, h) },
                            onBubblePositionChanged = { x, y -> onBubblePositionChanged(firstIndex, x, y) },
                            onEditBubble = { onEditBubble(firstIndex) },
                            onRemoveBubble = { onRemoveBubble(firstIndex) },
                            onMoveUp = { onMovePanelUp(firstIndex) },
                            onMoveDown = { onMovePanelDown(firstIndex) },
                            onDelete = { onDeletePanel(firstIndex) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        currentPanelIndex++
                    } else {
                        // Multi-column row (e.g. 50% + 50%, or 67% + 33%, or 33% + 33% + 33%)
                        val rowPanels = mutableListOf<Pair<Int, MangaPanel>>()
                        rowPanels.add(firstIndex to firstInRow)
                        var accumulatedWidth = firstInRow.widthFraction
                        currentPanelIndex++

                        while (currentPanelIndex < panels.size && (accumulatedWidth + panels[currentPanelIndex].widthFraction) <= 1.05f) {
                            rowPanels.add(currentPanelIndex to panels[currentPanelIndex])
                            accumulatedWidth += panels[currentPanelIndex].widthFraction
                            currentPanelIndex++
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPanels.forEach { (pIndex, pPanel) ->
                                CanvasPanelFrame(
                                    panel = pPanel,
                                    panelIndex = pIndex,
                                    isSelected = selectedIndex == pIndex,
                                    isHovered = hoveredIndex == pIndex,
                                    character = characters.firstOrNull { it.id == pPanel.characterId },
                                    background = backgrounds.firstOrNull { it.id == pPanel.backgroundId },
                                    onSelect = { onSelectPanel(pIndex) },
                                    onBoundsPositioned = { r -> onPanelBoundsPositioned(pIndex, r) },
                                    onHeightChanged = { h -> onPanelHeightChanged(pIndex, h) },
                                    onBubblePositionChanged = { x, y -> onBubblePositionChanged(pIndex, x, y) },
                                    onEditBubble = { onEditBubble(pIndex) },
                                    onRemoveBubble = { onRemoveBubble(pIndex) },
                                    onMoveUp = { onMovePanelUp(pIndex) },
                                    onMoveDown = { onMovePanelDown(pIndex) },
                                    onDelete = { onDeletePanel(pIndex) },
                                    modifier = Modifier.weight(pPanel.widthFraction.coerceIn(0.2f, 1.0f))
                                )
                            }
                        }
                    }
                }

                // Add Panel Quick Action Bar
                Button(
                    onClick = onAddPanel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2433)),
                    border = BorderStroke(1.dp, Color(0xFF333E56)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("canvas_add_panel_row_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = ManhuaCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ Ajouter une case au gabarit",
                        color = MangaPaperWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * An individual interactive Manga Panel Frame on the drafting canvas.
 * Supports:
 * - Variable height dragging handle
 * - Variable width display
 * - Interactive draggable speech bubble placeholder with live vector rendering
 * - Character / Background indication
 * - Sequential reordering controls
 */
@Composable
fun CanvasPanelFrame(
    panel: MangaPanel,
    panelIndex: Int,
    isSelected: Boolean,
    isHovered: Boolean,
    character: CharacterProfile?,
    background: BackgroundProfile?,
    onSelect: () -> Unit,
    onBoundsPositioned: (Rect) -> Unit,
    onHeightChanged: (Int) -> Unit,
    onBubblePositionChanged: (Float, Float) -> Unit,
    onEditBubble: () -> Unit,
    onRemoveBubble: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val hasImage = panel.imagePath != null && File(panel.imagePath).exists()
    val hasBubble = !panel.dialogueText.isNullOrBlank()

    // Height state with interactive drag-to-resize handle
    var localHeightDp by remember(panel.heightDp) { mutableIntStateOf(panel.heightDp) }

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                onBoundsPositioned(coords.boundsInRoot())
            }
            .clip(RoundedCornerShape(6.dp))
            .background(if (hasImage) Color.Black else Color(0xFF181C26))
            .border(
                width = if (isHovered) 3.dp else if (isSelected) 2.5.dp else 1.5.dp,
                color = if (isHovered) MangaCrimson else if (isSelected) ManhuaCyan else Color.Black,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onSelect() }
            .testTag("canvas_panel_frame_$panelIndex")
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(localHeightDp.dp)
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()

            // Artwork preview or Blueprint layout indicator
            if (hasImage) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(panel.imagePath!!))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Case $panelIndex",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Blueprint drawing area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CASE #${panelIndex + 1}",
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = panel.userPrompt.ifBlank { "Description de l'action..." },
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (character != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "★ ${character.name}",
                                color = QiGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Speech Bubble Placement Placeholder (Draggable directly within panel)
            if (hasBubble) {
                val bubbleX = (panel.bubbleNormalizedX * widthPx) - 60f
                val bubbleY = (panel.bubbleNormalizedY * heightPx) - 30f

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = bubbleX.coerceIn(8f, (widthPx - 140f).coerceAtLeast(8f)).roundToInt(),
                                y = bubbleY.coerceIn(24f, (heightPx - 60f).coerceAtLeast(24f)).roundToInt()
                            )
                        }
                        .pointerInput(panel.id) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                if (widthPx > 0f && heightPx > 0f) {
                                    val newNormX = ((panel.bubbleNormalizedX * widthPx + dragAmount.x) / widthPx)
                                        .coerceIn(0.1f, 0.9f)
                                    val newNormY = ((panel.bubbleNormalizedY * heightPx + dragAmount.y) / heightPx)
                                        .coerceIn(0.1f, 0.9f)
                                    onBubblePositionChanged(newNormX, newNormY)
                                }
                            }
                        }
                        .clickable { onEditBubble() }
                        .testTag("bubble_placeholder_$panelIndex")
                ) {
                    MangaSpeechBubble(
                        text = panel.dialogueText ?: "...",
                        type = panel.bubbleType,
                        fontId = panel.bubbleFont,
                        fontSizeSp = panel.bubbleFontSize,
                        tailDirectionId = panel.bubbleTailDirection,
                        bgColor = panel.bubbleBgColor,
                        textColor = panel.bubbleTextColor,
                        borderColor = panel.bubbleBorderColor
                    )
                }
            }

            // Top Badge Bar (Case Index & Dimensions)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, if (isSelected) ManhuaCyan else Color(0xFF333E56))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${panelIndex + 1}",
                            color = if (isSelected) ManhuaCyan else MangaPaperWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${(panel.widthFraction * 100).toInt()}% • ${localHeightDp}dp",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp
                        )
                    }
                }

                // Bubble Quick Edit / Add Badge
                if (hasBubble) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MangaCrimson),
                        modifier = Modifier.clickable { onEditBubble() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MangaCrimson, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Bulle", color = MangaCrimson, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bottom-Right Interactive Resize Drag Handle
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .align(Alignment.BottomEnd)
                    .pointerInput(panel.id) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val deltaDp = (dragAmount.y / density.density).roundToInt()
                                localHeightDp = (localHeightDp + deltaDp).coerceIn(90, 420)
                                onHeightChanged(localHeightDp)
                            }
                        )
                    }
                    .background(
                        color = if (isSelected) ManhuaCyan.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(topStart = 8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Height,
                    contentDescription = "Glisser pour redimensionner la hauteur",
                    tint = if (isSelected) ManhuaCyan else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Bottom controls bar for the currently selected panel on the canvas:
 * Variable width fraction, height slider/presets, and bubble controls.
 */
@Composable
fun SelectedPanelControlsBar(
    panel: MangaPanel,
    panelIndex: Int,
    totalPanels: Int,
    onWidthChanged: (Float) -> Unit,
    onHeightChanged: (Int) -> Unit,
    onAddBubble: () -> Unit,
    onEditBubble: () -> Unit,
    onDeletePanel: () -> Unit
) {
    Surface(
        color = Color(0xFF10141D),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, ManhuaCyan.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header & Width Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = ManhuaCyan,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "CASE #${panelIndex + 1}",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Largeur :",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                // Width fraction selector buttons: [100%], [67%], [50%], [33%]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val widthOptions = listOf(
                        1.0f to "100%",
                        0.67f to "67%",
                        0.5f to "50%",
                        0.33f to "33%"
                    )
                    widthOptions.forEach { (fraction, label) ->
                        val isSelected = kotlin.math.abs(panel.widthFraction - fraction) < 0.05f
                        Surface(
                            color = if (isSelected) ManhuaCyan.copy(alpha = 0.25f) else Color(0xFF1E2433),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (isSelected) ManhuaCyan else Color(0xFF333E56)),
                            modifier = Modifier.clickable { onWidthChanged(fraction) }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) ManhuaCyan else MangaPaperWhite,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Height Presets & Adjustment Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hauteur : ${panel.heightDp}dp",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                // Quick height presets: Bandeau (110), Standard (180), Impact (250), Splash (360)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val heightPresets = listOf(
                        110 to "Bandeau",
                        180 to "Standard",
                        250 to "Impact",
                        360 to "Splash"
                    )
                    heightPresets.forEach { (h, label) ->
                        val isCurrent = panel.heightDp == h
                        Surface(
                            color = if (isCurrent) QiGold.copy(alpha = 0.2f) else Color(0xFF1E2433),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (isCurrent) QiGold else Color(0xFF333E56)),
                            modifier = Modifier.clickable { onHeightChanged(h) }
                        ) {
                            Text(
                                text = label,
                                color = if (isCurrent) QiGold else MangaPaperWhite,
                                fontSize = 9.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: Edit/Add Bubble, Delete Panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (panel.dialogueText.isNullOrBlank()) {
                        Button(
                            onClick = onAddBubble,
                            colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Bulle Dialogue", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onEditBubble,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MangaCrimson),
                            border = BorderStroke(1.dp, MangaCrimson),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = MangaCrimson)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Modifier Bulle", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (totalPanels > 1) {
                    IconButton(
                        onClick = onDeletePanel,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer cette case",
                            tint = MangaCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bubble Placeholder Editor Dialog for customizing text, type, font, and tail direction.
 */
@Composable
fun BubblePlaceholderEditorDialog(
    panel: MangaPanel,
    panelNumber: Int,
    onDismiss: () -> Unit,
    onSave: (text: String, type: String, font: String, fontSize: Int, tailDirection: String) -> Unit,
    onDelete: () -> Unit
) {
    var text by remember { mutableStateOf(panel.dialogueText ?: "") }
    var selectedType by remember { mutableStateOf(panel.bubbleType) }
    var selectedFont by remember { mutableStateOf(panel.bubbleFont) }
    var fontSize by remember { mutableIntStateOf(panel.bubbleFontSize) }
    var tailDirection by remember { mutableStateOf(panel.bubbleTailDirection) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = InkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ChatBubble, contentDescription = null, tint = MangaCrimson, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bulle de la Case #$panelNumber", color = MangaPaperWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dialogue text input
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Texte du dialogue / cri / récite") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ManhuaCyan,
                        unfocusedBorderColor = InkBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Bubble Shape Type Chips
                Text("Forme de bulle :", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BubbleShapeType.values().forEach { shape ->
                        val isSelected = shape.id.equals(selectedType, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = shape.id },
                            label = { Text(shape.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MangaCrimson.copy(alpha = 0.3f),
                                selectedLabelColor = MangaCrimson,
                                containerColor = InkMidnight,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                // Tail Direction Chips
                Text("Orientation de la queue :", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TailDirection.values().forEach { tail ->
                        val isSelected = tail.id.equals(tailDirection, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { tailDirection = tail.id },
                            label = { Text(tail.displayName, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ManhuaCyan.copy(alpha = 0.3f),
                                selectedLabelColor = ManhuaCyan,
                                containerColor = InkMidnight,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                // Typography & Font Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Taille : ${fontSize}sp", color = TextSecondary, fontSize = 12.sp)
                    Slider(
                        value = fontSize.toFloat(),
                        onValueChange = { fontSize = it.roundToInt() },
                        valueRange = 10f..26f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = ManhuaCyan, activeTrackColor = ManhuaCyan),
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(text, selectedType, selectedFont, fontSize, tailDirection) },
                colors = ButtonDefaults.buttonColors(containerColor = ManhuaCyan)
            ) {
                Text("Valider", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Supprimer", color = MangaCrimson)
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onDismiss) {
                    Text("Annuler", color = TextMuted)
                }
            }
        }
    )
}

/**
 * Templates Selection Dialog for instant layout preset loading.
 */
@Composable
fun TemplatesSelectionDialog(
    templates: List<MangaPageLayoutTemplate>,
    onDismiss: () -> Unit,
    onSelectTemplate: (MangaPageLayoutTemplate) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = InkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = QiGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gabarits de Mise en Page Manga", color = MangaPaperWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                templates.forEach { tpl ->
                    Surface(
                        color = InkMidnight,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF2B3347)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTemplate(tpl) }
                            .testTag("template_${tpl.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(QiGold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(tpl.icon, contentDescription = null, tint = QiGold, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tpl.title, color = MangaPaperWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(tpl.subtitle, color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = TextMuted)
            }
        }
    )
}
