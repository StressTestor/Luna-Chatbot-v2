package com.luna.chat.domain.util

import com.luna.chat.domain.exception.ChatException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Utility class for consistent error handling across the application
 */
class ErrorHandler {
    
    companion object {
        /**
         * Handle exceptions and convert them to ChatException with child-friendly messages
         */
        fun handleException(throwable: Throwable): ChatException {
            return when (throwable) {
                is ChatException -> throwable
                else -> ChatException.fromThrowable(throwable)
            }
        }
        
        /**
         * Get a user-friendly error message from any throwable
         */
        fun getChildFriendlyMessage(throwable: Throwable): String {
            val chatException = handleException(throwable)
            return chatException.getChildFriendlyMessage()
        }
        
        /**
         * Check if an error is retryable
         */
        fun isRetryable(throwable: Throwable): Boolean {
            val chatException = handleException(throwable)
            return chatException.isRetryable()
        }
        
        /**
         * Get retry delay for an error
         */
        fun getRetryDelay(throwable: Throwable, attemptNumber: Int): Long {
            val chatException = handleException(throwable)
            return chatException.getRetryDelayMs(attemptNumber)
        }
        
        /**
         * Log error for debugging (in a child-safe way)
         */
        fun logError(throwable: Throwable, context: String = "") {
            // In a real app, this would use proper logging framework
            // For now, we'll use println for debugging
            val chatException = handleException(throwable)
            println("Luna Chat Error [$context]: ${chatException::class.simpleName} - ${throwable.message}")
            
            // Log stack trace for debugging
            // In production, this would be controlled by build configuration
            throwable.printStackTrace()
        }
    }
}

/**
 * Result wrapper for operations that can fail
 */
sealed class ChatResult<out T> {
    data class Success<T>(val data: T) : ChatResult<T>()
    data class Error(val exception: ChatException) : ChatResult<Nothing>()
    data class Loading(val message: String = "AI is thinking...") : ChatResult<Nothing>()
    
    /**
     * Check if result is successful
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * Check if result is an error
     */
    fun isError(): Boolean = this is Error
    
    /**
     * Check if result is loading
     */
    fun isLoading(): Boolean = this is Loading
    
    /**
     * Get data if successful, null otherwise
     */
    fun getDataOrNull(): T? = if (this is Success) data else null
    
    /**
     * Get exception if error, null otherwise
     */
    fun getExceptionOrNull(): ChatException? = if (this is Error) exception else null
    
    /**
     * Get child-friendly error message if error, null otherwise
     */
    fun getChildFriendlyErrorMessage(): String? = 
        getExceptionOrNull()?.getChildFriendlyMessage()
    
    /**
     * Transform success data
     */
    inline fun <R> map(transform: (T) -> R): ChatResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
    }
    
    /**
     * Handle both success and error cases
     */
    inline fun fold(
        onSuccess: (T) -> Unit,
        onError: (ChatException) -> Unit,
        onLoading: (String) -> Unit = {}
    ) {
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(exception)
            is Loading -> onLoading(message)
        }
    }
}

/**
 * Extension functions for Flow<ChatResult<T>>
 */
fun <T> Flow<T>.toChatResult(): Flow<ChatResult<T>> {
    return this
        .map<T, ChatResult<T>> { ChatResult.Success(it) }
        .catch { throwable ->
            val chatException = ErrorHandler.handleException(throwable)
            emit(ChatResult.Error(chatException))
        }
}

/**
 * Extension function to handle errors in Flow
 */
fun <T> Flow<T>.handleChatErrors(): Flow<T> {
    return this.catch { throwable ->
        val chatException = ErrorHandler.handleException(throwable)
        ErrorHandler.logError(chatException, "Flow operation")
        throw chatException
    }
}