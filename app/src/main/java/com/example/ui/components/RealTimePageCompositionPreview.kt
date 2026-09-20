package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
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

/**
 * Real-time Manga Page Preview component.
 * Displays the entire manga page layout in real time while the user edits each individual panel.
 * Shows where the currently active editing panel is located in the global composition,
 * rendered artwork, dialogue bubbles preview, and live sync status.
 */
@Composable
fun RealTimePageCompositionPreview(
    page: MangaPage?,
    panels: List<MangaPanel>,
    currentEditingIndex: Int,
    isGenerating: Boolean,
    characters: List<CharacterProfile>,
    backgrounds: List<BackgroundProfile>,
    onSelectPanelToEdit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    var showFullscreenDialog by remember { mutableStateOf(false) }

    val pageNumber = page?.pageNumber ?: 1
    val layoutPreset = GridPageLayout.values().firstOrNull { it.id == page?.layoutType }
        ?: if (panels.size >= 4) GridPageLayout.FOUR_YONKOMA
        else if (panels.size == 3) GridPageLayout.THREE_MANGA
        else if (panels.size == 1) GridPageLayout.SPLASH_FULL
        else GridPageLayout.TWO_VERTICAL

    Card(
        colors = CardDefaults.cardColors(containerColor = InkSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ManhuaCyan.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("realtime_page_preview_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpanded = !isExpanded }
                ) {
                    // Pulse or active indicator
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isGenerating) QiGold else ManhuaCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "COMPOSITION GLOBALE",
                                style = MaterialTheme.typography.labelMedium,
                                color = ManhuaCyan,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = InkSurfaceVariant,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, InkBorder)
                            ) {
                                Text(
                                    text = "LIVE",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Page #$pageNumber • ${panels.size} case(s) • Édition : Case #${currentEditingIndex + 1}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Fullscreen preview button
                    IconButton(
                        onClick = { showFullscreenDialog = true },
                        modifier = Modifier.size(32.dp).testTag("fullscreen_preview_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Plein écran",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Expand/Collapse toggle button
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp).testTag("toggle_preview_expand_btn")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Réduire" else "Agrandir",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    // Guide chip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Touchez une case pour basculer l'éditeur instantanément",
                            color = TextMuted,
                            fontSize = 10.5.sp
                        )
                        Surface(
                            color = MangaCrimson.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.8.dp, MangaCrimson.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MangaCrimson, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Case #${currentEditingIndex + 1} active",
                                    color = MangaCrimson,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Miniature Manga Page Sheet Container (Aspect Ratio ~ 1 : 1.414 authentic manga B4)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF090B10))
                            .border(BorderStroke(1.5.dp, Color(0xFF1E2436)), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        MangaPageSheetLayout(
                            layout = layoutPreset,
                            panels = panels,
                            currentEditingIndex = currentEditingIndex,
                            characters = characters,
                            backgrounds = backgrounds,
                            onSelectPanel = onSelectPanelToEdit
                        )
                    }

                    // Panel thumbnails horizontal navigation bar
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        panels.forEachIndexed { idx, p ->
                            val isSelected = idx == currentEditingIndex
                            val hasArt = p.imagePath != null && File(p.imagePath).exists()
                            Surface(
                                color = if (isSelected) MangaCrimson.copy(alpha = 0.2f) else InkSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MangaCrimson else InkBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectPanelToEdit(idx) }
                                    .testTag("preview_quick_tab_$idx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (hasArt) Icons.Default.CheckCircle else Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = if (hasArt) Color(0xFF34D399) else if (isSelected) MangaCrimson else TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Case ${idx + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                        color = if (isSelected) MangaPaperWhite else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Fullscreen Detailed Preview Dialog
    if (showFullscreenDialog) {
        Dialog(
            onDismissRequest = { showFullscreenDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Fullscreen Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = MangaCrimson, shape = RoundedCornerShape(4.dp)) {
                                    Text(
                                        text = "APERÇU GLOBAL B4",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Planche #${pageNumber} (${panels.size} cases)",
                                    color = MangaPaperWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Text(
                                text = "Composition en taille réelle avant publication",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = { showFullscreenDialog = false },
                            modifier = Modifier
                                .size(36.dp)
                                .background(InkSurfaceVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }

                    // Large Sheet View
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F121A))
                            .border(BorderStroke(2.dp, InkBorder), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        MangaPageSheetLayout(
                            layout = layoutPreset,
                            panels = panels,
                            currentEditingIndex = currentEditingIndex,
                            characters = characters,
                            backgrounds = backgrounds,
                            onSelectPanel = {
                                onSelectPanelToEdit(it)
                                showFullscreenDialog = false
                            },
                            isLarge = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Touchez n'importe quelle case pour la charger dans l'éditeur de création",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Renders the manga slots according to the selected GridPageLayout.
 */
@Composable
private fun MangaPageSheetLayout(
    layout: GridPageLayout,
    panels: List<MangaPanel>,
    currentEditingIndex: Int,
    characters: List<CharacterProfile>,
    backgrounds: List<BackgroundProfile>,
    onSelectPanel: (Int) -> Unit,
    isLarge: Boolean = false
) {
    if (panels.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aucune case définie pour cette planche.\nAjoutez une case pour commencer la composition !",
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    when (layout) {
        GridPageLayout.SPLASH_FULL -> {
            val panel = panels.first()
            MiniGridSlot(
                panel = panel,
                slotIndex = 0,
                isEditing = currentEditingIndex == 0,
                character = characters.firstOrNull { it.id == panel.characterId },
                background = backgrounds.firstOrNull { it.id == panel.backgroundId },
                onClick = { onSelectPanel(0) },
                isLarge = isLarge,
                modifier = Modifier.fillMaxSize()
            )
        }

        GridPageLayout.TWO_VERTICAL -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 0..1) {
                    val panel = panels.getOrNull(i)
                    if (panel != null) {
                        MiniGridSlot(
                            panel = panel,
                            slotIndex = i,
                            isEditing = currentEditingIndex == i,
                            character = characters.firstOrNull { it.id == panel.characterId },
                            background = backgrounds.firstOrNull { it.id == panel.backgroundId },
                            onClick = { onSelectPanel(i) },
                            isLarge = isLarge,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    } else {
                        EmptyMiniSlot(i, Modifier.weight(1f).fillMaxWidth())
                    }
                }
            }
        }

        GridPageLayout.THREE_MANGA -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Top Hero panel (60% weight)
                val topPanel = panels.getOrNull(0)
                if (topPanel != null) {
                    MiniGridSlot(
                        panel = topPanel,
                        slotIndex = 0,
                        isEditing = currentEditingIndex == 0,
                        character = characters.firstOrNull { it.id == topPanel.characterId },
                        background = backgrounds.firstOrNull { it.id == topPanel.backgroundId },
                        onClick = { onSelectPanel(0) },
                        isLarge = isLarge,
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxWidth()
                    )
                } else {
                    EmptyMiniSlot(0, Modifier.weight(1.3f).fillMaxWidth())
                }

                // Bottom 2 panels side-by-side (40% weight)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 1..2) {
                        val subPanel = panels.getOrNull(i)
                        if (subPanel != null) {
                            MiniGridSlot(
                                panel = subPanel,
                                slotIndex = i,
                                isEditing = currentEditingIndex == i,
                                character = characters.firstOrNull { it.id == subPanel.characterId },
                                background = backgrounds.firstOrNull { it.id == subPanel.backgroundId },
                                onClick = { onSelectPanel(i) },
                                isLarge = isLarge,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        } else {
                            EmptyMiniSlot(i, Modifier.weight(1f).fillMaxHeight())
                        }
                    }
                }
            }
        }

        GridPageLayout.FOUR_YONKOMA -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                for (i in 0..3) {
                    val panel = panels.getOrNull(i)
                    if (panel != null) {
                        MiniGridSlot(
                            panel = panel,
                            slotIndex = i,
                            isEditing = currentEditingIndex == i,
                            character = characters.firstOrNull { it.id == panel.characterId },
                            background = backgrounds.firstOrNull { it.id == panel.backgroundId },
                            onClick = { onSelectPanel(i) },
                            isLarge = isLarge,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    } else {
                        EmptyMiniSlot(i, Modifier.weight(1f).fillMaxWidth())
                    }
                }
            }
        }
    }
}

/**
 * Individual miniature slot inside the Real-Time Page Preview.
 * Highlights whether this slot is currently selected for editing in the studio below.
 */
@Composable
private fun MiniGridSlot(
    panel: MangaPanel,
    slotIndex: Int,
    isEditing: Boolean,
    character: CharacterProfile?,
    background: BackgroundProfile?,
    onClick: () -> Unit,
    isLarge: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasImage = panel.imagePath != null && File(panel.imagePath).exists()

    val borderColor = when {
        isEditing -> MangaCrimson
        hasImage -> ManhuaCyan.copy(alpha = 0.6f)
        else -> InkBorder
    }

    val borderWidth = if (isEditing) 2.dp else 1.dp

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF11141E))
            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .testTag("mini_slot_$slotIndex")
    ) {
        // Rendered Manga Art
        if (hasImage) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(panel.imagePath!!))
                    .crossfade(true)
                    .build(),
                contentDescription = "Miniature case ${slotIndex + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Semi-transparent gradient to emphasize text badges and speech bubble cues
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent, Color.Black.copy(alpha = 0.5f))
                        )
                    )
            )
        } else {
            // Placeholder wireframe drawing when artwork is not yet generated
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF141724))
                    .drawBehind {
                        // Drawing subtle Japanese manga screen guides
                        drawLine(
                            color = Color(0xFF22283A),
                            start = Offset(0f, size.height * 0.5f),
                            end = Offset(size.width, size.height * 0.5f),
                            strokeWidth = 1f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = TextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(if (isLarge) 24.dp else 16.dp)
                    )
                    if (isLarge) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = panel.userPrompt.take(28).ifBlank { "Case #${slotIndex + 1}" },
                            color = TextMuted,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Miniature Speech Bubble Indicator (shows where dialogue is placed)
        if (!panel.dialogueText.isNullOrBlank()) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val posX = (panel.bubbleNormalizedX * maxWidth.value).dp
                val posY = (panel.bubbleNormalizedY * maxHeight.value).dp

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(if (isLarge) 6.dp else 3.dp),
                    border = BorderStroke(0.8.dp, Color.Black),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(
                            start = posX.coerceIn(4.dp, (maxWidth.value - 60).coerceAtLeast(0f).dp),
                            top = posY.coerceIn(4.dp, (maxHeight.value - 24).coerceAtLeast(0f).dp)
                        )
                ) {
                    Text(
                        text = if (isLarge) panel.dialogueText.take(20) else "💬",
                        color = Color.Black,
                        fontSize = if (isLarge) 8.sp else 6.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // Top Left: Slot badge & Editing Status
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (isEditing) MangaCrimson else Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(3.dp),
                border = BorderStroke(0.5.dp, if (isEditing) MangaCrimson else InkBorder)
            ) {
                Text(
                    text = "#${slotIndex + 1}",
                    color = Color.White,
                    fontSize = if (isLarge) 10.sp else 8.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }

            if (isEditing) {
                Spacer(modifier = Modifier.width(3.dp))
                Surface(
                    color = MangaCrimson.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text(
                        text = "ÉDITION",
                        color = Color.White,
                        fontSize = if (isLarge) 8.sp else 6.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // Bottom Right: Character tag if assigned
        if (character != null) {
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(3.dp),
                border = BorderStroke(0.5.dp, MangaCrimson.copy(alpha = 0.5f)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            ) {
                Text(
                    text = character.name.take(12),
                    color = Color.White,
                    fontSize = if (isLarge) 9.sp else 7.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyMiniSlot(slotIndex: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0C0E14))
            .border(BorderStroke(1.dp, Color(0xFF1E2436)), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+ Case ${slotIndex + 1}",
            color = TextMuted.copy(alpha = 0.5f),
            fontSize = 9.sp
        )
    }
}
