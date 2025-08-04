package com.luna.chat.presentation.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.luna.chat.presentation.theme.LunaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageInputTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun messageInput_displaysCorrectly() {
        // Given
        var currentMessage = ""
        var sendClicked = false
        var voiceClicked = false

        // When
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = currentMessage,
                    onMessageChange = { currentMessage = it },
                    onSendMessage = { sendClicked = true },
                    onVoiceInput = { voiceClicked = true },
                    isEnabled = true,
                    isLoading = false
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("message_input_card")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("message_text_field")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("voice_input_button")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("send_button")
            .assertIsDisplayed()
    }

    @Test
    fun messageInput_handlesTextInput() {
        // Given
        var currentMessage = ""
        
        // When
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = currentMessage,
                    onMessageChange = { currentMessage = it },
                    onSendMessage = {},
                    onVoiceInput = {},
                    isEnabled = true,
                    isLoading = false
                )
            }
        }

        // Type text
        composeTestRule
            .onNodeWithTag("message_text_field")
            .performTextInput("Hello Luna!")

        // Then
        composeTestRule
            .onNodeWithTag("message_text_field")
            .assertTextContains("Hello Luna!")
    }

    @Test
    fun messageInput_sendButtonEnabledOnlyWithText() {
        // Given
        var currentMessage = ""
        
        // When - Empty message
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = currentMessage,
                    onMessageChange = { currentMessage = it },
                    onSendMessage = {},
                    onVoiceInput = {},
                    isEnabled = true,
                    isLoading = false
                )
            }
        }

        // Then - Send button should be disabled
        composeTestRule
            .onNodeWithTag("send_button")
            .assertIsNotEnabled()

        // When - With message
        currentMessage = "Hello"
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = currentMessage,
                    onMessageChange = { currentMessage = it },
                    onSendMessage = {},
                    onVoiceInput = {},
                    isEnabled = true,
                    isLoading = false
                )
            }
        }

        // Then - Send button should be enabled
        composeTestRule
            .onNodeWithTag("send_button")
            .assertIsEnabled()
    }

    @Test
    fun messageInput_handlesLoadingState() {
        // Given
        var currentMessage = "Test message"
        
        // When
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = currentMessage,
                    onMessageChange = {},
                    onSendMessage = {},
                    onVoiceInput = {},
                    isEnabled = false,
                    isLoading = true
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("message_text_field")
            .assertIsNotEnabled()
        
        composeTestRule
            .onNodeWithTag("send_button")
            .assertIsNotEnabled()
        
        composeTestRule
            .onNodeWithTag("voice_input_button")
            .assertIsNotEnabled()
    }

    @Test
    fun messageInput_triggersCallbacks() {
        // Given
        var sendClicked = false
        var voiceClicked = false
        var messageChanged = false
        
        // When
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = "Test",
                    onMessageChange = { messageChanged = true },
                    onSendMessage = { sendClicked = true },
                    onVoiceInput = { voiceClicked = true },
                    isEnabled = true,
                    isLoading = false
                )
            }
        }

        // Click send button
        composeTestRule
            .onNodeWithTag("send_button")
            .performClick()

        // Click voice button
        composeTestRule
            .onNodeWithTag("voice_input_button")
            .performClick()

        // Type in text field
        composeTestRule
            .onNodeWithTag("message_text_field")
            .performTextInput("New text")

        // Then
        assert(sendClicked) { "Send callback should be triggered" }
        assert(voiceClicked) { "Voice callback should be triggered" }
        assert(messageChanged) { "Message change callback should be triggered" }
    }

    @Test
    fun messageInput_hasAccessibilitySupport() {
        // Given
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = "",
                    onMessageChange = {},
                    onSendMessage = {},
                    onVoiceInput = {},
                    isEnabled = true,
                    isLoading = false
                )
            }
        }

        // Then - Check accessibility descriptions
        composeTestRule
            .onNode(hasContentDescription("Type your message to Luna"))
            .assertIsDisplayed()
        
        composeTestRule
            .onNode(hasContentDescription("Voice input - tap to speak your message"))
            .assertIsDisplayed()
        
        composeTestRule
            .onNode(hasContentDescription("Send message"))
            .assertIsDisplayed()
    }

    @Test
    fun messageInput_showsCorrectPlaceholder() {
        // Given - Normal state
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = "",
                    onMessageChange = {},
                    onSendMessage = {},
                    onVoiceInput = {},
                    isEnabled = true,
                    isLoading = false
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Ask Luna anything! 🌟")
            .assertIsDisplayed()

        // Given - Loading state
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = "",
                    onMessageChange = {},
                    onSendMessage = {},
                    onVoiceInput = {},
                    isEnabled = false,
                    isLoading = true
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Luna is thinking...")
            .assertIsDisplayed()
    }

    @Test
    fun messageInput_hasLargeTouchTargets() {
        // Given
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = "",
                    onMessageChange = {},
                    onSendMessage = {},
                    onVoiceInput = {},
                    isEnabled = true,
                    isLoading = false
                )
            }
        }

        // Then - Verify buttons have adequate size for child-friendly interaction
        composeTestRule
            .onNodeWithTag("send_button")
            .assertIsDisplayed()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
        
        composeTestRule
            .onNodeWithTag("voice_input_button")
            .assertIsDisplayed()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun messageInput_handlesMultilineText() {
        // Given
        var currentMessage = ""
        
        // When
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = currentMessage,
                    onMessageChange = { currentMessage = it },
                    onSendMessage = {},
                    onVoiceInput = {},
                    isEnabled = true,
                    isLoading = false
                )
            }
        }

        // Type multiline text
        val multilineText = "This is a long message\nthat spans multiple lines\nto test the input field"
        composeTestRule
            .onNodeWithTag("message_text_field")
            .performTextInput(multilineText)

        // Then
        composeTestRule
            .onNodeWithTag("message_text_field")
            .assertTextContains("This is a long message")
            .assertTextContains("that spans multiple lines")
    }
}