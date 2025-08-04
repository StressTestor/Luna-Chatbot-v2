package com.luna.chat.domain.exception

/**
 * Sealed class hierarchy for all chat-related exceptions
 * Provides child-friendly error messages and retry mechanisms
 */
sealed class ChatException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    /**
     * Get a child-friendly error message for display
     */
    abstract fun getChildFriendlyMessage(): String
    
    /**
     * Determine if this error is retryable
     */
    abstract fun isRetryable(): Boolean
    
    /**
     * Get the retry delay in milliseconds (for exponential backoff)
     */
    open fun getRetryDelayMs(attemptNumber: Int): Long {
        return if (isRetryable()) {
            // Exponential backoff: 1s, 2s, 4s, 8s, max 30s
            minOf(1000L * (1L shl attemptNumber), 30000L)
        } else {
            0L
        }
    }
    
    /**
     * Network connectivity issues
     */
    class NetworkException(
        message: String = "Network connection failed",
        cause: Throwable? = null
    ) : ChatException(message, cause) {
        
        override fun getChildFriendlyMessage(): String {
            return "Oops! Check your internet connection and try again 🌐"
        }
        
        override fun isRetryable(): Boolean = true
    }
    
    /**
     * API service errors (server issues, rate limits, etc.)
     */
    class ApiException(
        val errorCode: String? = null,
        message: String = "API service error",
        cause: Throwable? = null
    ) : ChatException(message, cause) {
        
        override fun getChildFriendlyMessage(): String {
            return when (errorCode) {
                "rate_limit" -> "The AI is taking a short break. Try again in a moment! ⏰"
                "invalid_api_key" -> "Settings need updating. Ask a grown-up for help! 🔧"
                "server_error" -> "The AI is having a hiccup. Let's try that again! 🤖"
                else -> "Something went wrong with the AI service. Let's try again! 🔄"
            }
        }
        
        override fun isRetryable(): Boolean {
            return when (errorCode) {
                "rate_limit", "server_error", "timeout" -> true
                "invalid_api_key", "quota_exceeded" -> false
                else -> true
            }
        }
        
        override fun getRetryDelayMs(attemptNumber: Int): Long {
            return when (errorCode) {
                "rate_limit" -> minOf(5000L * (1L shl attemptNumber), 60000L) // Longer delays for rate limits
                else -> super.getRetryDelayMs(attemptNumber)
            }
        }
    }
    
    /**
     * Content filtering violations
     */
    class ContentFilterException(
        val filterType: FilterType = FilterType.GENERAL,
        message: String = "Content was filtered",
        cause: Throwable? = null
    ) : ChatException(message, cause) {
        
        enum class FilterType {
            INAPPROPRIATE_INPUT,
            INAPPROPRIATE_RESPONSE,
            PERSONAL_INFO,
            GENERAL
        }
        
        override fun getChildFriendlyMessage(): String {
            return when (filterType) {
                FilterType.INAPPROPRIATE_INPUT -> "Let's talk about something else! What would you like to learn today? 📚"
                FilterType.INAPPROPRIATE_RESPONSE -> "The AI said something silly. Let me try a better answer! 🤖"
                FilterType.PERSONAL_INFO -> "Let's keep our personal information private! What else can I help with? 🔒"
                FilterType.GENERAL -> "Let's try a different topic! What would you like to explore? 🌟"
            }
        }
        
        override fun isRetryable(): Boolean = false // Content filtering is not retryable
    }
    
    /**
     * Local storage and database errors
     */
    class StorageException(
        message: String = "Storage operation failed",
        cause: Throwable? = null
    ) : ChatException(message, cause) {
        
        override fun getChildFriendlyMessage(): String {
            return "Having trouble saving our chat. Don't worry, we can keep talking! 💾"
        }
        
        override fun isRetryable(): Boolean = true
    }
    
    /**
     * Configuration and setup errors
     */
    class ConfigurationException(
        val configType: ConfigType = ConfigType.GENERAL,
        message: String = "Configuration error",
        cause: Throwable? = null
    ) : ChatException(message, cause) {
        
        enum class ConfigType {
            API_KEY_MISSING,
            API_KEY_INVALID,
            NETWORK_CONFIG,
            GENERAL
        }
        
        override fun getChildFriendlyMessage(): String {
            return when (configType) {
                ConfigType.API_KEY_MISSING, ConfigType.API_KEY_INVALID -> 
                    "The app needs to be set up by a grown-up! Ask for help with settings 🔧"
                ConfigType.NETWORK_CONFIG -> 
                    "Network settings need attention. Ask a grown-up to check! 🌐"
                ConfigType.GENERAL -> 
                    "The app needs some setup. Ask a grown-up for help! ⚙️"
            }
        }
        
        override fun isRetryable(): Boolean = false // Configuration issues need manual intervention
    }
    
    /**
     * Timeout errors
     */
    class TimeoutException(
        message: String = "Request timed out",
        cause: Throwable? = null
    ) : ChatException(message, cause) {
        
        override fun getChildFriendlyMessage(): String {
            return "The AI is thinking really hard! Let's try again! 🤔"
        }
        
        override fun isRetryable(): Boolean = true
    }
    
    /**
     * Validation errors (input too long, empty, etc.)
     */
    class ValidationException(
        val validationType: ValidationType = ValidationType.GENERAL,
        message: String = "Validation failed",
        cause: Throwable? = null
    ) : ChatException(message, cause) {
        
        enum class ValidationType {
            MESSAGE_TOO_LONG,
            MESSAGE_EMPTY,
            INVALID_FORMAT,
            GENERAL
        }
        
        override fun getChildFriendlyMessage(): String {
            return when (validationType) {
                ValidationType.MESSAGE_TOO_LONG -> 
                    "That message is super long! Try breaking it into smaller parts 📝"
                ValidationType.MESSAGE_EMPTY -> 
                    "Don't forget to type your message! What would you like to say? ✏️"
                ValidationType.INVALID_FORMAT -> 
                    "Something looks funny with that message. Try typing it again! 🔤"
                ValidationType.GENERAL -> 
                    "Let's try typing that message again! 📝"
            }
        }
        
        override fun isRetryable(): Boolean = false // User needs to fix input
    }
    
    /**
     * Unknown or unexpected errors
     */
    class UnknownException(
        message: String = "Unknown error occurred",
        cause: Throwable? = null
    ) : ChatException(message, cause) {
        
        override fun getChildFriendlyMessage(): String {
            return "Something unexpected happened. Let's try again! 🔄"
        }
        
        override fun isRetryable(): Boolean = true
    }
    
    companion object {
        /**
         * Create appropriate ChatException from generic throwable
         */
        fun fromThrowable(throwable: Throwable): ChatException {
            return when (throwable) {
                is java.io.IOException -> NetworkException("Network error: ${throwable.message}", throwable)
                is java.net.SocketTimeoutException -> TimeoutException("Request timeout", throwable)
                is java.net.UnknownHostException -> NetworkException("No internet connection", throwable)
                is ChatException -> throwable
                else -> UnknownException("Unexpected error: ${throwable.message}", throwable)
            }
        }
        
        /**
         * Create ChatException from HTTP response code
         */
        fun fromHttpCode(code: Int, message: String = ""): ChatException {
            return when (code) {
                401 -> ConfigurationException(
                    ConfigurationException.ConfigType.API_KEY_INVALID,
                    "Unauthorized: $message"
                )
                429 -> ApiException("rate_limit", "Rate limited: $message")
                in 500..599 -> ApiException("server_error", "Server error: $message")
                else -> ApiException("http_error", "HTTP error $code: $message")
            }
        }
    }
}