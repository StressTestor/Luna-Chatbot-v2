package com.luna.chat.data.remote.api

import com.luna.chat.data.remote.dto.GroqChatRequest
import com.luna.chat.data.remote.dto.GroqChatResponse
import com.luna.chat.data.remote.dto.GroqChoice
import com.luna.chat.data.remote.dto.GroqMessage
import com.luna.chat.data.remote.dto.GroqUsage
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

class GroqApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: GroqApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(GroqApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `sendChatMessage should return successful response`() = runTest {
        // Given
        val mockResponse = """
            {
                "choices": [
                    {
                        "message": {
                            "role": "assistant",
                            "content": "Hello! How can I help you today?"
                        },
                        "finish_reason": "stop",
                        "index": 0
                    }
                ],
                "usage": {
                    "prompt_tokens": 10,
                    "completion_tokens": 15,
                    "total_tokens": 25
                },
                "id": "test-id",
                "object": "chat.completion",
                "created": 1234567890,
                "model": "mixtral-8x7b-32768"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )

        val request = GroqChatRequest.create(
            messages = listOf(
                GroqMessage.createUserMessage("Hello, AI!")
            )
        )

        // When
        val response = apiService.sendChatMessage(
            authorization = "Bearer test-api-key",
            request = request
        )

        // Then
        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        
        val body = response.body()!!
        assertEquals(1, body.choices.size)
        assertEquals("Hello! How can I help you today?", body.choices[0].message.content)
        assertEquals("assistant", body.choices[0].message.role)
        assertEquals("stop", body.choices[0].finishReason)
        assertEquals(25, body.usage.totalTokens)

        // Verify request
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        // OpenRouter path under mock server base is /v1/chat/completions
        assertEquals("/v1/chat/completions", recordedRequest.path)
        assertEquals("Bearer test-api-key", recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun `sendChatMessage should handle unauthorized error`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setBody("""{"error": {"message": "Invalid API key"}}""")
        )

        val request = GroqChatRequest.create(
            messages = listOf(
                GroqMessage.createUserMessage("Hello, AI!")
            )
        )

        // When
        val response = apiService.sendChatMessage(
            authorization = "Bearer invalid-key",
            request = request
        )

        // Then
        assertFalse(response.isSuccessful)
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.code())
    }

    @Test
    fun `sendChatMessage should handle rate limit error`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setBody("""{"error": {"message": "Rate limit exceeded"}}""")
                .addHeader("Retry-After", "60")
        )

        val request = GroqChatRequest.create(
            messages = listOf(
                GroqMessage.createUserMessage("Hello, AI!")
            )
        )

        // When
        val response = apiService.sendChatMessage(
            authorization = "Bearer test-api-key",
            request = request
        )

        // Then
        assertFalse(response.isSuccessful)
        assertEquals(429, response.code())
        assertEquals("60", response.headers()["Retry-After"])
    }

    @Test
    fun `sendChatMessage should handle server error`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody("""{"error": {"message": "Internal server error"}}""")
        )

        val request = GroqChatRequest.create(
            messages = listOf(
                GroqMessage.createUserMessage("Hello, AI!")
            )
        )

        // When
        val response = apiService.sendChatMessage(
            authorization = "Bearer test-api-key",
            request = request
        )

        // Then
        assertFalse(response.isSuccessful)
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.code())
    }

    @Test
    fun `sendChatMessageWithHeaders should include custom headers`() = runTest {
        // Given
        val mockResponse = """
            {
                "choices": [
                    {
                        "message": {
                            "role": "assistant",
                            "content": "Response with custom headers"
                        },
                        "finish_reason": "stop"
                    }
                ],
                "usage": {
                    "prompt_tokens": 5,
                    "completion_tokens": 10,
                    "total_tokens": 15
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(mockResponse)
        )

        val request = GroqChatRequest.create(
            messages = listOf(
                GroqMessage.createUserMessage("Test message")
            )
        )

        // When
        val response = apiService.sendChatMessageWithHeaders(
            authorization = "Bearer test-api-key",
            contentType = "application/json; charset=utf-8",
            request = request
        )

        // Then
        assertTrue(response.isSuccessful)
        
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer test-api-key", recordedRequest.getHeader("Authorization"))
        assertEquals("application/json; charset=utf-8", recordedRequest.getHeader("Content-Type"))
    }

    @Test
    fun `formatAuthHeader should format API key correctly`() {
        // Given
        val apiKey = "test_openrouter_key_value"
        
        // When
        val authHeader = GroqApiService.formatAuthHeader(apiKey)
        
        // Then
        assertEquals("Bearer test_openrouter_key_value", authHeader)
    }

    @Test
    fun `isValidApiKeyFormat should validate API key format`() {
        // Accept non-blank tokens for OpenRouter (format varies)
        assertTrue(GroqApiService.isValidApiKeyFormat("some_openrouter_key"))
        assertFalse(GroqApiService.isValidApiKeyFormat(""))
    }

    @Test
    fun `sendChatMessage should handle content filtering`() = runTest {
        // Given
        val mockResponse = """
            {
                "choices": [
                    {
                        "message": {
                            "role": "assistant",
                            "content": ""
                        },
                        "finish_reason": "content_filter"
                    }
                ],
                "usage": {
                    "prompt_tokens": 10,
                    "completion_tokens": 0,
                    "total_tokens": 10
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(mockResponse)
        )

        val request = GroqChatRequest.create(
            messages = listOf(
                GroqMessage.createUserMessage("Inappropriate content")
            )
        )

        // When
        val response = apiService.sendChatMessage(
            authorization = "Bearer test-api-key",
            request = request
        )

        // Then
        assertTrue(response.isSuccessful)
        val body = response.body()!!
        assertEquals("content_filter", body.choices[0].finishReason)
        assertTrue(body.choices[0].wasContentFiltered())
    }

    @Test
    fun `sendChatMessage should handle multiple messages conversation`() = runTest {
        // Given
        val mockResponse = """
            {
                "choices": [
                    {
                        "message": {
                            "role": "assistant",
                            "content": "That's a great question about math!"
                        },
                        "finish_reason": "stop"
                    }
                ],
                "usage": {
                    "prompt_tokens": 25,
                    "completion_tokens": 12,
                    "total_tokens": 37
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(mockResponse)
        )

        val request = GroqChatRequest.create(
            messages = listOf(
                GroqMessage.createSystemMessage("You are a helpful AI assistant for children."),
                GroqMessage.createUserMessage("What is 2 + 2?"),
                GroqMessage.createAssistantMessage("2 + 2 equals 4!"),
                GroqMessage.createUserMessage("Can you explain how addition works?")
            )
        )

        // When
        val response = apiService.sendChatMessage(
            authorization = "Bearer test-api-key",
            request = request
        )

        // Then
        assertTrue(response.isSuccessful)
        val body = response.body()!!
        assertEquals("That's a great question about math!", body.getAssistantMessage())
        assertTrue(body.isComplete())
    }
}