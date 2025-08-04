package com.luna.chat.data.remote.api

import java.io.IOException

/**
 * Sealed class hierarchy for API-related exceptions
 * Provides child-friendly error messages and specific error types
 */
sealed class ApiException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    /**
     * Get a child-friendly error message for display
     */
    abstract fun getChildFriendlyMessage(): String
    
    /**
     * Network connectivity issues
     */
    class NetworkException(
        message: String = "Network connection failed",
        cause: Throwable? = null
    ) : ApiException(message, cause) {
        
        override fun getChildFriendlyMessage(): String {
            return "Oops! Check your internet connection and try again 🌐"
        }
    }
    
    /**
     * HTTP error responses (4xx, 5xx)
     */
    class HttpException(
        val code: Int,
        message: String,
        cause: Throwable? = null
    ) : ApiException(message, cause) {
        
        override fun getChildFriendlyMessage(): String {
            return when (code) {
                401 -> "Settings need updating. Ask a grown-up for help! 🔧"
                429 -> "The AI is taking a short break. Try again in a moment! ⏰"
                in 500..599 -> "The AI is having a hiccup. Let's try that again! 🤖"
                else -> "Something went wrong. Let's try again! 🔄"
            }
        }
        
        fun isUnauthorized(): Boolean = code == 401
        fun isRateLimited(): Boolean = code == 429
        fun isServerError(): Boolean = code in 500..599
        fun isClientError(): Boolean = code in 400..499
    }
    
    /**
     * API key related errors
     */
    class ApiKeyException(
        message: String = "Invalid API key",
        cause: Throwable? = null
    ) : ApiException(message, cause) {
        
        override fun getChildFriendlyMessage(): String {
            return "Settings need updating. Ask a grown-up for help! 🔧"
        }
    }
    
    /**
     * Rate limiting errors
     */
    class RateLimitException(
        message: String = "Rate limit exceeded",
        val retryAfterSeconds: Long? = null,
        cause: Throwable? = null
    ) : ApiException(message, cause) {
        
        override fun getChildFriendlyMessage(): String {
            return if (retryAfterSeconds != null) {
                "The AI is taking a ${retryAfterSeconds}s break. Try again soon! ⏰"
            } else {
                "The AI is taking a short break. Try again in a moment! ⏰"
            }
        }
    }
    
    /**
     * Content filtering violations
     */
    class ContentFilterException(
        message: String = "Content was filtered",
        cause: Throwable? = null
    ) : ApiException(message, cause) {
        
        override fun getChildFriendlyMessage(): String {
            return "Let's talk about something else! What would you like to learn today? 📚"
        }
    }
    
    /**
     * JSON parsing errors
     */
    class ParseException(
        message: String = "Failed to parse response",
        cause: Throwable? = null
    ) : ApiException(message, cause) {
        
        override fun getChildFriendlyMessage(): String {
            return "The AI sent a confusing message. Let's try again! 🤖"
        }
    }
    
    /**
     * Timeout errors
     */
    class TimeoutException(
        message: String = "Request timed out",
        cause: Throwable? = null
    ) : ApiException(message, cause) {
        
        override fun getChildFriendlyMessage(): String {
            return "The AI is thinking really hard! Let's try again! 🤔"
        }
    }
    
    /**
     * Unknown or unexpected errors
     */
    class UnknownException(
        message: String = "Unknown error occurred",
        cause: Throwable? = null
    ) : ApiException(message, cause) {
        
        override fun getChildFriendlyMessage(): String {
            return "Something unexpected happened. Let's try again! 🔄"
        }
    }
    
    companion object {
        /**
         * Create appropriate ApiException from HTTP response code
         */
        fun fromHttpCode(code: Int, message: String = "", cause: Throwable? = null): ApiException {
            return when (code) {
                401 -> ApiKeyException("Unauthorized: $message", cause)
                429 -> RateLimitException("Rate limited: $message", cause = cause)
                else -> HttpException(code, message, cause)
            }
        }
        
        /**
         * Create appropriate ApiException from generic throwable
         */
        fun fromThrowable(throwable: Throwable): ApiException {
            return when (throwable) {
                is IOException -> NetworkException("Network error: ${throwable.message}", throwable)
                is java.net.SocketTimeoutException -> TimeoutException("Request timeout", throwable)
                is java.net.UnknownHostException -> NetworkException("No internet connection", throwable)
                is ApiException -> throwable
                else -> UnknownException("Unexpected error: ${throwable.message}", throwable)
            }
        }
    }
}