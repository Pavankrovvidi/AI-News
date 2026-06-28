package com.purevibe.newsagent.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.purevibe.newsagent.ai.AiProvider
import com.purevibe.newsagent.ai.ApiKeyStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { ApiKeyStore(context) }

    var selected by remember { mutableStateOf(store.selectedProvider) }
    var apiKey by remember { mutableStateOf(store.getApiKey(store.selectedProvider)) }
    var model by remember { mutableStateOf(store.getModel(store.selectedProvider)) }
    var newsKey by remember { mutableStateOf(store.newsApiKey) }
    var keyVisible by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    fun selectProvider(p: AiProvider) {
        selected = p
        apiKey = store.getApiKey(p)
        model = store.getModel(p)
        status = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Choose your AI provider", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Every agent uses the selected provider to write its report.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(12.dp))

            Card(Modifier.fillMaxWidth()) {
                Column {
                    AiProvider.entries.forEachIndexed { index, provider ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = provider == selected, onClick = { selectProvider(provider) })
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            RadioButton(selected = provider == selected, onClick = { selectProvider(provider) })
                            Column(Modifier.padding(start = 8.dp)) {
                                Text(provider.label, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    if (store.getApiKey(provider).isNotBlank()) "Key saved" else "No key yet",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (store.getApiKey(provider).isNotBlank())
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                        if (index < AiProvider.entries.lastIndex) HorizontalDivider()
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("${selected.label} API key", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it; status = null },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Paste API key") },
                placeholder = { Text(selected.keyHint) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { keyVisible = !keyVisible }) {
                        Icon(
                            if (keyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (keyVisible) "Hide" else "Show"
                        )
                    }
                }
            )
            Text(
                "Get a key: ${selected.getKeyUrl}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Model (optional)") },
                placeholder = { Text(selected.defaultModel) }
            )

            Spacer(Modifier.height(20.dp))
            Text("Real news source (required for sourced news)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "A free GNews key fetches real articles with sources and links, so you can verify each story.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            OutlinedTextField(
                value = newsKey,
                onValueChange = { newsKey = it; status = null },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("GNews API key") },
                placeholder = { Text("Paste your GNews key") },
                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation()
            )
            Text(
                "Get a free key: https://gnews.io",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    store.selectedProvider = selected
                    store.setApiKey(selected, apiKey)
                    store.setModel(selected, model.ifBlank { selected.defaultModel })
                    store.newsApiKey = newsKey
                    status = when {
                        newsKey.isBlank() -> "Saved. Add a GNews key to get real, sourced news."
                        apiKey.isBlank() -> "Saved. ${selected.label} still needs a key for AI summaries."
                        else -> "Saved. Real news + ${selected.label} summaries are ready."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }

            status?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
