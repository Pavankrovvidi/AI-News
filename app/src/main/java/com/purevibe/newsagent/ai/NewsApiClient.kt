package com.purevibe.newsagent.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** A single real news article returned by the news API. */
data class Article(
    val title: String,
    val description: String,
    val sourceName: String,
    val url: String,
    val publishedAt: String
) {
    /** Just the date part, e.g. 2026-06-28. */
    val date: String get() = publishedAt.substringBefore("T").ifBlank { "" }
}

/**
 * Fetches REAL news articles (with source name + url + date) from GNews.
 * This is what makes the news verifiable: every item links back to its origin.
 *
 * Free key: https://gnews.io  (free tier, no card required)
 */
class NewsApiClient(private val store: ApiKeyStore) {

    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .build()

    /**
     * @param category GNews category (sports, technology, business, entertainment, nation…)
     * @param query    optional free-text search; when non-blank it overrides [category]
     */
    suspend fun fetch(category: String, query: String): List<Article> =
        withContext(Dispatchers.IO) {
            val key = store.newsApiKey
            if (key.isBlank()) {
                throw AiException("No News API key set. Open Settings and add a free GNews key to get real, sourced news.")
            }
            val cleaned = query.replace(Regex("[^\\p{L}\\p{N} ]"), " ").trim().replace(Regex("\\s+"), " ")
            val url = if (cleaned.isNotBlank()) {
                val q = URLEncoder.encode(cleaned, "UTF-8")
                "https://gnews.io/api/v4/search?q=$q&lang=en&max=10&apikey=$key"
            } else {
                "https://gnews.io/api/v4/top-headlines?category=$category&lang=en&max=10&apikey=$key"
            }

            val req = Request.Builder().url(url).get().build()
            http.newCall(req).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    when (resp.code) {
                        401, 403 -> throw AiException("News API rejected the key (HTTP ${resp.code}). Check it in Settings.")
                        429 -> throw AiException("News API daily limit reached. Try again later.")
                        else -> throw AiException("News API error ${resp.code}: $raw")
                    }
                }
                parse(raw)
            }
        }

    private fun parse(raw: String): List<Article> {
        val root = runCatching { JSONObject(raw) }
            .getOrElse { throw AiException("News API returned an unexpected response.") }
        val arr = root.optJSONArray("articles") ?: return emptyList()
        val out = ArrayList<Article>(arr.length())
        for (i in 0 until arr.length()) {
            val a = arr.optJSONObject(i) ?: continue
            out.add(
                Article(
                    title = a.optString("title").trim(),
                    description = a.optString("description").trim(),
                    sourceName = a.optJSONObject("source")?.optString("name").orEmpty().ifBlank { "Unknown source" },
                    url = a.optString("url"),
                    publishedAt = a.optString("publishedAt")
                )
            )
        }
        return out
    }
}
