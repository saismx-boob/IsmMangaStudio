package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.CameraPerspective
import com.example.ai.ColorRenderingMode
import com.example.ai.LineWeightStyle
import com.example.ai.MangaArtStyle
import com.example.ai.MangaStyleSettingsHelper
import com.example.data.model.BackgroundProfile
import com.example.data.model.CharacterProfile
import com.example.ui.theme.InkBorder
import com.example.ui.theme.InkSurface
import com.example.ui.theme.InkSurfaceVariant
import com.example.ui.theme.MangaCrimson
import com.example.ui.theme.MangaPaperWhite
import com.example.ui.theme.ManhuaCyan
import com.example.ui.theme.QiGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ArtStyleSelectorRow(
    selectedStyle: MangaArtStyle,
    onStyleSelected: (MangaArtStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "STYLE ARTISTIQUE PRINCIPAL",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MangaArtStyle.entries.forEach { style ->
                val isSelected = style == selectedStyle
                Surface(
                    color = if (isSelected) MangaCrimson else InkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) MangaCrimson else InkBorder),
                    modifier = Modifier
                        .clickable { onStyleSelected(style) }
                        .testTag("style_chip_${style.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = style.displayName,
                            color = if (isSelected) Color.White else MangaPaperWhite,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorAndLineStyleSelector(
    selectedColor: ColorRenderingMode,
    onColorSelected: (ColorRenderingMode) -> Unit,
    selectedLine: LineWeightStyle,
    onLineSelected: (LineWeightStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "COLORISATION & ENCRAGE",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorRenderingMode.entries.forEach { mode ->
                val isSelected = mode == selectedColor
                Surface(
                    color = if (isSelected) ManhuaCyan.copy(alpha = 0.2f) else InkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) ManhuaCyan else InkBorder),
                    modifier = Modifier
                        .clickable { onColorSelected(mode) }
                        .testTag("color_chip_${mode.id}")
                ) {
                    Text(
                        text = mode.displayName,
                        color = if (isSelected) ManhuaCyan else MangaPaperWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LineWeightStyle.entries.forEach { line ->
                val isSelected = line == selectedLine
                Surface(
                    color = if (isSelected) QiGold.copy(alpha = 0.2f) else InkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) QiGold else InkBorder),
                    modifier = Modifier
                        .clickable { onLineSelected(line) }
                        .testTag("line_chip_${line.id}")
                ) {
                    Text(
                        text = line.displayName,
                        color = if (isSelected) QiGold else MangaPaperWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CameraAngleSelector(
    selectedCamera: CameraPerspective,
    onCameraSelected: (CameraPerspective) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "CADRAGE CAMÉRA",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CameraPerspective.entries.forEach { camera ->
                val isSelected = camera == selectedCamera
                Surface(
                    color = if (isSelected) Color(0xFF8B5CF6) else InkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF8B5CF6) else InkBorder),
                    modifier = Modifier
                        .clickable { onCameraSelected(camera) }
                        .testTag("camera_chip_${camera.id}")
                ) {
                    Text(
                        text = camera.displayName,
                        color = if (isSelected) Color.White else MangaPaperWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ConsistencyDNASummaryCard(
    activeCharacter: CharacterProfile?,
    activeBackground: BackgroundProfile?,
    artStyle: MangaArtStyle,
    onSelectCharacterClick: () -> Unit,
    onSelectBackgroundClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF161A28),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF272F47)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MangaCrimson, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MOTEUR DE COHÉRENCE VISUELLE ACTIF",
                    color = MangaCrimson,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = artStyle.displayName,
                        color = QiGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Character badge button
                Surface(
                    color = Color(0xFF1E2438),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (activeCharacter != null) Color(0xFF3B82F6) else InkBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectCharacterClick() }
                        .testTag("select_character_anchor")
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (activeCharacter != null) Color(0xFF60A5FA) else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Personnage:",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                                if (activeCharacter != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = activeCharacter.visualUid,
                                        color = QiGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = activeCharacter?.name ?: "Aucun (Générique)",
                                color = if (activeCharacter != null) Color.White else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            if (activeCharacter != null) {
                                val anchorText = if (activeCharacter.firstAppearanceImagePath != null) {
                                    "⭐ Ancre 1ère appar. active"
                                } else {
                                    "⏳ 1ère appar. en attente"
                                }
                                Text(
                                    text = anchorText,
                                    color = if (activeCharacter.firstAppearanceImagePath != null) Color(0xFF34D399) else Color(0xFFFBBF24),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Background badge button
                Surface(
                    color = Color(0xFF1E2438),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (activeBackground != null) Color(0xFF10B981) else InkBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectBackgroundClick() }
                        .testTag("select_background_anchor")
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terrain,
                            contentDescription = null,
                            tint = if (activeBackground != null) Color(0xFF34D399) else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Décor / Lieu:",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = activeBackground?.name ?: "Libre",
                                color = if (activeBackground != null) Color.White else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Real-time canvas preview of the pen stroke width with authentic manga inking aesthetics.
 */
@Composable
fun StrokePreviewCanvas(
    thickness: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF7F5EE)) // Authentic manga manuscript paper tint
            .border(1.dp, Color(0xFFDDD7C8), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Manga manuscript faint guideline
            drawLine(
                color = Color(0xFFE8E3D5),
                start = Offset(0f, height * 0.5f),
                end = Offset(width, height * 0.5f),
                strokeWidth = 1f
            )

            // Scaled stroke width (0.1 mm -> ~2.5 px, 1.5 mm -> ~24 px)
            val strokePx = (thickness * 12f).coerceIn(2.5f, 26f)

            // Curved dynamic manga line imitating G-pen / Maru-pen
            val path = Path().apply {
                moveTo(width * 0.08f, height * 0.75f)
                cubicTo(
                    width * 0.30f, height * 0.18f,
                    width * 0.62f, height * 0.85f,
                    width * 0.92f, height * 0.30f
                )
            }

            drawPath(
                path = path,
                color = Color(0xFF111111), // Pure sumi ink
                style = Stroke(
                    width = strokePx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Pen contact pressure dot
            drawCircle(
                color = Color(0xFF111111),
                radius = (strokePx * 0.6f).coerceAtLeast(2f),
                center = Offset(width * 0.08f, height * 0.75f)
            )
        }

        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color(0xFF111111), CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${String.format(java.util.Locale.US, "%.1f", thickness)} mm",
                color = Color(0xFF444444),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Real-time canvas preview of manga tonal contrast (halftone screentones, dark ink fills, high-key whites).
 */
@Composable
fun ContrastPreviewCanvas(
    contrast: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF7F5EE))
            .border(1.dp, Color(0xFFDDD7C8), RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Deep solid black ink block on the left (expands with contrast)
            val blackWidth = w * (0.18f + contrast * 0.16f)
            drawRect(
                color = Color(0xFF0D0D0D),
                topLeft = Offset(0f, 0f),
                size = Size(blackWidth, h)
            )

            // 2. Middle zone: Manga halftone screentone dots
            val halftoneStart = blackWidth
            val halftoneEnd = w * 0.80f
            val dotSpacing = 8.5f
            val dotRadius = (contrast * 3.4f).coerceIn(0.8f, 4.0f)

            var x = halftoneStart + 4f
            while (x < halftoneEnd) {
                var y = 4f
                while (y < h) {
                    drawCircle(
                        color = Color(0xFF1F1F1F),
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                    y += dotSpacing
                }
                x += dotSpacing
            }

            // 3. Right zone: Pure stark white highlight
            drawRect(
                color = Color(0xFFFFFFFF),
                topLeft = Offset(halftoneEnd, 0f),
                size = Size(w - halftoneEnd, h)
            )

            // Highlight border
            drawRect(
                color = Color(0xFFE2DDD0),
                topLeft = Offset(halftoneEnd, 0f),
                size = Size(w - halftoneEnd, h),
                style = Stroke(width = 1f)
            )
        }

        Text(
            text = "${(contrast * 100).toInt()}%",
            color = if (contrast > 0.65f) Color(0xFFF1F5F9) else Color(0xFF1E293B),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 2.dp, bottom = 2.dp)
        )
    }
}

/**
 * Interactive Slider Control for Line Thickness (Épaisseur du trait)
 */
@Composable
fun StrokeWeightControl(
    strokeWeight: Float,
    onStrokeWeightChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Brush,
                contentDescription = null,
                tint = ManhuaCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ÉPAISSEUR DU TRAIT",
                style = MaterialTheme.typography.labelSmall,
                color = ManhuaCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${String.format(java.util.Locale.US, "%.1f", strokeWeight)} mm",
                color = MangaPaperWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = MangaStyleSettingsHelper.getStrokeDescription(strokeWeight),
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Interactive Slider
            Slider(
                value = strokeWeight,
                onValueChange = onStrokeWeightChange,
                valueRange = 0.1f..1.5f,
                steps = 13,
                colors = SliderDefaults.colors(
                    thumbColor = ManhuaCyan,
                    activeTrackColor = ManhuaCyan,
                    inactiveTrackColor = InkBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("stroke_weight_slider")
            )

            // Live Preview Canvas
            StrokePreviewCanvas(
                thickness = strokeWeight,
                modifier = Modifier
                    .size(width = 86.dp, height = 44.dp)
                    .testTag("stroke_preview_canvas")
            )
        }

        // Quick Preset Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MangaStyleSettingsHelper.strokePresets.forEach { preset ->
                val isSelected = kotlin.math.abs(strokeWeight - preset.value) < 0.08f
                Surface(
                    color = if (isSelected) ManhuaCyan.copy(alpha = 0.25f) else InkSurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (isSelected) ManhuaCyan else InkBorder),
                    modifier = Modifier
                        .clickable { onStrokeWeightChange(preset.value) }
                        .testTag("stroke_preset_${preset.label.lowercase()}")
                ) {
                    Text(
                        text = "${preset.label} (${preset.mmDisplay})",
                        color = if (isSelected) ManhuaCyan else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Interactive Slider Control for Tonal Contrast (Contraste)
 */
@Composable
fun ContrastControl(
    contrastLevel: Float,
    onContrastChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Contrast,
                contentDescription = null,
                tint = QiGold,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "CONTRASTE & TRAMES (HALFTONE)",
                style = MaterialTheme.typography.labelSmall,
                color = QiGold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${(contrastLevel * 100).toInt()}%",
                color = MangaPaperWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = MangaStyleSettingsHelper.getContrastDescription(contrastLevel),
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Interactive Slider
            Slider(
                value = contrastLevel,
                onValueChange = onContrastChange,
                valueRange = 0.0f..1.0f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = QiGold,
                    activeTrackColor = QiGold,
                    inactiveTrackColor = InkBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("contrast_slider")
            )

            // Live Preview Canvas
            ContrastPreviewCanvas(
                contrast = contrastLevel,
                modifier = Modifier
                    .size(width = 86.dp, height = 44.dp)
                    .testTag("contrast_preview_canvas")
            )
        }

        // Quick Preset Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MangaStyleSettingsHelper.contrastPresets.forEach { preset ->
                val isSelected = kotlin.math.abs(contrastLevel - preset.value) < 0.08f
                Surface(
                    color = if (isSelected) QiGold.copy(alpha = 0.25f) else InkSurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (isSelected) QiGold else InkBorder),
                    modifier = Modifier
                        .clickable { onContrastChange(preset.value) }
                        .testTag("contrast_preset_${preset.label.lowercase()}")
                ) {
                    Text(
                        text = "${preset.label} (${preset.percentDisplay})",
                        color = if (isSelected) QiGold else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Visual Selection Cards for the 4 Predefined Manga Art Styles:
 * - Shōnen
 * - Seinen
 * - Shōjo
 * - Noir et Blanc classique
 */
@Composable
fun MangaArtStylePresetsSelector(
    selectedStyle: MangaArtStyle,
    onStyleSelected: (MangaArtStyle, Boolean) -> Unit,
    onOpenStudioDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAdditionalStyles by remember { mutableStateOf(false) }

    val coreStyles = listOf(
        MangaArtStyle.MANGA_SHONEN,
        MangaArtStyle.MANGA_SEINEN,
        MangaArtStyle.MANGA_SHOJO,
        MangaArtStyle.CLASSIC_BLACK_AND_WHITE
    )

    val additionalStyles = listOf(
        MangaArtStyle.MANHUA_XIANXIA,
        MangaArtStyle.MANHWA_WEBTOON,
        MangaArtStyle.WESTERN_COMIC,
        MangaArtStyle.CHIBI_ANIME
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "STYLES ARTISTIQUES PRÉDÉFINIS",
                style = MaterialTheme.typography.labelSmall,
                color = MangaCrimson,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                color = MangaCrimson.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, MangaCrimson.copy(alpha = 0.4f)),
                modifier = Modifier
                    .clickable { onOpenStudioDialog() }
                    .testTag("open_style_studio_dialog_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MangaCrimson,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Atelier Encrage",
                        color = MangaCrimson,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2x2 Grid of the 4 prominent styles
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MangaArtStyleCard(
                    style = coreStyles[0], // Shonen
                    isSelected = selectedStyle == coreStyles[0],
                    accentColor = MangaCrimson,
                    onClick = { onStyleSelected(coreStyles[0], true) },
                    modifier = Modifier.weight(1f)
                )
                MangaArtStyleCard(
                    style = coreStyles[1], // Seinen
                    isSelected = selectedStyle == coreStyles[1],
                    accentColor = Color(0xFF6366F1), // Indigo
                    onClick = { onStyleSelected(coreStyles[1], true) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MangaArtStyleCard(
                    style = coreStyles[2], // Shojo
                    isSelected = selectedStyle == coreStyles[2],
                    accentColor = Color(0xFFEC4899), // Pink
                    onClick = { onStyleSelected(coreStyles[2], true) },
                    modifier = Modifier.weight(1f)
                )
                MangaArtStyleCard(
                    style = coreStyles[3], // Classic B&W
                    isSelected = selectedStyle == coreStyles[3],
                    accentColor = Color(0xFFCBD5E1), // Monochrome silver
                    onClick = { onStyleSelected(coreStyles[3], true) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Toggle for additional genres (Xianxia, Webtoon, Comic, Chibi)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAdditionalStyles = !showAdditionalStyles }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (showAdditionalStyles) "Masquer les styles secondaires" else "Autres styles (Webtoon, Xianxia, Comics...)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = if (showAdditionalStyles) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }

        AnimatedVisibility(visible = showAdditionalStyles) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                additionalStyles.forEach { extra ->
                    val isSel = extra == selectedStyle
                    Surface(
                        color = if (isSel) Color(0xFF1E293B) else InkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isSel) ManhuaCyan else InkBorder),
                        modifier = Modifier
                            .clickable { onStyleSelected(extra, true) }
                            .testTag("extra_style_${extra.id}")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = extra.displayName,
                                color = if (isSel) ManhuaCyan else MangaPaperWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = extra.subtitle,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Card for an artistic style preset.
 */
@Composable
private fun MangaArtStyleCard(
    style: MangaArtStyle,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) accentColor.copy(alpha = 0.16f) else InkSurfaceVariant,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentColor else InkBorder
        ),
        modifier = modifier
            .clickable { onClick() }
            .testTag("style_preset_card_${style.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Badge color indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accentColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = style.displayName,
                    color = if (isSelected) accentColor else MangaPaperWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sélectionné",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle / characteristics
            Text(
                text = style.subtitle,
                color = if (isSelected) MangaPaperWhite else TextSecondary,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Recommended defaults pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", style.recommendedStrokeWeight)} mm",
                        color = ManhuaCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${(style.recommendedContrast * 100).toInt()}% N&B",
                        color = QiGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Full Dialog: Atelier de Styles & Encrage Manga
 * Allows inspecting the 4 styles in high detail, tweaking stroke thickness and contrast,
 * and previewing how Gemini AI will format the prompt.
 */
@Composable
fun ArtStyleStudioDialog(
    currentStyle: MangaArtStyle,
    currentStrokeWeight: Float,
    currentContrast: Float,
    onApplySettings: (MangaArtStyle, Float, Float) -> Unit,
    onDismiss: () -> Unit
) {
    var tempStyle by remember { mutableStateOf(currentStyle) }
    var tempStroke by remember { mutableFloatStateOf(currentStrokeWeight) }
    var tempContrast by remember { mutableFloatStateOf(currentContrast) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = InkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, InkBorder),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
                .testTag("art_style_studio_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MangaCrimson,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ATELIER DE STYLES & ENCRAGE",
                            color = MangaPaperWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Préréglages d'auteurs, épaisseur du trait & contraste",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // The 4 Predefined Styles Selector
                Text(
                    text = "CHOISIR UN STYLE PRÉDÉFINI",
                    color = MangaCrimson,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val presets = listOf(
                    MangaArtStyle.MANGA_SHONEN to MangaCrimson,
                    MangaArtStyle.MANGA_SEINEN to Color(0xFF6366F1),
                    MangaArtStyle.MANGA_SHOJO to Color(0xFFEC4899),
                    MangaArtStyle.CLASSIC_BLACK_AND_WHITE to Color(0xFFCBD5E1)
                )

                presets.forEach { (style, color) ->
                    val isSelected = tempStyle == style
                    Surface(
                        color = if (isSelected) color.copy(alpha = 0.18f) else InkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) color else InkBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                tempStyle = style
                                tempStroke = style.recommendedStrokeWeight
                                tempContrast = style.recommendedContrast
                            }
                            .testTag("dialog_preset_${style.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = style.displayName,
                                        color = if (isSelected) color else MangaPaperWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = style.penTool,
                                            color = TextSecondary,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = style.subtitle,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Line thickness adjustment with preview
                Surface(
                    color = Color(0xFF171B29),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, InkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        StrokeWeightControl(
                            strokeWeight = tempStroke,
                            onStrokeWeightChange = { tempStroke = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Contrast adjustment with preview
                Surface(
                    color = Color(0xFF171B29),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, InkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        ContrastControl(
                            contrastLevel = tempContrast,
                            onContrastChange = { tempContrast = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prompt Consistency Preview Banner
                Surface(
                    color = Color(0xFF0F121C),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF232A3E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = QiGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Paramétrage Encrage Actif pour Gemini :",
                                color = QiGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${tempStyle.displayName} • ${MangaStyleSettingsHelper.getStrokeDescription(tempStroke)} • ${(tempContrast * 100).toInt()}% Contraste",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Reset & Apply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            tempStroke = tempStyle.recommendedStrokeWeight
                            tempContrast = tempStyle.recommendedContrast
                        },
                        border = BorderStroke(1.dp, InkBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Valeurs Recommandées", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onApplySettings(tempStyle, tempStroke, tempContrast)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_style_settings_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Appliquer au Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * All-in-one artistic styles and adjustable ink settings section.
 * Directly plugs into the Studio editor with preset cards, stroke weight slider,
 * contrast slider, and a button to open the full dialog.
 */
@Composable
fun MangaArtAndInkControlSection(
    selectedStyle: MangaArtStyle,
    strokeWeight: Float,
    contrastLevel: Float,
    onStyleSelected: (MangaArtStyle, Boolean) -> Unit,
    onStrokeWeightChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStudioDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. The 4 Predefined Artistic Styles (Shonen, Seinen, Shojo, Noir & Blanc classique)
        MangaArtStylePresetsSelector(
            selectedStyle = selectedStyle,
            onStyleSelected = onStyleSelected,
            onOpenStudioDialog = { showStudioDialog = true }
        )

        // 2. Stroke Thickness Slider & Live Preview
        StrokeWeightControl(
            strokeWeight = strokeWeight,
            onStrokeWeightChange = onStrokeWeightChange
        )

        // 3. Contrast Slider & Live Halftone Preview
        ContrastControl(
            contrastLevel = contrastLevel,
            onContrastChange = onContrastChange
        )
    }

    if (showStudioDialog) {
        ArtStyleStudioDialog(
            currentStyle = selectedStyle,
            currentStrokeWeight = strokeWeight,
            currentContrast = contrastLevel,
            onApplySettings = { newStyle, newStroke, newContrast ->
                onStyleSelected(newStyle, false)
                onStrokeWeightChange(newStroke)
                onContrastChange(newContrast)
            },
            onDismiss = { showStudioDialog = false }
        )
    }
}

