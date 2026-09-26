package com.example.ai.provider

data class AiProviderConfig(
    val provider: AiProvider = AiProvider.GEMINI,
    val apiKey: String = "",
    val selectedModel: String = provider.defaultModel,
    val customBaseUrl: String = "",
    val imageQuality: String = "standard", // "standard", "hd"
    val imageStyle: String = "vivid" // "vivid", "natural"
) {
    /**
     * Checks if this provider is configured sufficiently to make calls.
     */
    fun isReady(defaultGeminiKey: String = ""): Boolean {
        if (!provider.requiresKey) return true
        if (provider == AiProvider.GEMINI) {
            val key = apiKey.ifBlank { defaultGeminiKey }
            return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        }
        return apiKey.isNotBlank()
    }
}
