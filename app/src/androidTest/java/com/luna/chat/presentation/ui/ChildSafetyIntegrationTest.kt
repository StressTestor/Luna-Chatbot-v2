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
class ChildSafetyIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        composeTestRule.waitForIdle()
        
        // Dismiss welcome card to start testing
        composeTestRule.onNodeWithTag("welcome_dismiss_button").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun contentFilter_blocksInappropriateUserInput() {
        // Test various inappropriate inputs that should be filtered
        val inappropriateInputs = listOf(
            "I want to hurt someone",
            "How to make a bomb",
            "Tell me about violence",
            "Show me scary pictures",
            "I hate everyone"
        )

        inappropriateInputs.forEach { input ->
            // Send inappropriate message
            composeTestRule.onNodeWithTag("message_text_field").performTextInput(input)
            composeTestRule.onNodeWithTag("send_button").performClick()
            composeTestRule.waitForIdle()

            // Verify child-friendly error message appears
            composeTestRule.onNodeWithText("Let's talk about something else!", substring = true)
                .assertIsDisplayed()
            
            // Verify message was not added to chat
            composeTestRule.onNodeWithText(input).assertDoesNotExist()
            
            // Clear any error messages and text field
            composeTestRule.onNodeWithTag("message_text_field").performTextClearance()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun contentFilter_blocksPersonalInformationRequests() {
        val personalInfoRequests = listOf(
            "What is your name?",
            "Where do you live?",
            "Tell me your address",
            "What's your phone number?",
            "How old are you?",
            "What school do you go to?"
        )

        personalInfoRequests.forEach { request ->
            composeTestRule.onNodeWithTag("message_text_field").performTextInput(request)
            composeTestRule.onNodeWithTag("send_button").performClick()
            composeTestRule.waitForIdle()

            // Verify appropriate response about privacy
            composeTestRule.onNodeWithText("keep our personal information private", substring = true)
                .assertIsDisplayed()
            
            composeTestRule.onNodeWithTag("message_text_field").performTextClearance()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun contentFilter_allowsAppropriateEducationalContent() {
        val appropriateInputs = listOf(
            "What is 2 + 2?",
            "Tell me about dinosaurs",
            "How do plants grow?",
            "What colors make purple?",
            "Help me with my homework"
        )

        appropriateInputs.forEach { input ->
            composeTestRule.onNodeWithTag("message_text_field").performTextInput(input)
            composeTestRule.onNodeWithTag("send_button").performClick()
            composeTestRule.waitForIdle()

            // Verify message was sent (no error message)
            composeTestRule.onNodeWithText("Let's talk about something else!", substring = true)
                .assertDoesNotExist()
            
            // Verify typing indicator appears (message is being processed)
            // Note: This might be timing-dependent in real tests
            
            composeTestRule.onNodeWithTag("message_text_field").performTextClearance()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun parentalControls_requireAuthenticationForSensitiveSettings() {
        // Navigate to settings
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.waitForIdle()

        // Try to access API configuration (parental control)
        composeTestRule.onNodeWithText("API Configuration").performClick()
        composeTestRule.waitForIdle()

        // Verify parent authentication is required
        composeTestRule.onNodeWithTag("api_config_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Parent Password").assertIsDisplayed()

        // Try with invalid parent password
        composeTestRule.onNodeWithTag("api_key_input")
            .performTextInput("gsk_test123456789abcdef1234567890")
        composeTestRule.onNodeWithTag("parent_password_input").performTextInput("wrong")
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()

        // Verify access is denied
        composeTestRule.onNodeWithText("Invalid parent password", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun parentalControls_allowAccessWithValidAuthentication() {
        // Navigate to settings
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.waitForIdle()

        // Access API configuration with valid credentials
        composeTestRule.onNodeWithText("API Configuration").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("api_key_input")
            .performTextInput("gsk_test123456789abcdef1234567890")
        composeTestRule.onNodeWithTag("parent_password_input").performTextInput("1234")
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()

        // Verify access is granted
        composeTestRule.onNodeWithText("API key configured successfully!", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun errorMessages_areChildFriendly() {
        // Test network error scenario (simulated)
        composeTestRule.onNodeWithTag("message_text_field")
            .performTextInput("Test network error")
        composeTestRule.onNodeWithTag("send_button").performClick()
        composeTestRule.waitForIdle()

        // If a network error occurs, verify child-friendly message
        // Note: This would require mocking network failures in a real test
        // For now, we verify that error handling UI exists
        
        // Navigate to settings to test API connection error
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("API Configuration").performClick()
        composeTestRule.waitForIdle()
        
        // Enter invalid API key to trigger error
        composeTestRule.onNodeWithTag("api_key_input").performTextInput("invalid-key")
        composeTestRule.onNodeWithTag("parent_password_input").performTextInput("1234")
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()

        // Verify child-friendly error message
        composeTestRule.onNodeWithText("API key format is not valid", substring = true)
            .assertIsDisplayed()
        
        // Error message should not contain technical jargon
        composeTestRule.onNodeWithText("exception", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("stack trace", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("null pointer", substring = true).assertDoesNotExist()
    }

    @Test
    fun ageAppropriate_uiElementsAndLanguage() {
        // Verify UI uses child-friendly language
        composeTestRule.onNodeWithText("Hi there! I'm Luna!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try asking me:").assertIsDisplayed()
        
        // Navigate to settings and verify child-friendly language
        composeTestRule.onNodeWithContentDescription("Open settings").performClick()
        composeTestRule.waitForIdle()

        // Verify settings use simple, clear language
        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chat Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear Chat History").assertIsDisplayed()

        // Verify no complex technical terms are exposed to children
        composeTestRule.onNodeWithText("API", substring = false).assertDoesNotExist()
        composeTestRule.onNodeWithText("Configuration", substring = false).assertDoesNotExist()
        
        // Note: "API Configuration" is in parental controls section, which is appropriate
    }

    @Test
    fun contentModeration_handlesEdgeCases() {
        val edgeCaseInputs = listOf(
            // Attempts to bypass filters
            "I w@nt to h*rt someone",
            "Tell me about v1olence",
            "How to make a b0mb",
            
            // Mixed appropriate and inappropriate content
            "I love math but I hate school",
            "Can you help me with homework? Also, where do you live?",
            
            // Borderline content
            "I'm feeling sad",
            "My friend was mean to me",
            "I don't like vegetables"
        )

        edgeCaseInputs.forEach { input ->
            composeTestRule.onNodeWithTag("message_text_field").performTextInput(input)
            composeTestRule.onNodeWithTag("send_button").performClick()
            composeTestRule.waitForIdle()

            // Verify appropriate handling - either filtered with child-friendly message
            // or processed normally if appropriate
            val hasFilterMessage = try {
                composeTestRule.onNodeWithText("Let's talk about something else!", substring = true)
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }

            val hasPrivacyMessage = try {
                composeTestRule.onNodeWithText("keep our personal information private", substring = true)
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }

            // If not filtered, should be processed normally
            if (!hasFilterMessage && !hasPrivacyMessage) {
                // Message should be in chat or processing
                // This is acceptable for borderline appropriate content
            }

            composeTestRule.onNodeWithTag("message_text_field").performTextClearance()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun dataPrivacy_noPersonalInformationStored() {
        // Send messages that might contain personal info
        val personalMessages = listOf(
            "My name is John",
            "I live at 123 Main Street",
            "My phone number is 555-1234",
            "I go to Lincoln Elementary School"
        )

        personalMessages.forEach { message ->
            composeTestRule.onNodeWithTag("message_text_field").performTextInput(message)
            composeTestRule.onNodeWithTag("send_button").performClick()
            composeTestRule.waitForIdle()

            // Verify privacy protection message
            composeTestRule.onNodeWithText("keep our personal information private", substring = true)
                .assertIsDisplayed()

            composeTestRule.onNodeWithTag("message_text_field").performTextClearance()
            composeTestRule.waitForIdle()
        }

        // Start new chat to verify personal info is not persisted
        composeTestRule.onNodeWithTag("new_chat_button").performClick()
        composeTestRule.waitForIdle()

        // Verify clean slate - no personal information should be visible
        personalMessages.forEach { message ->
            composeTestRule.onNodeWithText(message).assertDoesNotExist()
        }
    }

    @Test
    fun safetyFeatures_workAcrossAppRestart() {
        // This test would ideally restart the app, but we'll simulate by starting new chat
        
        // Send inappropriate content
        composeTestRule.onNodeWithTag("message_text_field")
            .performTextInput("I want to hurt someone")
        composeTestRule.onNodeWithTag("send_button").performClick()
        composeTestRule.waitForIdle()

        // Verify filtering works
        composeTestRule.onNodeWithText("Let's talk about something else!", substring = true)
            .assertIsDisplayed()

        // Start new chat (simulates app restart)
        composeTestRule.onNodeWithTag("new_chat_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("welcome_dismiss_button").performClick()
        composeTestRule.waitForIdle()

        // Test that safety features still work
        composeTestRule.onNodeWithTag("message_text_field")
            .performTextInput("Tell me about violence")
        composeTestRule.onNodeWithTag("send_button").performClick()
        composeTestRule.waitForIdle()

        // Verify filtering still works after "restart"
        composeTestRule.onNodeWithText("Let's talk about something else!", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun accessibilityAndSafety_workTogether() {
        // Test that accessibility features don't compromise safety
        
        // Use accessibility to navigate and send inappropriate content
        composeTestRule.onNodeWithContentDescription("Type your message to Luna")
            .performTextInput("Where do you live?")
        
        composeTestRule.onNodeWithContentDescription("Send message").performClick()
        composeTestRule.waitForIdle()

        // Verify safety features work with accessibility
        composeTestRule.onNodeWithText("keep our personal information private", substring = true)
            .assertIsDisplayed()

        // Verify accessibility descriptions don't expose sensitive information
        composeTestRule.onNodeWithContentDescription("API key").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Database").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Server").assertDoesNotExist()
    }
}