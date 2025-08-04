package com.luna.chat.data.remote.api

/**
 * Sealed class representing the result of an API operation
 * Provides type-safe error handling for network operations
 */
sealed class ApiResult<out T> {
    
    /**
     * Successful API response
     */
    data class Success<T>(val data: T) : ApiResult<T>()
    
    /**
     * API error response
     */
    data class Error(val exception: ApiException) : ApiResult<Nothing>()
    
    /**
     * Loading state for ongoing operations
     */
    data class Loading(val message: String = "Processing...") : ApiResult<Nothing>()
    
    /**
     * Check if the result is successful
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * Check if the result is an error
     */
    fun isError(): Boolean = this is Error
    
    /**
     * Check if the result is loading
     */
    fun isLoading(): Boolean = this is Loading
    
    /**
     * Get data if successful, null otherwise
     */
    fun getDataOrNull(): T? = if (this is Success) data else null
    
    /**
     * Get error if failed, null otherwise
     */
    fun getErrorOrNull(): ApiException? = if (this is Error) exception else null
    
    /**
     * Transform successful result with the given function
     */
    inline fun <R> map(transform: (T) -> R): ApiResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
    }
    
    /**
     * Execute action if result is successful
     */
    inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }
    
    /**
     * Execute action if result is an error
     */
    inline fun onError(action: (ApiException) -> Unit): ApiResult<T> {
        if (this is Error) {
            action(exception)
        }
        return this
    }
    
    /**
     * Execute action if result is loading
     */
    inline fun onLoading(action: (String) -> Unit): ApiResult<T> {
        if (this is Loading) {
            action(message)
        }
        return this
    }
}

/**
 * Extension function to convert nullable data to ApiResult
 */
fun <T> T?.toApiResult(errorMessage: String = "Data is null"): ApiResult<T> {
    return if (this != null) {
        ApiResult.Success(this)
    } else {
        ApiResult.Error(ApiException.UnknownException(errorMessage))
    }
}