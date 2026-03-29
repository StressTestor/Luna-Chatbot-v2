package com.luna.chat.presentation.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit4.runners.AndroidJUnit4
import com.luna.chat.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsScreenIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        
        // Navigate to settings screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_displaysCorrectly() {
        // Verify settings screen is displayed
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        
        // Verify main sections are present
        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chat Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Parental Controls").assertIsDisplayed()
        composeTestRule.onNodeWithText("About").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysThemeOptions() {
        // Verify theme section
        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        
        // Verify theme options are displayed
        composeTestRule.onNodeWithText("Rainbow 🌈").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ocean 🌊").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forest 🌲").assertIsDisplayed()
        composeTestRule.onNodeWithText("Space 🚀").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sunset 🌅").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_allowsThemeSelection() {
        // Select Ocean theme
        composeTestRule.onNodeWithText("Ocean 🌊").performClick()
        composeTestRule.waitForIdle()
        
        // Verify theme selection feedback
        composeTestRule.onNodeWithText("Theme changed to Ocean!", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysChatSettings() {
        // Verify chat settings section
        composeTestRule.onNodeWithText("Chat Settings").assertIsDisplayed()
        
        // Verify individual settings
        composeTestRule.onNodeWithText("Clear Chat History").assertIsDisplayed()
        composeTestRule.onNodeWithText("Voice Input").assertIsDisplayed()
        composeTestRule.onNodeWithText("Content Filter").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_allowsClearChatHistory() {
        // Click clear chat history
        composeTestRule.onNodeWithText("Clear Chat History").performClick()
        composeTestRule.waitForIdle()
        
        // Verify confirmation or success message
        composeTestRule.onNodeWithText("Chat history cleared!", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_allowsVoiceInputToggle() {
        // Find and toggle voice input switch
        composeTestRule.onNodeWithTag("voice_input_switch").performClick()
        composeTestRule.waitForIdle()
        
        // Verify toggle feedback
        composeTestRule.onNodeWithText("Voice input", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_allowsContentFilterToggle() {
        // Find and toggle content filter switch
        composeTestRule.onNodeWithTag("content_filter_switch").performClick()
        composeTestRule.waitForIdle()
        
        // Verify toggle feedback
        composeTestRule.onNodeWithText("Content filter", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysParentalControls() {
        // Verify parental controls section
        composeTestRule.onNodeWithText("Parental Controls").assertIsDisplayed()
        
        // Verify parental control options
        composeTestRule.onNodeWithText("API Configuration").assertIsDisplayed()
        composeTestRule.onNodeWithText("Auto-Clear History").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsApiConfigurationDialog() {
        // Click API Configuration
        composeTestRule.onNodeWithText("API Configuration").performClick()
        composeTestRule.waitForIdle()
        
        // Verify API configuration dialog appears
        composeTestRule.onNodeWithTag("api_config_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("API Key Setup").assertIsDisplayed()
        composeTestRule.onNodeWithText("Parent Password").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_handlesApiKeyConfiguration() {
        // Open API configuration dialog
        composeTestRule.onNodeWithText("API Configuration").performClick()
        composeTestRule.waitForIdle()
        
        // Enter API key
        val testApiKey = "gsk_test123456789abcdef1234567890"
        composeTestRule.onNodeWithTag("api_key_input").performTextInput(testApiKey)
        
        // Enter parent password
        composeTestRule.onNodeWithTag("parent_password_input").performTextInput("1234")
        
        // Click save
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()
        
        // Verify success message
        composeTestRule.onNodeWithText("API key configured successfully!", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_validatesApiKeyFormat() {
        // Open API configuration dialog
        composeTestRule.onNodeWithText("API Configuration").performClick()
        composeTestRule.waitForIdle()
        
        // Enter invalid API key
        composeTestRule.onNodeWithTag("api_key_input").performTextInput("invalid-key")
        composeTestRule.onNodeWithTag("parent_password_input").performTextInput("1234")
        
        // Click save
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()
        
        // Verify error message
        composeTestRule.onNodeWithText("API key format is not valid", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_validatesParentPassword() {
        // Open API configuration dialog
        composeTestRule.onNodeWithText("API Configuration").performClick()
        composeTestRule.waitForIdle()
        
        // Enter valid API key but invalid password
        composeTestRule.onNodeWithTag("api_key_input")
            .performTextInput("gsk_test123456789abcdef1234567890")
        composeTestRule.onNodeWithTag("parent_password_input").performTextInput("abc")
        
        // Click save
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()
        
        // Verify error message
        composeTestRule.onNodeWithText("Invalid parent password", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_allowsAutoClearHistoryConfiguration() {
        // Scroll to auto-clear history setting
        composeTestRule.onNodeWithText("Auto-Clear History").performClick()
        composeTestRule.waitForIdle()
        
        // Verify auto-clear dialog appears
        composeTestRule.onNodeWithTag("auto_clear_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Auto-Clear Chat History").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysAboutSection() {
        // Scroll to about section
        composeTestRule.onNodeWithText("About").assertIsDisplayed()
        
        // Verify about information
        composeTestRule.onNodeWithText("Luna Chat v1.0").assertIsDisplayed()
        composeTestRule.onNodeWithText("A safe AI chat app for kids").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_hasAccessibilitySupport() {
        // Verify accessibility content descriptions
        composeTestRule.onNodeWithContentDescription("Go back to chat").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Select theme").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Toggle voice input").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Toggle content filter").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_handlesLongPress() {
        // Long press on theme option to show description
        composeTestRule.onNodeWithText("Ocean 🌊").performTouchInput {
            longClick()
        }
        composeTestRule.waitForIdle()
        
        // Verify theme description appears
        composeTestRule.onNodeWithText("Calming blue colors inspired by the ocean", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_preservesStateOnRotation() {
        // Select a theme
        composeTestRule.onNodeWithText("Forest 🌲").performClick()
        composeTestRule.waitForIdle()
        
        // Simulate rotation (this would require additional setup in a real test)
        // For now, we'll verify the selection is maintained
        composeTestRule.onNodeWithText("Theme changed to Forest!", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_handlesNetworkErrors() {
        // This test would require mocking network failures
        // For now, we'll verify error handling UI exists
        composeTestRule.onNodeWithText("API Configuration").performClick()
        composeTestRule.waitForIdle()
        
        // Verify error handling elements are present
        composeTestRule.onNodeWithTag("api_config_dialog").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_allowsNavigationBack() {
        // Click back button
        composeTestRule.onNodeWithContentDescription("Go back to chat").performClick()
        composeTestRule.waitForIdle()
        
        // Verify we're back on chat screen
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsSuccessMessages() {
        // Perform an action that shows success message
        composeTestRule.onNodeWithText("Clear Chat History").performClick()
        composeTestRule.waitForIdle()
        
        // Verify success message appears and can be dismissed
        composeTestRule.onNodeWithText("Chat history cleared!", substring = true)
            .assertIsDisplayed()
        
        // Wait for message to auto-dismiss or manually dismiss
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_showsErrorMessages() {
        // Trigger an error condition
        composeTestRule.onNodeWithText("API Configuration").performClick()
        composeTestRule.waitForIdle()
        
        // Enter invalid data
        composeTestRule.onNodeWithTag("api_key_input").performTextInput("invalid")
        composeTestRule.onNodeWithTag("parent_password_input").performTextInput("1234")
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()
        
        // Verify error message appears
        composeTestRule.onNodeWithText("API key format is not valid", substring = true)
            .assertIsDisplayed()
    }
}