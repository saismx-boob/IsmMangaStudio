package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.MangaPanel
import com.example.ui.components.BubbleShapeType
import com.example.ui.components.TailDirection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object MangaPanelExporter {

    private const val EXPORT_WIDTH = 1200
    private const val EXPORT_HEIGHT = 1040 // Matches 1.15f aspect ratio (1200 / 1.15 ≈ 1043)

    /**
     * Renders a high-resolution bitmap with background image, vector speech bubble, typography,
     * ink borders, and metadata watermark.
     */
    suspend fun renderHighResBitmap(
        context: Context,
        panel: MangaPanel,
        panelNumber: Int,
        targetWidth: Int = EXPORT_WIDTH,
        targetHeight: Int = EXPORT_HEIGHT
    ): Bitmap = withContext(Dispatchers.Default) {
        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw Background Image or Default Studio Canvas
        var drawnBaseImage = false
        if (panel.imagePath != null) {
            val imgFile = File(panel.imagePath)
            if (imgFile.exists()) {
                try {
                    val originalBmp = BitmapFactory.decodeFile(imgFile.absolutePath)
                    if (originalBmp != null) {
                        val srcRect = android.graphics.Rect(0, 0, originalBmp.width, originalBmp.height)
                        val dstRect = android.graphics.Rect(0, 0, targetWidth, targetHeight)
                        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                        canvas.drawBitmap(originalBmp, srcRect, dstRect, paint)
                        drawnBaseImage = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (!drawnBaseImage) {
            // Draw dark tone studio canvas
            val darkPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#161924")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), darkPaint)

            // Inner watermark grid pattern
            val gridPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#1F2433")
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            for (y in 0..targetHeight step 80) {
                canvas.drawLine(0f, y.toFloat(), targetWidth.toFloat(), y.toFloat(), gridPaint)
            }
        }

        // 2. Heavy Manga Border Framing (G-Pen Ink Style)
        val borderPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 14f
        }
        canvas.drawRect(7f, 7f, targetWidth - 7f, targetHeight - 7f, borderPaint)

        // 3. Render Vector Speech Bubble (if present)
        if (!panel.dialogueText.isNullOrBlank()) {
            drawVectorSpeechBubbleOnCanvas(
                context = context,
                canvas = canvas,
                panel = panel,
                canvasWidth = targetWidth.toFloat(),
                canvasHeight = targetHeight.toFloat()
            )
        }

        // 4. Panel Watermark & Number Tag (Bottom-Right corner)
        drawMetadataTag(canvas, panelNumber, targetWidth.toFloat(), targetHeight.toFloat())

        bitmap
    }

    private fun drawVectorSpeechBubbleOnCanvas(
        context: Context,
        canvas: Canvas,
        panel: MangaPanel,
        canvasWidth: Float,
        canvasHeight: Float
    ) {
        val text = panel.dialogueText ?: return
        val shape = BubbleShapeType.fromId(panel.bubbleType)
        val tailDir = TailDirection.fromId(panel.bubbleTailDirection)
        val scale = canvasWidth / 400f // Scale proportionally from phone preview (~400dp) to 1200px

        val typeface = getFontTypeface(context, panel.bubbleFont)
        val fontSizePx = panel.bubbleFontSize * scale * 1.5f

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = panel.bubbleTextColor.toInt()
            textSize = fontSizePx
            this.typeface = typeface
            isFakeBoldText = shape == BubbleShapeType.SHOUT || shape == BubbleShapeType.ONOMATOPOEIA
        }

        val maxTextWidth = (canvasWidth * 0.45f).toInt()
        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, maxTextWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.1f)
                .setIncludePad(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                textPaint,
                maxTextWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.1f,
                0f,
                true
            )
        }

        val textWidth = staticLayout.width.toFloat().coerceAtLeast(100f * scale)
        val textHeight = staticLayout.height.toFloat().coerceAtLeast(30f * scale)

        // Bubble dimension with paddings
        val hPad = (when (shape) {
            BubbleShapeType.SHOUT -> 36f
            BubbleShapeType.THOUGHT -> 30f
            BubbleShapeType.ONOMATOPOEIA -> 26f
            BubbleShapeType.NARRATION -> 18f
            else -> 24f
        }) * scale

        val vPad = (when (shape) {
            BubbleShapeType.SHOUT -> 26f
            BubbleShapeType.THOUGHT -> 22f
            BubbleShapeType.ONOMATOPOEIA -> 18f
            BubbleShapeType.NARRATION -> 14f
            else -> 18f
        }) * scale

        val tailExtra = if (tailDir != TailDirection.NONE) 22f * scale else 0f
        val bubbleWidth = textWidth + (hPad * 2)
        val bubbleHeight = textHeight + (vPad * 2) + tailExtra

        // Anchor position
        val centerX = (panel.bubbleNormalizedX.coerceIn(0.12f, 0.88f) * canvasWidth)
        val centerY = (panel.bubbleNormalizedY.coerceIn(0.12f, 0.88f) * canvasHeight)

        var left = centerX - (bubbleWidth / 2f)
        var top = centerY - (bubbleHeight / 2f)

        // Clamp to panel frame
        left = left.coerceIn(20f, canvasWidth - bubbleWidth - 20f)
        top = top.coerceIn(20f, canvasHeight - bubbleHeight - 20f)
        val right = left + bubbleWidth
        val bottom = top + bubbleHeight

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = panel.bubbleBgColor.toInt()
            style = Paint.Style.FILL
        }

        val strokeWidthPx = (when (shape) {
            BubbleShapeType.SHOUT -> 7f
            BubbleShapeType.ONOMATOPOEIA -> 6f
            else -> 5f
        }) * scale

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = panel.bubbleBorderColor.toInt()
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(70, 0, 0, 0)
            style = Paint.Style.FILL
        }

        val path = android.graphics.Path()

        when (shape) {
            BubbleShapeType.SHOUT -> {
                // Starburst
                val cx = (left + right) / 2f
                val cy = (top + bottom) / 2f
                val rx = bubbleWidth / 2f
                val ry = bubbleHeight / 2f
                val numPoints = 16
                val step = (2 * PI / numPoints).toFloat()
                for (i in 0 until numPoints) {
                    val angle = i * step
                    val isOuter = i % 2 == 0
                    val s = if (isOuter) 1.0f else 0.78f
                    val px = cx + rx * s * cos(angle)
                    val py = cy + ry * s * sin(angle)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                // Drop shadow
                val shadowPath = android.graphics.Path(path)
                shadowPath.offset(8f * scale, 8f * scale)
                canvas.drawPath(shadowPath, shadowPaint)
                canvas.drawPath(path, fillPaint)
                strokePaint.strokeJoin = Paint.Join.MITER
                canvas.drawPath(path, strokePaint)
            }
            BubbleShapeType.THOUGHT -> {
                // Cloud scalloped
                val cloudBottom = if (tailDir == TailDirection.NONE) bottom else bottom - tailExtra
                path.addRoundRect(
                    android.graphics.RectF(left, top, right, cloudBottom),
                    28f * scale,
                    28f * scale,
                    android.graphics.Path.Direction.CW
                )
                canvas.drawPath(path, fillPaint)
                canvas.drawPath(path, strokePaint)

                if (tailDir != TailDirection.NONE) {
                    val trailX = if (tailDir == TailDirection.BOTTOM_LEFT || tailDir == TailDirection.TOP_LEFT) {
                        left + bubbleWidth * 0.25f
                    } else {
                        left + bubbleWidth * 0.75f
                    }
                    canvas.drawCircle(trailX, cloudBottom + 10f * scale, 8f * scale, fillPaint)
                    canvas.drawCircle(trailX, cloudBottom + 10f * scale, 8f * scale, strokePaint)
                    canvas.drawCircle(trailX - 10f * scale, bottom - 4f * scale, 5f * scale, fillPaint)
                    canvas.drawCircle(trailX - 10f * scale, bottom - 4f * scale, 5f * scale, strokePaint)
                }
            }
            BubbleShapeType.WHISPER -> {
                val rectF = android.graphics.RectF(left, top, right, bottom)
                path.addRoundRect(rectF, 24f * scale, 24f * scale, android.graphics.Path.Direction.CW)
                canvas.drawPath(path, fillPaint)
                strokePaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(14f * scale, 10f * scale), 0f)
                canvas.drawPath(path, strokePaint)
            }
            BubbleShapeType.NARRATION -> {
                val rectF = android.graphics.RectF(left, top, right, bottom)
                // Drop shadow
                val shadowRect = android.graphics.RectF(left + 6f * scale, top + 6f * scale, right + 6f * scale, bottom + 6f * scale)
                canvas.drawRoundRect(shadowRect, 6f * scale, 6f * scale, shadowPaint)
                path.addRoundRect(rectF, 6f * scale, 6f * scale, android.graphics.Path.Direction.CW)
                canvas.drawPath(path, fillPaint)
                canvas.drawPath(path, strokePaint)
                // Inner border
                val innerRect = android.graphics.RectF(left + 6f * scale, top + 6f * scale, right - 6f * scale, bottom - 6f * scale)
                val innerPaint = Paint(strokePaint).apply {
                    strokeWidth = 2f * scale
                    alpha = 110
                }
                canvas.drawRoundRect(innerRect, 3f * scale, 3f * scale, innerPaint)
            }
            BubbleShapeType.ONOMATOPOEIA -> {
                val skew = 22f * scale
                val shadowOffset = 8f * scale
                val shadowPath = android.graphics.Path().apply {
                    moveTo(left + skew + shadowOffset, top + shadowOffset)
                    lineTo(right + shadowOffset, top + shadowOffset)
                    lineTo(right - skew + shadowOffset, bottom + shadowOffset)
                    lineTo(left + shadowOffset, bottom + shadowOffset)
                    close()
                }
                canvas.drawPath(shadowPath, Paint().apply { color = android.graphics.Color.BLACK; style = Paint.Style.FILL })

                val onomatoPath = android.graphics.Path().apply {
                    moveTo(left + skew, top)
                    lineTo(right, top)
                    lineTo(right - skew, bottom)
                    lineTo(left, bottom)
                    close()
                }
                canvas.drawPath(onomatoPath, fillPaint)
                strokePaint.strokeJoin = Paint.Join.MITER
                canvas.drawPath(onomatoPath, strokePaint)
            }
            else -> {
                // Classic Speech with pointer tail
                val cr = 28f * scale
                val mainTop = if (tailDir == TailDirection.TOP_LEFT || tailDir == TailDirection.TOP_RIGHT) top + tailExtra else top
                val mainBottom = if (tailDir == TailDirection.BOTTOM_LEFT || tailDir == TailDirection.BOTTOM_RIGHT) bottom - tailExtra else bottom

                path.moveTo(left + cr, mainTop)
                if (tailDir == TailDirection.TOP_LEFT) {
                    path.lineTo(left + bubbleWidth * 0.25f, mainTop)
                    path.lineTo(left + bubbleWidth * 0.15f, top)
                    path.lineTo(left + bubbleWidth * 0.40f, mainTop)
                } else if (tailDir == TailDirection.TOP_RIGHT) {
                    path.lineTo(left + bubbleWidth * 0.60f, mainTop)
                    path.lineTo(left + bubbleWidth * 0.85f, top)
                    path.lineTo(left + bubbleWidth * 0.75f, mainTop)
                }
                path.lineTo(right - cr, mainTop)
                path.arcTo(android.graphics.RectF(right - cr * 2, mainTop, right, mainTop + cr * 2), -90f, 90f)

                path.lineTo(right, mainBottom - cr)
                path.arcTo(android.graphics.RectF(right - cr * 2, mainBottom - cr * 2, right, mainBottom), 0f, 90f)

                if (tailDir == TailDirection.BOTTOM_RIGHT) {
                    path.lineTo(left + bubbleWidth * 0.75f, mainBottom)
                    path.lineTo(left + bubbleWidth * 0.85f, bottom)
                    path.lineTo(left + bubbleWidth * 0.55f, mainBottom)
                } else if (tailDir == TailDirection.BOTTOM_LEFT) {
                    path.lineTo(left + bubbleWidth * 0.45f, mainBottom)
                    path.lineTo(left + bubbleWidth * 0.15f, bottom)
                    path.lineTo(left + bubbleWidth * 0.25f, mainBottom)
                }
                path.lineTo(left + cr, mainBottom)
                path.arcTo(android.graphics.RectF(left, mainBottom - cr * 2, left + cr * 2, mainBottom), 90f, 90f)

                path.lineTo(left, mainTop + cr)
                path.arcTo(android.graphics.RectF(left, mainTop, left + cr * 2, mainTop + cr * 2), 180f, 90f)
                path.close()

                val shadowPath = android.graphics.Path(path)
                shadowPath.offset(6f * scale, 6f * scale)
                canvas.drawPath(shadowPath, shadowPaint)
                canvas.drawPath(path, fillPaint)
                canvas.drawPath(path, strokePaint)
            }
        }

        // Render StaticLayout text inside bubble bounds
        canvas.save()
        val textLeft = left + (bubbleWidth - staticLayout.width) / 2f
        val textTopOffset = if (tailDir == TailDirection.TOP_LEFT || tailDir == TailDirection.TOP_RIGHT) tailExtra else 0f
        val textTop = top + textTopOffset + vPad
        canvas.translate(textLeft, textTop)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawMetadataTag(canvas: Canvas, panelNumber: Int, width: Float, height: Float) {
        val tagPaint = Paint().apply {
            color = android.graphics.Color.argb(200, 0, 0, 0)
            style = Paint.Style.FILL
        }
        val tagBorder = Paint().apply {
            color = android.graphics.Color.parseColor("#E11D48")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
        }
        val accentText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#E11D48")
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
        }

        val tagRect = android.graphics.RectF(width - 260f, height - 60f, width - 20f, height - 20f)
        canvas.drawRoundRect(tagRect, 8f, 8f, tagPaint)
        canvas.drawRoundRect(tagRect, 8f, 8f, tagBorder)

        canvas.drawText("MANGA CASE #$panelNumber", width - 245f, height - 33f, accentText)
    }

    private fun getFontTypeface(context: Context, fontId: String): Typeface {
        return try {
            when (fontId.uppercase()) {
                "COMIC_NEUE" -> ResourcesCompat.getFont(context, R.font.comic_neue) ?: Typeface.DEFAULT_BOLD
                "BANGERS" -> ResourcesCompat.getFont(context, R.font.bangers) ?: Typeface.DEFAULT_BOLD
                "PERMANENT_MARKER" -> ResourcesCompat.getFont(context, R.font.permanent_marker) ?: Typeface.DEFAULT_BOLD
                "CAVEAT" -> ResourcesCompat.getFont(context, R.font.caveat) ?: Typeface.DEFAULT
                else -> Typeface.DEFAULT_BOLD
            }
        } catch (e: Exception) {
            Typeface.DEFAULT_BOLD
        }
    }

    /**
     * Saves the high-res bitmap to the device's public Pictures/MangaStudio directory using MediaStore,
     * fully compatible with Android 10+ scoped storage (zero permissions required).
     */
    suspend fun saveToGallery(
        context: Context,
        bitmap: Bitmap,
        filenamePrefix: String = "manga_panel"
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val filename = "${filenamePrefix}_${System.currentTimeMillis()}.png"
            var outputStream: OutputStream? = null
            var imageUri: Uri? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MangaStudio")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext Result.failure(Exception("Échec de création de l'enregistrement MediaStore"))

                outputStream = resolver.openOutputStream(imageUri)
                outputStream?.let {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val studioDir = File(picturesDir, "MangaStudio").apply { if (!exists()) mkdirs() }
                val file = File(studioDir, filename)
                outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                imageUri = Uri.fromFile(file)
            }

            outputStream?.flush()
            outputStream?.close()

            Result.success(imageUri!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exports bitmap to a cached file and returns a sharable Intent using Android FileProvider.
     */
    suspend fun createShareIntent(
        context: Context,
        bitmap: Bitmap,
        panelNumber: Int
    ): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            val cacheFolder = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
            val exportFile = File(cacheFolder, "manga_panel_${panelNumber}_${System.currentTimeMillis()}.png")
            FileOutputStream(exportFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Manga Studio - Case #$panelNumber")
                putExtra(Intent.EXTRA_TEXT, "Voici ma case manga créée dans Manga Studio !")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Partager la case manga HD")
            Result.success(chooser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
