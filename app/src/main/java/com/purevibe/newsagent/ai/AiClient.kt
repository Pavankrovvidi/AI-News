package com.purevibe.newsagent.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * One entry point for AI text generation across Claude, OpenAI, Gemini, and Groq.
 * The active provider + key come from [ApiKeyStore], so callers (the news agents)
 * never need to know which provider is in use.
 *
 * Uses okhttp + org.json only — no extra dependencies.
 */
class AiClient(private val store: ApiKeyStore) {

    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    suspend fun generate(prompt: String, systemPrompt: String? = null): String =
        withContext(Dispatchers.IO) {
            val provider = store.selectedProvider
            val apiKey = store.getApiKey(provider)
            if (apiKey.isBlank()) {
                throw AiException("No API key set for ${provider.label}. Open Settings and add one.")
            }
            val model = store.getModel(provider)
            when (provider) {
                AiProvider.CLAUDE -> callClaude(apiKey, model, prompt, systemPrompt)
                AiProvider.OPENAI -> callOpenAi("https://api.openai.com/v1/chat/completions", apiKey, model, prompt, systemPrompt)
                AiProvider.GROQ -> callOpenAi("https://api.groq.com/openai/v1/chat/completions", apiKey, model, prompt, systemPrompt)
                AiProvider.GEMINI -> callGemini(apiKey, model, prompt, systemPrompt)
            }
        }

    private fun callClaude(apiKey: String, model: String, prompt: String, system: String?): String {
        val body = JSONObject().apply {
            put("model", model)
            put("max_tokens", 1500)
            system?.let { put("system", it) }
            put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
        }
        val req = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("content-type", "application/json")
            .post(body.toString().toRequestBody(jsonMedia))
            .build()
        val json = execute(req, AiProvider.CLAUDE)
        val content = json.optJSONArray("content") ?: throw apiError(json, AiProvider.CLAUDE)
        val sb = StringBuilder()
        for (i in 0 until content.length()) {
            val b = content.getJSONObject(i)
            if (b.optString("type") == "text") sb.append(b.optString("text"))
        }
        return sb.toString().ifBlank { throw apiError(json, AiProvider.CLAUDE) }
    }

    private fun callOpenAi(url: String, apiKey: String, model: String, prompt: String, system: String?): String {
        val messages = JSONArray()
        system?.let { messages.put(JSONObject().put("role", "system").put("content", it)) }
        messages.put(JSONObject().put("role", "user").put("content", prompt))
        val body = JSONObject().put("model", model).put("messages", messages)
        val provider = if (url.contains("groq")) AiProvider.GROQ else AiProvider.OPENAI
        val req = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody(jsonMedia))
            .build()
        val json = execute(req, provider)
        return json.optJSONArray("choices")?.optJSONObject(0)
            ?.optJSONObject("message")?.optString("content")
            ?.takeIf { it.isNotBlank() } ?: throw apiError(json, provider)
    }

    private fun callGemini(apiKey: String, model: String, prompt: String, system: String?): String {
        val body = JSONObject().apply {
            system?.let {
                put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", it))))
            }
            put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))))
        }
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val req = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody(jsonMedia))
            .build()
        val json = execute(req, AiProvider.GEMINI)
        return json.optJSONArray("candidates")?.optJSONObject(0)
            ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)
            ?.optString("text")?.takeIf { it.isNotBlank() } ?: throw apiError(json, AiProvider.GEMINI)
    }

    private fun execute(req: Request, provider: AiProvider): JSONObject {
        http.newCall(req).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                when (resp.code) {
                    401, 403 -> throw AiException("${provider.label} rejected the API key (HTTP ${resp.code}). Check it in Settings.")
                    429 -> throw AiException("${provider.label} rate limit hit. Try again shortly.")
                    else -> throw AiException("${provider.label} error ${resp.code}: $raw")
                }
            }
            return runCatching { JSONObject(raw) }
                .getOrElse { throw AiException("${provider.label} returned an unexpected response.") }
        }
    }

    private fun apiError(json: JSONObject, provider: AiProvider): AiException {
        val detail = json.optJSONObject("error")?.optString("message") ?: json.optString("error")
        return AiException("${provider.label} returned no text. ${detail.orEmpty()}".trim())
    }
}

class AiException(message: String) : Exception(message)
