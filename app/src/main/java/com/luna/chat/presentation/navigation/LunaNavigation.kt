package com.luna.chat.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.luna.chat.presentation.ui.screen.ChatScreen
import com.luna.chat.presentation.ui.screen.SettingsScreen

/**
 * Navigation destinations for Luna Chat app
 */
sealed class LunaDestination(val route: String) {
    object Chat : LunaDestination("chat")
    object Settings : LunaDestination("settings")
}

/**
 * Main navigation component for Luna Chat app
 * Handles navigation between chat and settings screens
 */
@Composable
fun LunaNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = LunaDestination.Chat.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Chat Screen - Main conversation interface
        composable(LunaDestination.Chat.route) {
            ChatScreen(
                onSettingsClick = {
                    navController.navigate(LunaDestination.Settings.route)
                }
            )
        }
        
        // Settings Screen - App configuration and preferences
        composable(LunaDestination.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}