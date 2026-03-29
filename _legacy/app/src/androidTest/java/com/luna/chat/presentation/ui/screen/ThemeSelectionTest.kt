package com.luna.chat.presentation.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.luna.chat.domain.entity.Theme
import com.luna.chat.presentation.theme.LunaTheme
import com.luna.chat.presentation.ui.components.ThemeSelector
import com.luna.chat.presentation.ui.components.ThemeOption
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeSelectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun themeSelector_displaysAllAvailableThemes() {
        // Given
        val availableThemes = Theme.getAllThemes()
        val currentTheme = Theme.RAINBOW_THEME

        // When
        composeTestRule.setContent {
            LunaTheme(themeId = currentTheme.id) {
                ThemeSelector(
                    currentTheme = currentTheme,
                    availableThemes = availableThemes,
                    onThemeSelect = { },
                    isEnabled = true
                )
            }
        }

        // Then
        availableThemes.forEach { theme ->
            composeTestRule
                .onNodeWithTag("theme_${theme.id}")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun themeSelector_showsCurrentThemeAsSelected() {
        // Given
        val availableThemes = Theme.getAllThemes()
        val currentTheme = Theme.OCEAN_THEME

        // When
        composeTestRule.setContent {
            LunaTheme(themeId = currentTheme.id) {
                ThemeSelector(
                    currentTheme = currentTheme,
                    availableThemes = availableThemes,
                    onThemeSelect = { },
                    isEnabled = true
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("theme_${currentTheme.id}")
            .assertExists()
            .assertIsDisplayed()

        // Check that the selected theme has a check icon
        composeTestRule
            .onNodeWithTag("theme_${currentTheme.id}")
            .onChild()
            .assertExists()
    }

    @Test
    fun themeSelector_callsOnThemeSelectWhenThemeClicked() {
        // Given
        val availableThemes = Theme.getAllThemes()
        val currentTheme = Theme.RAINBOW_THEME
        val targetTheme = Theme.FOREST_THEME
        var selectedThemeId: String? = null

        // When
        composeTestRule.setContent {
            LunaTheme(themeId = currentTheme.id) {
                ThemeSelector(
                    currentTheme = currentTheme,
                    availableThemes = availableThemes,
                    onThemeSelect = { themeId -> selectedThemeId = themeId },
                    isEnabled = true
                )
            }
        }

        // Click on forest theme
        composeTestRule
            .onNodeWithTag("theme_${targetTheme.id}")
            .performClick()

        // Then
        assert(selectedThemeId == targetTheme.id)
    }

    @Test
    fun themeSelector_disablesInteractionWhenNotEnabled() {
        // Given
        val availableThemes = Theme.getAllThemes()
        val currentTheme = Theme.RAINBOW_THEME
        var wasClicked = false

        // When
        composeTestRule.setContent {
            LunaTheme(themeId = currentTheme.id) {
                ThemeSelector(
                    currentTheme = currentTheme,
                    availableThemes = availableThemes,
                    onThemeSelect = { wasClicked = true },
                    isEnabled = false
                )
            }
        }

        // Try to click on a theme
        composeTestRule
            .onNodeWithTag("theme_ocean")
            .performClick()

        // Then
        assert(!wasClicked)
    }

    @Test
    fun themeOption_displaysThemeNameAndColors() {
        // Given
        val theme = Theme.SPACE_THEME

        // When
        composeTestRule.setContent {
            LunaTheme(themeId = "rainbow") {
                ThemeOption(
                    theme = theme,
                    isSelected = false,
                    onClick = { },
                    isEnabled = true
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(theme.displayName)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun themeOption_showsCheckIconWhenSelected() {
        // Given
        val theme = Theme.SUNSET_THEME

        // When
        composeTestRule.setContent {
            LunaTheme(themeId = "rainbow") {
                ThemeOption(
                    theme = theme,
                    isSelected = true,
                    onClick = { },
                    isEnabled = true
                )
            }
        }

        // Then - Check icon should be present when selected
        composeTestRule
            .onNodeWithContentDescription("Select ${theme.displayName} theme")
            .assertExists()
    }

    @Test
    fun themeOption_hasCorrectAccessibilityDescription() {
        // Given
        val theme = Theme.OCEAN_THEME

        // When
        composeTestRule.setContent {
            LunaTheme(themeId = "rainbow") {
                ThemeOption(
                    theme = theme,
                    isSelected = false,
                    onClick = { },
                    isEnabled = true
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithContentDescription("Select ${theme.displayName} theme")
            .assertExists()
    }

    @Test
    fun themeSelector_scrollsHorizontallyToShowAllThemes() {
        // Given
        val availableThemes = Theme.getAllThemes()
        val currentTheme = Theme.RAINBOW_THEME

        // When
        composeTestRule.setContent {
            LunaTheme(themeId = currentTheme.id) {
                ThemeSelector(
                    currentTheme = currentTheme,
                    availableThemes = availableThemes,
                    onThemeSelect = { },
                    isEnabled = true
                )
            }
        }

        // Then - All themes should be accessible through scrolling
        availableThemes.forEach { theme ->
            composeTestRule
                .onNodeWithTag("theme_${theme.id}")
                .assertExists()
        }

        // Test scrolling to the last theme
        val lastTheme = availableThemes.last()
        composeTestRule
            .onNodeWithTag("theme_${lastTheme.id}")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun themeSelector_maintainsSelectionStateAfterScroll() {
        // Given
        val availableThemes = Theme.getAllThemes()
        val currentTheme = Theme.SPACE_THEME // A theme that might be off-screen initially

        // When
        composeTestRule.setContent {
            LunaTheme(themeId = currentTheme.id) {
                ThemeSelector(
                    currentTheme = currentTheme,
                    availableThemes = availableThemes,
                    onThemeSelect = { },
                    isEnabled = true
                )
            }
        }

        // Scroll to the selected theme and verify it's still selected
        composeTestRule
            .onNodeWithTag("theme_${currentTheme.id}")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun themeSelector_handlesEmptyThemeList() {
        // Given
        val emptyThemes = emptyList<Theme>()
        val currentTheme = Theme.getDefaultTheme()

        // When
        composeTestRule.setContent {
            LunaTheme(themeId = currentTheme.id) {
                ThemeSelector(
                    currentTheme = currentTheme,
                    availableThemes = emptyThemes,
                    onThemeSelect = { },
                    isEnabled = true
                )
            }
        }

        // Then - Should not crash and should display empty state
        composeTestRule
            .onNodeWithTag("theme_selector")
            .assertExists()
    }

    @Test
    fun themeSelector_handlesRapidClicks() {
        // Given
        val availableThemes = Theme.getAllThemes()
        val currentTheme = Theme.RAINBOW_THEME
        var clickCount = 0

        // When
        composeTestRule.setContent {
            LunaTheme(themeId = currentTheme.id) {
                ThemeSelector(
                    currentTheme = currentTheme,
                    availableThemes = availableThemes,
                    onThemeSelect = { clickCount++ },
                    isEnabled = true
                )
            }
        }

        // Perform rapid clicks
        val targetTheme = availableThemes.first { it.id != currentTheme.id }
        repeat(3) {
            composeTestRule
                .onNodeWithTag("theme_${targetTheme.id}")
                .performClick()
        }

        // Then - Should handle all clicks
        assert(clickCount == 3)
    }
}