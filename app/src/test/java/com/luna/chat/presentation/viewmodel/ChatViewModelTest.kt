package com.luna.chat.presentation.viewmodel

import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.ChatSession
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.domain.usecase.SendMessageUseCase
import com.luna.chat.domain.usecase.ChatHistoryUseCase
import com.luna.chat.domain.usecase.ContentFilterException
import com.luna.chat.data.repository.UserPreferencesRepository
import com.luna.chat.data.repository.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.io.IOException
import java.net.UnknownHostException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @Mock
    private lateinit var sendMessageUseCase: SendMessageUseCase

    @Mock
    private lateinit var chatHistoryUseCase: ChatHistoryUseCase

    @Mock
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    private lateinit var viewModel: ChatViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    
    private val sampleUserMessage = ChatMessage.create(
        content = "Test message",
        isFromUser = true,
        status = MessageStatus.DELIVERED
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Setup default mock behaviors
        whenever(userPreferencesRepository.userPreferencesFlow)
            .thenReturn(flowOf(UserPreferences()))
        whenever(chatHistoryUseCase.getChatHistory())
            .thenReturn(flowOf(emptyList()))

        viewModel = ChatViewModel(
            sendMessageUseCase = sendMessageUseCase,
            chatHistoryUseCase = chatHistoryUseCase,
            userPreferencesRepository = userPreferencesRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() {
        val initialState = viewModel.uiState.value
        
        assertTrue(initialState.isFirstMessage)
        assertTrue(initialState.showWelcomeCard)
        assertFalse(initialState.isLoading)
        assertFalse(initialState.isAiThinking)
        assertNull(initialState.error)
        assertTrue(initialState.canSendMessage)
    }

    @Test
    fun `sendMessage should handle successful message sending`() = runTest {
        // Arrange
        val userMessage = "Hello, AI!"
        val userChatMessage = ChatMessage.create(userMessage, true, status = MessageStatus.SENDING)
        val aiResponse = ChatMessage.create("Hello! How can I help you?", false, status = MessageStatus.DELIVERED)
        
        whenever(sendMessageUseCase(userMessage))
            .thenReturn(flowOf(Result.success(userChatMessage), Result.success(aiResponse)))

        // Act
        viewModel.sendMessage(userMessage)

        // Assert
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isFirstMessage)
        assertFalse(finalState.showWelcomeCard)
        assertFalse(finalState.isLoading)
        assertNull(finalState.error)

        val session = viewModel.currentSession.value
        assertEquals(2, session.messages.size)
        assertEquals(userMessage, session.messages[0].content)
        assertTrue(session.messages[0].isFromUser)
        assertEquals("Hello! How can I help you?", session.messages[1].content)
        assertFalse(session.messages[1].isFromUser)

        verify(chatHistoryUseCase).saveMessage(userChatMessage)
        verify(chatHistoryUseCase).saveMessage(aiResponse)
        verify(userPreferencesRepository).incrementMessagesSent()
    }

    @Test
    fun `sendMessage should handle blank message`() = runTest {
        // Act
        viewModel.sendMessage("")
        viewModel.sendMessage("   ")

        // Assert
        verifyNoInteractions(sendMessageUseCase)
        assertTrue(viewModel.currentSession.value.messages.isEmpty())
    }

    @Test
    fun `sendMessage should handle message too long`() = runTest {
        // Arrange
        val longMessage = "a".repeat(ChatMessage.MAX_MESSAGE_LENGTH + 1)

        // Act
        viewModel.sendMessage(longMessage)

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.hasError)
        assertTrue(state.error!!.contains("too long"))
        verifyNoInteractions(sendMessageUseCase)
    }

    @Test
    fun `sendMessage should handle content filter exception`() = runTest {
        // Arrange
        val inappropriateMessage = "inappropriate content"
        val filterException = ContentFilterException("Let's talk about something else! What would you like to learn today? 📚")
        
        whenever(sendMessageUseCase(inappropriateMessage))
            .thenReturn(flowOf(Result.failure(filterException)))

        // Act
        viewModel.sendMessage(inappropriateMessage)

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.hasError)
        assertEquals(filterException.message, state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `sendMessage should handle network errors with friendly messages`() = runTest {
        val testCases = listOf(
            UnknownHostException() to "Check your internet connection and try again! 🌐",
            SocketTimeoutException() to "The request took too long. Please try again! ⏰",
            IOException() to "Network error. Please check your connection! 📶",
            RuntimeException() to "Something went wrong. Let's try that again! 🤖"
        )

        testCases.forEach { (exception, expectedMessage) ->
            // Arrange
            whenever(sendMessageUseCase("test"))
                .thenReturn(flow { throw exception })

            // Act
            viewModel.sendMessage("test")

            // Assert
            val state = viewModel.uiState.value
            assertTrue(state.hasError)
            assertEquals(expectedMessage, state.error)
            assertFalse(state.isLoading)

            // Reset for next test
            viewModel.dismissError()
        }
    }

    @Test
    fun `startNewChat should create new session and save current messages`() = runTest {
        // Arrange - add some messages to current session
        val message1 = ChatMessage.create("Hello", true)
        val message2 = ChatMessage.create("Hi there!", false)
        
        whenever(sendMessageUseCase("Hello"))
            .thenReturn(flowOf(Result.success(message1), Result.success(message2)))
        
        viewModel.sendMessage("Hello")

        // Act
        viewModel.startNewChat()

        // Assert
        val session = viewModel.currentSession.value
        assertTrue(session.messages.isEmpty())
        
        val state = viewModel.uiState.value
        assertTrue(state.isFirstMessage)
        assertTrue(state.showWelcomeCard)
        assertNull(state.error)

        verify(chatHistoryUseCase).saveMessages(any())
    }

    @Test
    fun `clearChatHistory should clear all data`() = runTest {
        // Act
        viewModel.clearChatHistory()

        // Assert
        val session = viewModel.currentSession.value
        assertTrue(session.messages.isEmpty())
        
        val state = viewModel.uiState.value
        assertTrue(state.isFirstMessage)
        assertTrue(state.showWelcomeCard)
        assertNull(state.error)

        verify(chatHistoryUseCase).clearChatHistory()
    }

    @Test
    fun `retryLastMessage should resend failed user message`() = runTest {
        // Arrange - simulate a failed message
        val failedMessage = ChatMessage.create("test message", true, status = MessageStatus.FAILED)
        viewModel.currentSession.value.addMessage(failedMessage)

        whenever(sendMessageUseCase("test message"))
            .thenReturn(flowOf(Result.success(failedMessage)))

        // Act
        viewModel.retryLastMessage()

        // Assert
        verify(sendMessageUseCase).invoke("test message")
    }

    @Test
    fun `dismissError should clear error state`() = runTest {
        // Arrange - set an error
        whenever(sendMessageUseCase("test"))
            .thenReturn(flow { throw RuntimeException() })
        
        viewModel.sendMessage("test")
        assertTrue(viewModel.uiState.value.hasError)

        // Act
        viewModel.dismissError()

        // Assert
        assertFalse(viewModel.uiState.value.hasError)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `hideWelcomeCard should update state`() = runTest {
        // Arrange
        assertTrue(viewModel.uiState.value.showWelcomeCard)

        // Act
        viewModel.hideWelcomeCard()

        // Assert
        assertFalse(viewModel.uiState.value.showWelcomeCard)
    }

    @Test
    fun `setTyping should update typing state`() = runTest {
        // Act
        viewModel.setTyping(true)

        // Assert
        assertTrue(viewModel.uiState.value.isUserTyping)

        // Act
        viewModel.setTyping(false)

        // Assert
        assertFalse(viewModel.uiState.value.isUserTyping)
    }

    @Test
    fun `loadChatHistory should populate session with existing messages`() = runTest {
        // Arrange
        val existingMessages = listOf(
            ChatMessage.create("Previous message 1", true),
            ChatMessage.create("Previous response 1", false),
            ChatMessage.create("Previous message 2", true),
            ChatMessage.create("Previous response 2", false)
        )

        whenever(chatHistoryUseCase.getChatHistory())
            .thenReturn(flowOf(existingMessages))

        // Act - create new viewModel to trigger init
        val newViewModel = ChatViewModel(
            sendMessageUseCase = sendMessageUseCase,
            chatHistoryUseCase = chatHistoryUseCase,
            userPreferencesRepository = userPreferencesRepository
        )

        // Assert
        val session = newViewModel.currentSession.value
        assertEquals(4, session.messages.size)
        assertEquals(existingMessages, session.messages)

        val state = newViewModel.uiState.value
        assertFalse(state.isFirstMessage)
        assertFalse(state.showWelcomeCard)
    }

    @Test
    fun `observeUserPreferences should update UI state`() = runTest {
        // Arrange
        val preferences = UserPreferences(
            firstTimeUser = false,
            contentFilterEnabled = false
        )

        whenever(userPreferencesRepository.userPreferencesFlow)
            .thenReturn(flowOf(preferences))

        // Act - create new viewModel to trigger init
        val newViewModel = ChatViewModel(
            sendMessageUseCase = sendMessageUseCase,
            chatHistoryUseCase = chatHistoryUseCase,
            userPreferencesRepository = userPreferencesRepository
        )

        // Assert
        val state = newViewModel.uiState.value
        assertFalse(state.isFirstTimeUser)
        assertFalse(state.contentFilterEnabled)
    }

    @Test
    fun `ChatUiState computed properties should work correctly`() {
        // Test canSendMessage
        var state = ChatUiState(isLoading = true)
        assertFalse(state.canSendMessage)

        state = ChatUiState(isAiThinking = true)
        assertFalse(state.canSendMessage)

        state = ChatUiState(isLoading = false, isAiThinking = false)
        assertTrue(state.canSendMessage)

        // Test showTypingIndicator
        state = ChatUiState(isAiThinking = true, isLoading = false)
        assertTrue(state.showTypingIndicator)

        state = ChatUiState(isAiThinking = true, isLoading = true)
        assertFalse(state.showTypingIndicator)

        // Test hasError
        state = ChatUiState(error = null)
        assertFalse(state.hasError)

        state = ChatUiState(error = "Some error")
        assertTrue(state.hasError)
    }

    // Educational Feature Tests

    @Test
    fun `sendEducationalPrompt should add math context when prompt contains math`() = runTest {
        // Arrange
        val mathPrompt = "Help me with math homework"
        val expectedContextualPrompt = "$mathPrompt Please explain this in a way that's easy for an 11-year-old to understand, with step-by-step examples."
        
        whenever(sendMessageUseCase(expectedContextualPrompt))
            .thenReturn(flowOf(Result.success(sampleUserMessage)))
        
        // Act
        viewModel.sendEducationalPrompt(mathPrompt)
        
        // Assert
        verify(sendMessageUseCase).invoke(expectedContextualPrompt)
    }

    @Test
    fun `sendEducationalPrompt should add science context when prompt contains science`() = runTest {
        // Arrange
        val sciencePrompt = "Tell me about science topics"
        val expectedContextualPrompt = "$sciencePrompt Please explain this science topic in a fun and engaging way for a curious 11-year-old, using simple examples."
        
        whenever(sendMessageUseCase(expectedContextualPrompt))
            .thenReturn(flowOf(Result.success(sampleUserMessage)))
        
        // Act
        viewModel.sendEducationalPrompt(sciencePrompt)
        
        // Assert
        verify(sendMessageUseCase).invoke(expectedContextualPrompt)
    }

    @Test
    fun `sendEducationalPrompt should add story context when prompt contains story`() = runTest {
        // Arrange
        val storyPrompt = "Help me write a story"
        val expectedContextualPrompt = "$storyPrompt Help me create an age-appropriate, fun story suitable for an 11-year-old."
        
        whenever(sendMessageUseCase(expectedContextualPrompt))
            .thenReturn(flowOf(Result.success(sampleUserMessage)))
        
        // Act
        viewModel.sendEducationalPrompt(storyPrompt)
        
        // Assert
        verify(sendMessageUseCase).invoke(expectedContextualPrompt)
    }

    @Test
    fun `sendEducationalPrompt should add game context when prompt contains game`() = runTest {
        // Arrange
        val gamePrompt = "Let's play a game"
        val expectedContextualPrompt = "$gamePrompt Let's play a fun, educational game that's perfect for an 11-year-old."
        
        whenever(sendMessageUseCase(expectedContextualPrompt))
            .thenReturn(flowOf(Result.success(sampleUserMessage)))
        
        // Act
        viewModel.sendEducationalPrompt(gamePrompt)
        
        // Assert
        verify(sendMessageUseCase).invoke(expectedContextualPrompt)
    }

    @Test
    fun `sendEducationalPrompt should add creative context when prompt contains creative`() = runTest {
        // Arrange
        val creativePrompt = "Let's be creative"
        val expectedContextualPrompt = "$creativePrompt Let's do something creative and fun that an 11-year-old would enjoy!"
        
        whenever(sendMessageUseCase(expectedContextualPrompt))
            .thenReturn(flowOf(Result.success(sampleUserMessage)))
        
        // Act
        viewModel.sendEducationalPrompt(creativePrompt)
        
        // Assert
        verify(sendMessageUseCase).invoke(expectedContextualPrompt)
    }

    @Test
    fun `sendEducationalPrompt should add default context when prompt does not match specific categories`() = runTest {
        // Arrange
        val genericPrompt = "Tell me something interesting"
        val expectedContextualPrompt = "$genericPrompt Please respond in a way that's fun and appropriate for an 11-year-old."
        
        whenever(sendMessageUseCase(expectedContextualPrompt))
            .thenReturn(flowOf(Result.success(sampleUserMessage)))
        
        // Act
        viewModel.sendEducationalPrompt(genericPrompt)
        
        // Assert
        verify(sendMessageUseCase).invoke(expectedContextualPrompt)
    }

    @Test
    fun `getContextualSuggestions should return initial suggestions when no messages`() = runTest {
        // Act
        val suggestions = viewModel.getContextualSuggestions()
        
        // Assert
        assertEquals(5, suggestions.size)
        assertTrue(suggestions.contains("Help me with math homework"))
        assertTrue(suggestions.contains("Tell me about space and planets"))
        assertTrue(suggestions.contains("Help me write a story"))
        assertTrue(suggestions.contains("Let's play a word game"))
        assertTrue(suggestions.contains("Give me a fun drawing idea"))
    }

    @Test
    fun `getContextualSuggestions should return math suggestions when recent messages contain math`() = runTest {
        // Arrange
        val mathMessages = listOf(
            ChatMessage.create("Help me with math problems", true, status = MessageStatus.DELIVERED),
            ChatMessage.create("Sure! I'd love to help with math.", false, status = MessageStatus.DELIVERED)
        )
        val sessionWithMath = ChatSession.create().copy(messages = mathMessages)
        viewModel.currentSession.value = sessionWithMath
        
        // Act
        val suggestions = viewModel.getContextualSuggestions()
        
        // Assert
        assertEquals(4, suggestions.size)
        assertTrue(suggestions.contains("Show me another math problem"))
        assertTrue(suggestions.contains("Explain fractions with pizza slices"))
        assertTrue(suggestions.contains("Help me with multiplication tables"))
        assertTrue(suggestions.contains("What's a fun math game?"))
    }

    @Test
    fun `getContextualSuggestions should return science suggestions when recent messages contain science`() = runTest {
        // Arrange
        val scienceMessages = listOf(
            ChatMessage.create("Tell me about science", true, status = MessageStatus.DELIVERED),
            ChatMessage.create("Science is fascinating! What would you like to know?", false, status = MessageStatus.DELIVERED)
        )
        val sessionWithScience = ChatSession.create().copy(messages = scienceMessages)
        viewModel.currentSession.value = sessionWithScience
        
        // Act
        val suggestions = viewModel.getContextualSuggestions()
        
        // Assert
        assertEquals(4, suggestions.size)
        assertTrue(suggestions.contains("Tell me about dinosaurs"))
        assertTrue(suggestions.contains("How do volcanoes work?"))
        assertTrue(suggestions.contains("What makes rainbows?"))
        assertTrue(suggestions.contains("Fun facts about animals"))
    }

    @Test
    fun `getContextualSuggestions should return story suggestions when recent messages contain story`() = runTest {
        // Arrange
        val storyMessages = listOf(
            ChatMessage.create("Help me write a story", true, status = MessageStatus.DELIVERED),
            ChatMessage.create("I'd love to help you write a story!", false, status = MessageStatus.DELIVERED)
        )
        val sessionWithStory = ChatSession.create().copy(messages = storyMessages)
        viewModel.currentSession.value = sessionWithStory
        
        // Act
        val suggestions = viewModel.getContextualSuggestions()
        
        // Assert
        assertEquals(4, suggestions.size)
        assertTrue(suggestions.contains("Help me write another story"))
        assertTrue(suggestions.contains("Give me story ideas"))
        assertTrue(suggestions.contains("What makes a good character?"))
        assertTrue(suggestions.contains("Let's create a funny ending"))
    }

    @Test
    fun `getContextualSuggestions should return generic suggestions when no specific topic detected`() = runTest {
        // Arrange
        val genericMessages = listOf(
            ChatMessage.create("Hello there", true, status = MessageStatus.DELIVERED),
            ChatMessage.create("Hi! How can I help you today?", false, status = MessageStatus.DELIVERED)
        )
        val sessionWithGeneric = ChatSession.create().copy(messages = genericMessages)
        viewModel.currentSession.value = sessionWithGeneric
        
        // Act
        val suggestions = viewModel.getContextualSuggestions()
        
        // Assert
        assertEquals(4, suggestions.size)
        assertTrue(suggestions.contains("Ask me something new!"))
        assertTrue(suggestions.contains("Let's try a different topic"))
        assertTrue(suggestions.contains("What would you like to learn?"))
        assertTrue(suggestions.contains("Tell me about your day"))
    }
}