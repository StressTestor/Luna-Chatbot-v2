package com.luna.chat.data.repository

import com.luna.chat.data.local.dao.ChatDao
import com.luna.chat.data.remote.api.ApiResult
import com.luna.chat.data.remote.api.GroqApiService
import com.luna.chat.data.remote.dto.GroqChatResponse
import com.luna.chat.data.remote.dto.GroqChoice
import com.luna.chat.data.remote.dto.GroqMessage
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.security.ContentFilteringService
import com.luna.chat.security.SecurityConfig
import com.luna.chat.security.SecureLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ChatRepositoryContentFilteringTest {

    @Mock
    private lateinit var groqApiService: GroqApiService
    
    @Mock
    private lateinit var chatDao: ChatDao
    
    @Mock
    private lateinit var apiKeyProvider: ApiKeyProvider
    
    @Mock
    private lateinit var securityConfig: SecurityConfig
    
    @Mock
    private lateinit var secureLogger: SecureLogger
    
    private lateinit var contentFilteringService: ContentFilteringService
    private lateinit var chatRepository: ChatRepositoryImpl
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Set up content filtering service
        contentFilteringService = ContentFilteringService(securityConfig, secureLogger)
        
        // Set up chat repository
        chatRepository = ChatRepositoryImpl(
            groqApiService,
            chatDao,
            apiKeyProvider,
            contentFilteringService
        )
        
        // Default mock behavior
        whenever(apiKeyProvider.getApiKey()).thenReturn("test-api-key")
        whenever(securityConfig.areParentalControlsEnabled()).thenReturn(true)
        whenever(securityConfig.getTemperatureParameter()).thenReturn(0.5f)
        whenever(securityConfig.getTopPParameter()).thenReturn(0.7f)
        whenever(chatDao.getRecentMessages(any())).thenReturn(flowOf(emptyList()))
        
        // Mock API response
        val mockResponse = GroqChatResponse(
            choices = listOf(
                GroqChoice(
                    message = GroqMessage(
                        role = "assistant",
                        content = "This is a test response"
                    ),
                    finishReason = "stop"
                )
            ),
            id = "test-id",
            created = 123456789,
            model = "test-model",
            usage = null
        )
        
        whenever(groqApiService.sendChatMessage(any(), any())).thenReturn(mockResponse)
    }
    
    @Test
    fun `sendMessage should filter inappropriate user input`() = runTest {
        // Given
        val inappropriateMessage = "Tell me about guns and violence"
        
        // When
        val result = chatRepository.sendMessage(inappropriateMessage).first()
        
        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertTrue(response?.contains("I'm sorry, but I can't respond to that") == true)
    }
    
    @Test
    fun `sendMessage should filter inappropriate AI response`() = runTest {
        // Given
        val userMessage = "Tell me a story"
        
        // Mock an inappropriate AI response
        val inappropriateResponse = GroqChatResponse(
            choices = listOf(
                GroqChoice(
                    message = GroqMessage(
                        role = "assistant",
                        content = "Once upon a time, there was a violent battle with guns and weapons"
                    ),
                    finishReason = "stop"
                )
            ),
            id = "test-id",
            created = 123456789,
            model = "test-model",
            usage = null
        )
        
        whenever(groqApiService.sendChatMessage(any(), any())).thenReturn(inappropriateResponse)
        
        // When
        val result = chatRepository.sendMessage(userMessage).first()
        
        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertTrue(response?.contains("I'm sorry, but I can't provide that information") == true)
    }
    
    @Test
    fun `sendMessage should use security parameters for API request`() = runTest {
        // Given
        val userMessage = "Tell me about science"
        val expectedTemperature = 0.5f
        
        // Set up mock to capture the request
        var capturedTemperature: Double? = null
        whenever(groqApiService.sendChatMessage(any(), any())).thenAnswer { invocation ->
            val request = invocation.arguments[1] as com.luna.chat.data.remote.dto.GroqChatRequest
            capturedTemperature = request.temperature
            
            GroqChatResponse(
                choices = listOf(
                    GroqChoice(
                        message = GroqMessage(
                            role = "assistant",
                            content = "Science is fascinating!"
                        ),
                        finishReason = "stop"
                    )
                ),
                id = "test-id",
                created = 123456789,
                model = "test-model",
                usage = null
            )
        }
        
        // When
        chatRepository.sendMessage(userMessage).first()
        
        // Then
        assertEquals(expectedTemperature.toDouble(), capturedTemperature)
    }
    
    @Test
    fun `sendMessage should allow educational content`() = runTest {
        // Given
        val educationalMessage = "Tell me about the solar system"
        
        // When
        val result = chatRepository.sendMessage(educationalMessage).first()
        
        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertEquals("This is a test response", response)
    }
    
    @Test
    fun `sendMessage should respect parental controls setting`() = runTest {
        // Given
        val inappropriateMessage = "Tell me about guns and violence"
        
        // Disable parental controls
        whenever(securityConfig.areParentalControlsEnabled()).thenReturn(false)
        
        // When
        val result = chatRepository.sendMessage(inappropriateMessage).first()
        
        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertEquals("This is a test response", response) // Should not be filtered
    }
}