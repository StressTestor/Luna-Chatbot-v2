package com.luna.chat.presentation.ui.screen

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.ChatSession
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.presentation.theme.LunaTheme
import com.luna.chat.presentation.viewmodel.ChatUiState
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var sampleMessages: List<ChatMessage>
    private lateinit var sampleSession: ChatSession
    private lateinit var emptySession: ChatSession

    @Before
    fun setUp() {
        sampleMessages = listOf(
            ChatMessage.create(
                content = "Hi Luna! Can you help me with my math homework?",
                isFromUser = true,
                status = MessageStatus.DELIVERED
            ),
            ChatMessage.create(
                content = "Hi there! I'd love to help you with your math homework! What problem are you working on? 📚✨",
                isFromUser = false,
                status = MessageStatus.DELIVERED
            ),
            ChatMessage.create(
                content = "I need help with fractions. How do I add 1/4 + 1/3?",
                isFromUser = true,
                status = MessageStatus.DELIVERED
            )
        )
        
        sampleSession = ChatSession.create().copy(messages = sampleMessages)
        emptySession = ChatSession.create()
    }

    @Test
    fun chatContent_displaysWelcomeCard_whenShowWelcomeCardIsTrue() {
        val uiState = ChatUiState(showWelcomeCard = true)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = emptySession,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        composeTestRule.onNodeWithTag("welcome_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome_title").assertTextContains("Hi there! I'm Luna!")
        composeTestRule.onNodeWithTag("welcome_description").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome_suggestions_title").assertTextContains("Try asking me:")
    }

    @Test
    fun chatContent_hidesWelcomeCard_whenShowWelcomeCardIsFalse() {
        val uiState = ChatUiState(showWelcomeCard = false)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = sampleSession,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        composeTestRule.onNodeWithTag("welcome_card").assertDoesNotExist()
    }

    @Test
    fun chatContent_displaysMessages_whenSessionHasMessages() {
        val uiState = ChatUiState(showWelcomeCard = false)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = sampleSession,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        // Check that all messages are displayed
        sampleMessages.forEach { message ->
            composeTestRule.onNodeWithTag("message_${message.id}").assertIsDisplayed()
        }
        
        // Check message content
        composeTestRule.onNodeWithTag("user_message_text").assertTextContains("Hi Luna!")
        composeTestRule.onNodeWithTag("ai_message_text").assertTextContains("I'd love to help")
    }

    @Test
    fun chatContent_displaysTypingIndicator_whenShowTypingIndicatorIsTrue() {
        val uiState = ChatUiState(showTypingIndicator = true)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = sampleSession,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        composeTestRule.onNodeWithTag("chat_typing_indicator").assertIsDisplayed()
    }

    @Test
    fun chatContent_hidesTypingIndicator_whenShowTypingIndicatorIsFalse() {
        val uiState = ChatUiState(showTypingIndicator = false)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = sampleSession,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        composeTestRule.onNodeWithTag("chat_typing_indicator").assertDoesNotExist()
    }

    @Test
    fun chatContent_displaysErrorMessage_whenHasErrorIsTrue() {
        val errorMessage = "Check your internet connection and try again! 🌐"
        val uiState = ChatUiState(error = errorMessage)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = sampleSession,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        composeTestRule.onNodeWithTag("error_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("error_message_text").assertTextContains(errorMessage)
        composeTestRule.onNodeWithTag("error_retry_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("error_dismiss_button").assertIsDisplayed()
    }

    @Test
    fun welcomeCard_callsOnDismiss_whenDismissButtonClicked() {
        var dismissCalled = false
        val uiState = ChatUiState(showWelcomeCard = true)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = emptySession,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = { dismissCalled = true },
                    onSendSuggestion = {}
                )
            }
        }
        
        composeTestRule.onNodeWithTag("welcome_dismiss_button").performClick()
        assert(dismissCalled)
    }

    @Test
    fun welcomeCard_callsOnDismiss_whenSuggestionChipClicked() {
        var dismissCalled = false
        val uiState = ChatUiState(showWelcomeCard = true)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = emptySession,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = { dismissCalled = true },
                    onSendSuggestion = {}
                )
            }
        }
        
        composeTestRule.onNodeWithTag("welcome_suggestion_0").performClick()
        assert(dismissCalled)
    }

    @Test
    fun errorMessage_callsOnRetry_whenRetryButtonClicked() {
        var retryCalled = false
        val uiState = ChatUiState(error = "Test error message")
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = sampleSession,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = { retryCalled = true },
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        composeTestRule.onNodeWithTag("error_retry_button").performClick()
        assert(retryCalled)
    }

    @Test
    fun errorMessage_callsOnDismiss_whenDismissButtonClicked() {
        var dismissCalled = false
        val uiState = ChatUiState(error = "Test error message")
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = sampleSession,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = { dismissCalled = true },
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        composeTestRule.onNodeWithTag("error_dismiss_button").performClick()
        assert(dismissCalled)
    }

    @Test
    fun chatTopBar_displaysLunaTitle() {
        composeTestRule.setContent {
            LunaTheme {
                ChatTopBar(
                    onSettingsClick = {},
                    onNewChatClick = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText("Luna").assertIsDisplayed()
        composeTestRule.onNodeWithText("🌙").assertIsDisplayed()
    }

    @Test
    fun chatTopBar_callsOnSettingsClick_whenSettingsButtonClicked() {
        var settingsClicked = false
        
        composeTestRule.setContent {
            LunaTheme {
                ChatTopBar(
                    onSettingsClick = { settingsClicked = true },
                    onNewChatClick = {}
                )
            }
        }
        
        composeTestRule.onNodeWithTag("settings_button").performClick()
        assert(settingsClicked)
    }

    @Test
    fun chatTopBar_callsOnNewChatClick_whenNewChatButtonClicked() {
        var newChatClicked = false
        
        composeTestRule.setContent {
            LunaTheme {
                ChatTopBar(
                    onSettingsClick = {},
                    onNewChatClick = { newChatClicked = true }
                )
            }
        }
        
        composeTestRule.onNodeWithTag("new_chat_button").performClick()
        assert(newChatClicked)
    }

    @Test
    fun welcomeCard_displaysAllSuggestions() {
        composeTestRule.setContent {
            LunaTheme {
                WelcomeCard(
                    onDismiss = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        // Check that all 5 suggestion chips are displayed
        for (i in 0..4) {
            composeTestRule.onNodeWithTag("welcome_suggestion_$i").assertIsDisplayed()
        }
        
        // Check specific suggestion content
        composeTestRule.onNodeWithText("🧮 Help me with math homework", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("🔬 Tell me about space and planets", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("📖 Help me write a story", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("🎲 Let's play a word game", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("🎨 Give me a fun drawing idea", substring = true).assertIsDisplayed()
    }

    @Test
    fun welcomeCard_hasAccessibilitySupport() {
        composeTestRule.setContent {
            LunaTheme {
                WelcomeCard(
                    onDismiss = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        // Check that the dismiss button has proper content description
        composeTestRule.onNodeWithContentDescription("Start chatting with Luna").assertIsDisplayed()
    }

    @Test
    fun errorMessage_displaysCorrectEmoji() {
        val uiState = ChatUiState(error = "Test error")
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = sampleSession,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        composeTestRule.onNodeWithTag("error_emoji").assertTextEquals("😅")
    }

    @Test
    fun chatContent_hasCorrectTestTags() {
        val uiState = ChatUiState(showWelcomeCard = false)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = sampleSession,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        composeTestRule.onNodeWithTag("chat_messages_list").assertIsDisplayed()
    }

    // Educational Feature Tests

    @Test
    fun welcomeCard_callsOnSendSuggestion_whenSuggestionChipClicked() {
        var sentSuggestion = ""
        val uiState = ChatUiState(showWelcomeCard = true)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = emptySession,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = { suggestion -> sentSuggestion = suggestion }
                )
            }
        }
        
        composeTestRule.onNodeWithTag("welcome_suggestion_0").performClick()
        assert(sentSuggestion.isNotEmpty())
        assert(sentSuggestion.contains("Help me with math homework"))
    }

    @Test
    fun chatContent_displaysEducationalPrompt_whenMessageCountIsMultipleOfFive() {
        // Create a session with exactly 5 messages to trigger educational prompt
        val messagesForPrompt = (1..5).map { index ->
            ChatMessage.create(
                content = "Message $index",
                isFromUser = index % 2 == 1,
                status = MessageStatus.DELIVERED
            )
        }
        val sessionWithPrompt = ChatSession.create().copy(messages = messagesForPrompt)
        val uiState = ChatUiState(showWelcomeCard = false)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = sessionWithPrompt,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        composeTestRule.onNodeWithTag("educational_prompt").assertIsDisplayed()
        composeTestRule.onNodeWithTag("prompt_title").assertTextContains("Great chatting! Want to try something new?")
    }

    @Test
    fun chatContent_hidesEducationalPrompt_whenWelcomeCardIsShown() {
        // Create a session with exactly 5 messages but welcome card is shown
        val messagesForPrompt = (1..5).map { index ->
            ChatMessage.create(
                content = "Message $index",
                isFromUser = index % 2 == 1,
                status = MessageStatus.DELIVERED
            )
        }
        val sessionWithPrompt = ChatSession.create().copy(messages = messagesForPrompt)
        val uiState = ChatUiState(showWelcomeCard = true)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = sessionWithPrompt,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        composeTestRule.onNodeWithTag("educational_prompt").assertDoesNotExist()
        composeTestRule.onNodeWithTag("welcome_card").assertIsDisplayed()
    }

    @Test
    fun educationalPromptCard_displaysCorrectSuggestions() {
        val messagesForPrompt = (1..5).map { index ->
            ChatMessage.create(
                content = "Message $index",
                isFromUser = index % 2 == 1,
                status = MessageStatus.DELIVERED
            )
        }
        val sessionWithPrompt = ChatSession.create().copy(messages = messagesForPrompt)
        val uiState = ChatUiState(showWelcomeCard = false)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = sessionWithPrompt,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        // Check that all 4 educational suggestions are displayed
        for (i in 0..3) {
            composeTestRule.onNodeWithTag("educational_suggestion_$i").assertIsDisplayed()
        }
        
        // Check specific educational content
        composeTestRule.onNodeWithText("🧮 Can you help me with a math problem?", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("🔬 Tell me something cool about science!", substring = true).assertIsDisplayed()
    }

    @Test
    fun educationalPromptCard_callsOnSendSuggestion_whenSuggestionClicked() {
        var sentSuggestion = ""
        val messagesForPrompt = (1..5).map { index ->
            ChatMessage.create(
                content = "Message $index",
                isFromUser = index % 2 == 1,
                status = MessageStatus.DELIVERED
            )
        }
        val sessionWithPrompt = ChatSession.create().copy(messages = messagesForPrompt)
        val uiState = ChatUiState(showWelcomeCard = false)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = sessionWithPrompt,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = { suggestion -> sentSuggestion = suggestion }
                )
            }
        }
        
        composeTestRule.onNodeWithTag("educational_suggestion_0").performClick()
        assert(sentSuggestion.isNotEmpty())
        assert(sentSuggestion.contains("Can you help me with a math problem?"))
    }

    @Test
    fun educationalPromptCard_showsDifferentPrompts_forDifferentMessageCounts() {
        // Test first set of prompts (5 messages)
        val firstPromptMessages = (1..5).map { index ->
            ChatMessage.create(
                content = "Message $index",
                isFromUser = index % 2 == 1,
                status = MessageStatus.DELIVERED
            )
        }
        val firstSession = ChatSession.create().copy(messages = firstPromptMessages)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = firstSession,
                    uiState = ChatUiState(showWelcomeCard = false),
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        // Should show first set of prompts
        composeTestRule.onNodeWithText("🧮 Can you help me with a math problem?", substring = true).assertIsDisplayed()
        
        // Test second set of prompts (10 messages)
        val secondPromptMessages = (1..10).map { index ->
            ChatMessage.create(
                content = "Message $index",
                isFromUser = index % 2 == 1,
                status = MessageStatus.DELIVERED
            )
        }
        val secondSession = ChatSession.create().copy(messages = secondPromptMessages)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = secondSession,
                    uiState = ChatUiState(showWelcomeCard = false),
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        // Should show second set of prompts
        composeTestRule.onNodeWithText("🎨 Let's be creative together!", substring = true).assertIsDisplayed()
    }

    @Test
    fun interactiveSuggestionChip_hasCorrectStyling() {
        val uiState = ChatUiState(showWelcomeCard = true)
        
        composeTestRule.setContent {
            LunaTheme {
                ChatContent(
                    session = emptySession,
                    uiState = uiState,
                    listState = rememberLazyListState(),
                    onRetryMessage = {},
                    onDismissError = {},
                    onHideWelcomeCard = {},
                    onSendSuggestion = {}
                )
            }
        }
        
        // Check that suggestion chips are clickable and displayed
        composeTestRule.onNodeWithTag("welcome_suggestion_0")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}