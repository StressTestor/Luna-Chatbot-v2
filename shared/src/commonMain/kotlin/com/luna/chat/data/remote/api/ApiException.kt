package com.luna.chat.data.remote.api

sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkException(message: String = "Network error", cause: Throwable? = null) : ApiException(message, cause)
    class HttpException(val statusCode: Int, message: String) : ApiException(message)
    class ApiKeyException(message: String = "Invalid API key") : ApiException(message)
    class RateLimitException(message: String = "Rate limit exceeded") : ApiException(message)
    class ContentFilterException(message: String = "Content filtered") : ApiException(message)
    class ParseException(message: String = "Failed to parse response", cause: Throwable? = null) : ApiException(message, cause)
    class TimeoutException(message: String = "Request timed out", cause: Throwable? = null) : ApiException(message, cause)
    class UnknownException(message: String = "Unknown error", cause: Throwable? = null) : ApiException(message, cause)

    fun getChildFriendlyMessage(): String = when (this) {
        is NetworkException -> "Oops! Can't connect to the internet right now. Check your connection!"
        is HttpException -> "Something went wrong on the server. Try again in a moment!"
        is ApiKeyException -> "Luna needs a special key to work. Ask a grown-up to set it up!"
        is RateLimitException -> "Luna needs a short break. Try again in a moment!"
        is ContentFilterException -> "Let's talk about something different!"
        is ParseException -> "Luna got a bit confused. Try asking again!"
        is TimeoutException -> "That took too long! Let's try again!"
        is UnknownException -> "Something unexpected happened. Let's try again!"
    }

    companion object {
        fun fromHttpCode(statusCode: Int, message: String = ""): ApiException = when (statusCode) {
            401, 403 -> ApiKeyException("Authentication failed: $message")
            429 -> RateLimitException("Too many requests: $message")
            in 500..599 -> HttpException(statusCode, "Server error: $message")
            else -> HttpException(statusCode, "HTTP $statusCode: $message")
        }
    }
}
