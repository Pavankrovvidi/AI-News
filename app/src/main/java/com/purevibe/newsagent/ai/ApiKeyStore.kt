package com.purevibe.newsagent.ai

import android.content.Context
import android.content.SharedPreferences

/** Saves the chosen provider, one API key per provider, and optional model overrides. */
class ApiKeyStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("news_agent_settings", Context.MODE_PRIVATE)

    var selectedProvider: AiProvider
        get() = AiProvider.fromId(prefs.getString("selected_provider", AiProvider.GEMINI.id))
        set(value) = prefs.edit().putString("selected_provider", value.id).apply()

    fun getApiKey(p: AiProvider): String = prefs.getString("api_key_${p.id}", "").orEmpty()

    fun setApiKey(p: AiProvider, key: String) {
        prefs.edit().putString("api_key_${p.id}", key.trim()).apply()
    }

    fun getModel(p: AiProvider): String =
        prefs.getString("model_${p.id}", null)?.takeIf { it.isNotBlank() } ?: p.defaultModel

    fun setModel(p: AiProvider, model: String) {
        prefs.edit().putString("model_${p.id}", model.trim()).apply()
    }

    fun isReady(): Boolean = getApiKey(selectedProvider).isNotBlank()

    /** Key for the GNews real-news API (gives real articles with sources + links). */
    var newsApiKey: String
        get() = prefs.getString("news_api_key", "").orEmpty()
        set(value) = prefs.edit().putString("news_api_key", value.trim()).apply()
}
