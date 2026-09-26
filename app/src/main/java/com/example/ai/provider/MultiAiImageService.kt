package com.example.ai.provider

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
import com.example.ai.MangaGenerationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class MultiAiImageService(private val context: Context) {

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
     * Dispatches generation to the configured provider.
     */
    suspend fun generateMangaPanel(
        prompt: String,
        aspectRatio: String = "1:1",
        referenceImagePaths: List<String> = emptyList(),
        config: AiProviderConfig
    ): MangaGenerationResult = withContext(Dispatchers.IO) {
        val provider = config.provider
        val apiKey = config.apiKey.ifBlank {
            if (provider == AiProvider.GEMINI) BuildConfig.GEMINI_API_KEY else ""
        }

        // Check if API key is required and missing
        if (provider.requiresKey && (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY")) {
            val fallback = createStylizedMangaFallback(
                prompt = prompt,
                subtitle = "Clé ${provider.displayName} manquante. Configurez-la dans les Paramètres IA.",
                badgeText = provider.shortName
            )
            return@withContext MangaGenerationResult.Error(
                message = "Clé API manquante pour ${provider.displayName}. Ouvrez les 'Paramètres IA' pour saisir votre clé ou sélectionnez le 'Mode Gratuit'.",
                fallbackImagePath = fallback
            )
        }

        try {
            when (provider) {
                AiProvider.GEMINI -> generateWithGemini(prompt, aspectRatio, referenceImagePaths, apiKey)
                AiProvider.OPENAI -> generateWithOpenAi(prompt, aspectRatio, config, apiKey)
                AiProvider.GROK -> generateWithGrok(prompt, aspectRatio, config, apiKey)
                AiProvider.META -> generateWithMetaTogether(prompt, aspectRatio, config, apiKey)
                AiProvider.ALIBABA_WANX -> generateWithAlibabaWanx(prompt, aspectRatio, config, apiKey)
                AiProvider.ZHIPU_COGVIEW -> generateWithZhipuCogView(prompt, aspectRatio, config, apiKey)
                AiProvider.SILICONFLOW -> generateWithSiliconFlow(prompt, aspectRatio, config, apiKey)
                AiProvider.POLLINATIONS_FREE -> generateWithPollinationsFree(prompt, config)
                AiProvider.CUSTOM -> generateWithCustomOpenAi(prompt, aspectRatio, config, apiKey)
            }
        } catch (e: Exception) {
            Log.e("MultiAiImageService", "Error with provider ${provider.displayName}", e)
            val fallback = createStylizedMangaFallback(
                prompt = prompt,
                subtitle = "Erreur: ${e.message ?: "Connexion échouée"}",
                badgeText = provider.shortName
            )
            MangaGenerationResult.Error(
                message = "Erreur [${provider.shortName}]: ${e.localizedMessage ?: "Échec de génération"}",
                fallbackImagePath = fallback
            )
        }
    }

    /**
     * Tests the connection and API Key validity for a given provider.
     */
    suspend fun testConnection(config: AiProviderConfig): Result<String> = withContext(Dispatchers.IO) {
        val provider = config.provider
        val apiKey = config.apiKey.ifBlank {
            if (provider == AiProvider.GEMINI) BuildConfig.GEMINI_API_KEY else ""
        }

        if (provider.requiresKey && (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY")) {
            return@withContext Result.failure(Exception("Veuillez saisir une clé API valide pour ${provider.displayName}."))
        }

        try {
            when (provider) {
                AiProvider.POLLINATIONS_FREE -> {
                    Result.success("Mode Gratuit opérationnel ! Aucune clé n'est requise.")
                }
                AiProvider.GEMINI -> {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
                    val req = Request.Builder().url(url).get().build()
                    val resp = okHttpClient.newCall(req).execute()
                    if (resp.isSuccessful) {
                        Result.success("Clé Gemini valide ! Connexion à Google AI Studio établie.")
                    } else {
                        Result.failure(Exception("Code ${resp.code}: ${resp.body?.string()?.take(160)}"))
                    }
                }
                AiProvider.OPENAI -> {
                    val baseUrl = config.customBaseUrl.ifBlank { "https://api.openai.com/v1" }
                    val req = Request.Builder()
                        .url("$baseUrl/models")
                        .header("Authorization", "Bearer $apiKey")
                        .get()
                        .build()
                    val resp = okHttpClient.newCall(req).execute()
                    if (resp.isSuccessful) {
                        Result.success("Clé OpenAI vérifiée avec succès ! Modèles DALL-E accessibles.")
                    } else {
                        Result.failure(Exception("Erreur OpenAI (${resp.code}): clé invalide ou expirée."))
                    }
                }
                AiProvider.GROK -> {
                    val baseUrl = config.customBaseUrl.ifBlank { "https://api.x.ai/v1" }
                    val req = Request.Builder()
                        .url("$baseUrl/models")
                        .header("Authorization", "Bearer $apiKey")
                        .get()
                        .build()
                    val resp = okHttpClient.newCall(req).execute()
                    if (resp.isSuccessful) {
                        Result.success("Clé xAI Grok validée avec succès !")
                    } else {
                        Result.failure(Exception("Erreur Grok (${resp.code}): vérifiez votre clé dans console.x.ai"))
                    }
                }
                AiProvider.META -> {
                    val baseUrl = config.customBaseUrl.ifBlank { "https://api.together.xyz/v1" }
                    val req = Request.Builder()
                        .url("$baseUrl/models")
                        .header("Authorization", "Bearer $apiKey")
                        .get()
                        .build()
                    val resp = okHttpClient.newCall(req).execute()
                    if (resp.isSuccessful) {
                        Result.success("Connexion à l'API Meta/Together validée !")
                    } else {
                        Result.failure(Exception("Erreur (${resp.code}): clé Together/Meta non reconnue."))
                    }
                }
                AiProvider.ALIBABA_WANX -> {
                    // Quick check header
                    if (apiKey.startsWith("sk-") && apiKey.length > 15) {
                        Result.success("Format de clé Alibaba DashScope valide (sk-...). Prêt pour Wanxiang !")
                    } else {
                        Result.failure(Exception("La clé DashScope Alibaba doit commencer par 'sk-'."))
                    }
                }
                AiProvider.ZHIPU_COGVIEW -> {
                    if (apiKey.isNotBlank() && apiKey.length > 12) {
                        Result.success("Clé Zhipu AI CogView configurée avec succès !")
                    } else {
                        Result.failure(Exception("Format de clé Zhipu invalide."))
                    }
                }
                AiProvider.SILICONFLOW -> {
                    val baseUrl = config.customBaseUrl.ifBlank { "https://api.siliconflow.cn/v1" }
                    val req = Request.Builder()
                        .url("$baseUrl/models")
                        .header("Authorization", "Bearer $apiKey")
                        .get()
                        .build()
                    val resp = okHttpClient.newCall(req).execute()
                    if (resp.isSuccessful) {
                        Result.success("Connexion à SiliconFlow établie ! Modèles Kolors & FLUX prêts.")
                    } else {
                        Result.failure(Exception("Erreur SiliconFlow (${resp.code}): vérifiez votre clé API."))
                    }
                }
                AiProvider.CUSTOM -> {
                    val baseUrl = config.customBaseUrl.ifBlank { "https://api.openai.com/v1" }
                    val req = Request.Builder()
                        .url("$baseUrl/models")
                        .apply { if (apiKey.isNotBlank()) header("Authorization", "Bearer $apiKey") }
                        .get()
                        .build()
                    val resp = okHttpClient.newCall(req).execute()
                    if (resp.isSuccessful) {
                        Result.success("Passerelle personnalisée connectée avec succès (${resp.code}).")
                    } else {
                        Result.failure(Exception("Échec de connexion (${resp.code}) sur $baseUrl"))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erreur de test réseau : ${e.message}"))
        }
    }

    // =========================================================================
    // 1. Google Gemini Implementation
    // =========================================================================
    private fun generateWithGemini(
        prompt: String,
        aspectRatio: String,
        referenceImagePaths: List<String>,
        apiKey: String
    ): MangaGenerationResult {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=$apiKey"

        val partsArray = JSONArray()
        partsArray.put(JSONObject().put("text", prompt))

        for (refPath in referenceImagePaths) {
            val refFile = File(refPath)
            if (refFile.exists()) {
                val base64Data = encodeImageToBase64(refFile)
                if (base64Data != null) {
                    val inlineData = JSONObject()
                        .put("mimeType", "image/jpeg")
                        .put("data", base64Data)
                    partsArray.put(JSONObject().put("inlineData", inlineData))
                }
            }
        }

        val contentObj = JSONObject().put("parts", partsArray)
        val contentsArray = JSONArray().put(contentObj)

        val rootJson = JSONObject().apply {
            put("contents", contentsArray)
            val imageConfig = JSONObject().apply {
                put("aspectRatio", if (aspectRatio == "9:16") "9:16" else if (aspectRatio == "3:4") "3:4" else "1:1")
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
        val request = Request.Builder().url(url).post(requestBody).build()
        val response = okHttpClient.newCall(request).execute()
        val responseString = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val fallback = createStylizedMangaFallback(prompt, "Gemini API (${response.code})", "Gemini")
            return MangaGenerationResult.Error("Erreur Gemini (${response.code}): $responseString", fallback)
        }

        val responseJson = JSONObject(responseString)
        val candidates = responseJson.optJSONArray("candidates")
        val content = candidates?.optJSONObject(0)?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")

        if (parts != null) {
            for (i in 0 until parts.length()) {
                val part = parts.optJSONObject(i)
                val inlineData = part?.optJSONObject("inlineData")
                if (inlineData != null) {
                    val dataBase64 = inlineData.optString("data")
                    if (dataBase64.isNotBlank()) {
                        val path = saveBase64Image(dataBase64, "gemini")
                        return MangaGenerationResult.Success(path, prompt, isAiGenerated = true)
                    }
                }
            }
        }

        val textResp = parts?.optJSONObject(0)?.optString("text") ?: "Pas d'image renvoyée"
        val fallback = createStylizedMangaFallback(prompt, textResp.take(120), "Gemini")
        return MangaGenerationResult.Success(fallback, prompt, isAiGenerated = false)
    }

    // =========================================================================
    // 2. OpenAI GPT / DALL-E 3 Implementation
    // =========================================================================
    private fun generateWithOpenAi(
        prompt: String,
        aspectRatio: String,
        config: AiProviderConfig,
        apiKey: String
    ): MangaGenerationResult {
        val baseUrl = config.customBaseUrl.ifBlank { "https://api.openai.com/v1" }
        val url = "$baseUrl/images/generations"

        val size = when (aspectRatio) {
            "9:16", "3:4" -> "1024x1792"
            "16:9", "4:3" -> "1792x1024"
            else -> "1024x1024"
        }

        val payload = JSONObject().apply {
            put("model", config.selectedModel.ifBlank { "dall-e-3" })
            put("prompt", prompt.take(3900))
            put("n", 1)
            put("size", size)
            put("quality", if (config.imageQuality == "hd") "hd" else "standard")
            put("style", if (config.imageStyle == "natural") "natural" else "vivid")
            put("response_format", "b64_json")
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val fallback = createStylizedMangaFallback(prompt, "OpenAI Erreur (${response.code})", "DALL-E 3")
            return MangaGenerationResult.Error("OpenAI DALL-E erreur (${response.code}): $responseBody", fallback)
        }

        val json = JSONObject(responseBody)
        val dataArray = json.optJSONArray("data")
        val item = dataArray?.optJSONObject(0)

        val b64 = item?.optString("b64_json")
        if (!b64.isNullOrBlank()) {
            val path = saveBase64Image(b64, "openai")
            return MangaGenerationResult.Success(path, prompt, isAiGenerated = true)
        }

        val imgUrl = item?.optString("url")
        if (!imgUrl.isNullOrBlank()) {
            val path = downloadAndSaveImage(imgUrl, "openai")
            return MangaGenerationResult.Success(path, prompt, isAiGenerated = true)
        }

        val fallback = createStylizedMangaFallback(prompt, "Réponse OpenAI vide", "DALL-E 3")
        return MangaGenerationResult.Error("Aucune image reçue d'OpenAI", fallback)
    }

    // =========================================================================
    // 3. xAI Grok Implementation
    // =========================================================================
    private fun generateWithGrok(
        prompt: String,
        aspectRatio: String,
        config: AiProviderConfig,
        apiKey: String
    ): MangaGenerationResult {
        val baseUrl = config.customBaseUrl.ifBlank { "https://api.x.ai/v1" }
        val url = "$baseUrl/images/generations"

        val payload = JSONObject().apply {
            put("model", config.selectedModel.ifBlank { "grok-2-image" })
            put("prompt", prompt.take(3000))
            put("n", 1)
            put("response_format", "b64_json")
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val fallback = createStylizedMangaFallback(prompt, "xAI Grok Erreur (${response.code})", "Grok")
            return MangaGenerationResult.Error("xAI Grok erreur (${response.code}): $responseBody", fallback)
        }

        val json = JSONObject(responseBody)
        val dataArray = json.optJSONArray("data")
        val item = dataArray?.optJSONObject(0)

        val b64 = item?.optString("b64_json")
        if (!b64.isNullOrBlank()) {
            val path = saveBase64Image(b64, "grok")
            return MangaGenerationResult.Success(path, prompt, isAiGenerated = true)
        }

        val imgUrl = item?.optString("url")
        if (!imgUrl.isNullOrBlank()) {
            val path = downloadAndSaveImage(imgUrl, "grok")
            return MangaGenerationResult.Success(path, prompt, isAiGenerated = true)
        }

        val fallback = createStylizedMangaFallback(prompt, "Réponse Grok vide", "Grok")
        return MangaGenerationResult.Error("Aucune image reçue de Grok", fallback)
    }

    // =========================================================================
    // 4. Meta AI / FLUX via Together AI Implementation
    // =========================================================================
    private fun generateWithMetaTogether(
        prompt: String,
        aspectRatio: String,
        config: AiProviderConfig,
        apiKey: String
    ): MangaGenerationResult {
        val baseUrl = config.customBaseUrl.ifBlank { "https://api.together.xyz/v1" }
        val url = "$baseUrl/images/generations"

        val (w, h) = when (aspectRatio) {
            "9:16", "3:4" -> 768 to 1024
            "16:9", "4:3" -> 1024 to 768
            else -> 1024 to 1024
        }

        val payload = JSONObject().apply {
            put("model", config.selectedModel.ifBlank { "black-forest-labs/FLUX.1-schnell" })
            put("prompt", prompt.take(2500))
            put("width", w)
            put("height", h)
            put("steps", 4)
            put("n", 1)
            put("response_format", "b64_json")
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val fallback = createStylizedMangaFallback(prompt, "Meta/FLUX Erreur (${response.code})", "FLUX")
            return MangaGenerationResult.Error("Meta/Together erreur (${response.code}): $responseBody", fallback)
        }

        val json = JSONObject(responseBody)
        val dataArray = json.optJSONArray("data")
        val item = dataArray?.optJSONObject(0)

        val b64 = item?.optString("b64_json")
        if (!b64.isNullOrBlank()) {
            val path = saveBase64Image(b64, "meta_flux")
            return MangaGenerationResult.Success(path, prompt, isAiGenerated = true)
        }

        val imgUrl = item?.optString("url")
        if (!imgUrl.isNullOrBlank()) {
            val path = downloadAndSaveImage(imgUrl, "meta_flux")
            return MangaGenerationResult.Success(path, prompt, isAiGenerated = true)
        }

        val fallback = createStylizedMangaFallback(prompt, "Pas d'image reçue de FLUX", "FLUX")
        return MangaGenerationResult.Error("Aucune image reçue de Together/Meta", fallback)
    }

    // =========================================================================
    // 5. Alibaba DashScope (通义万相 - Tongyi Wanxiang) Implementation
    // =========================================================================
    private suspend fun generateWithAlibabaWanx(
        prompt: String,
        aspectRatio: String,
        config: AiProviderConfig,
        apiKey: String
    ): MangaGenerationResult {
        val baseUrl = config.customBaseUrl.ifBlank { "https://dashscope.aliyuncs.com" }
        val submitUrl = "$baseUrl/api/v1/services/aigc/text2image/image-synthesis"

        val size = when (aspectRatio) {
            "9:16", "3:4" -> "768*1280"
            "16:9", "4:3" -> "1280*768"
            else -> "1024*1024"
        }

        val inputObj = JSONObject().apply {
            put("prompt", prompt.take(1800))
        }

        val paramObj = JSONObject().apply {
            put("style", "<auto>")
            put("size", size)
            put("n", 1)
        }

        val root = JSONObject().apply {
            put("model", config.selectedModel.ifBlank { "wanx-v1" })
            put("input", inputObj)
            put("parameters", paramObj)
        }

        val request = Request.Builder()
            .url(submitUrl)
            .header("Authorization", "Bearer $apiKey")
            .header("X-DashScope-Async", "enable")
            .post(root.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val fallback = createStylizedMangaFallback(prompt, "Alibaba Wanx (${response.code})", "Wanxiang")
            return MangaGenerationResult.Error("DashScope Wanxiang erreur (${response.code}): $responseBody", fallback)
        }

        val json = JSONObject(responseBody)
        val output = json.optJSONObject("output")
        val taskId = output?.optString("task_id")

        if (taskId.isNullOrBlank()) {
            val fallback = createStylizedMangaFallback(prompt, "Erreur DashScope ID", "Wanxiang")
            return MangaGenerationResult.Error("Identifiant de tâche DashScope introuvable", fallback)
        }

        // Poll for completion (max 25 seconds)
        val pollUrl = "$baseUrl/api/v1/tasks/$taskId"
        var finalImageUrl: String? = null

        for (i in 0 until 12) {
            delay(2000)
            val pollReq = Request.Builder()
                .url(pollUrl)
                .header("Authorization", "Bearer $apiKey")
                .get()
                .build()
            val pollResp = okHttpClient.newCall(pollReq).execute()
            val pollBody = pollResp.body?.string() ?: ""
            if (pollResp.isSuccessful) {
                val pollJson = JSONObject(pollBody)
                val pollOutput = pollJson.optJSONObject("output")
                val status = pollOutput?.optString("task_status")
                if (status == "SUCCEEDED") {
                    val results = pollOutput.optJSONArray("results")
                    finalImageUrl = results?.optJSONObject(0)?.optString("url")
                    break
                } else if (status == "FAILED") {
                    val errMsg = pollOutput.optString("message", "Échec de rendu DashScope")
                    val fallback = createStylizedMangaFallback(prompt, errMsg, "Wanxiang")
                    return MangaGenerationResult.Error(errMsg, fallback)
                }
            }
        }

        if (!finalImageUrl.isNullOrBlank()) {
            val path = downloadAndSaveImage(finalImageUrl, "alibaba_wanx")
            return MangaGenerationResult.Success(path, prompt, isAiGenerated = true)
        }

        val fallback = createStylizedMangaFallback(prompt, "Délai Wanxiang dépassé", "Wanxiang")
        return MangaGenerationResult.Error("Délai d'attente de Wanxiang dépassé", fallback)
    }

    // =========================================================================
    // 6. Zhipu AI CogView 3 Implementation
    // =========================================================================
    private fun generateWithZhipuCogView(
        prompt: String,
        aspectRatio: String,
        config: AiProviderConfig,
        apiKey: String
    ): MangaGenerationResult {
        val baseUrl = config.customBaseUrl.ifBlank { "https://open.bigmodel.cn/api/paas/v4" }
        val url = "$baseUrl/images/generations"

        val size = when (aspectRatio) {
            "9:16", "3:4" -> "768x1344"
            "16:9", "4:3" -> "1344x768"
            else -> "1024x1024"
        }

        val payload = JSONObject().apply {
            put("model", config.selectedModel.ifBlank { "cogview-3" })
            put("prompt", prompt.take(1500))
            put("size", size)
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val fallback = createStylizedMangaFallback(prompt, "Zhipu CogView (${response.code})", "CogView 3")
            return MangaGenerationResult.Error("Zhipu AI CogView erreur (${response.code}): $responseBody", fallback)
        }

        val json = JSONObject(responseBody)
        val dataArray = json.optJSONArray("data")
        val imgUrl = dataArray?.optJSONObject(0)?.optString("url")

        if (!imgUrl.isNullOrBlank()) {
            val path = downloadAndSaveImage(imgUrl, "zhipu_cogview")
            return MangaGenerationResult.Success(path, prompt, isAiGenerated = true)
        }

        val fallback = createStylizedMangaFallback(prompt, "Pas d'image reçue de CogView", "CogView 3")
        return MangaGenerationResult.Error("Aucune URL d'image renvoyée par Zhipu CogView", fallback)
    }

    // =========================================================================
    // 7. SiliconFlow (Kolors / SDXL) Implementation
    // =========================================================================
    private fun generateWithSiliconFlow(
        prompt: String,
        aspectRatio: String,
        config: AiProviderConfig,
        apiKey: String
    ): MangaGenerationResult {
        val baseUrl = config.customBaseUrl.ifBlank { "https://api.siliconflow.cn/v1" }
        val url = "$baseUrl/images/generations"

        val size = when (aspectRatio) {
            "9:16", "3:4" -> "768x1024"
            "16:9", "4:3" -> "1024x768"
            else -> "1024x1024"
        }

        val payload = JSONObject().apply {
            put("model", config.selectedModel.ifBlank { "Kwai-Kolors/Kolors" })
            put("prompt", prompt.take(1800))
            put("image_size", size)
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val fallback = createStylizedMangaFallback(prompt, "SiliconFlow (${response.code})", "SiliconFlow")
            return MangaGenerationResult.Error("SiliconFlow erreur (${response.code}): $responseBody", fallback)
        }

        val json = JSONObject(responseBody)
        var imgUrl = json.optJSONArray("images")?.optJSONObject(0)?.optString("url")
        if (imgUrl.isNullOrBlank()) {
            imgUrl = json.optJSONArray("data")?.optJSONObject(0)?.optString("url")
        }

        if (!imgUrl.isNullOrBlank()) {
            val path = downloadAndSaveImage(imgUrl, "siliconflow")
            return MangaGenerationResult.Success(path, prompt, isAiGenerated = true)
        }

        val fallback = createStylizedMangaFallback(prompt, "Réponse SiliconFlow vide", "SiliconFlow")
        return MangaGenerationResult.Error("Aucune image reçue de SiliconFlow", fallback)
    }

    // =========================================================================
    // 8. Pollinations Free AI (Mode Instantané Gratuit Sans Clé)
    // =========================================================================
    private fun generateWithPollinationsFree(
        prompt: String,
        config: AiProviderConfig
    ): MangaGenerationResult {
        val model = config.selectedModel.ifBlank { "flux" }
        val cleanPrompt = prompt.take(600).replace("\n", " ")
        val encodedPrompt = URLEncoder.encode(cleanPrompt, "UTF-8")
        val seed = System.currentTimeMillis() % 10000000

        val url = "https://image.pollinations.ai/prompt/$encodedPrompt?model=$model&width=1024&height=1024&nologo=true&seed=$seed"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val fallback = createStylizedMangaFallback(prompt, "Erreur réseau Mode Gratuit (${response.code})", "Gratuit")
            return MangaGenerationResult.Error("Erreur Mode Gratuit (${response.code})", fallback)
        }

        val bytes = response.body?.bytes()
        if (bytes != null && bytes.isNotEmpty()) {
            val file = File(imagesDir, "panel_free_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { fos ->
                fos.write(bytes)
                fos.flush()
            }
            return MangaGenerationResult.Success(file.absolutePath, prompt, isAiGenerated = true)
        }

        val fallback = createStylizedMangaFallback(prompt, "Aucun flux d'image reçu", "Gratuit")
        return MangaGenerationResult.Error("Échec du téléchargement d'image gratuite", fallback)
    }

    // =========================================================================
    // 9. Custom OpenAI-Compatible Endpoint Implementation
    // =========================================================================
    private fun generateWithCustomOpenAi(
        prompt: String,
        aspectRatio: String,
        config: AiProviderConfig,
        apiKey: String
    ): MangaGenerationResult {
        val baseUrl = config.customBaseUrl.ifBlank { "https://api.openai.com/v1" }
        val url = if (baseUrl.endsWith("/images/generations")) baseUrl else "$baseUrl/images/generations"

        val payload = JSONObject().apply {
            put("model", config.selectedModel.ifBlank { "custom-manga-model" })
            put("prompt", prompt)
            put("n", 1)
            put("size", "1024x1024")
        }

        val reqBuilder = Request.Builder().url(url)
        if (apiKey.isNotBlank()) {
            reqBuilder.header("Authorization", "Bearer $apiKey")
        }
        val request = reqBuilder.post(payload.toString().toRequestBody("application/json".toMediaType())).build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val fallback = createStylizedMangaFallback(prompt, "API Perso (${response.code})", "Custom")
            return MangaGenerationResult.Error("API personnalisée erreur (${response.code}): $responseBody", fallback)
        }

        val json = JSONObject(responseBody)
        val dataArray = json.optJSONArray("data")
        val item = dataArray?.optJSONObject(0)

        val b64 = item?.optString("b64_json")
        if (!b64.isNullOrBlank()) {
            val path = saveBase64Image(b64, "custom")
            return MangaGenerationResult.Success(path, prompt, isAiGenerated = true)
        }

        val imgUrl = item?.optString("url")
        if (!imgUrl.isNullOrBlank()) {
            val path = downloadAndSaveImage(imgUrl, "custom")
            return MangaGenerationResult.Success(path, prompt, isAiGenerated = true)
        }

        val fallback = createStylizedMangaFallback(prompt, "Réponse API Perso vide", "Custom")
        return MangaGenerationResult.Error("Aucune image renvoyée par le serveur personnalisé", fallback)
    }

    // =========================================================================
    // Utilities
    // =========================================================================
    private fun downloadAndSaveImage(imageUrl: String, prefix: String): String {
        val request = Request.Builder().url(imageUrl).get().build()
        val response = okHttpClient.newCall(request).execute()
        val bytes = response.body?.bytes() ?: throw Exception("Impossible de télécharger l'image depuis $imageUrl")

        val file = File(imagesDir, "panel_${prefix}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { fos ->
            fos.write(bytes)
            fos.flush()
        }
        return file.absolutePath
    }

    private fun saveBase64Image(base64Data: String, prefix: String): String {
        val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
        val file = File(imagesDir, "panel_${prefix}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { fos ->
            fos.write(decodedBytes)
            fos.flush()
        }
        return file.absolutePath
    }

    private fun encodeImageToBase64(file: File): String? {
        return try {
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    fun createStylizedMangaFallback(
        prompt: String,
        subtitle: String,
        badgeText: String = "Studio Démo Manga"
    ): String {
        val width = 768
        val height = 768
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

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

        // Inner dramatic frame
        val framePaint = Paint().apply {
            color = Color.parseColor("#FF2A55")
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRect(24f, 24f, (width - 24).toFloat(), (height - 24).toFloat(), framePaint)

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

        val onomatoPaint = Paint().apply {
            color = Color.parseColor("#FFD166")
            textSize = 42f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("ゴゴゴ...", 50f, 100f, onomatoPaint)
        canvas.drawText("ドドド！", (width - 200).toFloat(), 100f, onomatoPaint)

        val cardPaint = Paint().apply {
            color = Color.parseColor("#1A1D2B")
            style = Paint.Style.FILL
        }
        canvas.drawRect(36f, (height - 210).toFloat(), (width - 36).toFloat(), (height - 36).toFloat(), cardPaint)

        val badgePaint = Paint().apply {
            color = Color.parseColor("#FF2A55")
            style = Paint.Style.FILL
        }
        canvas.drawRect(36f, (height - 210).toFloat(), 280f, (height - 170).toFloat(), badgePaint)

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
