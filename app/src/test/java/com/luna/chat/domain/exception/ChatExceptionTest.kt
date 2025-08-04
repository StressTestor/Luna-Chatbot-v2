package com.luna.chat.domain.exception

import org.junit.Test
import org.junit.Assert.*
import java.net.UnknownHostException

class ChatExceptionTest {

    @Test
    fun `NetworkException should have correct properties`() {
        val exception = ChatException.NetworkException("Network error")
        
        assertEquals("Network error", exception.message)
        assertTrue(exception.isRetryable)
        assertEquals("NETWORK_ERROR", exception.errorCode)
        assertEquals(1000L, exception.retryDelayMs)
    }

    @Test
    fun `NetworkException with default message should work`() {
        val exception = ChatException.NetworkException()
        
        assertEquals("Network connection error", exception.message)
        assertTrue(exception.isRetryable)
    }

    @Test
    fun `ApiKeyException should have correct properties`() {
        val exception = ChatException.ApiKeyException("Invalid API key")
        
        assertEquals("Invalid API key", exception.message)
        assertFalse(exception.isRetryable)
        assertEquals("API_KEY_ERROR", exception.errorCode)
        assertEquals(0L, exception.retryDelayMs)
    }

    @Test
    fun `ApiKeyException with default message should work`() {
        val exception = ChatException.ApiKeyException()
        
        assertEquals("API key configuration error", exception.message)
        assertFalse(exception.isRetryable)
    }

    @Test
    fun `RateLimitException should have correct properties`() {
        val exception = ChatException.RateLimitException("Rate limit exceeded")
        
        assertEquals("Rate limit exceeded", exception.message)
        assertTrue(exception.isRetryable)
        assertEquals("RATE_LIMIT_ERROR", exception.errorCode)
        assertEquals(5000L, exception.retryDelayMs) // Longer delay for rate limits
    }

    @Test
    fun `RateLimitException with retry after should use custom delay`() {
        val exception = ChatException.RateLimitException("Rate limit", retryAfterSeconds = 30)
        
        assertEquals(30000L, exception.retryDelayMs) // 30 seconds in milliseconds
    }

    @Test
    fun `ContentFilterException should have correct properties`() {
        val filterType = ChatException.ContentFilterException.FilterType.INAPPROPRIATE_INPUT
        val exception = ChatException.ContentFilterException(filterType, "Inappropriate content")
        
        assertEquals("Inappropriate content", exception.message)
        assertFalse(exception.isRetryable)
        assertEquals("CONTENT_FILTER_ERROR", exception.errorCode)
        assertEquals(filterType, exception.filterType)
        assertEquals(0L, exception.retryDelayMs)
    }

    @Test
    fun `ContentFilterException should have child-friendly messages`() {
        val inappropriateInputException = ChatException.ContentFilterException(
            ChatException.ContentFilterException.FilterType.INAPPROPRIATE_INPUT,
            "Blocked content"
        )
        assertEquals("Let's talk about something else! What would you like to learn today? 📚", 
            inappropriateInputException.getChildFriendlyMessage())

        val personalInfoException = ChatException.ContentFilterException(
            ChatException.ContentFilterException.FilterType.PERSONAL_INFO,
            "Personal info detected"
        )
        assertEquals("Let's keep our personal information private! What else can I help with? 🔒", 
            personalInfoException.getChildFriendlyMessage())

        val inappropriateResponseException = ChatException.ContentFilterException(
            ChatException.ContentFilterException.FilterType.INAPPROPRIATE_RESPONSE,
            "AI response filtered"
        )
        assertEquals("I'd rather talk about something else! What's your favorite subject in school? 🎓", 
            inappropriateResponseException.getChildFriendlyMessage())
    }

    @Test
    fun `ServerException should have correct properties`() {
        val exception = ChatException.ServerException("Server error")
        
        assertEquals("Server error", exception.message)
        assertTrue(exception.isRetryable)
        assertEquals("SERVER_ERROR", exception.errorCode)
        assertEquals(2000L, exception.retryDelayMs)
    }

    @Test
    fun `ValidationException should have correct properties`() {
        val exception = ChatException.ValidationException("Invalid input")
        
        assertEquals("Invalid input", exception.message)
        assertFalse(exception.isRetryable)
        assertEquals("VALIDATION_ERROR", exception.errorCode)
        assertEquals(0L, exception.retryDelayMs)
    }

    @Test
    fun `UnknownException should have correct properties`() {
        val exception = ChatException.UnknownException("Unknown error")
        
        assertEquals("Unknown error", exception.message)
        assertTrue(exception.isRetryable)
        assertEquals("UNKNOWN_ERROR", exception.errorCode)
        assertEquals(1000L, exception.retryDelayMs)
    }

    @Test
    fun `fromThrowable should convert known exceptions correctly`() {
        // Network exceptions
        val unknownHostException = UnknownHostException("Host not found")
        val networkException = ChatException.fromThrowable(unknownHostException)
        assertTrue(networkException is ChatException.NetworkException)
        assertTrue(networkException.message?.contains("Host not found") == true)

        val socketTimeoutException = java.net.SocketTimeoutException("Timeout")
        val timeoutException = ChatException.fromThrowable(socketTimeoutException)
        assertTrue(timeoutException is ChatException.NetworkException)
        assertTrue(timeoutException.message?.contains("Timeout") == true)

        // IO exceptions
        val ioException = java.io.IOException("IO error")
        val convertedIoException = ChatException.fromThrowable(ioException)
        assertTrue(convertedIoException is ChatException.NetworkException)

        // Illegal argument exceptions
        val illegalArgException = IllegalArgumentException("Invalid argument")
        val validationException = ChatException.fromThrowable(illegalArgException)
        assertTrue(validationException is ChatException.ValidationException)
        assertTrue(validationException.message?.contains("Invalid argument") == true)

        // Unknown exceptions
        val runtimeException = RuntimeException("Runtime error")
        val unknownException = ChatException.fromThrowable(runtimeException)
        assertTrue(unknownException is ChatException.UnknownException)
        assertTrue(unknownException.message?.contains("Runtime error") == true)
    }

    @Test
    fun `fromThrowable should preserve ChatException instances`() {
        val originalException = ChatException.NetworkException("Original network error")
        val preservedException = ChatException.fromThrowable(originalException)
        
        assertSame(originalException, preservedException)
    }

    @Test
    fun `getChildFriendlyMessage should return appropriate messages for all types`() {
        val networkException = ChatException.NetworkException()
        assertEquals("Oops! Check your internet connection and try again 🌐", 
            networkException.getChildFriendlyMessage())

        val apiKeyException = ChatException.ApiKeyException()
        assertEquals("Settings need updating. Ask a grown-up for help! 🔧", 
            apiKeyException.getChildFriendlyMessage())

        val rateLimitException = ChatException.RateLimitException()
        assertEquals("The AI is taking a short break. Try again in a moment! ⏰", 
            rateLimitException.getChildFriendlyMessage())

        val serverException = ChatException.ServerException()
        assertEquals("The AI is having a hiccup. Let's try that again! 🤖", 
            serverException.getChildFriendlyMessage())

        val validationException = ChatException.ValidationException()
        assertEquals("Something doesn't look right. Let's try again! ✏️", 
            validationException.getChildFriendlyMessage())

        val unknownException = ChatException.UnknownException()
        assertEquals("Something unexpected happened. Let's try again! 🔄", 
            unknownException.getChildFriendlyMessage())
    }

    @Test
    fun `FilterType enum should have all expected values`() {
        val filterTypes = ChatException.ContentFilterException.FilterType.values()
        assertEquals(3, filterTypes.size)
        assertTrue(filterTypes.contains(ChatException.ContentFilterException.FilterType.INAPPROPRIATE_INPUT))
        assertTrue(filterTypes.contains(ChatException.ContentFilterException.FilterType.INAPPROPRIATE_RESPONSE))
        assertTrue(filterTypes.contains(ChatException.ContentFilterException.FilterType.PERSONAL_INFO))
    }

    @Test
    fun `exception hierarchy should be correct`() {
        val networkException = ChatException.NetworkException()
        assertTrue(networkException is ChatException)
        assertTrue(networkException is Exception)

        val contentFilterException = ChatException.ContentFilterException(
            ChatException.ContentFilterException.FilterType.INAPPROPRIATE_INPUT,
            "Test"
        )
        assertTrue(contentFilterException is ChatException)
        assertTrue(contentFilterException is Exception)
    }

    @Test
    fun `exception serialization should preserve important data`() {
        val originalException = ChatException.ContentFilterException(
            ChatException.ContentFilterException.FilterType.PERSONAL_INFO,
            "Personal info detected"
        )

        // Test that important properties are accessible
        assertEquals("Personal info detected", originalException.message)
        assertEquals(ChatException.ContentFilterException.FilterType.PERSONAL_INFO, originalException.filterType)
        assertEquals("CONTENT_FILTER_ERROR", originalException.errorCode)
        assertFalse(originalException.isRetryable)
    }

    @Test
    fun `exception with cause should preserve cause`() {
        val cause = RuntimeException("Root cause")
        val exception = ChatException.NetworkException("Network error", cause)
        
        assertEquals("Network error", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `retry delay calculation should be consistent`() {
        val networkException = ChatException.NetworkException()
        val rateLimitException = ChatException.RateLimitException()
        val serverException = ChatException.ServerException()
        
        // Network and unknown exceptions should have shorter delays
        assertEquals(1000L, networkException.retryDelayMs)
        
        // Rate limit exceptions should have longer delays
        assertEquals(5000L, rateLimitException.retryDelayMs)
        
        // Server exceptions should have medium delays
        assertEquals(2000L, serverException.retryDelayMs)
        
        // Non-retryable exceptions should have no delay
        val apiKeyException = ChatException.ApiKeyException()
        assertEquals(0L, apiKeyException.retryDelayMs)
    }
}