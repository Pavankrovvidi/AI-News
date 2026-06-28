package com.purevibe.newsagent.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.purevibe.newsagent.agents.AgentRegistry
import com.purevibe.newsagent.ai.AiClient
import com.purevibe.newsagent.ai.AiException
import com.purevibe.newsagent.ai.ApiKeyStore
import com.purevibe.newsagent.ai.Article
import com.purevibe.newsagent.ai.NewsApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentReportScreen(agentId: String?, onBack: () -> Unit, onSettings: () -> Unit) {
    val context = LocalContext.current
    val agent = remember(agentId) { AgentRegistry.byId(agentId) }
    val store = remember { ApiKeyStore(context) }
    val newsClient = remember { NewsApiClient(store) }
    val aiClient = remember { AiClient(store) }
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var articles by remember { mutableStateOf<List<Article>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    var summarizing by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf<String?>(null) }

    fun load() {
        val a = agent ?: return
        loading = true; error = null; articles = emptyList(); summary = null
        scope.launch {
            try {
                val q = query.ifBlank { a.defaultQuery }
                articles = newsClient.fetch(a.gnewsCategory, q)
                if (articles.isEmpty()) error = "No news found. Try a different search."
            } catch (e: AiException) {
                error = e.message
            } catch (e: Exception) {
                error = "Could not load news. Check your internet connection."
            } finally {
                loading = false
            }
        }
    }

    fun summarize() {
        val a = agent ?: return
        if (articles.isEmpty()) return
        summarizing = true; summary = null
        scope.launch {
            try {
                summary = aiClient.generate(a.summaryPrompt(articles), a.aiSystemRole)
            } catch (e: AiException) {
                summary = "AI summary failed: ${e.message}"
            } catch (e: Exception) {
                summary = "AI summary failed. Check your AI provider key in Settings."
            } finally {
                summarizing = false
            }
        }
    }

    LaunchedEffect(agentId) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(agent?.title ?: "News") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Column(modifier = Modifier.fillMaxSize().padding(inner)) {

            // --- Search bar ---
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                label = { Text("Search ${agent?.title ?: ""} news") },
                placeholder = { Text("Type a topic, then press search…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { load() }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )

            when {
                loading -> CenterMessage {
                    CircularProgressIndicator()
                    Spacer(Modifier.size(12.dp))
                    Text("Fetching real news…", style = MaterialTheme.typography.bodyMedium)
                }

                error != null -> CenterMessage {
                    Text(
                        error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.size(12.dp))
                    Button(onClick = onSettings) { Text("Open Settings") }
                    Spacer(Modifier.size(8.dp))
                    OutlinedButton(onClick = { load() }) { Text("Try again") }
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        // AI summary section (optional, grounded in the real articles below)
                        if (summary != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Text("AI Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.size(6.dp))
                                    Text(summary!!, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { summarize() },
                                enabled = !summarizing && articles.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text(if (summarizing) "Summarizing…" else "Summarize these with AI")
                            }
                        }
                        Spacer(Modifier.size(6.dp))
                        Text(
                            "Sources below — tap any card to open the original article.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    items(articles) { article ->
                        ArticleCard(article = article, onOpen = {
                            if (article.url.isNotBlank()) {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url)))
                                }
                            }
                        })
                    }

                    item { Spacer(Modifier.size(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(article: Article, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (article.description.isNotBlank()) {
                Spacer(Modifier.size(4.dp))
                Text(
                    article.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            Spacer(Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${article.sourceName}  •  ${article.date}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.size(6.dp))
                Icon(
                    Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open source",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CenterMessage(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { content() }
}
