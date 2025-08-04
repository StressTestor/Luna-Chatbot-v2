package com.luna.chat.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.chat.presentation.viewmodel.SettingsViewModel

/**
 * Theme provider that manages theme state and provides the current theme to the app
 */
@Composable
fun LunaThemeProvider(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val currentTheme by settingsViewModel.currentTheme.collectAsState()
    
    LunaTheme(
        content = content
    )
}

/**
 * Standalone theme provider for when you need to specify a theme directly
 */
@Composable
fun LunaThemeProvider(
    themeId: String,
    content: @Composable () -> Unit
) {
    LunaTheme(
        content = content
    )
}