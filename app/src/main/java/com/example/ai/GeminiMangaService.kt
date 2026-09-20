package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed class MangaGenerationResult {
    data class Success(val imagePath: String, val enrichedPrompt: String, val isAiGenerated: Boolean) : MangaGenerationResult()
    data class Error(val message: String, val fallbackImagePath: String? = null) : MangaGenerationResult()
}

class GeminiMangaService(private val context: Context) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val imagesDir: File by lazy {
        File(context.filesDir, "manga_panels").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Generates a manga/comic panel image using Gemini 2.5 Flash Image.
     * Supports multimodal character/background reference images for visual coherence.
     */
    suspend fun generateMangaPanel(
        prompt: String,
        aspectRatio: String = "1:1",
        referenceImagePaths: List<String> = emptyList()
    ): MangaGenerationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("GeminiMangaService", "No Gemini API key provided. Using stylized local manga engine fallback.")
            val fallback = createStylizedMangaFallback(
                prompt = prompt,
                subtitle = "Configurez votre clé Gemini dans les Secrets AI Studio",
                badgeText = "Studio Démo Manga"
            )
            return@withContext MangaGenerationResult.Success(
                imagePath = fallback,
                enrichedPrompt = prompt,
                isAiGenerated = false
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=$apiKey"

            // Construct JSON payload
            val partsArray = JSONArray()

            // Main text prompt
            val textPart = JSONObject().put("text", prompt)
            partsArray.put(textPart)

            // Optional reference images for character & background consistency
            for (refPath in referenceImagePaths) {
                val refFile = File(refPath)
                if (refFile.exists()) {
                    val base64Data = encodeImageToBase64(refFile)
                    if (base64Data != null) {
                        val inlineData = JSONObject()
                            .put("mimeType", "image/jpeg")
                            .put("data", base64Data)
                        val imgPart = JSONObject().put("inlineData", inlineData)
                        partsArray.put(imgPart)
                    }
                }
            }

            val contentObj = JSONObject().put("parts", partsArray)
            val contentsArray = JSONArray().put(contentObj)

            val rootJson = JSONObject().apply {
                put("contents", contentsArray)

                val imageConfig = JSONObject().apply {
                    put("aspectRatio", aspectRatio)
                    put("imageSize", "1K")
                }

                val genConfig = JSONObject().apply {
                    put("imageConfig", imageConfig)
                    put("responseModalities", JSONArray().put("TEXT").put("IMAGE"))
                }

                put("generationConfig", genConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiMangaService", "API call failed with code: ${response.code} body: $responseString")
                val fallback = createStylizedMangaFallback(prompt, "Erreur API (${response.code}) - Aperçu simulé")
                return@withContext MangaGenerationResult.Error(
                    message = "Erreur de génération (${response.code}): $responseString",
                    fallbackImagePath = fallback
                )
            }

            // Parse response for image data
            val responseJson = JSONObject(responseString)
            val candidates = responseJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var savedImagePath: String? = null

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i)
                    val inlineData = part?.optJSONObject("inlineData")
                    if (inlineData != null) {
                        val dataBase64 = inlineData.optString("data")
                        if (dataBase64.isNotBlank()) {
                            savedImagePath = saveBase64Image(dataBase64)
                            break
                        }
                    }
                }
            }

            if (savedImagePath != null) {
                MangaGenerationResult.Success(
                    imagePath = savedImagePath,
                    enrichedPrompt = prompt,
                    isAiGenerated = true
                )
            } else {
                // In case model responded with text only
                val textResponse = parts?.optJSONObject(0)?.optString("text") ?: "Aucune image renvoyée"
                val fallback = createStylizedMangaFallback(prompt, textResponse.take(120))
                MangaGenerationResult.Success(
                    imagePath = fallback,
                    enrichedPrompt = prompt,
                    isAiGenerated = false
                )
            }

        } catch (e: Exception) {
            Log.e("GeminiMangaService", "Error calling Gemini Image API", e)
            val fallback = createStylizedMangaFallback(prompt, "Mode hors-ligne / Erreur réseau")
            MangaGenerationResult.Error(
                message = e.localizedMessage ?: "Erreur inconnue lors de la génération",
                fallbackImagePath = fallback
            )
        }
    }

    /**
     * Uses Gemini 3.5 Flash to automatically enhance and storyboard a manga panel idea,
     * suggesting camera framing, action lines, and Japanese sound onomatopoeias.
     */
    suspend fun storyboardAssistant(
        userIdea: String,
        artStyle: MangaArtStyle
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Plan dynamique : $userIdea avec lignes de vitesse intenses et cadrage en contre-plongée dramatique. [Effet sonore: DODODODO]"
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val prompt = """
                Tu es un mangaka et directeur artistique expert en bandes dessinées (manga, manhua, comics).
                À partir de l'idée suivante : "$userIdea" dans le style "${artStyle.displayName}",
                rédige une description visuelle concise pour case de manga en français (2 phrases maximum)
                avec le cadrage de caméra, l'éclairage dramatique et l'onomatopée sonore manga recommandée (ex: BAM, SHING, DON!).
            """.trimIndent()

            val json = JSONObject().apply {
                val parts = JSONArray().put(JSONObject().put("text", prompt))
                val content = JSONObject().put("parts", parts)
                put("contents", JSONArray().put(content))
            }

            val request = Request.Builder()
                .url(url)
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val respBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val respJson = JSONObject(respBody)
                val text = respJson.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                if (!text.isNullOrBlank()) return@withContext text.trim()
            }
        } catch (e: Exception) {
            Log.e("GeminiMangaService", "Failed storyboard assistant", e)
        }
        return@withContext userIdea
    }

    private fun encodeImageToBase64(file: File): String? {
        return try {
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    private fun saveBase64Image(base64Data: String): String {
        val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
        val file = File(imagesDir, "panel_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { fos ->
            fos.write(decodedBytes)
            fos.flush()
        }
        return file.absolutePath
    }

    /**
     * Creates a high-fidelity stylized manga illustration canvas used as immediate preview or fallback.
     */
    private fun createStylizedMangaFallback(
        prompt: String,
        subtitle: String,
        badgeText: String = "Aperçu Manga Studio"
    ): String {
        val width = 768
        val height = 768
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Ink canvas background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#141620")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Manga Speedlines / Action Rays
        val linePaint = Paint().apply {
            color = Color.parseColor("#282D42")
            strokeWidth = 3f
            isAntiAlias = true
        }
        val centerX = width / 2f
        val centerY = height * 0.42f
        val numLines = 36
        for (i in 0 until numLines) {
            val angle = (i * (360.0 / numLines)) * Math.PI / 180.0
            val startRadius = 160f
            val endRadius = 550f
            val x1 = (centerX + Math.cos(angle) * startRadius).toFloat()
            val y1 = (centerY + Math.sin(angle) * startRadius).toFloat()
            val x2 = (centerX + Math.cos(angle) * endRadius).toFloat()
            val y2 = (centerY + Math.sin(angle) * endRadius).toFloat()
            canvas.drawLine(x1, y1, x2, y2, linePaint)
        }

        // Inner dramatic frame / screentone vignette
        val framePaint = Paint().apply {
            color = Color.parseColor("#FF2A55")
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRect(24f, 24f, (width - 24).toFloat(), (height - 24).toFloat(), framePaint)

        // Central Manga Silhouette / Symbol
        val circlePaint = Paint().apply {
            color = Color.parseColor("#221D38")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, 130f, circlePaint)

        val accentPaint = Paint().apply {
            color = Color.parseColor("#FF2A55")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        // Draw dramatic eye / brush emblem
        val path = Path().apply {
            moveTo(centerX - 70f, centerY)
            quadTo(centerX, centerY - 60f, centerX + 70f, centerY)
            quadTo(centerX, centerY + 60f, centerX - 70f, centerY)
            close()
        }
        canvas.drawPath(path, accentPaint)

        val pupilPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, 22f, pupilPaint)

        // Japanese Onomatopoeia Accent (ドドド or バン!)
        val onomatoPaint = Paint().apply {
            color = Color.parseColor("#FFD166")
            textSize = 42f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("ゴゴゴ...", 50f, 100f, onomatoPaint)
        canvas.drawText("ドドド！", (width - 200).toFloat(), 100f, onomatoPaint)

        // Bottom text card
        val cardPaint = Paint().apply {
            color = Color.parseColor("#1A1D2B")
            style = Paint.Style.FILL
        }
        canvas.drawRect(36f, (height - 210).toFloat(), (width - 36).toFloat(), (height - 36).toFloat(), cardPaint)

        val badgePaint = Paint().apply {
            color = Color.parseColor("#FF2A55")
            style = Paint.Style.FILL
        }
        canvas.drawRect(36f, (height - 210).toFloat(), 240f, (height - 170).toFloat(), badgePaint)

        val badgeTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(badgeText, 50f, (height - 182).toFloat(), badgeTextPaint)

        val promptPaint = Paint().apply {
            color = Color.WHITE
            textSize = 26f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val cleanPrompt = if (prompt.length > 55) prompt.take(52) + "..." else prompt
        canvas.drawText(cleanPrompt, 50f, (height - 130).toFloat(), promptPaint)

        val subPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 20f
            isAntiAlias = true
        }
        val cleanSub = if (subtitle.length > 65) subtitle.take(62) + "..." else subtitle
        canvas.drawText(cleanSub, 50f, (height - 85).toFloat(), subPaint)

        val file = File(imagesDir, "panel_preview_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
        }
        return file.absolutePath
    }
}
