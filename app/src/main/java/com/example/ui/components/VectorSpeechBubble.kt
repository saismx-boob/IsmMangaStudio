package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BubbleFontOption
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class BubbleShapeType(
    val id: String,
    val displayName: String,
    val description: String
) {
    SPEECH(
        id = "SPEECH",
        displayName = "Classique Ovale",
        description = "Bulle de dialogue manga arrondie avec pointe"
    ),
    SHOUT(
        id = "SHOUT",
        displayName = "Cri / Explosion",
        description = "Étoile dentée shonen pour cris et coups"
    ),
    THOUGHT(
        id = "THOUGHT",
        displayName = "Pensée / Nuage",
        description = "Nuage à festons avec bulles descendantes"
    ),
    WHISPER(
        id = "WHISPER",
        displayName = "Chuchotement",
        description = "Contour en pointillés pour voix basse"
    ),
    NARRATION(
        id = "NARRATION",
        displayName = "Récitatif / Cartouche",
        description = "Cadre rectangulaire pour narrateur"
    ),
    ONOMATOPOEIA(
        id = "ONOMATOPOEIA",
        displayName = "Onomatopée Impact",
        description = "Bannière dynamique inclinée avec ombre franche"
    );

    companion object {
        fun fromId(id: String?): BubbleShapeType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SPEECH
        }
    }
}

enum class TailDirection(
    val id: String,
    val displayName: String
) {
    BOTTOM_LEFT(id = "BOTTOM_LEFT", displayName = "Bas Gauche"),
    BOTTOM_RIGHT(id = "BOTTOM_RIGHT", displayName = "Bas Droite"),
    TOP_LEFT(id = "TOP_LEFT", displayName = "Haut Gauche"),
    TOP_RIGHT(id = "TOP_RIGHT", displayName = "Haut Droite"),
    NONE(id = "NONE", displayName = "Sans pointe");

    companion object {
        fun fromId(id: String?): TailDirection {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: BOTTOM_LEFT
        }
    }
}

/**
 * VectorSpeechBubble: A 100% vector-rendered comic/manga speech bubble component.
 * Dynamically constructs vector paths based on text bounds, supporting custom shapes,
 * directional tails, stroke weights, and typography.
 */
@Composable
fun VectorSpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    shapeType: BubbleShapeType = BubbleShapeType.SPEECH,
    tailDirection: TailDirection = TailDirection.BOTTOM_LEFT,
    fontOption: BubbleFontOption = BubbleFontOption.COMIC_NEUE,
    fontSizeSp: Int = 14,
    backgroundColor: Color = Color.White,
    textColor: Color = Color.Black,
    borderColor: Color = Color.Black,
    borderWidthDp: Dp = 2.dp,
    textAlign: TextAlign = TextAlign.Center
) {
    val tailLength = if (tailDirection == TailDirection.NONE) 0f else 22f
    val strokeWidthPx = borderWidthDp.value * 2.5f

    // Padding inside the bubble depending on the shape
    val horizontalPadding = when (shapeType) {
        BubbleShapeType.SHOUT -> 26.dp
        BubbleShapeType.THOUGHT -> 22.dp
        BubbleShapeType.ONOMATOPOEIA -> 20.dp
        BubbleShapeType.NARRATION -> 14.dp
        else -> 18.dp
    }
    val verticalPadding = when (shapeType) {
        BubbleShapeType.SHOUT -> 18.dp
        BubbleShapeType.THOUGHT -> 16.dp
        BubbleShapeType.ONOMATOPOEIA -> 12.dp
        BubbleShapeType.NARRATION -> 10.dp
        else -> 12.dp
    }

    // Extra padding to avoid text overlapping the tail
    val topExtra = if (tailDirection == TailDirection.TOP_LEFT || tailDirection == TailDirection.TOP_RIGHT) 10.dp else 0.dp
    val bottomExtra = if (tailDirection == TailDirection.BOTTOM_LEFT || tailDirection == TailDirection.BOTTOM_RIGHT) 10.dp else 0.dp

    Box(
        modifier = modifier
            .wrapContentSize()
            .drawBehind {
                drawVectorMangaBubble(
                    shapeType = shapeType,
                    tailDirection = tailDirection,
                    tailLength = tailLength,
                    bgColor = backgroundColor,
                    borderColor = borderColor,
                    strokeWidth = strokeWidthPx
                )
            }
            .padding(
                start = horizontalPadding,
                end = horizontalPadding,
                top = verticalPadding + topExtra,
                bottom = verticalPadding + bottomExtra
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = fontOption.fontFamily,
            fontSize = fontSizeSp.sp,
            color = textColor,
            fontWeight = when (shapeType) {
                BubbleShapeType.SHOUT, BubbleShapeType.ONOMATOPOEIA -> FontWeight.Black
                BubbleShapeType.THOUGHT -> FontWeight.Normal
                else -> FontWeight.Bold
            },
            textAlign = textAlign,
            letterSpacing = if (shapeType == BubbleShapeType.SHOUT) 0.5.sp else 0.sp
        )
    }
}

/**
 * Custom DrawScope vector math rendering paths for all manga bubble varieties.
 */
private fun DrawScope.drawVectorMangaBubble(
    shapeType: BubbleShapeType,
    tailDirection: TailDirection,
    tailLength: Float,
    bgColor: Color,
    borderColor: Color,
    strokeWidth: Float
) {
    val w = size.width
    val h = size.height

    if (w <= 0f || h <= 0f) return

    when (shapeType) {
        BubbleShapeType.SPEECH -> {
            drawClassicSpeechBubble(w, h, tailDirection, tailLength, bgColor, borderColor, strokeWidth)
        }
        BubbleShapeType.SHOUT -> {
            drawShoutBurstBubble(w, h, bgColor, borderColor, strokeWidth)
        }
        BubbleShapeType.THOUGHT -> {
            drawThoughtCloudBubble(w, h, tailDirection, bgColor, borderColor, strokeWidth)
        }
        BubbleShapeType.WHISPER -> {
            drawWhisperDashedBubble(w, h, tailDirection, tailLength, bgColor, borderColor, strokeWidth)
        }
        BubbleShapeType.NARRATION -> {
            drawNarrationBox(w, h, bgColor, borderColor, strokeWidth)
        }
        BubbleShapeType.ONOMATOPOEIA -> {
            drawOnomatopoeiaBanner(w, h, bgColor, borderColor, strokeWidth)
        }
    }
}

/**
 * 1. Classic Speech Bubble: Smooth rounded rect with integrated pointer tail.
 */
private fun DrawScope.drawClassicSpeechBubble(
    w: Float,
    h: Float,
    tail: TailDirection,
    tailLen: Float,
    bgColor: Color,
    borderColor: Color,
    strokeWidth: Float
) {
    val cornerRadius = 24f
    val mainTop = if (tail == TailDirection.TOP_LEFT || tail == TailDirection.TOP_RIGHT) tailLen else 0f
    val mainBottom = if (tail == TailDirection.BOTTOM_LEFT || tail == TailDirection.BOTTOM_RIGHT) h - tailLen else h

    val path = Path()

    // Start at top-left corner
    path.moveTo(cornerRadius, mainTop)

    // Top edge with optional top tail
    if (tail == TailDirection.TOP_LEFT) {
        path.lineTo(w * 0.25f, mainTop)
        path.lineTo(w * 0.15f, 0f) // tail tip
        path.lineTo(w * 0.40f, mainTop)
    } else if (tail == TailDirection.TOP_RIGHT) {
        path.lineTo(w * 0.60f, mainTop)
        path.lineTo(w * 0.85f, 0f) // tail tip
        path.lineTo(w * 0.75f, mainTop)
    }
    path.lineTo(w - cornerRadius, mainTop)
    path.arcTo(Rect(w - cornerRadius * 2, mainTop, w, mainTop + cornerRadius * 2), -90f, 90f, false)

    // Right edge
    path.lineTo(w, mainBottom - cornerRadius)
    path.arcTo(Rect(w - cornerRadius * 2, mainBottom - cornerRadius * 2, w, mainBottom), 0f, 90f, false)

    // Bottom edge with optional bottom tail
    if (tail == TailDirection.BOTTOM_RIGHT) {
        path.lineTo(w * 0.75f, mainBottom)
        path.lineTo(w * 0.85f, h) // tail tip
        path.lineTo(w * 0.55f, mainBottom)
    } else if (tail == TailDirection.BOTTOM_LEFT) {
        path.lineTo(w * 0.45f, mainBottom)
        path.lineTo(w * 0.15f, h) // tail tip
        path.lineTo(w * 0.25f, mainBottom)
    }
    path.lineTo(cornerRadius, mainBottom)
    path.arcTo(Rect(0f, mainBottom - cornerRadius * 2, cornerRadius * 2, mainBottom), 90f, 90f, false)

    // Left edge
    path.lineTo(0f, mainTop + cornerRadius)
    path.arcTo(Rect(0f, mainTop, cornerRadius * 2, mainTop + cornerRadius * 2), 180f, 90f, false)
    path.close()

    // Subtle manga drop shadow
    drawPath(
        path = path,
        color = Color.Black.copy(alpha = 0.25f),
        style = Fill
    )

    // Fill
    drawPath(path = path, color = bgColor, style = Fill)

    // Ink stroke
    drawPath(
        path = path,
        color = borderColor,
        style = Stroke(
            width = strokeWidth,
            join = StrokeJoin.Round,
            cap = StrokeCap.Round
        )
    )
}

/**
 * 2. Shout / Explosion Starburst Bubble for shonen screaming & special attacks.
 */
private fun DrawScope.drawShoutBurstBubble(
    w: Float,
    h: Float,
    bgColor: Color,
    borderColor: Color,
    strokeWidth: Float
) {
    val path = Path()
    val cx = w / 2f
    val cy = h / 2f
    val rx = w / 2f
    val ry = h / 2f

    val numPoints = 16
    val angleStep = (2 * PI / numPoints).toFloat()

    // Jagged star polygon
    for (i in 0 until numPoints) {
        val angle = i * angleStep
        val isOuter = i % 2 == 0
        val scale = if (isOuter) 1.0f else 0.78f
        val px = cx + rx * scale * cos(angle)
        val py = cy + ry * scale * sin(angle)

        if (i == 0) {
            path.moveTo(px, py)
        } else {
            path.lineTo(px, py)
        }
    }
    path.close()

    // Manga impact drop shadow
    drawPath(path = path, color = Color.Black.copy(alpha = 0.3f), style = Fill)

    // Fill
    drawPath(path = path, color = bgColor, style = Fill)

    // Bold jagged stroke
    drawPath(
        path = path,
        color = borderColor,
        style = Stroke(
            width = strokeWidth * 1.4f,
            join = StrokeJoin.Miter,
            cap = StrokeCap.Square
        )
    )
}

/**
 * 3. Thought Cloud Bubble: Scalloped edges with descending bubble trail.
 */
private fun DrawScope.drawThoughtCloudBubble(
    w: Float,
    h: Float,
    tail: TailDirection,
    bgColor: Color,
    borderColor: Color,
    strokeWidth: Float
) {
    val cloudBottom = if (tail == TailDirection.NONE) h else h - 14f
    val path = Path()

    val numBumpsX = 4
    val numBumpsY = 3
    val stepX = w / numBumpsX
    val stepY = cloudBottom / numBumpsY

    // Construct scalloped perimeter with quadratic beziers
    path.moveTo(0f, stepY)

    // Left side going up
    for (i in (numBumpsY - 1) downTo 0) {
        val y1 = i * stepY
        val yMid = y1 + stepY / 2f
        path.quadraticTo(-10f, yMid, 0f, y1)
    }

    // Top side going right
    for (i in 0 until numBumpsX) {
        val x2 = (i + 1) * stepX
        val xMid = i * stepX + stepX / 2f
        path.quadraticTo(xMid, -12f, x2, 0f)
    }

    // Right side going down
    for (i in 0 until numBumpsY) {
        val y2 = (i + 1) * stepY
        val yMid = i * stepY + stepY / 2f
        path.quadraticTo(w + 10f, yMid, w, y2)
    }

    // Bottom side going left
    for (i in (numBumpsX - 1) downTo 0) {
        val x1 = i * stepX
        val xMid = x1 + stepX / 2f
        path.quadraticTo(xMid, cloudBottom + 12f, x1, cloudBottom)
    }
    path.close()

    // Fill
    drawPath(path = path, color = bgColor, style = Fill)

    // Border
    drawPath(
        path = path,
        color = borderColor,
        style = Stroke(width = strokeWidth, join = StrokeJoin.Round)
    )

    // Trailing thought bubbles
    if (tail != TailDirection.NONE) {
        val trailX = if (tail == TailDirection.BOTTOM_LEFT || tail == TailDirection.TOP_LEFT) w * 0.25f else w * 0.75f
        val bubble1Center = Offset(trailX, cloudBottom + 6f)
        val bubble2Center = Offset(trailX - 8f, h - 3f)

        drawCircle(color = bgColor, radius = 5f, center = bubble1Center)
        drawCircle(color = borderColor, radius = 5f, center = bubble1Center, style = Stroke(strokeWidth))

        drawCircle(color = bgColor, radius = 3f, center = bubble2Center)
        drawCircle(color = borderColor, radius = 3f, center = bubble2Center, style = Stroke(strokeWidth))
    }
}

/**
 * 4. Whisper Bubble: Dashed outline indicating soft murmur.
 */
private fun DrawScope.drawWhisperDashedBubble(
    w: Float,
    h: Float,
    tail: TailDirection,
    tailLen: Float,
    bgColor: Color,
    borderColor: Color,
    strokeWidth: Float
) {
    val cornerRadius = 20f
    val mainTop = if (tail == TailDirection.TOP_LEFT || tail == TailDirection.TOP_RIGHT) tailLen else 0f
    val mainBottom = if (tail == TailDirection.BOTTOM_LEFT || tail == TailDirection.BOTTOM_RIGHT) h - tailLen else h

    val roundRect = RoundRect(
        left = 0f,
        top = mainTop,
        right = w,
        bottom = mainBottom,
        radiusX = cornerRadius,
        radiusY = cornerRadius
    )

    val path = Path().apply {
        addRoundRect(roundRect)
    }

    // Fill
    drawPath(path = path, color = bgColor, style = Fill)

    // Dashed border
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
    drawPath(
        path = path,
        color = borderColor,
        style = Stroke(
            width = strokeWidth,
            pathEffect = dashEffect,
            cap = StrokeCap.Round
        )
    )
}

/**
 * 5. Narration Box: Clean sharp cartouche with double-line header or border.
 */
private fun DrawScope.drawNarrationBox(
    w: Float,
    h: Float,
    bgColor: Color,
    borderColor: Color,
    strokeWidth: Float
) {
    val cornerRadius = 4f
    val rect = RoundRect(0f, 0f, w, h, cornerRadius, cornerRadius)
    val path = Path().apply { addRoundRect(rect) }

    // Drop shadow
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.35f),
        topLeft = Offset(4f, 4f),
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
    )

    // Fill
    drawPath(path = path, color = bgColor, style = Fill)

    // Outer border
    drawPath(path = path, color = borderColor, style = Stroke(width = strokeWidth))

    // Inner subtle decorative border
    val innerMargin = 3f
    if (w > innerMargin * 4 && h > innerMargin * 4) {
        val innerRect = RoundRect(
            innerMargin, innerMargin,
            w - innerMargin, h - innerMargin,
            2f, 2f
        )
        drawPath(
            path = Path().apply { addRoundRect(innerRect) },
            color = borderColor.copy(alpha = 0.4f),
            style = Stroke(width = 1f)
        )
    }
}

/**
 * 6. Onomatopoeia Banner: Dynamic angled parallelogram with comic ink shadow.
 */
private fun DrawScope.drawOnomatopoeiaBanner(
    w: Float,
    h: Float,
    bgColor: Color,
    borderColor: Color,
    strokeWidth: Float
) {
    val skew = 16f
    val shadowOffset = 6f

    // Shadow path
    val shadowPath = Path().apply {
        moveTo(skew + shadowOffset, shadowOffset)
        lineTo(w + shadowOffset, shadowOffset)
        lineTo(w - skew + shadowOffset, h + shadowOffset)
        lineTo(shadowOffset, h + shadowOffset)
        close()
    }
    drawPath(path = shadowPath, color = Color.Black, style = Fill)

    // Main banner path
    val bannerPath = Path().apply {
        moveTo(skew, 0f)
        lineTo(w, 0f)
        lineTo(w - skew, h)
        lineTo(0f, h)
        close()
    }
    drawPath(path = bannerPath, color = bgColor, style = Fill)
    drawPath(
        path = bannerPath,
        color = borderColor,
        style = Stroke(width = strokeWidth * 1.3f, join = StrokeJoin.Miter)
    )
}
