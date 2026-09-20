package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MangaPanel
import com.example.ui.theme.InkBorder
import com.example.ui.theme.InkMidnight
import com.example.ui.theme.MangaCrimson
import com.example.ui.theme.MangaPaperWhite
import com.example.ui.theme.QiGold
import java.io.File

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.example.ui.theme.BubbleFontOption
import kotlin.math.roundToInt

@Composable
fun MangaPanelCanvas(
    panel: MangaPanel,
    panelNumber: Int,
    characterName: String?,
    backgroundName: String?,
    isGenerating: Boolean,
    isFirstAppearanceAnchor: Boolean = false,
    onSetAsFirstAppearanceAnchor: (() -> Unit)? = null,
    onRegenerate: () -> Unit,
    onEditBubble: () -> Unit,
    onBubblePositionChanged: ((Float, Float) -> Unit)? = null,
    onExport: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val hasImage = panel.imagePath != null && File(panel.imagePath).exists()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(InkMidnight)
            .border(2.dp, InkBorder, RoundedCornerShape(8.dp))
            .testTag("manga_panel_$panelNumber")
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.15f)
        ) {
            val canvasWidthPx = constraints.maxWidth.toFloat()
            val canvasHeightPx = constraints.maxHeight.toFloat()

            if (hasImage) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(panel.imagePath!!))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Case de manga $panelNumber",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Placeholder canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF161924)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                color = MangaCrimson,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Encrage de la case par l'IA...",
                                color = MangaPaperWhite,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MangaCrimson,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Case #$panelNumber en attente de génération",
                                color = MangaPaperWhite,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = panel.userPrompt.ifBlank { "Cliquez sur 'Générer la case' pour créer l'illustration" },
                                color = Color(0xFF94A3B8),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Dialogue & Vector Speech Bubble Overlay with Drag-to-Reposition
            if (!panel.dialogueText.isNullOrBlank()) {
                val bubbleX = (panel.bubbleNormalizedX.coerceIn(0.1f, 0.9f) * canvasWidthPx) - 100f
                val bubbleY = (panel.bubbleNormalizedY.coerceIn(0.1f, 0.9f) * canvasHeightPx) - 40f

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = bubbleX.coerceIn(8f, (canvasWidthPx - 180f).coerceAtLeast(8f)).roundToInt(),
                                y = bubbleY.coerceIn(40f, (canvasHeightPx - 80f).coerceAtLeast(40f)).roundToInt()
                            )
                        }
                        .pointerInput(panel.id) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                if (canvasWidthPx > 0f && canvasHeightPx > 0f && onBubblePositionChanged != null) {
                                    val newNormX = ((panel.bubbleNormalizedX * canvasWidthPx + dragAmount.x) / canvasWidthPx)
                                        .coerceIn(0.08f, 0.92f)
                                    val newNormY = ((panel.bubbleNormalizedY * canvasHeightPx + dragAmount.y) / canvasHeightPx)
                                        .coerceIn(0.12f, 0.88f)
                                    onBubblePositionChanged(newNormX, newNormY)
                                }
                            }
                        }
                        .clickable { onEditBubble() }
                        .testTag("speech_bubble_$panelNumber")
                ) {
                    MangaSpeechBubble(
                        text = panel.dialogueText,
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
        }

        // Top Metadata Overlay Bar: Panel # & Character/Setting indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Case Index Badge
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MangaCrimson)
            ) {
                Text(
                    text = "CASE $panelNumber",
                    color = MangaCrimson,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            if (!characterName.isNullOrBlank()) {
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "★ $characterName",
                        color = MangaPaperWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            if (isFirstAppearanceAnchor) {
                Surface(
                    color = Color(0xFF065F46).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Text(
                        text = "⭐ 1ÈRE APPARITION",
                        color = Color(0xFF6EE7B7),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else if (hasImage && panel.characterId != null && onSetAsFirstAppearanceAnchor != null) {
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, QiGold.copy(alpha = 0.6f)),
                    modifier = Modifier.clickable { onSetAsFirstAppearanceAnchor() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = QiGold,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Ancrer 1ère appar.",
                            color = QiGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Edit Bubble button
            IconButton(
                onClick = onEditBubble,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                    .testTag("edit_bubble_panel_$panelNumber")
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = "Éditer la bulle",
                    tint = MangaCrimson,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Export High-Res PNG button
            if (onExport != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onExport,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                        .testTag("export_panel_$panelNumber")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Exporter la case en HD",
                        tint = QiGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Action buttons
            IconButton(
                onClick = onRegenerate,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                    .testTag("regenerate_panel_$panelNumber")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Régénérer",
                    tint = MangaPaperWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * MangaSpeechBubble: Vector-powered manga speech bubble wrapper.
 */
@Composable
fun MangaSpeechBubble(
    text: String,
    type: String,
    fontId: String = "COMIC_NEUE",
    fontSizeSp: Int = 14,
    tailDirectionId: String = "BOTTOM_LEFT",
    bgColor: Long = 0xFFFFFFFF,
    textColor: Long = 0xFF000000,
    borderColor: Long = 0xFF000000,
    modifier: Modifier = Modifier
) {
    val shapeType = BubbleShapeType.fromId(type)
    val fontOption = BubbleFontOption.fromId(fontId)
    val tailDir = TailDirection.fromId(tailDirectionId)

    VectorSpeechBubble(
        text = text,
        shapeType = shapeType,
        tailDirection = tailDir,
        fontOption = fontOption,
        fontSizeSp = fontSizeSp,
        backgroundColor = Color(bgColor),
        textColor = Color(textColor),
        borderColor = Color(borderColor),
        modifier = modifier
    )
}
