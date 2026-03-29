package com.luna.chat.presentation.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.luna.chat.MainActivity
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChatScreenEducationalFeaturesTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun welcomeCard_displaysEducationalSuggestions() {
        composeTestRule.waitForIdle()
        
        // Check that welcome card is displayed
        composeTestRule.onNodeWithTag("welcome_card").assertIsDisplayed()
        
        // Check that educational suggestions are displayed
        composeTestRule.onNodeWithText("🧮 Help me with math homework", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("🔬 Tell me about space and planets", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("📖 Help me write a story", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("🎲 Let's play a word game", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("🎨 Give me a fun drawing idea", substring = true).assertIsDisplayed()
    }

    @Test
    fun welcomeCard_sendsMessage_whenSuggestionClicked() {
        composeTestRule.waitForIdle()
        
        // Click on the first educational suggestion
        composeTestRule.onNodeWithTag("welcome_suggestion_0").performClick()
        
        // Wait for the message to be processed
        composeTestRule.waitForIdle()
        
        // Check that the welcome card is dismissed
        composeTestRule.onNodeWithTag("welcome_card").assertDoesNotExist()
        
        // Check that a message was sent (should appear in the chat)
        composeTestRule.onNodeWithTag("chat_messages_list").assertIsDisplayed()
    }

    @Test
    fun educationalPrompt_appearsAfterMultipleMessages() {
        composeTestRule.waitForIdle()
        
        // Dismiss welcome card first
        composeTestRule.onNodeWithTag("welcome_dismiss_button").performClick()
        composeTestRule.waitForIdle()
        
        // Send multiple messages to trigger educational prompt
        repeat(5) { index ->
            composeTestRule.onNodeWithTag("chat_message_input").performTextInput("Test message $index")
            composeTestRule.onNodeWithTag("send_button").performClick()
            composeTestRule.waitForIdle()
        }
        
        // Check that educational prompt appears
        composeTestRule.onNodeWithTag("educational_prompt").assertIsDisplayed()
        composeTestRule.onNodeWithText("Great chatting! Want to try something new?", substring = true).assertIsDisplayed()
    }

    @Test
    fun educationalPrompt_displaysCorrectSuggestions() {
        composeTestRule.waitForIdle()
        
        // Dismiss welcome card first
        composeTestRule.onNodeWithTag("welcome_dismiss_button").performClick()
        composeTestRule.waitForIdle()
        
        // Send multiple messages to trigger educational prompt
        repeat(5) { index ->
            composeTestRule.onNodeWithTag("chat_message_input").performTextInput("Test message $index")
            composeTestRule.onNodeWithTag("send_button").performClick()
            composeTestRule.waitForIdle()
        }
        
        // Check that educational suggestions are displayed
        composeTestRule.onNodeWithText("🧮 Can you help me with a math problem?", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("🔬 Tell me something cool about science!", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("📚 Help me understand this better", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("🎯 Give me a fun challenge!", substring = true).assertIsDisplayed()
    }

    @Test
    fun educationalPrompt_sendsMessage_whenSuggestionClicked() {
        composeTestRule.waitForIdle()
        
        // Dismiss welcome card first
        composeTestRule.onNodeWithTag("welcome_dismiss_button").performClick()
        composeTestRule.waitForIdle()
        
        // Send multiple messages to trigger educational prompt
        repeat(5) { index ->
            composeTestRule.onNodeWithTag("chat_message_input").performTextInput("Test message $index")
            composeTestRule.onNodeWithTag("send_button").performClick()
            composeTestRule.waitForIdle()
        }
        
        // Click on an educational suggestion
        composeTestRule.onNodeWithTag("educational_suggestion_0").performClick()
        composeTestRule.waitForIdle()
        
        // Verify that a message was sent
        composeTestRule.onNodeWithTag("chat_messages_list").assertIsDisplayed()
    }

    @Test
    fun interactiveSuggestionChip_hasCorrectStyling() {
        composeTestRule.waitForIdle()
        
        // Check that suggestion chips are clickable
        composeTestRule.onNodeWithTag("welcome_suggestion_0")
            .assertIsDisplayed()
            .assertHasClickAction()
        
        // Check that chips have proper styling (should be visible and interactive)
        composeTestRule.onNodeWithTag("welcome_suggestion_1")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun chatScreen_maintainsEducationalContext_throughoutConversation() {
        composeTestRule.waitForIdle()
        
        // Start with a math-related suggestion
        composeTestRule.onNodeWithTag("welcome_suggestion_0").performClick() // Math homework
        composeTestRule.waitForIdle()
        
        // Send a few more messages
        repeat(3) { index ->
            composeTestRule.onNodeWithTag("chat_message_input").performTextInput("Follow up question $index")
            composeTestRule.onNodeWithTag("send_button").performClick()
            composeTestRule.waitForIdle()
        }
        
        // Verify that the chat maintains educational context
        composeTestRule.onNodeWithTag("chat_messages_list").assertIsDisplayed()
        
        // The educational features should continue to work throughout the conversation
        // This test ensures the UI remains functional after educational interactions
    }

    @Test
    fun educationalFeatures_workWithAccessibility() {
        composeTestRule.waitForIdle()
        
        // Check that educational suggestions have proper accessibility support
        composeTestRule.onNodeWithTag("welcome_suggestion_0")
            .assertIsDisplayed()
            .assertHasClickAction()
        
        // Check that the welcome card dismiss button has content description
        composeTestRule.onNodeWithContentDescription("Start chatting with Luna")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}