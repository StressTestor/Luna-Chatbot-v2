package com.luna.chat.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.luna.chat.presentation.ui.screen.ChatScreen
import com.luna.chat.presentation.ui.screen.SettingsScreen

sealed class LunaDestination {
    data object Chat : LunaDestination()
    data object Settings : LunaDestination()
}

@Composable
fun LunaNavigation(
    modifier: Modifier = Modifier
) {
    var currentDestination: LunaDestination by remember { mutableStateOf(LunaDestination.Chat) }

    AnimatedContent(
        targetState = currentDestination,
        transitionSpec = {
            if (targetState is LunaDestination.Settings) {
                slideInHorizontally { width -> width } + fadeIn(tween(300)) togetherWith
                    slideOutHorizontally { width -> -width } + fadeOut(tween(300))
            } else {
                slideInHorizontally { width -> -width } + fadeIn(tween(300)) togetherWith
                    slideOutHorizontally { width -> width } + fadeOut(tween(300))
            }
        },
        modifier = modifier,
        label = "navigation"
    ) { destination ->
        when (destination) {
            is LunaDestination.Chat -> {
                ChatScreen(
                    onSettingsClick = { currentDestination = LunaDestination.Settings }
                )
            }
            is LunaDestination.Settings -> {
                SettingsScreen(
                    onBackClick = { currentDestination = LunaDestination.Chat }
                )
            }
        }
    }
}
