package com.purevibe.newsagent.ai

/** The four AI providers the user can choose between in Settings. */
enum class AiProvider(
    val id: String,
    val label: String,
    val defaultModel: String,
    val keyHint: String,
    val getKeyUrl: String
) {
    CLAUDE("claude", "Claude (Anthropic)", "claude-sonnet-4-6", "Starts with sk-ant-", "https://console.anthropic.com/settings/keys"),
    OPENAI("openai", "OpenAI", "gpt-4o-mini", "Starts with sk-", "https://platform.openai.com/api-keys"),
    GEMINI("gemini", "Google Gemini", "gemini-2.0-flash", "Starts with AIza", "https://aistudio.google.com/app/apikey"),
    GROQ("groq", "Groq", "llama-3.3-70b-versatile", "Starts with gsk_", "https://console.groq.com/keys");

    companion object {
        fun fromId(id: String?): AiProvider = entries.firstOrNull { it.id == id } ?: GEMINI
    }
}
