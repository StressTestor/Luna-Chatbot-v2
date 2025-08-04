package com.luna.chat.presentation.ui

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
class EndToEndUserJourneyTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun completeFirstTimeUserJourney() {
        // 1. App launches and shows welcome screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hi there! I'm Luna!").assertIsDisplayed()

        // 2. User sees educational suggestions
        composeTestRule.onNodeWithText("🧮 Help me with math homework", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("🔬 Tell me about space and planets", substring = true).assertIsDisplayed()

        // 3. User clicks on math homework suggestion
        composeTestRule.onNodeWithTag("welcome_suggestion_0").performClick()
        composeTestRule.waitForIdle()

        // 4. Welcome card disappears and message is sent
        composeTestRule.onNodeWithTag("welcome_card").assertDoesNotExist()
        composeTestRule.onNodeWithTag("chat_messages_list").assertIsDisplayed()

        // 5. User types and sends a follow-up message
        val followUpMessage = "Can you help me with fractions?"
        composeTestRule.onNodeWithTag("message_text_field").performTextInput(followUpMessage)
        composeTestRule.onNodeWithTag("send_button").performClick()
        composeTestRule.waitForIdle()

        // 6. User navigates to settings
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()

        // 7. User changes theme
        composeTestRule.onNodeWithText("Ocean 🌊").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Theme changed to Ocean!", substring = true).assertIsDisplayed()

        // 8. User goes back to chat
        composeTestRule.onNodeWithContentDescription("Go back to chat").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()

        // 9. User starts a new chat
        composeTestRule.onNodeWithTag("new_chat_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("welcome_card").assertIsDisplayed()

        // 10. User sends another message to verify everything works
        composeTestRule.onNodeWithTag("welcome_dismiss_button").performClick()
        composeTestRule.waitForIdle()
        
        val finalMessage = "Tell me about dinosaurs!"
        composeTestRule.onNodeWithTag("message_text_field").performTextInput(finalMessage)
        composeTestRule.onNodeWithTag("send_button").performClick()
        composeTestRule.waitForIdle()

        // Verify the complete journey worked
        composeTestRule.onNodeWithTag("chat_messages_list").assertIsDisplayed()
    }

    @Test
    fun parentalControlsSetupJourney() {
        composeTestRule.waitForIdle()

        // 1. Navigate to settings
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.waitForIdle()

        // 2. Access parental controls
        composeTestRule.onNodeWithText("API Configuration").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("api_config_dialog").assertIsDisplayed()

        // 3. Enter API key and parent password
        val validApiKey = "gsk_test123456789abcdef1234567890"
        composeTestRule.onNodeWithTag("api_key_input").performTextInput(validApiKey)
        composeTestRule.onNodeWithTag("parent_password_input").performTextInput("1234")

        // 4. Save configuration
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()

        // 5. Verify success
        composeTestRule.onNodeWithText("API key configured successfully!", substring = true)
            .assertIsDisplayed()

        // 6. Test API connection
        composeTestRule.onNodeWithText("Test Connection").performClick()
        composeTestRule.waitForIdle()

        // 7. Configure content filter
        composeTestRule.onNodeWithTag("content_filter_switch").performClick()
        composeTestRule.waitForIdle()

        // 8. Set auto-clear history
        composeTestRule.onNodeWithText("Auto-Clear History").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("auto_clear_dialog").assertIsDisplayed()

        // 9. Return to chat and verify everything works
        composeTestRule.onNodeWithContentDescription("Go back to chat").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
    }

    @Test
    fun accessibilityUserJourney() {
        composeTestRule.waitForIdle()

        // 1. Verify all accessibility content descriptions are present
        composeTestRule.onNodeWithContentDescription("Open settings").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Start new chat").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Type your message to Luna").assertIsDisplayed()

        // 2. Navigate using accessibility
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.waitForIdle()

        // 3. Verify settings accessibility
        composeTestRule.onNodeWithContentDescription("Go back to chat").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Select theme").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Toggle voice input").assertIsDisplayed()

        // 4. Test voice input toggle
        composeTestRule.onNodeWithContentDescription("Toggle voice input").performClick()
        composeTestRule.waitForIdle()

        // 5. Return to chat
        composeTestRule.onNodeWithContentDescription("Go back to chat").performClick()
        composeTestRule.waitForIdle()

        // 6. Test message input accessibility
        composeTestRule.onNodeWithContentDescription("Type your message to Luna")
            .performTextInput("Accessibility test message")
        
        composeTestRule.onNodeWithContentDescription("Send message").performClick()
        composeTestRule.waitForIdle()

        // Verify accessibility features work throughout the journey
        composeTestRule.onNodeWithTag("chat_messages_list").assertIsDisplayed()
    }

    @Test
    fun errorHandlingUserJourney() {
        composeTestRule.waitForIdle()

        // 1. Try to send empty message
        composeTestRule.onNodeWithTag("welcome_dismiss_button").performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithTag("send_button").performClick()
        composeTestRule.waitForIdle()
        
        // Should not send empty message
        composeTestRule.onNodeWithTag("chat_typing_indicator").assertDoesNotExist()

        // 2. Send very long message
        val longMessage = "This is a very long message that tests how the app handles extremely long user input and ensures that the UI remains responsive and functional even with large amounts of text that might exceed normal message length limits and could potentially cause issues with the layout or processing of the message content."
        
        composeTestRule.onNodeWithTag("message_text_field").performTextInput(longMessage)
        composeTestRule.onNodeWithTag("send_button").performClick()
        composeTestRule.waitForIdle()

        // 3. Navigate to settings and test invalid API key
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("API Configuration").performClick()
        composeTestRule.waitForIdle()
        
        // Enter invalid API key
        composeTestRule.onNodeWithTag("api_key_input").performTextInput("invalid-key")
        composeTestRule.onNodeWithTag("parent_password_input").performTextInput("1234")
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()
        
        // Verify error handling
        composeTestRule.onNodeWithText("API key format is not valid", substring = true)
            .assertIsDisplayed()

        // 4. Test invalid parent password
        composeTestRule.onNodeWithTag("api_key_input").performTextClearance()
        composeTestRule.onNodeWithTag("api_key_input")
            .performTextInput("gsk_valid123456789abcdef1234567890")
        composeTestRule.onNodeWithTag("parent_password_input").performTextClearance()
        composeTestRule.onNodeWithTag("parent_password_input").performTextInput("abc")
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()
        
        // Verify error handling
        composeTestRule.onNodeWithText("Invalid parent password", substring = true)
            .assertIsDisplayed()

        // 5. Return to chat and verify app is still functional
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Go back to chat").performClick()
        composeTestRule.waitForIdle()
        
        // App should still be functional
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("message_text_field").performTextInput("Recovery test")
        composeTestRule.onNodeWithTag("send_button").assertIsEnabled()
    }

    @Test
    fun themeChangeUserJourney() {
        composeTestRule.waitForIdle()

        // 1. Start with default theme and navigate to settings
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.waitForIdle()

        // 2. Test each theme change
        val themes = listOf(
            "Ocean 🌊",
            "Forest 🌲", 
            "Space 🚀",
            "Sunset 🌅",
            "Rainbow 🌈"
        )

        themes.forEach { theme ->
            // Select theme
            composeTestRule.onNodeWithText(theme).performClick()
            composeTestRule.waitForIdle()
            
            // Verify theme change feedback
            composeTestRule.onNodeWithText("Theme changed to", substring = true)
                .assertIsDisplayed()
            
            // Brief pause to see theme change
            Thread.sleep(500)
        }

        // 3. Return to chat and verify theme is applied
        composeTestRule.onNodeWithContentDescription("Go back to chat").performClick()
        composeTestRule.waitForIdle()
        
        // 4. Send a message to verify theme works in chat
        composeTestRule.onNodeWithTag("welcome_dismiss_button").performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithTag("message_text_field")
            .performTextInput("Testing theme in chat")
        composeTestRule.onNodeWithTag("send_button").performClick()
        composeTestRule.waitForIdle()

        // 5. Start new chat to verify theme persists
        composeTestRule.onNodeWithTag("new_chat_button").performClick()
        composeTestRule.waitForIdle()
        
        // Theme should persist across new chats
        composeTestRule.onNodeWithTag("welcome_card").assertIsDisplayed()
    }

    @Test
    fun educationalFeaturesUserJourney() {
        composeTestRule.waitForIdle()

        // 1. Start with educational suggestion
        composeTestRule.onNodeWithTag("welcome_suggestion_0").performClick() // Math homework
        composeTestRule.waitForIdle()

        // 2. Send follow-up educational messages
        val educationalMessages = listOf(
            "Can you explain fractions?",
            "What is 15 divided by 3?",
            "Help me understand multiplication",
            "Show me how to solve word problems"
        )

        educationalMessages.forEach { message ->
            composeTestRule.onNodeWithTag("message_text_field").performTextInput(message)
            composeTestRule.onNodeWithTag("send_button").performClick()
            composeTestRule.waitForIdle()
            
            // Clear the text field for next message
            composeTestRule.onNodeWithTag("message_text_field").performTextClearance()
        }

        // 3. Verify educational prompt appears after multiple messages
        composeTestRule.onNodeWithTag("educational_prompt").assertIsDisplayed()
        composeTestRule.onNodeWithText("Great chatting! Want to try something new?", substring = true)
            .assertIsDisplayed()

        // 4. Click on educational suggestion from prompt
        composeTestRule.onNodeWithTag("educational_suggestion_0").performClick()
        composeTestRule.waitForIdle()

        // 5. Switch to different educational topic
        composeTestRule.onNodeWithTag("message_text_field")
            .performTextInput("Tell me about science experiments")
        composeTestRule.onNodeWithTag("send_button").performClick()
        composeTestRule.waitForIdle()

        // 6. Verify educational features work throughout conversation
        composeTestRule.onNodeWithTag("chat_messages_list").assertIsDisplayed()
    }

    @Test
    fun performanceStressTest() {
        composeTestRule.waitForIdle()

        // 1. Dismiss welcome card
        composeTestRule.onNodeWithTag("welcome_dismiss_button").performClick()
        composeTestRule.waitForIdle()

        // 2. Send many messages rapidly to test performance
        repeat(10) { index ->
            val message = "Performance test message number $index with some additional content to make it longer"
            composeTestRule.onNodeWithTag("message_text_field").performTextInput(message)
            composeTestRule.onNodeWithTag("send_button").performClick()
            
            // Small delay to prevent overwhelming the system
            Thread.sleep(100)
            
            // Clear text field
            composeTestRule.onNodeWithTag("message_text_field").performTextClearance()
        }

        composeTestRule.waitForIdle()

        // 3. Navigate to settings and back rapidly
        repeat(3) {
            composeTestRule.onNodeWithContentDescription("Open settings").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithContentDescription("Go back to chat").performClick()
            composeTestRule.waitForIdle()
        }

        // 4. Change themes rapidly
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.waitForIdle()

        val themes = listOf("Ocean 🌊", "Forest 🌲", "Space 🚀", "Rainbow 🌈")
        themes.forEach { theme ->
            composeTestRule.onNodeWithText(theme).performClick()
            Thread.sleep(200) // Brief pause
        }

        // 5. Return to chat and verify app is still responsive
        composeTestRule.onNodeWithContentDescription("Go back to chat").performClick()
        composeTestRule.waitForIdle()

        // App should still be functional after stress test
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("message_text_field")
            .performTextInput("App is still working after stress test")
        composeTestRule.onNodeWithTag("send_button").assertIsEnabled()
    }
}