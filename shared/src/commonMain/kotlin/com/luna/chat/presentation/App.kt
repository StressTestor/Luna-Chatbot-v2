package com.luna.chat.presentation

import androidx.compose.runtime.Composable
import com.luna.chat.presentation.navigation.LunaNavigation
import com.luna.chat.presentation.theme.LunaThemeProvider

@Composable
fun App() {
    LunaThemeProvider {
        LunaNavigation()
    }
}
