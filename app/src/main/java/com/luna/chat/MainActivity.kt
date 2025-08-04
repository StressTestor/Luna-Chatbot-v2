package com.luna.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.luna.chat.presentation.navigation.LunaNavigation
import com.luna.chat.presentation.theme.LunaThemeProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LunaThemeProvider {
                // Baseline system bars insets handling to avoid content overlap with status/navigation bars.
                Scaffold(
                    // Use explicit insets to avoid unresolved systemBars symbol on older compose versions
                    contentWindowInsets = WindowInsets.statusBars
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            // Apply navigation bar padding explicitly to avoid unresolved systemBars
                            .padding(WindowInsets.navigationBars.asPaddingValues()),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        LunaNavigation()
                    }
                }
            }
        }
    }
}