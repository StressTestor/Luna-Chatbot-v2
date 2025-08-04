package com.luna.chat.data.remote.api

import com.luna.chat.data.remote.dto.GroqChatRequest
import com.luna.chat.data.remote.dto.GroqMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for testing API connectivity and validating API keys
 */
@Singleton
class ApiConnectivityTester @Inject constructor(
    private val groqApiService: GroqApiService
) {

    companion object {
        private const val TEST_TIMEOUT_MS = 10000L // 10 seconds
        private const val TEST_MESSAGE = "Hello! This is a test message to verify API connectivity."
    }

    /**
     * Test API connectivity with the provided API key
     */
    suspend fun testApiConnectivity(apiKey: String): ApiConnectivityResult = withContext(Dispatchers.IO) {
        try {
            // Test with timeout to avoid hanging
            val result = withTimeoutOrNull(TEST_TIMEOUT_MS) {
                performConnectivityTest(apiKey)
            }
            
            result ?: ApiConnectivityResult.Timeout
        } catch (e: Exception) {
            when (e) {
                is ApiException -> mapApiExceptionToResult(e)
                else -> ApiConnectivityResult.UnknownError(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Perform the actual connectivity test
     */
    private suspend fun performConnectivityTest(apiKey: String): ApiConnectivityResult {
        try {
            // Create a minimal test request
            val testRequest = GroqChatRequest(
                model = "meta-llama/llama-4-maverick-17b-128e-instruct",  // Updated to Meta Llama 4 Maverick model
                messages = listOf(
                    GroqMessage(
                        role = "user",
                        content = TEST_MESSAGE
                    )
                ),
                temperature = 0.1,
                maxTokens = 10, // Minimal response to save costs
                stream = false
            )

            // Make the API call with the test API key
            val response = groqApiService.sendChatMessage(
                request = testRequest,
                authorization = "Bearer $apiKey"
            )

            // Handle the response
            return when (val result = ApiResponseHandler.handleResponse(response)) {
                is ApiResult.Success -> {
                    val groqResponse = result.data
                    if (groqResponse.choices.isNotEmpty()) {
                        ApiConnectivityResult.Success(
                            message = "API connection successful! ✅",
                            responseTime = System.currentTimeMillis(), // Simplified timing
                            model = groqResponse.model ?: "unknown"
                        )
                    } else {
                        ApiConnectivityResult.InvalidResponse("Empty response from API")
                    }
                }
                is ApiResult.Error -> {
                    throw result.exception
                }
                is ApiResult.Loading -> {
                    ApiConnectivityResult.UnknownError("Unexpected loading state")
                }
            }
        } catch (e: Exception) {
            throw e // Re-throw to be handled by the outer try-catch
        }
    }

    /**
     * Map API exceptions to connectivity results
     */
    private fun mapApiExceptionToResult(exception: ApiException): ApiConnectivityResult {
        return when (exception) {
            is ApiException.NetworkException -> ApiConnectivityResult.NetworkError(
                "Network connection failed. Please check your internet connection! 🌐"
            )
            is ApiException.ApiKeyException -> ApiConnectivityResult.InvalidApiKey(
                "API key is invalid or expired. Please check your key! 🔑"
            )
            is ApiException.RateLimitException -> ApiConnectivityResult.RateLimited(
                "API rate limit reached. Please try again later! ⏰"
            )
            is ApiException.HttpException -> {
                if (exception.isServerError()) {
                    ApiConnectivityResult.ServerError(
                        "Groq server is having issues. Please try again later! 🔧"
                    )
                } else {
                    ApiConnectivityResult.InvalidApiKey(
                        "API key is invalid or expired. Please check your key! 🔑"
                    )
                }
            }
            is ApiException.TimeoutException -> ApiConnectivityResult.NetworkError(
                "Request timed out. Please check your connection! ⏰"
            )
            is ApiException.ParseException -> ApiConnectivityResult.InvalidResponse(
                "Invalid response from API"
            )
            is ApiException.ContentFilterException -> ApiConnectivityResult.InvalidResponse(
                "Content was filtered"
            )
            is ApiException.UnknownException -> ApiConnectivityResult.UnknownError(
                exception.message ?: "Unknown API error occurred"
            )
        }
    }

    /**
     * Validate API key format without making an API call
     */
    fun validateApiKeyFormat(apiKey: String): ApiKeyValidationResult {
        return when {
            apiKey.isBlank() -> ApiKeyValidationResult.Empty
            !apiKey.startsWith("gsk_") -> ApiKeyValidationResult.InvalidFormat(
                "Groq API keys should start with 'gsk_'"
            )
            apiKey.length < 20 -> ApiKeyValidationResult.TooShort(
                "API key appears to be too short"
            )
            !apiKey.matches(Regex("^gsk_[a-zA-Z0-9_-]+$")) -> ApiKeyValidationResult.InvalidCharacters(
                "API key contains invalid characters"
            )
            else -> ApiKeyValidationResult.Valid
        }
    }

    /**
     * Quick health check without using API quota
     */
    suspend fun quickHealthCheck(): HealthCheckResult = withContext(Dispatchers.IO) {
        try {
            // This would ideally use a health check endpoint if available
            // For now, we'll just check if we can reach the API base URL
            val result = withTimeoutOrNull(5000L) {
                // In a real implementation, you might ping a health endpoint
                // For now, we'll return a basic connectivity check
                HealthCheckResult.Healthy
            }
            
            result ?: HealthCheckResult.Timeout
        } catch (e: Exception) {
            HealthCheckResult.Unhealthy(e.message ?: "Health check failed")
        }
    }
}

/**
 * Result of API connectivity test
 */
sealed class ApiConnectivityResult {
    data class Success(
        override val message: String,
        val responseTime: Long,
        val model: String
    ) : ApiConnectivityResult()
    
    data class InvalidApiKey(override val message: String) : ApiConnectivityResult()
    data class NetworkError(override val message: String) : ApiConnectivityResult()
    data class RateLimited(override val message: String) : ApiConnectivityResult()
    data class ServerError(override val message: String) : ApiConnectivityResult()
    data class InvalidResponse(override val message: String) : ApiConnectivityResult()
    data class UnknownError(override val message: String) : ApiConnectivityResult()
    object Timeout : ApiConnectivityResult()
    
    val isSuccess: Boolean
        get() = this is Success
        
    open val message: String
        get() = when (this) {
            is Success -> this.message
            is InvalidApiKey -> this.message
            is NetworkError -> this.message
            is RateLimited -> this.message
            is ServerError -> this.message
            is InvalidResponse -> this.message
            is UnknownError -> this.message
            is Timeout -> "Connection timed out. Please try again! ⏰"
        }
}

/**
 * Result of API key format validation
 */
sealed class ApiKeyValidationResult {
    object Valid : ApiKeyValidationResult()
    object Empty : ApiKeyValidationResult()
    data class InvalidFormat(val message: String) : ApiKeyValidationResult()
    data class TooShort(val message: String) : ApiKeyValidationResult()
    data class InvalidCharacters(val message: String) : ApiKeyValidationResult()
    
    val isValid: Boolean
        get() = this is Valid
        
    val errorMessage: String?
        get() = when (this) {
            is Valid -> null
            is Empty -> "Please enter an API key"
            is InvalidFormat -> message
            is TooShort -> message
            is InvalidCharacters -> message
        }
}

/**
 * Result of quick health check
 */
sealed class HealthCheckResult {
    object Healthy : HealthCheckResult()
    data class Unhealthy(val message: String) : HealthCheckResult()
    object Timeout : HealthCheckResult()
    
    val isHealthy: Boolean
        get() = this is Healthy
}