package com.luna.chat.domain.integration

import com.luna.chat.domain.exception.ChatException
import com.luna.chat.domain.usecase.ContentFilterUseCase
import com.luna.chat.domain.util.ErrorHandler
import com.luna.chat.domain.util.RetryMechanism
import com.luna.chat.domain.util.ChatResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Integration tests for error handling and content filtering working together
 */
class ErrorHandlingIntegrationTest {
    
    private lateinit var contentFilterUseCase: ContentFilterUseCase
    
    @Before
    fun setUp() {
        contentFilterUseCase = ContentFilterUseCase()
    }
    
    @Test
    fun `complete message processing flow with appropriate content should succeed`() = runTest {
        // Given
        val userMessage = "What is 2 + 2?"
        
        // When - Simulate complete message processing flow
        val result = RetryMechanism.withRetry {
            // Step 1: Validate and filter user input
            val validatedInput = contentFilterUseCase.validateAndFilterInput(userMessage)
            
            // Step 2: Simulate API call (would normally call Groq API)
            val aiResponse = "2 + 2 equals 4! Math is fun! 😊"
            
            // Step 3: Filter AI response
            val safeResponse = contentFilterUseCase.getSafeResponseReplacement(aiResponse)
            
            ChatResult.Success(safeResponse)
        }
        
        // Then
        assertTrue(result is ChatResult.Success)
        assertEquals("2 + 2 equals 4! Math is fun! 😊", (result as ChatResult.Success).data)
    }
    
    @Test
    fun `message processing flow with inappropriate user input should throw ContentFilterException`() = runTest {
        // Given
        val inappropriateMessage = "I want to hurt someone"
        
        // When & Then
        try {
            RetryMechanism.withRetry {
                contentFilterUseCase.validateAndFilterInput(inappropriateMessage)
                "This should not be reached"
            }
            fail("Expected ContentFilterException to be thrown")
        } catch (e: ChatException.ContentFilterException) {
            assertEquals(ChatException.ContentFilterException.FilterType.INAPPROPRIATE_INPUT, e.filterType)
            assertEquals("Let's talk about something else! What would you like to learn today? 📚", 
                e.getChildFriendlyMessage())
            assertFalse(e.isRetryable()) // Content filter exceptions are not retryable
        }
    }
    
    @Test
    fun `message processing flow with inappropriate AI response should return safe replacement`() = runTest {
        // Given
        val userMessage = "Tell me about animals"
        val inappropriateAiResponse = "Here's how to hurt animals..."
        
        // When
        val result = RetryMechanism.withRetry {
            // Step 1: Validate user input (should pass)
            val validatedInput = contentFilterUseCase.validateAndFilterInput(userMessage)
            
            // Step 2: Simulate inappropriate AI response
            val safeResponse = contentFilterUseCase.getSafeResponseReplacement(inappropriateAiResponse)
            
            ChatResult.Success(safeResponse)
        }
        
        // Then
        assertTrue(result is ChatResult.Success)
        assertEquals("The AI said something silly. Let me try a better answer! 🤖", 
            (result as ChatResult.Success).data)
    }
    
    @Test
    fun `network error during message processing should retry with exponential backoff`() = runTest {
        // Given
        var attemptCount = 0
        val networkError = IOException("Network connection failed")
        
        // When
        try {
            RetryMechanism.withRetry { attemptNumber ->
                attemptCount++
                if (attemptCount < 3) {
                    throw networkError
                }
                "Success after retries"
            }
            fail("Expected NetworkException to be thrown after max retries")
        } catch (e: ChatException.NetworkException) {
            // Then
            assertEquals(3, attemptCount) // Should have retried 3 times
            assertEquals("Oops! Check your internet connection and try again 🌐", 
                e.getChildFriendlyMessage())
            assertTrue(e.isRetryable())
        }
    }
    
    @Test
    fun `API error with rate limiting should use longer retry delays`() = runTest {
        // Given
        val rateLimitException = ChatException.ApiException("rate_limit", "Rate limit exceeded")
        
        // When
        val delay0 = rateLimitException.getRetryDelayMs(0)
        val delay1 = rateLimitException.getRetryDelayMs(1)
        val delay2 = rateLimitException.getRetryDelayMs(2)
        
        // Then
        assertEquals(5000L, delay0) // 5 seconds
        assertEquals(10000L, delay1) // 10 seconds
        assertEquals(20000L, delay2) // 20 seconds
        assertEquals("The AI is taking a short break. Try again in a moment! ⏰", 
            rateLimitException.getChildFriendlyMessage())
    }
    
    @Test
    fun `configuration error should not retry and provide helpful message`() = runTest {
        // Given
        val configError = ChatException.ConfigurationException(
            ChatException.ConfigurationException.ConfigType.API_KEY_INVALID,
            "Invalid API key"
        )
        
        // When & Then
        try {
            RetryMechanism.withRetry {
                throw configError
            }
            fail("Expected ConfigurationException to be thrown")
        } catch (e: ChatException.ConfigurationException) {
            assertEquals("The app needs to be set up by a grown-up! Ask for help with settings 🔧", 
                e.getChildFriendlyMessage())
            assertFalse(e.isRetryable()) // Configuration errors should not retry
        }
    }
    
    @Test
    fun `ErrorHandler should provide consistent child-friendly messages`() {
        // Given
        val networkError = IOException("Connection timeout")
        val contentFilterError = ChatException.ContentFilterException(
            ChatException.ContentFilterException.FilterType.PERSONAL_INFO,
            "Personal info detected"
        )
        val unknownError = RuntimeException("Unexpected error")
        
        // When
        val networkMessage = ErrorHandler.getChildFriendlyMessage(networkError)
        val contentMessage = ErrorHandler.getChildFriendlyMessage(contentFilterError)
        val unknownMessage = ErrorHandler.getChildFriendlyMessage(unknownError)
        
        // Then
        assertEquals("Oops! Check your internet connection and try again 🌐", networkMessage)
        assertEquals("Let's keep our personal information private! What else can I help with? 🔒", contentMessage)
        assertEquals("Something unexpected happened. Let's try again! 🔄", unknownMessage)
    }
    
    @Test
    fun `ChatResult should handle success and error cases properly`() {
        // Given
        val successResult = ChatResult.Success("Hello!")
        val errorResult = ChatResult.Error(ChatException.NetworkException("Network error"))
        val loadingResult = ChatResult.Loading("Processing...")
        
        // When & Then - Test success result
        assertTrue(successResult.isSuccess())
        assertFalse(successResult.isError())
        assertFalse(successResult.isLoading())
        assertEquals("Hello!", successResult.getDataOrNull())
        assertNull(successResult.getExceptionOrNull())
        assertNull(successResult.getChildFriendlyErrorMessage())
        
        // Test error result
        assertFalse(errorResult.isSuccess())
        assertTrue(errorResult.isError())
        assertFalse(errorResult.isLoading())
        assertNull(errorResult.getDataOrNull())
        assertNotNull(errorResult.getExceptionOrNull())
        assertEquals("Oops! Check your internet connection and try again 🌐", 
            errorResult.getChildFriendlyErrorMessage())
        
        // Test loading result
        assertFalse(loadingResult.isSuccess())
        assertFalse(loadingResult.isError())
        assertTrue(loadingResult.isLoading())
        assertNull(loadingResult.getDataOrNull())
        assertNull(loadingResult.getExceptionOrNull())
        assertNull(loadingResult.getChildFriendlyErrorMessage())
    }
    
    @Test
    fun `ChatResult fold should handle all cases correctly`() {
        // Given
        val successResult = ChatResult.Success("data")
        val errorResult = ChatResult.Error(ChatException.NetworkException())
        val loadingResult = ChatResult.Loading("loading")
        
        var successCalled = false
        var errorCalled = false
        var loadingCalled = false
        
        // When - Test success case
        successResult.fold(
            onSuccess = { successCalled = true },
            onError = { errorCalled = true },
            onLoading = { loadingCalled = true }
        )
        
        // Then
        assertTrue(successCalled)
        assertFalse(errorCalled)
        assertFalse(loadingCalled)
        
        // Reset flags
        successCalled = false
        errorCalled = false
        loadingCalled = false
        
        // When - Test error case
        errorResult.fold(
            onSuccess = { successCalled = true },
            onError = { errorCalled = true },
            onLoading = { loadingCalled = true }
        )
        
        // Then
        assertFalse(successCalled)
        assertTrue(errorCalled)
        assertFalse(loadingCalled)
        
        // Reset flags
        successCalled = false
        errorCalled = false
        loadingCalled = false
        
        // When - Test loading case
        loadingResult.fold(
            onSuccess = { successCalled = true },
            onError = { errorCalled = true },
            onLoading = { loadingCalled = true }
        )
        
        // Then
        assertFalse(successCalled)
        assertFalse(errorCalled)
        assertTrue(loadingCalled)
    }
    
    @Test
    fun `complete error handling flow should provide appropriate user experience`() {
        // Test different error scenarios and their user-facing messages
        val testCases = listOf(
            // Network errors
            IOException("Connection failed") to "Oops! Check your internet connection and try again 🌐",
            
            // Content filter errors
            ChatException.ContentFilterException(
                ChatException.ContentFilterException.FilterType.INAPPROPRIATE_INPUT
            ) to "Let's talk about something else! What would you like to learn today? 📚",
            
            // Configuration errors
            ChatException.ConfigurationException(
                ChatException.ConfigurationException.ConfigType.API_KEY_MISSING
            ) to "The app needs to be set up by a grown-up! Ask for help with settings 🔧",
            
            // Validation errors
            ChatException.ValidationException(
                ChatException.ValidationException.ValidationType.MESSAGE_TOO_LONG
            ) to "That message is super long! Try breaking it into smaller parts 📝",
            
            // API errors
            ChatException.ApiException("rate_limit") to "The AI is taking a short break. Try again in a moment! ⏰"
        )
        
        testCases.forEach { (exception, expectedMessage) ->
            val actualMessage = ErrorHandler.getChildFriendlyMessage(exception)
            assertEquals("Error message for ${exception.javaClass.simpleName} should be child-friendly", 
                expectedMessage, actualMessage)
        }
    }
}