package com.luna.chat.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

object LunaColorSchemes {

    val RainbowLightColorScheme = lightColorScheme(
        primary = RainbowPrimary,
        onPrimary = RainbowOnPrimary,
        primaryContainer = RainbowPrimary.copy(alpha = 0.1f),
        onPrimaryContainer = RainbowOnBackground,
        secondary = RainbowSecondary,
        onSecondary = RainbowOnSecondary,
        secondaryContainer = RainbowSecondary.copy(alpha = 0.1f),
        onSecondaryContainer = RainbowOnBackground,
        tertiary = RainbowTertiary,
        onTertiary = RainbowOnTertiary,
        tertiaryContainer = RainbowTertiary.copy(alpha = 0.1f),
        onTertiaryContainer = RainbowOnBackground,
        error = Color(0xFFDC2626),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFF991B1B),
        background = RainbowBackground,
        onBackground = RainbowOnBackground,
        surface = RainbowSurface,
        onSurface = RainbowOnSurface,
        surfaceVariant = RainbowSurfaceVariant,
        onSurfaceVariant = RainbowOnSurfaceVariant,
        outline = RainbowOnSurfaceVariant.copy(alpha = 0.5f),
        outlineVariant = RainbowOnSurfaceVariant.copy(alpha = 0.2f),
        scrim = Color.Black,
        inverseSurface = RainbowOnBackground,
        inverseOnSurface = RainbowBackground,
        inversePrimary = RainbowPrimary.copy(alpha = 0.8f)
    )

    val OceanLightColorScheme = lightColorScheme(
        primary = OceanPrimary,
        onPrimary = OceanOnPrimary,
        primaryContainer = OceanPrimary.copy(alpha = 0.1f),
        onPrimaryContainer = OceanOnBackground,
        secondary = OceanSecondary,
        onSecondary = OceanOnSecondary,
        secondaryContainer = OceanSecondary.copy(alpha = 0.1f),
        onSecondaryContainer = OceanOnBackground,
        tertiary = OceanTertiary,
        onTertiary = OceanOnTertiary,
        tertiaryContainer = OceanTertiary.copy(alpha = 0.1f),
        onTertiaryContainer = OceanOnBackground,
        error = Color(0xFFDC2626),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFF991B1B),
        background = OceanBackground,
        onBackground = OceanOnBackground,
        surface = OceanSurface,
        onSurface = OceanOnSurface,
        surfaceVariant = OceanSurfaceVariant,
        onSurfaceVariant = OceanOnSurfaceVariant,
        outline = OceanOnSurfaceVariant.copy(alpha = 0.5f),
        outlineVariant = OceanOnSurfaceVariant.copy(alpha = 0.2f),
        scrim = Color.Black,
        inverseSurface = OceanOnBackground,
        inverseOnSurface = OceanBackground,
        inversePrimary = OceanPrimary.copy(alpha = 0.8f)
    )

    val ForestLightColorScheme = lightColorScheme(
        primary = ForestPrimary,
        onPrimary = ForestOnPrimary,
        primaryContainer = ForestPrimary.copy(alpha = 0.1f),
        onPrimaryContainer = ForestOnBackground,
        secondary = ForestSecondary,
        onSecondary = ForestOnSecondary,
        secondaryContainer = ForestSecondary.copy(alpha = 0.1f),
        onSecondaryContainer = ForestOnBackground,
        tertiary = ForestTertiary,
        onTertiary = ForestOnTertiary,
        tertiaryContainer = ForestTertiary.copy(alpha = 0.1f),
        onTertiaryContainer = ForestOnBackground,
        error = Color(0xFFDC2626),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFF991B1B),
        background = ForestBackground,
        onBackground = ForestOnBackground,
        surface = ForestSurface,
        onSurface = ForestOnSurface,
        surfaceVariant = ForestSurfaceVariant,
        onSurfaceVariant = ForestOnSurfaceVariant,
        outline = ForestOnSurfaceVariant.copy(alpha = 0.5f),
        outlineVariant = ForestOnSurfaceVariant.copy(alpha = 0.2f),
        scrim = Color.Black,
        inverseSurface = ForestOnBackground,
        inverseOnSurface = ForestBackground,
        inversePrimary = ForestPrimary.copy(alpha = 0.8f)
    )

    val SpaceDarkColorScheme = darkColorScheme(
        primary = SpacePrimary,
        onPrimary = SpaceOnPrimary,
        primaryContainer = SpacePrimary.copy(alpha = 0.3f),
        onPrimaryContainer = SpaceOnBackground,
        secondary = SpaceSecondary,
        onSecondary = SpaceOnSecondary,
        secondaryContainer = SpaceSecondary.copy(alpha = 0.3f),
        onSecondaryContainer = SpaceOnBackground,
        tertiary = SpaceTertiary,
        onTertiary = SpaceOnTertiary,
        tertiaryContainer = SpaceTertiary.copy(alpha = 0.3f),
        onTertiaryContainer = SpaceOnBackground,
        error = Color(0xFFEF4444),
        onError = Color(0xFF000000),
        errorContainer = Color(0xFF7F1D1D),
        onErrorContainer = Color(0xFFFECACA),
        background = SpaceBackground,
        onBackground = SpaceOnBackground,
        surface = SpaceSurface,
        onSurface = SpaceOnSurface,
        surfaceVariant = SpaceSurfaceVariant,
        onSurfaceVariant = SpaceOnSurfaceVariant,
        outline = SpaceOnSurfaceVariant.copy(alpha = 0.5f),
        outlineVariant = SpaceOnSurfaceVariant.copy(alpha = 0.2f),
        scrim = Color.Black,
        inverseSurface = SpaceOnBackground,
        inverseOnSurface = SpaceBackground,
        inversePrimary = SpacePrimary.copy(alpha = 0.8f)
    )

    val SunsetLightColorScheme = lightColorScheme(
        primary = SunsetPrimary,
        onPrimary = SunsetOnPrimary,
        primaryContainer = SunsetPrimary.copy(alpha = 0.1f),
        onPrimaryContainer = SunsetOnBackground,
        secondary = SunsetSecondary,
        onSecondary = SunsetOnSecondary,
        secondaryContainer = SunsetSecondary.copy(alpha = 0.1f),
        onSecondaryContainer = SunsetOnBackground,
        tertiary = SunsetTertiary,
        onTertiary = SunsetOnTertiary,
        tertiaryContainer = SunsetTertiary.copy(alpha = 0.1f),
        onTertiaryContainer = SunsetOnBackground,
        error = Color(0xFFDC2626),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFF991B1B),
        background = SunsetBackground,
        onBackground = SunsetOnBackground,
        surface = SunsetSurface,
        onSurface = SunsetOnSurface,
        surfaceVariant = SunsetSurfaceVariant,
        onSurfaceVariant = SunsetOnSurfaceVariant,
        outline = SunsetOnSurfaceVariant.copy(alpha = 0.5f),
        outlineVariant = SunsetOnSurfaceVariant.copy(alpha = 0.2f),
        scrim = Color.Black,
        inverseSurface = SunsetOnBackground,
        inverseOnSurface = SunsetBackground,
        inversePrimary = SunsetPrimary.copy(alpha = 0.8f)
    )

    fun getColorSchemeForTheme(themeId: String): ColorScheme {
        return when (themeId) {
            "rainbow" -> RainbowLightColorScheme
            "ocean" -> OceanLightColorScheme
            "forest" -> ForestLightColorScheme
            "space" -> SpaceDarkColorScheme
            "sunset" -> SunsetLightColorScheme
            else -> RainbowLightColorScheme
        }
    }

    fun isThemeDark(themeId: String): Boolean {
        return themeId == "space"
    }
}
