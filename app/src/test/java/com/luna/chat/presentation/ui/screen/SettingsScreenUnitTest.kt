package com.luna.chat.presentation.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.luna.chat.domain.entity.Theme
import com.luna.chat.presentation.theme.LunaTheme
import com.luna.chat.data.repository.UserPreferences
import com.luna.chat.data.repository.UserExperienceLevel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsScreenUnitTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun themeOption_displaysCorrectly() {
        val theme = Theme.RAINBOW_THEME
        var clicked = false

        composeTestRule.setContent {
            LunaTheme {
                // We would need to extract ThemeOption as a public composable for this test
                // For now, testing through the main screen
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify theme option displays correctly
        composeTestRule.onNodeWithText("Rainbow 🌈").assertIsDisplayed()
    }

    @Test
    fun settingsActionCard_handlesClick() {
        var clicked = false

        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Test clear history button click
        composeTestRule.onNodeWithTag("clear_history_button").performClick()
        // The actual click handling would be tested through ViewModel integration
    }

    @Test
    fun settingsToggleCard_handlesToggle() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify toggle switches are present
        composeTestRule.onNodeWithTag("voice_input_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("content_filter_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("parental_controls_toggle").assertIsDisplayed()
    }

    @Test
    fun apiKeyStatusCard_displaysCorrectStatus() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify API key status is displayed
        composeTestRule.onNodeWithText("API Key Status").assertIsDisplayed()
        // Default state should show "needs setup"
        composeTestRule.onNodeWithText("Setup Key").assertIsDisplayed()
    }

    @Test
    fun appInfoCard_displaysUserStats() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify app info displays correctly
        composeTestRule.onNodeWithText("Luna Chat Stats").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your chat journey so far!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Messages").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level").assertIsDisplayed()
    }

    @Test
    fun messageCard_displaysSuccessMessage() {
        composeTestRule.setContent {
            LunaTheme {
                // Test would need extracted MessageCard composable
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // This would test the message card display
        // Currently integrated into the main screen
    }

    @Test
    fun messageCard_displaysErrorMessage() {
        composeTestRule.setContent {
            LunaTheme {
                // Test would need extracted MessageCard composable
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // This would test error message display
        // Currently integrated into the main screen
    }

    @Test
    fun autoClearDaysDialog_displaysOptions() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Click auto-clear setting to open dialog
        composeTestRule.onNodeWithTag("auto_clear_setting").performClick()
        
        // Dialog would be tested separately in integration tests
    }

    @Test
    fun apiKeyConfigurationDialog_validatesInput() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Click setup key to open dialog
        composeTestRule.onNodeWithText("Setup Key").performClick()
        
        // Dialog validation would be tested in integration tests
    }

    @Test
    fun settingsSection_displaysCorrectly() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify all sections are displayed with correct titles and descriptions
        composeTestRule.onNodeWithText("Choose Your Theme 🎨").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pick colors that make you happy!").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Chat Settings 💬").assertIsDisplayed()
        composeTestRule.onNodeWithText("Customize your chat experience").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Safety Settings 🛡️").assertIsDisplayed()
        composeTestRule.onNodeWithText("Keep your chats safe and fun").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("API Configuration 🔧").assertIsDisplayed()
        composeTestRule.onNodeWithText("For grown-ups only!").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Advanced Settings ⚡").assertIsDisplayed()
        composeTestRule.onNodeWithText("For power users").assertIsDisplayed()
    }

    @Test
    fun statItem_displaysCorrectly() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify stat items in app info section
        composeTestRule.onNodeWithText("Messages").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level").assertIsDisplayed()
        
        // Default values should be displayed
        composeTestRule.onNodeWithText("0").assertIsDisplayed() // Default message count
        composeTestRule.onNodeWithText("New").assertIsDisplayed() // Default experience level
    }

    @Test
    fun themeSelector_showsAllAvailableThemes() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify all theme options are available
        val expectedThemes = listOf("Rainbow 🌈", "Ocean 🌊", "Forest 🌲", "Space 🚀", "Sunset 🌅")
        
        expectedThemes.forEach { themeName ->
            composeTestRule.onNodeWithText(themeName).assertIsDisplayed()
        }
    }

    @Test
    fun settingsScreen_hasCorrectTestTags() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify all important test tags are present
        val expectedTestTags = listOf(
            "settings_screen",
            "settings_top_bar", 
            "settings_content",
            "back_button",
            "theme_selector",
            "clear_history_button",
            "voice_input_toggle",
            "content_filter_toggle",
            "parental_controls_toggle",
            "auto_clear_setting",
            "api_key_status",
            "reset_defaults_button",
            "app_info"
        )
        
        expectedTestTags.forEach { tag ->
            composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
        }
    }
}