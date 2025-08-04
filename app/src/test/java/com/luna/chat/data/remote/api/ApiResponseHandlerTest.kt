package com.luna.chat.data.remote.api

import com.luna.chat.data.remote.dto.GroqChatResponse
import com.luna.chat.data.remote.dto.GroqChoice
import com.luna.chat.data.remote.dto.GroqMessage
import com.luna.chat.data.remote.dto.GroqUsage
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiResponseHandlerTest {

    @Test
    fun `handleResponse should return success for successful response`() {
        // Given
        val successfulResponse = GroqChatResponse(
            choices = listOf(
                GroqChoice(
                    message = GroqMessage.createAssistantMessage("Hello!"),
                    finishReason = "stop"
                )
            ),
            usage = GroqUsage(10, 5, 15)
        )
        val response = Response.success(successfulResponse)

        // When
        val result = ApiResponseHandler.handleResponse(response)

        // Then
        assertTrue(result.isSuccess())
        assertEquals("Hello!", result.getDataOrNull())
        assertNull(result.getErrorOrNull())
    }

    @Test
    fun `handleResponse should handle unauthorized error`() {
        // Given
        val errorBody = ResponseBody.create(null, """{"error": {"message": "Invalid API key"}}""")
        val response = Response.error<GroqChatResponse>(HttpURLConnection.HTTP_UNAUTHORIZED, errorBody)

        // When
        val result = ApiResponseHandler.handleResponse(response)

        // Then
        assertTrue(result.isError())
        val error = result.getErrorOrNull()
        assertTrue(error is ApiException.ApiKeyException)
        assertEquals("Invalid API key - please check your Groq API key", error?.message)
    }

    @Test
    fun `handleResponse should handle rate limit error`() {
        // Given
        val errorBody = ResponseBody.create(null, """{"error": {"message": "Rate limit exceeded"}}""")
        val response = Response.error<GroqChatResponse>(429, errorBody)

        // When
        val result = ApiResponseHandler.handleResponse(response)

        // Then
        assertTrue(result.isError())
        val error = result.getErrorOrNull()
        assertTrue(error is ApiException.RateLimitException)
        assertEquals("Too many requests - please wait a moment before trying again", error?.message)
    }

    @Test
    fun `handleResponse should handle server error`() {
        // Given
        val errorBody = ResponseBody.create(null, """{"error": {"message": "Internal server error"}}""")
        val response = Response.error<GroqChatResponse>(HttpURLConnection.HTTP_INTERNAL_ERROR, errorBody)

        // When
        val result = ApiResponseHandler.handleResponse(response)

        // Then
        assertTrue(result.isError())
        val error = result.getErrorOrNull()
        assertTrue(error is ApiException.ServerException)
        assertEquals("Server error - please try again later", error?.message)
    }

    @Test
    fun `handleResponse should handle bad request error`() {
        // Given
        val errorBody = ResponseBody.create(null, """{"error": {"message": "Invalid request format"}}""")
        val response = Response.error<GroqChatResponse>(HttpURLConnection.HTTP_BAD_REQUEST, errorBody)

        // When
        val result = ApiResponseHandler.handleResponse(response)

        // Then
        assertTrue(result.isError())
        val error = result.getErrorOrNull()
        assertTrue(error is ApiException.RequestException)
        assertEquals("Invalid request - please check your message format", error?.message)
    }

    @Test
    fun `handleResponse should handle unknown HTTP error`() {
        // Given
        val errorBody = ResponseBody.create(null, """{"error": {"message": "Unknown error"}}""")
        val response = Response.error<GroqChatResponse>(418, errorBody) // I'm a teapot

        // When
        val result = ApiResponseHandler.handleResponse(response)

        // Then
        assertTrue(result.isError())
        val error = result.getErrorOrNull()
        assertTrue(error is ApiException.UnknownException)
        assertTrue(error?.message?.contains("Unexpected error") == true)
    }

    @Test
    fun `handleResponse should handle malformed error response`() {
        // Given
        val errorBody = ResponseBody.create(null, "Invalid JSON")
        val response = Response.error<GroqChatResponse>(HttpURLConnection.HTTP_BAD_REQUEST, errorBody)

        // When
        val result = ApiResponseHandler.handleResponse(response)

        // Then
        assertTrue(result.isError())
        val error = result.getErrorOrNull()
        assertTrue(error is ApiException.RequestException)
        assertEquals("Invalid request - please check your message format", error?.message)
    }

    @Test
    fun `handleResponse should handle empty response body`() {
        // Given
        val successResponse = Response.success<GroqChatResponse>(null)

        // When
        val result = ApiResponseHandler.handleResponse(successResponse)

        // Then
        assertTrue(result.isError())
        val error = result.getErrorOrNull()
        assertTrue(error is ApiException.UnknownException)
        assertEquals("Empty response from server", error?.message)
    }

    @Test
    fun `handleResponse should handle content filtered response`() {
        // Given
        val filteredResponse = GroqChatResponse(
            choices = listOf(
                GroqChoice(
                    message = GroqMessage.createAssistantMessage(""),
                    finishReason = "content_filter"
                )
            ),
            usage = GroqUsage(10, 0, 10)
        )
        val response = Response.success(filteredResponse)

        // When
        val result = ApiResponseHandler.handleResponse(response)

        // Then
        assertTrue(result.isError())
        val error = result.getErrorOrNull()
        assertTrue(error is ApiException.ContentFilterException)
        assertEquals("Content was filtered - let's try a different topic", error?.message)
    }

    @Test
    fun `handleException should convert network exceptions`() {
        // Test UnknownHostException
        val unknownHostException = UnknownHostException("Unable to resolve host")
        val networkResult = ApiResponseHandler.handleException(unknownHostException)
        
        assertTrue(networkResult.isError())
        val networkError = networkResult.getErrorOrNull()
        assertTrue(networkError is ApiException.NetworkException)
        assertEquals("No internet connection - please check your network", networkError?.message)

        // Test SocketTimeoutException
        val timeoutException = SocketTimeoutException("timeout")
        val timeoutResult = ApiResponseHandler.handleException(timeoutException)
        
        assertTrue(timeoutResult.isError())
        val timeoutError = timeoutResult.getErrorOrNull()
        assertTrue(timeoutError is ApiException.NetworkException)
        assertEquals("Request timed out - please try again", timeoutError?.message)
    }

    @Test
    fun `handleException should convert unknown exceptions`() {
        // Given
        val unknownException = RuntimeException("Something went wrong")

        // When
        val result = ApiResponseHandler.handleException(unknownException)

        // Then
        assertTrue(result.isError())
        val error = result.getErrorOrNull()
        assertTrue(error is ApiException.UnknownException)
        assertTrue(error?.message?.contains("Something went wrong") == true)
    }

    @Test
    fun `extractAssistantMessage should get message content`() {
        // Given
        val response = GroqChatResponse(
            choices = listOf(
                GroqChoice(
                    message = GroqMessage.createAssistantMessage("Hello there!"),
                    finishReason = "stop"
                )
            ),
            usage = GroqUsage(5, 10, 15)
        )

        // When
        val message = ApiResponseHandler.extractAssistantMessage(response)

        // Then
        assertEquals("Hello there!", message)
    }

    @Test
    fun `extractAssistantMessage should handle empty choices`() {
        // Given
        val response = GroqChatResponse(
            choices = emptyList(),
            usage = GroqUsage(5, 0, 5)
        )

        // When
        val message = ApiResponseHandler.extractAssistantMessage(response)

        // Then
        assertEquals("", message)
    }

    @Test
    fun `extractAssistantMessage should handle null message content`() {
        // Given
        val response = GroqChatResponse(
            choices = listOf(
                GroqChoice(
                    message = GroqMessage("assistant", null),
                    finishReason = "stop"
                )
            ),
            usage = GroqUsage(5, 0, 5)
        )

        // When
        val message = ApiResponseHandler.extractAssistantMessage(response)

        // Then
        assertEquals("", message)
    }

    @Test
    fun `isSuccessfulResponse should identify successful responses`() {
        // Successful response
        val successResponse = GroqChatResponse(
            choices = listOf(
                GroqChoice(
                    message = GroqMessage.createAssistantMessage("Success!"),
                    finishReason = "stop"
                )
            ),
            usage = GroqUsage(5, 10, 15)
        )
        assertTrue(ApiResponseHandler.isSuccessfulResponse(successResponse))

        // Content filtered response
        val filteredResponse = GroqChatResponse(
            choices = listOf(
                GroqChoice(
                    message = GroqMessage.createAssistantMessage(""),
                    finishReason = "content_filter"
                )
            ),
            usage = GroqUsage(5, 0, 5)
        )
        assertFalse(ApiResponseHandler.isSuccessfulResponse(filteredResponse))

        // Empty choices
        val emptyResponse = GroqChatResponse(
            choices = emptyList(),
            usage = GroqUsage(5, 0, 5)
        )
        assertFalse(ApiResponseHandler.isSuccessfulResponse(emptyResponse))
    }

    @Test
    fun `getChildFriendlyErrorMessage should return appropriate messages`() {
        // Network error
        val networkException = ApiException.NetworkException("Network error")
        assertEquals("Oops! Check your internet connection and try again 🌐", 
            ApiResponseHandler.getChildFriendlyErrorMessage(networkException))

        // API key error
        val apiKeyException = ApiException.ApiKeyException("Invalid API key")
        assertEquals("Settings need updating. Ask a grown-up for help! 🔧", 
            ApiResponseHandler.getChildFriendlyErrorMessage(apiKeyException))

        // Rate limit error
        val rateLimitException = ApiException.RateLimitException("Rate limit")
        assertEquals("The AI is taking a short break. Try again in a moment! ⏰", 
            ApiResponseHandler.getChildFriendlyErrorMessage(rateLimitException))

        // Content filter error
        val contentFilterException = ApiException.ContentFilterException("Filtered")
        assertEquals("Let's talk about something else! What would you like to learn today? 📚", 
            ApiResponseHandler.getChildFriendlyErrorMessage(contentFilterException))

        // Server error
        val serverException = ApiException.ServerException("Server error")
        assertEquals("The AI is having a hiccup. Let's try that again! 🤖", 
            ApiResponseHandler.getChildFriendlyErrorMessage(serverException))

        // Unknown error
        val unknownException = ApiException.UnknownException("Unknown")
        assertEquals("Something unexpected happened. Let's try again! 🔄", 
            ApiResponseHandler.getChildFriendlyErrorMessage(unknownException))
    }

    @Test
    fun `ApiResult should work correctly`() {
        // Success result
        val successResult = ApiResult.success("test data")
        assertTrue(successResult.isSuccess())
        assertFalse(successResult.isError())
        assertEquals("test data", successResult.getDataOrNull())
        assertNull(successResult.getErrorOrNull())

        // Error result
        val exception = ApiException.NetworkException("Network error")
        val errorResult = ApiResult.error(exception)
        assertFalse(errorResult.isSuccess())
        assertTrue(errorResult.isError())
        assertNull(errorResult.getDataOrNull())
        assertEquals(exception, errorResult.getErrorOrNull())
    }
}