package com.example.ai.provider

import androidx.compose.ui.graphics.Color

/**
 * Represents the supported AI image generation providers.
 */
enum class AiProvider(
    val id: String,
    val displayName: String,
    val shortName: String,
    val subtitle: String,
    val badgeColor: Long,
    val defaultModel: String,
    val availableModels: List<String>,
    val keyPlaceholder: String,
    val requiresKey: Boolean,
    val helpUrl: String,
    val documentationHint: String
) {
    GEMINI(
        id = "gemini",
        displayName = "Google Gemini",
        shortName = "Gemini",
        subtitle = "Gemini 2.5 Flash Image & Multimodal",
        badgeColor = 0xFF4285F4,
        defaultModel = "gemini-2.5-flash-image",
        availableModels = listOf("gemini-2.5-flash-image", "imagen-3.0-generate-002"),
        keyPlaceholder = "AIzaSy...",
        requiresKey = true,
        helpUrl = "https://aistudio.google.com/app/apikey",
        documentationHint = "Clé Google AI Studio (préconfigurée par défaut si définie dans les Secrets)"
    ),

    OPENAI(
        id = "openai",
        displayName = "OpenAI GPT (DALL-E)",
        shortName = "DALL-E 3",
        subtitle = "DALL-E 3 & GPT Image haute fidélité",
        badgeColor = 0xFF10A37F,
        defaultModel = "dall-e-3",
        availableModels = listOf("dall-e-3", "dall-e-2"),
        keyPlaceholder = "sk-proj-...",
        requiresKey = true,
        helpUrl = "https://platform.openai.com/api-keys",
        documentationHint = "Obtenez votre clé API sur la plateforme OpenAI (compte OpenAI avec crédits)"
    ),

    GROK(
        id = "grok",
        displayName = "xAI Grok",
        shortName = "Grok",
        subtitle = "Grok 2 Image & Aurora par xAI",
        badgeColor = 0xFF1DA1F2,
        defaultModel = "grok-2-image",
        availableModels = listOf("grok-2-image", "aurora"),
        keyPlaceholder = "xai-...",
        requiresKey = true,
        helpUrl = "https://console.x.ai/",
        documentationHint = "Clé API issue de la console développeur xAI (Grok)"
    ),

    META(
        id = "meta",
        displayName = "Meta AI & FLUX",
        shortName = "FLUX / Meta",
        subtitle = "FLUX.1 Schnell & Llama 3.2 Vision via Together/Replicate",
        badgeColor = 0xFF0866FF,
        defaultModel = "black-forest-labs/FLUX.1-schnell",
        availableModels = listOf(
            "black-forest-labs/FLUX.1-schnell",
            "black-forest-labs/FLUX.1-dev",
            "meta-llama/Llama-3.2-11B-Vision-Instruct"
        ),
        keyPlaceholder = "Clé Together AI / Replicate...",
        requiresKey = true,
        helpUrl = "https://api.together.xyz/settings/api-keys",
        documentationHint = "Utilise la passerelle Together AI ou Replicate pour les modèles open source de Meta et FLUX"
    ),

    ALIBABA_WANX(
        id = "alibaba_wanx",
        displayName = "Alibaba DashScope (通义万相)",
        shortName = "Wanxiang",
        subtitle = "Tongyi Wanxiang (Alibaba Cloud) spécialisé Manga & Manhua",
        badgeColor = 0xFFFF6A00,
        defaultModel = "wanx-v1",
        availableModels = listOf("wanx-v1", "wanx-2.1-t2i-turbo", "wanx-2.1-t2i-plus"),
        keyPlaceholder = "sk-...",
        requiresKey = true,
        helpUrl = "https://dashscope.console.aliyun.com/",
        documentationHint = "Clé Alibaba Cloud DashScope (leader des IA génératives en Asie)"
    ),

    ZHIPU_COGVIEW(
        id = "zhipu_cogview",
        displayName = "Zhipu AI (智谱 CogView)",
        shortName = "CogView 3",
        subtitle = "CogView-3 & GLM-4V par Zhipu AI (Bejing)",
        badgeColor = 0xFF3B82F6,
        defaultModel = "cogview-3",
        availableModels = listOf("cogview-3", "cogview-3-plus"),
        keyPlaceholder = "clé api zhipu...",
        requiresKey = true,
        helpUrl = "https://open.bigmodel.cn/usercenter/apikeys",
        documentationHint = "Clé Zhipu BigModel Open Platform (excellente maîtrise des traits manga et comics orientaux)"
    ),

    SILICONFLOW(
        id = "siliconflow",
        displayName = "SiliconFlow (硅基流动)",
        shortName = "SiliconFlow",
        subtitle = "Kwai-Kolors (Kuaishou), FLUX et SDXL asiatiques",
        badgeColor = 0xFF8B5CF6,
        defaultModel = "Kwai-Kolors/Kolors",
        availableModels = listOf(
            "Kwai-Kolors/Kolors",
            "black-forest-labs/FLUX.1-schnell",
            "stabilityai/stable-diffusion-3-medium"
        ),
        keyPlaceholder = "sk-...",
        requiresKey = true,
        helpUrl = "https://cloud.siliconflow.cn/account/ak",
        documentationHint = "Plateforme chinoise SiliconFlow supportant le modèle Kolors (très fort sur le style anime/manga)"
    ),

    POLLINATIONS_FREE(
        id = "pollinations_free",
        displayName = "Mode Gratuit (Sans Clé)",
        shortName = "Gratuit Direct",
        subtitle = "Génération instantanée FLUX / Manga sans compte ni carte",
        badgeColor = 0xFF10B981,
        defaultModel = "flux",
        availableModels = listOf("flux", "flux-anime", "turbo"),
        keyPlaceholder = "Aucune clé nécessaire !",
        requiresKey = false,
        helpUrl = "https://pollinations.ai",
        documentationHint = "100% gratuit et immédiatement fonctionnel sans inscription ni clé API"
    ),

    CUSTOM(
        id = "custom",
        displayName = "API Personnalisée (OpenAI Compliant)",
        shortName = "Personnalisé",
        subtitle = "Votre passerelle OneAPI, NewAPI, Groq ou serveur local",
        badgeColor = 0xFFEAB308,
        defaultModel = "custom-manga-model",
        availableModels = listOf("custom-manga-model", "flux", "sdxl"),
        keyPlaceholder = "Bearer token / clé...",
        requiresKey = false,
        helpUrl = "https://github.com/openai/openai-openapi",
        documentationHint = "Permet de brancher tout serveur local ou proxy tiers compatible OpenAI /v1/images/generations"
    );

    companion object {
        fun fromId(id: String): AiProvider {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GEMINI
        }
    }
}
