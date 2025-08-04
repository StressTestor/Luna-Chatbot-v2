package com.luna.chat.data.repository

import com.luna.chat.data.local.dao.ChatDao
import com.luna.chat.data.local.entity.ChatMessageEntity
import com.luna.chat.data.remote.api.ApiException
import com.luna.chat.data.remote.api.ApiResult
import com.luna.chat.data.remote.api.GroqApiService
import com.luna.chat.data.remote.dto.GroqChatRequest
import com.luna.chat.data.remote.dto.GroqChatResponse
import com.luna.chat.data.remote.dto.GroqChoice
import com.luna.chat.data.remote.dto.GroqMessage
import com.luna.chat.data.remote.dto.GroqUsage
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import retrofit2.Response
import java.net.HttpURLConnection

class ChatRepositoryImplTest {

    @Mock
    private lateinit var groqApiService: GroqApiService

    @Mock
    private lateinit var chatDao: ChatDao

    @Mock
    private lateinit var apiKeyProvider: ApiKeyProvider

    private lateinit var chatRepository: ChatRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        chatRepository = ChatRepositoryImpl(groqApiService, chatDao, apiKeyProvider)
    }

    @Test
    fun `sendMessage should return success when API call succeeds`() = runTest {
        // Given
        val testMessage = "Hello, AI!"
        val testApiKey = "test_openrouter_key"
        val expectedResponse = "Hello! How can I help you today?"

        val mockResponse = GroqChatResponse(
            choices = listOf(
                GroqChoice(
                    message = GroqMessage.createAssistantMessage(expectedResponse),
                    finishReason = "stop"
                )
            ),
            usage = GroqUsage(10, 15, 25)
        )

        // Mock API key provider
        whenever(apiKeyProvider.getApiKey()).thenReturn(testApiKey)

        // Mock DAO to return empty conversation history
        whenever(chatDao.getRecentMessages(any())).thenReturn(flowOf(emptyList()))

        // Mock successful API response
        whenever(
            groqApiService.sendChatMessage(
                authorization = eq("Bearer $testApiKey"),
                request = any<GroqChatRequest>()
            )
        ).thenReturn(Response.success(mockResponse))

        // When
        val result = chatRepository.sendMessage(testMessage).first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())

        // Verify user message was saved
        verify(chatDao).insertMessage(argThat { entity ->
            entity.content == testMessage && 
            entity.isFromUser && 
            entity.status == MessageStatus.SENT.name
        })

        // Verify AI response was saved
        verify(chatDao).insertMessage(argThat { entity ->
            entity.content == expectedResponse && 
            !entity.isFromUser && 
            entity.status == MessageStatus.RECEIVED.name
        })
    }

    @Test
    fun `sendMessage should return failure when API key is not configured`() = runTest {
        // Given
        val testMessage = "Hello, AI!"

        // Mock API key provider to return null
        whenever(apiKeyProvider.getApiKey()).thenReturn(null)

        // When
        val result = chatRepository.sendMessage(testMessage).first()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals("API key not configured", result.exceptionOrNull()?.message)

        // Verify no API call was made
        verify(groqApiService, never()).sendChatMessage(any(), any())
    }

    @Test
    fun `sendMessage should return failure when message is blank`() = runTest {
        // Given
        val blankMessage = "   "

        // When
        val result = chatRepository.sendMessage(blankMessage).first()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Message cannot be empty", result.exceptionOrNull()?.message)

        // Verify no API call was made
        verify(groqApiService, never()).sendChatMessage(any(), any())
        verify(chatDao, never()).insertMessage(any())
    }

    @Test
    fun `sendMessage should handle API errors gracefully`() = runTest {
        // Given
        val testMessage = "Hello, AI!"
        val testApiKey = "test_openrouter_key"

        // Mock API key provider
        whenever(apiKeyProvider.getApiKey()).thenReturn(testApiKey)

        // Mock DAO to return empty conversation history
        whenever(chatDao.getRecentMessages(any())).thenReturn(flowOf(emptyList()))

        // Mock API error response
        whenever(
            groqApiService.sendChatMessage(
                authorization = eq("Bearer $testApiKey"),
                request = any<GroqChatRequest>()
            )
        ).thenReturn(Response.error(HttpURLConnection.HTTP_UNAUTHORIZED, 
            okhttp3.ResponseBody.create(null, """{"error": {"message": "Invalid API key"}}""")))

        // When
        val result = chatRepository.sendMessage(testMessage).first()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiException.ApiKeyException)

        // Verify user message was saved
        verify(chatDao).insertMessage(argThat { entity ->
            entity.content == testMessage && entity.isFromUser
        })

        // Verify error message was saved
        verify(chatDao).insertMessage(argThat { entity ->
            !entity.isFromUser && entity.status == MessageStatus.ERROR.name
        })
    }

    @Test
    fun `sendMessage should include conversation history in API request`() = runTest {
        // Given
        val testMessage = "What's 2 + 2?"
        val testApiKey = "test_openrouter_key"

        val conversationHistory = listOf(
            ChatMessageEntity(
                id = "1",
                content = "Hello",
                isFromUser = true,
                timestamp = System.currentTimeMillis() - 1000,
                sessionId = "default_session",
                status = MessageStatus.SENT.name
            ),
            ChatMessageEntity(
                id = "2",
                content = "Hi there! How can I help you?",
                isFromUser = false,
                timestamp = System.currentTimeMillis() - 500,
                sessionId = "default_session",
                status = MessageStatus.RECEIVED.name
            )
        )

        val mockResponse = GroqChatResponse(
            choices = listOf(
                GroqChoice(
                    message = GroqMessage.createAssistantMessage("2 + 2 equals 4!"),
                    finishReason = "stop"
                )
            ),
            usage = GroqUsage(15, 10, 25)
        )

        // Mock API key provider
        whenever(apiKeyProvider.getApiKey()).thenReturn(testApiKey)

        // Mock DAO to return conversation history
        whenever(chatDao.getRecentMessages(any())).thenReturn(flowOf(conversationHistory))

        // Mock successful API response
        whenever(
            groqApiService.sendChatMessage(
                authorization = eq("Bearer $testApiKey"),
                request = any<GroqChatRequest>()
            )
        ).thenReturn(Response.success(mockResponse))

        // When
        val result = chatRepository.sendMessage(testMessage).first()

        // Then
        assertTrue(result.isSuccess)

        // Verify API was called with conversation history
        verify(groqApiService).sendChatMessage(
            authorization = eq("Bearer $testApiKey"),
            request = argThat { request ->
                // Should include system message + history + current message
                request.messages.size >= 4 && // system + 2 history + current
                request.messages.any { it.role == "system" } &&
                request.messages.any { it.content == "Hello" && it.role == "user" } &&
                request.messages.any { it.content == "Hi there! How can I help you?" && it.role == "assistant" } &&
                request.messages.any { it.content == testMessage && it.role == "user" }
            }
        )
    }

    @Test
    fun `getChatHistory should return messages from database`() = runTest {
        // Given
        val mockEntities = listOf(
            ChatMessageEntity(
                id = "1",
                content = "Hello",
                isFromUser = true,
                timestamp = System.currentTimeMillis(),
                sessionId = "default_session",
                status = MessageStatus.SENT.name
            ),
            ChatMessageEntity(
                id = "2",
                content = "Hi there!",
                isFromUser = false,
                timestamp = System.currentTimeMillis(),
                sessionId = "default_session",
                status = MessageStatus.RECEIVED.name
            )
        )

        whenever(chatDao.getMessagesBySession("default_session"))
            .thenReturn(flowOf(mockEntities))

        // When
        val result = chatRepository.getChatHistory().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Hello", result[0].content)
        assertTrue(result[0].isFromUser)
        assertEquals("Hi there!", result[1].content)
        assertFalse(result[1].isFromUser)
    }

    @Test
    fun `saveChatHistory should save messages to database`() = runTest {
        // Given
        val messages = listOf(
            ChatMessage(
                id = "1",
                content = "Test message",
                isFromUser = true,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENT
            )
        )

        // When
        chatRepository.saveChatHistory(messages)

        // Then
        verify(chatDao).insertMessages(argThat { entities ->
            entities.size == 1 &&
            entities[0].content == "Test message" &&
            entities[0].isFromUser &&
            entities[0].sessionId == "default_session"
        })
    }

    @Test
    fun `clearChatHistory should clear session messages`() = runTest {
        // When
        chatRepository.clearChatHistory()

        // Then
        verify(chatDao).clearSessionMessages("default_session")
    }

    @Test
    fun `isApiConfigured should return true when API key is valid`() = runTest {
        // Given
        val validApiKey = "some_openrouter_key"
        whenever(apiKeyProvider.getApiKey()).thenReturn(validApiKey)

        // When
        val result = chatRepository.isApiConfigured()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isApiConfigured should return false when API key is invalid`() = runTest {
        // Given
        val invalidApiKey = ""
        whenever(apiKeyProvider.getApiKey()).thenReturn(invalidApiKey)

        // When
        val result = chatRepository.isApiConfigured()

        // Then
        assertFalse(result)
    }

    @Test
    fun `testApiConnection should return success when API responds`() = runTest {
        // Given
        val testApiKey = "test_openrouter_key"
        val mockResponse = GroqChatResponse(
            choices = listOf(
                GroqChoice(
                    message = GroqMessage.createAssistantMessage("Test response"),
                    finishReason = "stop"
                )
            ),
            usage = GroqUsage(5, 5, 10)
        )

        whenever(apiKeyProvider.getApiKey()).thenReturn(testApiKey)
        whenever(
            groqApiService.sendChatMessage(
                authorization = eq("Bearer $testApiKey"),
                request = any<GroqChatRequest>()
            )
        ).thenReturn(Response.success(mockResponse))

        // When
        val result = chatRepository.testApiConnection()

        // Then
        assertTrue(result.isSuccess())
        assertEquals("Test response", result.getDataOrNull())
    }

    @Test
    fun `testApiConnection should return error when API key is not configured`() = runTest {
        // Given
        whenever(apiKeyProvider.getApiKey()).thenReturn(null)

        // When
        val result = chatRepository.testApiConnection()

        // Then
        assertTrue(result.isError())
        val error = result.getErrorOrNull()
        assertTrue(error is ApiException.ApiKeyException)
        assertEquals("API key not configured", error?.message)
    }

    @Test
    fun `updateMessageStatus should update message in database`() = runTest {
        // Given
        val messageId = "test-message-id"
        val newStatus = MessageStatus.READ

        // When
        chatRepository.updateMessageStatus(messageId, newStatus)

        // Then
        verify(chatDao).updateMessageStatus(messageId, newStatus.name)
    }

    @Test
    fun `getMessagesByType should return filtered messages`() = runTest {
        // Given
        val userMessages = listOf(
            ChatMessageEntity(
                id = "1",
                content = "User message",
                isFromUser = true,
                timestamp = System.currentTimeMillis(),
                sessionId = "default_session",
                status = MessageStatus.SENT.name
            )
        )

        whenever(chatDao.getMessagesByType(true)).thenReturn(flowOf(userMessages))

        // When
        val result = chatRepository.getMessagesByType(true).first()

        // Then
        assertEquals(1, result.size)
        assertEquals("User message", result[0].content)
        assertTrue(result[0].isFromUser)
    }
}