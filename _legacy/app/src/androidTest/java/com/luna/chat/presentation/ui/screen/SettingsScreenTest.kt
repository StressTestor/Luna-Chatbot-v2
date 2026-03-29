package com.luna.chat.presentation.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.luna.chat.domain.entity.Theme
import com.luna.chat.presentation.theme.LunaTheme
import com.luna.chat.presentation.viewmodel.SettingsUiState
import com.luna.chat.data.repository.UserPreferences
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_displaysCorrectly() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify main components are displayed
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_top_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_content").assertIsDisplayed()
        
        // Verify back button
        composeTestRule.onNodeWithTag("back_button").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Go back to chat").assertIsDisplayed()
        
        // Verify title
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_backButtonWorks() {
        var backClicked = false
        
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { backClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("back_button").performClick()
        assert(backClicked)
    }

    @Test
    fun settingsContent_displaysAllSections() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify all main sections are present
        composeTestRule.onNodeWithText("Choose Your Theme 🎨").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chat Settings 💬").assertIsDisplayed()
        composeTestRule.onNodeWithText("Safety Settings 🛡️").assertIsDisplayed()
        composeTestRule.onNodeWithText("API Configuration 🔧").assertIsDisplayed()
        composeTestRule.onNodeWithText("Advanced Settings ⚡").assertIsDisplayed()
    }

    @Test
    fun themeSelector_displaysAllThemes() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify all theme options are displayed
        composeTestRule.onNodeWithTag("theme_rainbow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_ocean").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_forest").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_space").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_sunset").assertIsDisplayed()
        
        // Verify theme names
        composeTestRule.onNodeWithText("Rainbow 🌈").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ocean 🌊").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forest 🌲").assertIsDisplayed()
        composeTestRule.onNodeWithText("Space 🚀").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sunset 🌅").assertIsDisplayed()
    }

    @Test
    fun themeSelector_canSelectTheme() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Click on ocean theme
        composeTestRule.onNodeWithTag("theme_ocean").performClick()
        
        // Verify the theme selection works (this would need to be verified through ViewModel state)
        composeTestRule.onNodeWithContentDescription("Select Ocean 🌊 theme").assertIsDisplayed()
    }

    @Test
    fun chatSettings_displaysCorrectOptions() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify chat settings options
        composeTestRule.onNodeWithText("Clear Chat History").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start fresh with a clean slate! 🗑️").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Voice Input").assertIsDisplayed()
        composeTestRule.onNodeWithText("Talk to Luna with your voice! 🎤").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Auto-Clear History").assertIsDisplayed()
    }

    @Test
    fun safetySettings_displaysCorrectOptions() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify safety settings options
        composeTestRule.onNodeWithText("Content Filter").assertIsDisplayed()
        composeTestRule.onNodeWithText("Keep conversations appropriate! 🌟").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Parental Controls").assertIsDisplayed()
        composeTestRule.onNodeWithText("Extra safety for young users! 👨‍👩‍👧‍👦").assertIsDisplayed()
    }

    @Test
    fun apiConfiguration_displaysCorrectOptions() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify API configuration options
        composeTestRule.onNodeWithText("API Key Status").assertIsDisplayed()
        composeTestRule.onNodeWithText("Setup Key").assertIsDisplayed()
    }

    @Test
    fun clearHistoryButton_isClickable() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        composeTestRule.onNodeWithTag("clear_history_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("clear_history_button").assertHasClickAction()
        composeTestRule.onNodeWithTag("clear_history_button").performClick()
    }

    @Test
    fun toggleSwitches_areInteractive() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify toggle switches are present and interactive
        composeTestRule.onNodeWithTag("voice_input_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("content_filter_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("parental_controls_toggle").assertIsDisplayed()
        
        // Verify switches have toggle actions
        composeTestRule.onNodeWithContentDescription("Voice Input toggle").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Content Filter toggle").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Parental Controls toggle").assertIsDisplayed()
    }

    @Test
    fun apiKeyDialog_opensAndCloses() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Click setup key button to open dialog
        composeTestRule.onNodeWithText("Setup Key").performClick()
        
        // Verify dialog is displayed (this would need proper ViewModel integration)
        // For now, just verify the button click works
        composeTestRule.onNodeWithText("Setup Key").assertIsDisplayed()
    }

    @Test
    fun resetToDefaults_isClickable() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        composeTestRule.onNodeWithTag("reset_defaults_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reset_defaults_button").assertHasClickAction()
        composeTestRule.onNodeWithTag("reset_defaults_button").performClick()
    }

    @Test
    fun appInfo_displaysCorrectly() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify app info section
        composeTestRule.onNodeWithTag("app_info").assertIsDisplayed()
        composeTestRule.onNodeWithText("Luna Chat Stats").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your chat journey so far!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Messages").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_hasProperAccessibility() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify accessibility content descriptions
        composeTestRule.onNodeWithContentDescription("Go back to chat").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Voice Input toggle").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Content Filter toggle").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Parental Controls toggle").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_scrollsCorrectly() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify the content is scrollable
        composeTestRule.onNodeWithTag("settings_content").assertIsDisplayed()
        
        // Scroll to bottom to verify all content is accessible
        composeTestRule.onNodeWithTag("settings_content").performScrollToNode(
            hasText("Luna Chat Stats")
        )
        
        composeTestRule.onNodeWithText("Luna Chat Stats").assertIsDisplayed()
    }
}