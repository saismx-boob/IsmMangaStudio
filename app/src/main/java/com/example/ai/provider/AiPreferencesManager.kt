package com.example.ai.provider

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AiPreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("manga_ai_providers_prefs", Context.MODE_PRIVATE)

    private val _activeProvider = MutableStateFlow(loadActiveProvider())
    val activeProvider: StateFlow<AiProvider> = _activeProvider.asStateFlow()

    private val _currentConfig = MutableStateFlow(loadConfigFor(loadActiveProvider()))
    val currentConfig: StateFlow<AiProviderConfig> = _currentConfig.asStateFlow()

    fun loadActiveProvider(): AiProvider {
        val savedId = prefs.getString(PREF_ACTIVE_PROVIDER_ID, AiProvider.GEMINI.id) ?: AiProvider.GEMINI.id
        return AiProvider.fromId(savedId)
    }

    fun setActiveProvider(provider: AiProvider) {
        prefs.edit().putString(PREF_ACTIVE_PROVIDER_ID, provider.id).apply()
        _activeProvider.value = provider
        _currentConfig.value = loadConfigFor(provider)
    }

    fun loadConfigFor(provider: AiProvider): AiProviderConfig {
        val key = getApiKey(provider)
        val model = prefs.getString(keyForModel(provider), provider.defaultModel) ?: provider.defaultModel
        val baseUrl = prefs.getString(keyForBaseUrl(provider), defaultBaseUrlFor(provider)) ?: defaultBaseUrlFor(provider)
        val quality = prefs.getString(keyForQuality(provider), "standard") ?: "standard"
        val style = prefs.getString(keyForStyle(provider), "vivid") ?: "vivid"

        return AiProviderConfig(
            provider = provider,
            apiKey = key,
            selectedModel = model,
            customBaseUrl = baseUrl,
            imageQuality = quality,
            imageStyle = style
        )
    }

    fun saveConfig(config: AiProviderConfig) {
        val editor = prefs.edit()
        editor.putString(keyForApiKey(config.provider), config.apiKey)
        editor.putString(keyForModel(config.provider), config.selectedModel)
        editor.putString(keyForBaseUrl(config.provider), config.customBaseUrl)
        editor.putString(keyForQuality(config.provider), config.imageQuality)
        editor.putString(keyForStyle(config.provider), config.imageStyle)
        editor.apply()

        if (config.provider == _activeProvider.value) {
            _currentConfig.value = config
        }
    }

    fun getApiKey(provider: AiProvider): String {
        val stored = prefs.getString(keyForApiKey(provider), "") ?: ""
        if (stored.isNotBlank()) return stored
        // Fallback for Gemini to BuildConfig if not overridden
        if (provider == AiProvider.GEMINI) {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
                return buildKey
            }
        }
        return ""
    }

    fun setApiKey(provider: AiProvider, apiKey: String) {
        prefs.edit().putString(keyForApiKey(provider), apiKey.trim()).apply()
        if (provider == _activeProvider.value) {
            _currentConfig.value = _currentConfig.value.copy(apiKey = apiKey.trim())
        }
    }

    fun setModel(provider: AiProvider, model: String) {
        prefs.edit().putString(keyForModel(provider), model.trim()).apply()
        if (provider == _activeProvider.value) {
            _currentConfig.value = _currentConfig.value.copy(selectedModel = model.trim())
        }
    }

    fun setBaseUrl(provider: AiProvider, url: String) {
        prefs.edit().putString(keyForBaseUrl(provider), url.trim()).apply()
        if (provider == _activeProvider.value) {
            _currentConfig.value = _currentConfig.value.copy(customBaseUrl = url.trim())
        }
    }

    private fun keyForApiKey(provider: AiProvider) = "api_key_${provider.id}"
    private fun keyForModel(provider: AiProvider) = "model_${provider.id}"
    private fun keyForBaseUrl(provider: AiProvider) = "base_url_${provider.id}"
    private fun keyForQuality(provider: AiProvider) = "quality_${provider.id}"
    private fun keyForStyle(provider: AiProvider) = "style_${provider.id}"

    fun defaultBaseUrlFor(provider: AiProvider): String {
        return when (provider) {
            AiProvider.GEMINI -> "https://generativelanguage.googleapis.com"
            AiProvider.OPENAI -> "https://api.openai.com/v1"
            AiProvider.GROK -> "https://api.x.ai/v1"
            AiProvider.META -> "https://api.together.xyz/v1"
            AiProvider.ALIBABA_WANX -> "https://dashscope.aliyuncs.com"
            AiProvider.ZHIPU_COGVIEW -> "https://open.bigmodel.cn/api/paas/v4"
            AiProvider.SILICONFLOW -> "https://api.siliconflow.cn/v1"
            AiProvider.POLLINATIONS_FREE -> "https://image.pollinations.ai"
            AiProvider.CUSTOM -> "https://api.openai.com/v1"
        }
    }

    companion object {
        private const val PREF_ACTIVE_PROVIDER_ID = "active_ai_provider_id"
    }
}
