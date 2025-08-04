package com.luna.chat.presentation.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.presentation.theme.LunaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageBubbleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun messageBubble_displaysUserMessage_correctly() {
        // Given
        val userMessage = ChatMessage.create(
            content = "Hello Luna!",
            isFromUser = true,
            status = MessageStatus.DELIVERED
        )

        // When
        composeTestRule.setContent {
            LunaTheme {
                MessageBubble(message = userMessage)
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("message_bubble_${userMessage.id}")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("user_message_text")
            .assertIsDisplayed()
            .assertTextEquals("Hello Luna!")
        
        composeTestRule
            .onNodeWithTag("message_timestamp")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("message_status")
            .assertIsDisplayed()
    }

    @Test
    fun messageBubble_displaysAiMessage_correctly() {
        // Given
        val aiMessage = ChatMessage.create(
            content = "Hi there! How can I help you today? 😊",
            isFromUser = false,
            status = MessageStatus.DELIVERED
        )

        // When
        composeTestRule.setContent {
            LunaTheme {
                MessageBubble(message = aiMessage)
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("message_bubble_${aiMessage.id}")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("ai_message_text")
            .assertIsDisplayed()
            .assertTextEquals("Hi there! How can I help you today? 😊")
        
        composeTestRule
            .onNodeWithTag("luna_avatar")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("ai_message_timestamp")
            .assertIsDisplayed()
    }

    @Test
    fun messageBubble_handlesLongText_correctly() {
        // Given
        val longMessage = ChatMessage.create(
            content = "This is a very long message that should wrap properly within the message bubble constraints and maintain good readability for children using the app.",
            isFromUser = true,
            status = MessageStatus.SENT
        )

        // When
        composeTestRule.setContent {
            LunaTheme {
                MessageBubble(message = longMessage)
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("user_message_text")
            .assertIsDisplayed()
            .assertTextContains("This is a very long message")
    }

    @Test
    fun messageBubble_showsCorrectStatus_forDifferentStates() {
        // Test different message statuses
        val statuses = listOf(
            MessageStatus.SENDING,
            MessageStatus.SENT,
            MessageStatus.DELIVERED,
            MessageStatus.FAILED
        )

        statuses.forEach { status ->
            val message = ChatMessage.create(
                content = "Test message",
                isFromUser = true,
                status = status
            )

            composeTestRule.setContent {
                LunaTheme {
                    MessageBubble(message = message)
                }
            }

            composeTestRule
                .onNodeWithTag("message_status")
                .assertIsDisplayed()
        }
    }

    @Test
    fun messageBubble_hasAccessibilitySupport() {
        // Given
        val userMessage = ChatMessage.create(
            content = "Test accessibility",
            isFromUser = true,
            status = MessageStatus.DELIVERED
        )

        // When
        composeTestRule.setContent {
            LunaTheme {
                MessageBubble(message = userMessage)
            }
        }

        // Then - Check that content descriptions are present for accessibility
        composeTestRule
            .onNode(hasContentDescription("Your message: Test accessibility"))
            .assertIsDisplayed()
    }

    @Test
    fun messageBubble_animatesOnAppearance() {
        // Given
        val message = ChatMessage.create(
            content = "Animated message",
            isFromUser = true,
            status = MessageStatus.DELIVERED
        )

        // When - Start with invisible, then make visible
        composeTestRule.setContent {
            LunaTheme {
                MessageBubble(
                    message = message,
                    isVisible = false
                )
            }
        }

        // Initially not displayed
        composeTestRule
            .onNodeWithTag("message_bubble_${message.id}")
            .assertDoesNotExist()

        // Make visible
        composeTestRule.setContent {
            LunaTheme {
                MessageBubble(
                    message = message,
                    isVisible = true
                )
            }
        }

        // Should appear with animation
        composeTestRule
            .onNodeWithTag("message_bubble_${message.id}")
            .assertIsDisplayed()
    }

    @Test
    fun messageBubble_hasLargeTouchTargets() {
        // Given
        val message = ChatMessage.create(
            content = "Touch target test",
            isFromUser = true,
            status = MessageStatus.DELIVERED
        )

        // When
        composeTestRule.setContent {
            LunaTheme {
                MessageBubble(message = message)
            }
        }

        // Then - Verify the message bubble has adequate size for child-friendly interaction
        composeTestRule
            .onNodeWithTag("user_message_text")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp) // Minimum touch target size
    }
}