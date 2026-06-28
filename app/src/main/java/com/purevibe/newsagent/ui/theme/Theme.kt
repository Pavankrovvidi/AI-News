package com.purevibe.newsagent.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Brand = Color(0xFF0E5A4B)
private val BrandLight = Color(0xFFA8E063)

private val LightColors = lightColorScheme(
    primary = Brand,
    secondary = BrandLight,
    background = Color(0xFFF5F7F6),
    surface = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    primary = BrandLight,
    secondary = Brand,
    background = Color(0xFF101513),
    surface = Color(0xFF1A211E)
)

@Composable
fun NewsAgentTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
