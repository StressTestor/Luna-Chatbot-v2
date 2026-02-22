package com.luna.chat.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.luna.chat.presentation.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LunaThemeProvider(
    settingsViewModel: SettingsViewModel = koinViewModel(),
    content: @Composable () -> Unit
) {
    val currentTheme by settingsViewModel.currentTheme.collectAsState()

    LunaThemeWithId(
        themeId = currentTheme.id,
        content = content
    )
}

@Composable
fun LunaThemeProvider(
    themeId: String,
    content: @Composable () -> Unit
) {
    LunaThemeWithId(
        themeId = themeId,
        content = content
    )
}
