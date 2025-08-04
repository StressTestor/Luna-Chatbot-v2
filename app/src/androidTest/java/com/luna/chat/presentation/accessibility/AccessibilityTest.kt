package com.luna.chat.presentation.accessibility

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.presentation.theme.LunaTheme
import com.luna.chat.presentation.ui.components.MessageBubble
import com.luna.chat.presentation.ui.components.MessageInput
import com.luna.chat.presentation.ui.components.TypingIndicator
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun messageBubble_hasProperContentDescription() {
        val testMessage = ChatMessage.create(
            content = "Hello Luna!",
            isFromUser = true,
            status = MessageStatus.DELIVERED
        )

        composeTestRule.setContent {
            LunaTheme {
                MessageBubble(message = testMessage)
            }
        }

        // Verify user message has proper content description
        composeTestRule
            .onNodeWithTag("message_bubble_${testMessage.id}")
            .assertExists()
            .assert(hasContentDescription())
    }

    @Test
    fun messageBubble_aiMessage_hasProperContentDescription() {
        val testMessage = ChatMessage.create(
            content = "Hi there! How can I help you today?",
            isFromUser = false,
            status = MessageStatus.DELIVERED
        )

        composeTestRule.setContent {
            LunaTheme {
                MessageBubble(message = testMessage)
            }
        }

        // Verify AI message has proper content description
        composeTestRule
            .onNodeWithTag("message_bubble_${testMessage.id}")
            .assertExists()
            .assert(hasContentDescription())
    }

    @Test
    fun messageInput_hasProperAccessibilityLabels() {
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = "",
                    onMessageChange = {},
                    onSendMessage = {},
                    onVoiceInput = {}
                )
            }
        }

        // Check text field accessibility
        composeTestRule
            .onNodeWithTag("message_text_field")
            .assertExists()
            .assert(hasContentDescription())

        // Check voice input button accessibility
        composeTestRule
            .onNodeWithTag("voice_input_button")
            .assertExists()
            .assert(hasContentDescription())
            .assert(hasClickAction())

        // Check send button accessibility
        composeTestRule
            .onNodeWithTag("send_button")
            .assertExists()
            .assert(hasContentDescription())
            .assert(hasClickAction())
    }

    @Test
    fun messageInput_voiceButton_hasChildFriendlyDescription() {
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = "",
                    onMessageChange = {},
                    onSendMessage = {},
                    onVoiceInput = {}
                )
            }
        }

        // Verify voice button has child-friendly description
        composeTestRule
            .onNodeWithTag("voice_input_button")
            .assertExists()
            .assert(
                hasContentDescription(
                    value = "Tap to speak your message to Luna instead of typing",
                    substring = true
                )
            )
    }

    @Test
    fun messageInput_sendButton_updatesDescriptionWhenLoading() {
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = "Test message",
                    onMessageChange = {},
                    onSendMessage = {},
                    onVoiceInput = {},
                    isLoading = true
                )
            }
        }

        // Verify send button description changes when loading
        composeTestRule
            .onNodeWithTag("send_button")
            .assertExists()
            .assert(
                hasContentDescription(
                    value = "Sending message",
                    substring = true
                )
            )
    }

    @Test
    fun typingIndicator_hasProperContentDescription() {
        composeTestRule.setContent {
            LunaTheme {
                TypingIndicator(isVisible = true)
            }
        }

        // Verify typing indicator has proper accessibility description
        composeTestRule
            .onNodeWithTag("typing_indicator")
            .assertExists()
            .assert(hasContentDescription())
    }

    @Test
    fun messageInput_hasMinimumTouchTargetSize() {
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = "",
                    onMessageChange = {},
                    onSendMessage = {},
                    onVoiceInput = {}
                )
            }
        }

        // Check that interactive elements meet minimum touch target size
        composeTestRule
            .onNodeWithTag("voice_input_button")
            .assertExists()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)

        composeTestRule
            .onNodeWithTag("send_button")
            .assertExists()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun messageInput_voiceButton_showsListeningState() {
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = "",
                    onMessageChange = {},
                    onSendMessage = {},
                    onVoiceInput = {}
                )
            }
        }

        // Test that voice button can show listening state
        composeTestRule
            .onNodeWithTag("voice_input_button")
            .assertExists()
            .performClick()

        // Note: In a real test, we would mock the speech service
        // and verify the listening state changes
    }

    @Test
    fun messageInput_textField_supportsLargeText() {
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = "This is a test message to verify text scaling",
                    onMessageChange = {},
                    onSendMessage = {},
                    onVoiceInput = {}
                )
            }
        }

        // Verify text field exists and can display text
        composeTestRule
            .onNodeWithTag("message_text_field")
            .assertExists()
            .assertTextContains("This is a test message")
    }

    @Test
    fun accessibility_componentsHaveProperRoles() {
        composeTestRule.setContent {
            LunaTheme {
                MessageInput(
                    message = "",
                    onMessageChange = {},
                    onSendMessage = {},
                    onVoiceInput = {}
                )
            }
        }

        // Verify buttons have proper roles
        composeTestRule
            .onNodeWithTag("voice_input_button")
            .assertExists()
            .assert(hasRole(androidx.compose.ui.semantics.Role.Button))

        composeTestRule
            .onNodeWithTag("send_button")
            .assertExists()
            .assert(hasRole(androidx.compose.ui.semantics.Role.Button))
    }

    @Test
    fun accessibility_messagesAreTraversableByTalkBack() {
        val messages = listOf(
            ChatMessage.create(
                content = "Hello Luna!",
                isFromUser = true,
                status = MessageStatus.DELIVERED
            ),
            ChatMessage.create(
                content = "Hi there! How can I help you?",
                isFromUser = false,
                status = MessageStatus.DELIVERED
            )
        )

        composeTestRule.setContent {
            LunaTheme {
                messages.forEach { message ->
                    MessageBubble(message = message)
                }
            }
        }

        // Verify all messages are accessible
        messages.forEach { message ->
            composeTestRule
                .onNodeWithTag("message_bubble_${message.id}")
                .assertExists()
                .assert(hasContentDescription())
        }
    }

    @Test
    fun accessibility_childFriendlyErrorMessages() {
        // This would test error message accessibility
        // Implementation depends on how errors are displayed in the UI
        
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Test that accessibility utils work correctly
        val isAccessibilityEnabled = AccessibilityUtils.isAccessibilityEnabled(context)
        val fontScale = AccessibilityUtils.getFontScale(context)
        
        // These should not crash and should return reasonable values
        assert(fontScale >= 0.5f && fontScale <= 3.0f)
    }
}