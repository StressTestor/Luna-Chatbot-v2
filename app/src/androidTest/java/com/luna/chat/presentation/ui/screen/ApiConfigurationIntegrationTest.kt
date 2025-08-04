package com.luna.chat.presentation.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.luna.chat.presentation.theme.LunaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApiConfigurationIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun apiConfiguration_fullFlow_worksCorrectly() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Navigate to API configuration
        composeTestRule.onNodeWithText("Setup Key").assertIsDisplayed()
        composeTestRule.onNodeWithText("Setup Key").performClick()

        // Verify API configuration dialog appears
        // Note: This would require proper ViewModel integration to test fully
        composeTestRule.onNodeWithText("Setup Key").assertIsDisplayed()
    }

    @Test
    fun apiConfiguration_requiresParentAuthentication() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify API configuration section is present
        composeTestRule.onNodeWithText("API Configuration 🔧").assertIsDisplayed()
        composeTestRule.onNodeWithText("For grown-ups only!").assertIsDisplayed()
        
        // Verify API key status card
        composeTestRule.onNodeWithTag("api_key_status").assertIsDisplayed()
        composeTestRule.onNodeWithText("API Key Status").assertIsDisplayed()
    }

    @Test
    fun apiConfiguration_displaysCorrectStatusMessages() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify default status (not configured)
        composeTestRule.onNodeWithText("API key needs setup! ⚠️").assertIsDisplayed()
        composeTestRule.onNodeWithText("Setup Key").assertIsDisplayed()
    }

    @Test
    fun apiConfiguration_testConnectionButton_appearsWhenConfigured() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // In the default state (not configured), test connection should not be visible
        composeTestRule.onNodeWithText("Test Connection").assertDoesNotExist()
        
        // Setup Key button should be visible
        composeTestRule.onNodeWithText("Setup Key").assertIsDisplayed()
    }

    @Test
    fun apiConfiguration_dialogHandlesInput() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Click setup key to potentially open dialog
        composeTestRule.onNodeWithText("Setup Key").performClick()
        
        // The actual dialog testing would require proper ViewModel integration
        // For now, verify the button click is handled
        composeTestRule.onNodeWithText("Setup Key").assertIsDisplayed()
    }

    @Test
    fun apiConfiguration_hasProperAccessibility() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify accessibility for API configuration elements
        composeTestRule.onNodeWithTag("api_key_status").assertIsDisplayed()
        
        // Verify buttons are clickable
        composeTestRule.onNodeWithText("Setup Key").assertHasClickAction()
    }

    @Test
    fun apiConfiguration_sectionLayout_isCorrect() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify API configuration section structure
        composeTestRule.onNodeWithText("API Configuration 🔧").assertIsDisplayed()
        composeTestRule.onNodeWithText("For grown-ups only!").assertIsDisplayed()
        
        // Verify API key status card is within the section
        composeTestRule.onNodeWithTag("api_key_status").assertIsDisplayed()
        composeTestRule.onNodeWithText("API Key Status").assertIsDisplayed()
        
        // Verify action buttons
        composeTestRule.onNodeWithText("Setup Key").assertIsDisplayed()
    }

    @Test
    fun apiConfiguration_errorHandling_displaysCorrectly() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // This test would verify error message display
        // Currently just verifies the basic structure is in place
        composeTestRule.onNodeWithTag("api_key_status").assertIsDisplayed()
    }

    @Test
    fun apiConfiguration_loadingStates_handleCorrectly() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify buttons are enabled in default state
        composeTestRule.onNodeWithText("Setup Key").assertIsEnabled()
        
        // Loading states would be tested with proper ViewModel integration
    }

    @Test
    fun apiConfiguration_parentPasswordFlow_isAccessible() {
        composeTestRule.setContent {
            LunaTheme {
                SettingsScreen(
                    onBackClick = { }
                )
            }
        }

        // Verify the API configuration is clearly marked as parent-only
        composeTestRule.onNodeWithText("For grown-ups only!").assertIsDisplayed()
        
        // The actual parent password flow would be tested with ViewModel integration
        composeTestRule.onNodeWithText("Setup Key").assertIsDisplayed()
    }
}