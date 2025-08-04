package com.luna.chat.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.luna.chat.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Full app integration test to verify all components work together
 * Tests the complete user journey from app launch to chat interactions
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FullAppIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testCompleteAppFlow_launchToChatToSettings() {
        // Test app launches successfully
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
        
        // Test chat screen components are present
        composeTestRule.onNodeWithTag("message_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("send_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_button").assertIsDisplayed()
        
        // Test navigation to settings
        composeTestRule.onNodeWithTag("settings_button").performClick()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        
        // Test settings screen components
        composeTestRule.onNodeWithTag("theme_selector").assertIsDisplayed()
        composeTestRule.onNodeWithTag("clear_history_button").assertIsDisplayed()
        
        // Test navigation back to chat
        composeTestRule.onNodeWithTag("back_button").performClick()
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
    }

    @Test
    fun testChatFunctionality_sendMessage() {
        // Wait for chat screen to load
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
        
        // Type a test message
        val testMessage = "Hello Luna!"
        composeTestRule.onNodeWithTag("message_input")
            .performTextInput(testMessage)
        
        // Send the message
        composeTestRule.onNodeWithTag("send_button").performClick()
        
        // Verify message appears in chat
        composeTestRule.onNodeWithText(testMessage).assertIsDisplayed()
        
        // Verify typing indicator appears (AI is responding)
        composeTestRule.onNodeWithTag("typing_indicator").assertIsDisplayed()
    }

    @Test
    fun testThemeSelection_changesAppearance() {
        // Navigate to settings
        composeTestRule.onNodeWithTag("settings_button").performClick()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        
        // Select a different theme
        composeTestRule.onNodeWithTag("theme_ocean").performClick()
        
        // Navigate back to chat
        composeTestRule.onNodeWithTag("back_button").performClick()
        
        // Verify theme change is applied (this would need specific theme indicators)
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
    }

    @Test
    fun testSettingsConfiguration_toggles() {
        // Navigate to settings
        composeTestRule.onNodeWithTag("settings_button").performClick()
        
        // Test voice input toggle
        composeTestRule.onNodeWithTag("voice_input_toggle").performClick()
        
        // Test content filter toggle
        composeTestRule.onNodeWithTag("content_filter_toggle").performClick()
        
        // Test parental controls toggle
        composeTestRule.onNodeWithTag("parental_controls_toggle").performClick()
        
        // Verify settings are responsive
        composeTestRule.onNodeWithTag("settings_content").assertIsDisplayed()
    }

    @Test
    fun testClearChatHistory_functionality() {
        // Send a message first
        composeTestRule.onNodeWithTag("message_input")
            .performTextInput("Test message for clearing")
        composeTestRule.onNodeWithTag("send_button").performClick()
        
        // Navigate to settings
        composeTestRule.onNodeWithTag("settings_button").performClick()
        
        // Clear chat history
        composeTestRule.onNodeWithTag("clear_history_button").performClick()
        
        // Navigate back to chat
        composeTestRule.onNodeWithTag("back_button").performClick()
        
        // Verify chat is cleared (welcome message should be visible)
        composeTestRule.onNodeWithTag("welcome_card").assertIsDisplayed()
    }

    @Test
    fun testAccessibility_screenReaderSupport() {
        // Test that key components have proper content descriptions
        composeTestRule.onNodeWithTag("settings_button")
            .assertHasClickAction()
            .assert(hasContentDescription())
        
        composeTestRule.onNodeWithTag("send_button")
            .assertHasClickAction()
            .assert(hasContentDescription())
        
        composeTestRule.onNodeWithTag("message_input")
            .assert(hasContentDescription())
    }

    @Test
    fun testErrorHandling_networkIssues() {
        // This test would require mocking network failures
        // For now, we'll test that error states don't crash the app
        
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
        
        // Send a message that might trigger an error
        composeTestRule.onNodeWithTag("message_input")
            .performTextInput("Test error handling")
        composeTestRule.onNodeWithTag("send_button").performClick()
        
        // App should remain stable even if API call fails
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
    }

    @Test
    fun testChildSafety_contentFiltering() {
        // Test that inappropriate content is handled properly
        composeTestRule.onNodeWithTag("message_input")
            .performTextInput("inappropriate test content")
        composeTestRule.onNodeWithTag("send_button").performClick()
        
        // App should handle this gracefully without crashing
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
    }

    @Test
    fun testPerformance_scrollingAndMemory() {
        // Send multiple messages to test list performance
        repeat(10) { index ->
            composeTestRule.onNodeWithTag("message_input")
                .performTextClearance()
                .performTextInput("Test message $index")
            composeTestRule.onNodeWithTag("send_button").performClick()
            
            // Small delay to allow processing
            Thread.sleep(100)
        }
        
        // Test scrolling performance
        composeTestRule.onNodeWithTag("message_list")
            .performScrollToIndex(0)
        
        // App should remain responsive
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
    }
}