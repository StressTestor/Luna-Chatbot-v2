package com.luna.chat.data.remote.api

import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiExceptionTest {

    @Test
    fun `NetworkException should provide child-friendly message`() {
        // Given
        val exception = ApiException.NetworkException("Connection failed")

        // When
        val friendlyMessage = exception.getChildFriendlyMessage()

        // Then
        assertEquals("Oops! Check your internet connection and try again 🌐", friendlyMessage)
    }

    @Test
    fun `HttpException should provide appropriate child-friendly messages`() {
        // Test unauthorized (401)
        val unauthorizedException = ApiException.HttpException(401, "Unauthorized")
        assertEquals("Settings need updating. Ask a grown-up for help! 🔧", 
                    unauthorizedException.getChildFriendlyMessage())
        assertTrue(unauthorizedException.isUnauthorized())

        // Test rate limit (429)
        val rateLimitException = ApiException.HttpException(429, "Rate limited")
        assertEquals("The AI is taking a short break. Try again in a moment! ⏰", 
                    rateLimitException.getChildFriendlyMessage())
        assertTrue(rateLimitException.isRateLimited())

        // Test server error (500)
        val serverException = ApiException.HttpException(500, "Server error")
        assertEquals("The AI is having a hiccup. Let's try that again! 🤖", 
                    serverException.getChildFriendlyMessage())
        assertTrue(serverException.isServerError())

        // Test client error (400)
        val clientException = ApiException.HttpException(400, "Bad request")
        assertEquals("Something went wrong. Let's try again! 🔄", 
                    clientException.getChildFriendlyMessage())
        assertTrue(clientException.isClientError())
    }

    @Test
    fun `ApiKeyException should provide child-friendly message`() {
        // Given
        val exception = ApiException.ApiKeyException("Invalid API key")

        // When
        val friendlyMessage = exception.getChildFriendlyMessage()

        // Then
        assertEquals("Settings need updating. Ask a grown-up for help! 🔧", friendlyMessage)
    }

    @Test
    fun `RateLimitException should provide child-friendly message with retry time`() {
        // Given
        val exceptionWithRetry = ApiException.RateLimitException(
            message = "Rate limited", 
            retryAfterSeconds = 30
        )
        val exceptionWithoutRetry = ApiException.RateLimitException("Rate limited")

        // When
        val messageWithRetry = exceptionWithRetry.getChildFriendlyMessage()
        val messageWithoutRetry = exceptionWithoutRetry.getChildFriendlyMessage()

        // Then
        assertEquals("The AI is taking a 30s break. Try again soon! ⏰", messageWithRetry)
        assertEquals("The AI is taking a short break. Try again in a moment! ⏰", messageWithoutRetry)
    }

    @Test
    fun `ContentFilterException should provide child-friendly message`() {
        // Given
        val exception = ApiException.ContentFilterException("Content filtered")

        // When
        val friendlyMessage = exception.getChildFriendlyMessage()

        // Then
        assertEquals("Let's talk about something else! What would you like to learn today? 📚", 
                    friendlyMessage)
    }

    @Test
    fun `ParseException should provide child-friendly message`() {
        // Given
        val exception = ApiException.ParseException("JSON parse error")

        // When
        val friendlyMessage = exception.getChildFriendlyMessage()

        // Then
        assertEquals("The AI sent a confusing message. Let's try again! 🤖", friendlyMessage)
    }

    @Test
    fun `TimeoutException should provide child-friendly message`() {
        // Given
        val exception = ApiException.TimeoutException("Request timeout")

        // When
        val friendlyMessage = exception.getChildFriendlyMessage()

        // Then
        assertEquals("The AI is thinking really hard! Let's try again! 🤔", friendlyMessage)
    }

    @Test
    fun `UnknownException should provide child-friendly message`() {
        // Given
        val exception = ApiException.UnknownException("Unknown error")

        // When
        val friendlyMessage = exception.getChildFriendlyMessage()

        // Then
        assertEquals("Something unexpected happened. Let's try again! 🔄", friendlyMessage)
    }

    @Test
    fun `fromHttpCode should create appropriate exceptions`() {
        // Test unauthorized
        val unauthorizedException = ApiException.fromHttpCode(401, "Unauthorized")
        assertTrue(unauthorizedException is ApiException.ApiKeyException)

        // Test rate limit
        val rateLimitException = ApiException.fromHttpCode(429, "Rate limited")
        assertTrue(rateLimitException is ApiException.RateLimitException)

        // Test other HTTP codes
        val httpException = ApiException.fromHttpCode(500, "Server error")
        assertTrue(httpException is ApiException.HttpException)
        assertEquals(500, (httpException as ApiException.HttpException).code)
    }

    @Test
    fun `fromThrowable should create appropriate exceptions`() {
        // Test IOException
        val ioException = IOException("Network error")
        val networkException = ApiException.fromThrowable(ioException)
        assertTrue(networkException is ApiException.NetworkException)
        assertEquals(ioException, networkException.cause)

        // Test SocketTimeoutException
        val timeoutException = SocketTimeoutException("Timeout")
        val apiTimeoutException = ApiException.fromThrowable(timeoutException)
        assertTrue(apiTimeoutException is ApiException.TimeoutException)
        assertEquals(timeoutException, apiTimeoutException.cause)

        // Test UnknownHostException
        val hostException = UnknownHostException("Unknown host")
        val apiNetworkException = ApiException.fromThrowable(hostException)
        assertTrue(apiNetworkException is ApiException.NetworkException)
        assertEquals(hostException, apiNetworkException.cause)

        // Test existing ApiException
        val existingException = ApiException.ContentFilterException("Filtered")
        val sameException = ApiException.fromThrowable(existingException)
        assertEquals(existingException, sameException)

        // Test generic exception
        val genericException = RuntimeException("Generic error")
        val unknownException = ApiException.fromThrowable(genericException)
        assertTrue(unknownException is ApiException.UnknownException)
        assertEquals(genericException, unknownException.cause)
    }

    @Test
    fun `HttpException helper methods should work correctly`() {
        // Given
        val unauthorizedException = ApiException.HttpException(401, "Unauthorized")
        val rateLimitException = ApiException.HttpException(429, "Rate limited")
        val serverException = ApiException.HttpException(500, "Server error")
        val clientException = ApiException.HttpException(400, "Bad request")

        // Test isUnauthorized
        assertTrue(unauthorizedException.isUnauthorized())
        assertFalse(rateLimitException.isUnauthorized())

        // Test isRateLimited
        assertTrue(rateLimitException.isRateLimited())
        assertFalse(unauthorizedException.isRateLimited())

        // Test isServerError
        assertTrue(serverException.isServerError())
        assertFalse(clientException.isServerError())

        // Test isClientError
        assertTrue(clientException.isClientError())
        assertTrue(unauthorizedException.isClientError())
        assertFalse(serverException.isClientError())
    }

    @Test
    fun `exception messages should be preserved`() {
        // Given
        val customMessage = "Custom error message"
        val cause = RuntimeException("Root cause")

        // When
        val exception = ApiException.NetworkException(customMessage, cause)

        // Then
        assertEquals(customMessage, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `default messages should be used when not provided`() {
        // Test default messages
        assertEquals("Network connection failed", ApiException.NetworkException().message)
        assertEquals("Invalid API key", ApiException.ApiKeyException().message)
        assertEquals("Rate limit exceeded", ApiException.RateLimitException().message)
        assertEquals("Content was filtered", ApiException.ContentFilterException().message)
        assertEquals("Failed to parse response", ApiException.ParseException().message)
        assertEquals("Request timed out", ApiException.TimeoutException().message)
        assertEquals("Unknown error occurred", ApiException.UnknownException().message)
    }
}