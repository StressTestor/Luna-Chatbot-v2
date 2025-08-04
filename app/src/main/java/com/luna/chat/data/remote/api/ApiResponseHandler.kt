package com.luna.chat.data.remote.api

import com.google.gson.JsonSyntaxException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Utility class for handling API responses and converting them to ApiResult
 * Provides consistent error handling across all API calls
 */
object ApiResponseHandler {
    
    /**
     * Handle a Retrofit Response and convert it to ApiResult
     * 
     * @param response The Retrofit response to handle
     * @return ApiResult containing either success data or error
     */
    fun <T> handleResponse(response: Response<T>): ApiResult<T> {
        return try {
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error(
                        ApiException.ParseException("Response body is null")
                    )
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                val exception = ApiException.fromHttpCode(
                    code = response.code(),
                    message = errorMessage
                )
                ApiResult.Error(exception)
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiException.fromThrowable(e))
        }
    }
    
    /**
     * Execute an API call safely and return ApiResult
     * 
     * @param apiCall Suspend function that makes the API call
     * @return ApiResult containing either success data or error
     */
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): ApiResult<T> {
        return try {
            val response = apiCall()
            handleResponse(response)
        } catch (e: UnknownHostException) {
            ApiResult.Error(
                ApiException.NetworkException("No internet connection", e)
            )
        } catch (e: SocketTimeoutException) {
            ApiResult.Error(
                ApiException.TimeoutException("Request timed out", e)
            )
        } catch (e: IOException) {
            ApiResult.Error(
                ApiException.NetworkException("Network error: ${e.message}", e)
            )
        } catch (e: JsonSyntaxException) {
            ApiResult.Error(
                ApiException.ParseException("Failed to parse response: ${e.message}", e)
            )
        } catch (e: Exception) {
            ApiResult.Error(
                ApiException.UnknownException("Unexpected error: ${e.message}", e)
            )
        }
    }
    
    /**
     * Execute an API call with retry logic
     * 
     * @param maxRetries Maximum number of retry attempts
     * @param delayMs Delay between retries in milliseconds
     * @param apiCall Suspend function that makes the API call
     * @return ApiResult containing either success data or error
     */
    suspend fun <T> safeApiCallWithRetry(
        maxRetries: Int = 3,
        delayMs: Long = 1000,
        apiCall: suspend () -> Response<T>
    ): ApiResult<T> {
        var lastException: ApiException? = null
        
        repeat(maxRetries + 1) { attempt ->
            val result = safeApiCall(apiCall)
            
            when (result) {
                is ApiResult.Success -> return result
                is ApiResult.Error -> {
                    lastException = result.exception
                    
                    // Don't retry for certain error types
                    when (result.exception) {
                        is ApiException.ApiKeyException,
                        is ApiException.ContentFilterException -> return result
                        is ApiException.HttpException -> {
                            if (result.exception.isClientError() && !result.exception.isRateLimited()) {
                                return result
                            }
                        }
                        is ApiException.NetworkException,
                        is ApiException.ParseException,
                        is ApiException.RateLimitException,
                        is ApiException.TimeoutException,
                        is ApiException.UnknownException -> {
                            // These can be retried
                        }
                    }
                    
                    // Don't delay on the last attempt
                    if (attempt < maxRetries) {
                        kotlinx.coroutines.delay(delayMs * (attempt + 1)) // Exponential backoff
                    }
                }
                is ApiResult.Loading -> {
                    // This shouldn't happen in safeApiCall, but handle it just in case
                    // Just continue to the next iteration
                }
            }
        }
        
        // If we get here, all retries failed
        return ApiResult.Error(
            lastException ?: ApiException.UnknownException("All retry attempts failed")
        )
    }
    
    /**
     * Validate API key format before making requests
     * 
     * @param apiKey The API key to validate
     * @return ApiResult.Success if valid, ApiResult.Error if invalid
     */
    fun validateApiKey(apiKey: String): ApiResult<String> {
        return if (GroqApiService.isValidApiKeyFormat(apiKey)) {
            ApiResult.Success(apiKey)
        } else {
            ApiResult.Error(
                ApiException.ApiKeyException("Invalid API key format")
            )
        }
    }
    
    /**
     * Extract retry-after header from rate limit responses
     * 
     * @param response The HTTP response
     * @return Retry-after seconds, or null if not present
     */
    fun extractRetryAfter(response: Response<*>): Long? {
        return response.headers()["Retry-After"]?.toLongOrNull()
    }
}