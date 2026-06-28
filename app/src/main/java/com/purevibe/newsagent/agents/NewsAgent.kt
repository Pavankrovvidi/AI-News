package com.purevibe.newsagent.agents

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.purevibe.newsagent.ai.Article

/**
 * One news agent = one category. Each agent knows:
 *  - which real-news category to pull from (GNews), and
 *  - how to ask the AI to summarise the REAL articles it fetched.
 */
data class NewsAgent(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val gnewsCategory: String,
    val defaultQuery: String,
    val aiSystemRole: String
) {
    /** Builds a grounded summary prompt from the REAL fetched articles (no hallucination). */
    fun summaryPrompt(articles: List<Article>): String {
        val list = articles.take(8).mapIndexed { i, a ->
            "${i + 1}. ${a.title} — ${a.sourceName} (${a.date})\n   ${a.description}"
        }.joinToString("\n")
        return "Here are real $title headlines fetched just now:\n\n$list\n\n" +
            "Write a short, easy-to-read briefing of these. For each point, mention the source in brackets. " +
            "Use ONLY the information above — do not invent anything that is not in these headlines."
    }
}

object AgentRegistry {

    val agents: List<NewsAgent> = listOf(
        NewsAgent(
            id = "sports",
            title = "Sports",
            subtitle = "Latest sports news",
            icon = Icons.Filled.SportsSoccer,
            color = Color(0xFF2E7D32),
            gnewsCategory = "sports",
            defaultQuery = "",
            aiSystemRole = "You are a sports news reporter who writes clear, factual briefs."
        ),
        NewsAgent(
            id = "ai",
            title = "AI & Tech",
            subtitle = "Latest AI & tech news",
            icon = Icons.Filled.SmartToy,
            color = Color(0xFF1565C0),
            gnewsCategory = "technology",
            defaultQuery = "artificial intelligence",
            aiSystemRole = "You are a technology reporter specialising in artificial intelligence."
        ),
        NewsAgent(
            id = "politics",
            title = "Politics",
            subtitle = "Latest political news",
            icon = Icons.Filled.Gavel,
            color = Color(0xFF6A1B9A),
            gnewsCategory = "nation",
            defaultQuery = "",
            aiSystemRole = "You are a neutral political news reporter. Present facts, not opinions."
        ),
        NewsAgent(
            id = "stocks",
            title = "Stock Market",
            subtitle = "Latest market news",
            icon = Icons.Filled.TrendingUp,
            color = Color(0xFFC62828),
            gnewsCategory = "business",
            defaultQuery = "",
            aiSystemRole = "You are a financial markets reporter. Be precise; this is information, not investment advice."
        ),
        NewsAgent(
            id = "movies",
            title = "Movies",
            subtitle = "Latest movie news",
            icon = Icons.Filled.Movie,
            color = Color(0xFFE65100),
            gnewsCategory = "entertainment",
            defaultQuery = "",
            aiSystemRole = "You are an entertainment reporter covering film releases and movie news."
        )
    )

    fun byId(id: String?): NewsAgent? = agents.firstOrNull { it.id == id }
}
