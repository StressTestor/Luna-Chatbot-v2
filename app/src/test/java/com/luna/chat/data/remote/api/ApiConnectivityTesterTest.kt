package com.luna.chat.data.remote.api

import com.luna.chat.data.remote.dto.GroqChatRequest
import com.luna.chat.data.remote.dto.GroqChatResponse
import com.luna.chat.data.remote.dto.GroqChoice
import com.luna.chat.data.remote.dto.GroqMessage
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApiConnectivityTesterTest {

    @Mock
    private lateinit var groqApiService: GroqApiService

    @Mock
    private lateinit var apiResponseHandler: ApiResponseHandler

    private lateinit var apiConnectivityTester: ApiConnectivityTester

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        apiConnectivityTester = ApiConnectivityTester(groqApiService, apiResponseHandler)
    }

    @Test
    fun `testApiConnectivity returns success when API call succeeds`() = runTest {
        // Given
        val apiKey = "gsk_test_api_key_1234567890"
        val mockResponse = GroqChatResponse(
            choices = listOf(
                GroqChoice(
                    message = GroqMessage(role = "assistant", content = "Test response"),
                    index = 0,
                    finishReason = "stop"
                )
            ),
            model = "mixtral-8x7b-32768"
        )
        val mockRetrofitResponse = Response.success(mockResponse)

        whenever(groqApiService.sendChatMessage(any(), any())).thenReturn(mockRetrofitResponse)
        whenever(apiResponseHandler.handleResponse(any(), any<(GroqChatResponse) -> ApiConnectivityResult>()))
            .thenReturn(ApiConnectivityResult.Success(
                message = "API connection successful! ✅",
                responseTime = System.currentTimeMillis(),
                model = "mixtral-8x7b-32768"
            ))

        // When
        val result = apiConnectivityTester.testApiConnectivity(apiKey)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("API connection successful! ✅", result.message)
        verify(groqApiService).sendChatMessage(
            authorization = "Bearer $apiKey",
            request = any()
        )
    }

    @Test
    fun `testApiConnectivity returns InvalidApiKey when unauthorized`() = runTest {
        // Given
        val apiKey = "gsk_invalid_key"
        val exception = ApiException.UnauthorizedException("Invalid API key")

        whenever(groqApiService.sendChatMessage(any(), any())).thenThrow(exception)

        // When
        val result = apiConnectivityTester.testApiConnectivity(apiKey)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result is ApiConnectivityResult.InvalidApiKey)
        assertTrue(result.message.contains("API key is invalid"))
    }

    @Test
    fun `testApiConnectivity returns NetworkError when network fails`() = runTest {
        // Given
        val apiKey = "gsk_test_api_key_1234567890"
        val exception = ApiException.NetworkException("Network error")

        whenever(groqApiService.sendChatMessage(any(), any())).thenThrow(exception)

        // When
        val result = apiConnectivityTester.testApiConnectivity(apiKey)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result is ApiConnectivityResult.NetworkError)
        assertTrue(result.message.contains("Network connection failed"))
    }

    @Test
    fun `testApiConnectivity returns RateLimited when rate limit exceeded`() = runTest {
        // Given
        val apiKey = "gsk_test_api_key_1234567890"
        val exception = ApiException.RateLimitException("Rate limit exceeded")

        whenever(groqApiService.sendChatMessage(any(), any())).thenThrow(exception)

        // When
        val result = apiConnectivityTester.testApiConnectivity(apiKey)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result is ApiConnectivityResult.RateLimited)
        assertTrue(result.message.contains("rate limit"))
    }

    @Test
    fun `validateApiKeyFormat returns Valid for correct format`() {
        // Given
        val validApiKey = "gsk_1234567890abcdef1234567890abcdef"

        // When
        val result = apiConnectivityTester.validateApiKeyFormat(validApiKey)

        // Then
        assertTrue(result.isValid)
        assertTrue(result is ApiKeyValidationResult.Valid)
    }

    @Test
    fun `validateApiKeyFormat returns Empty for blank key`() {
        // Given
        val emptyApiKey = ""

        // When
        val result = apiConnectivityTester.validateApiKeyFormat(emptyApiKey)

        // Then
        assertFalse(result.isValid)
        assertTrue(result is ApiKeyValidationResult.Empty)
    }

    @Test
    fun `validateApiKeyFormat returns InvalidFormat for wrong prefix`() {
        // Given
        val invalidApiKey = "sk_1234567890abcdef1234567890abcdef"

        // When
        val result = apiConnectivityTester.validateApiKeyFormat(invalidApiKey)

        // Then
        assertFalse(result.isValid)
        assertTrue(result is ApiKeyValidationResult.InvalidFormat)
        assertEquals("Groq API keys should start with 'gsk_'", result.errorMessage)
    }

    @Test
    fun `validateApiKeyFormat returns TooShort for short key`() {
        // Given
        val shortApiKey = "gsk_123"

        // When
        val result = apiConnectivityTester.validateApiKeyFormat(shortApiKey)

        // Then
        assertFalse(result.isValid)
        assertTrue(result is ApiKeyValidationResult.TooShort)
        assertEquals("API key appears to be too short", result.errorMessage)
    }

    @Test
    fun `validateApiKeyFormat returns InvalidCharacters for special characters`() {
        // Given
        val invalidApiKey = "gsk_1234567890abcdef@#$%^&*()"

        // When
        val result = apiConnectivityTester.validateApiKeyFormat(invalidApiKey)

        // Then
        assertFalse(result.isValid)
        assertTrue(result is ApiKeyValidationResult.InvalidCharacters)
        assertEquals("API key contains invalid characters", result.errorMessage)
    }

    @Test
    fun `quickHealthCheck returns Healthy by default`() = runTest {
        // When
        val result = apiConnectivityTester.quickHealthCheck()

        // Then
        assertTrue(result.isHealthy)
        assertTrue(result is HealthCheckResult.Healthy)
    }
}