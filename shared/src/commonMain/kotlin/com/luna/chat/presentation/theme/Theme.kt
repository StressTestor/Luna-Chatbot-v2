package com.luna.chat.presentation.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun LunaThemeWithId(
    themeId: String = "rainbow",
    content: @Composable () -> Unit
) {
    val baseColorScheme = LunaColorSchemes.getColorSchemeForTheme(themeId)
    val animatedColorScheme = animateColorScheme(baseColorScheme)

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
private fun animateColorScheme(targetColorScheme: ColorScheme): ColorScheme {
    val animationSpec = tween<Color>(durationMillis = 300)

    val primary by animateColorAsState(targetColorScheme.primary, animationSpec, label = "primary")
    val onPrimary by animateColorAsState(targetColorScheme.onPrimary, animationSpec, label = "onPrimary")
    val primaryContainer by animateColorAsState(targetColorScheme.primaryContainer, animationSpec, label = "primaryContainer")
    val onPrimaryContainer by animateColorAsState(targetColorScheme.onPrimaryContainer, animationSpec, label = "onPrimaryContainer")
    val secondary by animateColorAsState(targetColorScheme.secondary, animationSpec, label = "secondary")
    val onSecondary by animateColorAsState(targetColorScheme.onSecondary, animationSpec, label = "onSecondary")
    val secondaryContainer by animateColorAsState(targetColorScheme.secondaryContainer, animationSpec, label = "secondaryContainer")
    val onSecondaryContainer by animateColorAsState(targetColorScheme.onSecondaryContainer, animationSpec, label = "onSecondaryContainer")
    val tertiary by animateColorAsState(targetColorScheme.tertiary, animationSpec, label = "tertiary")
    val onTertiary by animateColorAsState(targetColorScheme.onTertiary, animationSpec, label = "onTertiary")
    val tertiaryContainer by animateColorAsState(targetColorScheme.tertiaryContainer, animationSpec, label = "tertiaryContainer")
    val onTertiaryContainer by animateColorAsState(targetColorScheme.onTertiaryContainer, animationSpec, label = "onTertiaryContainer")
    val background by animateColorAsState(targetColorScheme.background, animationSpec, label = "background")
    val onBackground by animateColorAsState(targetColorScheme.onBackground, animationSpec, label = "onBackground")
    val surface by animateColorAsState(targetColorScheme.surface, animationSpec, label = "surface")
    val onSurface by animateColorAsState(targetColorScheme.onSurface, animationSpec, label = "onSurface")
    val surfaceVariant by animateColorAsState(targetColorScheme.surfaceVariant, animationSpec, label = "surfaceVariant")
    val onSurfaceVariant by animateColorAsState(targetColorScheme.onSurfaceVariant, animationSpec, label = "onSurfaceVariant")

    return remember(
        primary, onPrimary, primaryContainer, onPrimaryContainer,
        secondary, onSecondary, secondaryContainer, onSecondaryContainer,
        tertiary, onTertiary, tertiaryContainer, onTertiaryContainer,
        background, onBackground, surface, onSurface,
        surfaceVariant, onSurfaceVariant
    ) {
        targetColorScheme.copy(
            primary = primary, onPrimary = onPrimary,
            primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
            secondary = secondary, onSecondary = onSecondary,
            secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary, onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
            background = background, onBackground = onBackground,
            surface = surface, onSurface = onSurface,
            surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant
        )
    }
}
