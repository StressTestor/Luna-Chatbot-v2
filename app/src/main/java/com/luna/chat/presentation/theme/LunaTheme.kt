package com.luna.chat.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Import colors from Color.kt to avoid conflicts

// Light Color Scheme
private val LunaLightColorScheme = lightColorScheme(
    primary = LunaPrimary,
    secondary = LunaSecondary,
    background = LunaBackground,
    surface = LunaSurface,
    onPrimary = LunaOnPrimary,
    onSecondary = LunaOnSecondary,
    onBackground = LunaOnBackground,
    onSurface = LunaOnSurface,
    primaryContainer = LunaPrimary.copy(alpha = 0.1f),
    secondaryContainer = LunaSecondary.copy(alpha = 0.1f),
    onPrimaryContainer = LunaPrimary,
    onSecondaryContainer = LunaSecondary
)

// Dark Color Scheme (for future use)
private val LunaDarkColorScheme = darkColorScheme(
    primary = LunaPrimary,
    secondary = LunaSecondary,
    background = Color(0xFF1E293B),
    surface = Color(0xFF334155),
    onPrimary = LunaOnPrimary,
    onSecondary = LunaOnSecondary,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

@Composable
fun LunaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        LunaDarkColorScheme
    } else {
        LunaLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// Alias for compatibility
@Composable
fun LunaThemeProvider(content: @Composable () -> Unit) {
    LunaTheme(content = content)
}