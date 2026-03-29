package com.luna.chat.presentation.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Main Luna theme composable that supports dynamic theme switching
 */
@Composable
fun LunaThemeWithId(
    themeId: String = "rainbow",
    content: @Composable () -> Unit
) {
    val baseColorScheme = LunaColorSchemes.getColorSchemeForTheme(themeId)
    val isDarkTheme = LunaColorSchemes.isThemeDark(themeId)
    
    // Animate color transitions for smooth theme switching
    val animatedColorScheme = animateColorScheme(baseColorScheme)
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = animatedColorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Animates color scheme transitions for smooth theme switching
 */
@Composable
private fun animateColorScheme(targetColorScheme: ColorScheme): ColorScheme {
    val animationSpec = tween<Color>(durationMillis = 300)
    
    val primary by animateColorAsState(
        targetValue = targetColorScheme.primary,
        animationSpec = animationSpec,
        label = "primary"
    )
    val onPrimary by animateColorAsState(
        targetValue = targetColorScheme.onPrimary,
        animationSpec = animationSpec,
        label = "onPrimary"
    )
    val primaryContainer by animateColorAsState(
        targetValue = targetColorScheme.primaryContainer,
        animationSpec = animationSpec,
        label = "primaryContainer"
    )
    val onPrimaryContainer by animateColorAsState(
        targetValue = targetColorScheme.onPrimaryContainer,
        animationSpec = animationSpec,
        label = "onPrimaryContainer"
    )
    val secondary by animateColorAsState(
        targetValue = targetColorScheme.secondary,
        animationSpec = animationSpec,
        label = "secondary"
    )
    val onSecondary by animateColorAsState(
        targetValue = targetColorScheme.onSecondary,
        animationSpec = animationSpec,
        label = "onSecondary"
    )
    val secondaryContainer by animateColorAsState(
        targetValue = targetColorScheme.secondaryContainer,
        animationSpec = animationSpec,
        label = "secondaryContainer"
    )
    val onSecondaryContainer by animateColorAsState(
        targetValue = targetColorScheme.onSecondaryContainer,
        animationSpec = animationSpec,
        label = "onSecondaryContainer"
    )
    val tertiary by animateColorAsState(
        targetValue = targetColorScheme.tertiary,
        animationSpec = animationSpec,
        label = "tertiary"
    )
    val onTertiary by animateColorAsState(
        targetValue = targetColorScheme.onTertiary,
        animationSpec = animationSpec,
        label = "onTertiary"
    )
    val tertiaryContainer by animateColorAsState(
        targetValue = targetColorScheme.tertiaryContainer,
        animationSpec = animationSpec,
        label = "tertiaryContainer"
    )
    val onTertiaryContainer by animateColorAsState(
        targetValue = targetColorScheme.onTertiaryContainer,
        animationSpec = animationSpec,
        label = "onTertiaryContainer"
    )
    val background by animateColorAsState(
        targetValue = targetColorScheme.background,
        animationSpec = animationSpec,
        label = "background"
    )
    val onBackground by animateColorAsState(
        targetValue = targetColorScheme.onBackground,
        animationSpec = animationSpec,
        label = "onBackground"
    )
    val surface by animateColorAsState(
        targetValue = targetColorScheme.surface,
        animationSpec = animationSpec,
        label = "surface"
    )
    val onSurface by animateColorAsState(
        targetValue = targetColorScheme.onSurface,
        animationSpec = animationSpec,
        label = "onSurface"
    )
    val surfaceVariant by animateColorAsState(
        targetValue = targetColorScheme.surfaceVariant,
        animationSpec = animationSpec,
        label = "surfaceVariant"
    )
    val onSurfaceVariant by animateColorAsState(
        targetValue = targetColorScheme.onSurfaceVariant,
        animationSpec = animationSpec,
        label = "onSurfaceVariant"
    )
    
    return remember(
        primary, onPrimary, primaryContainer, onPrimaryContainer,
        secondary, onSecondary, secondaryContainer, onSecondaryContainer,
        tertiary, onTertiary, tertiaryContainer, onTertiaryContainer,
        background, onBackground, surface, onSurface,
        surfaceVariant, onSurfaceVariant
    ) {
        targetColorScheme.copy(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant
        )
    }
}