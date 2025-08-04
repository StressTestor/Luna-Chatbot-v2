package com.luna.chat.domain.util

import com.luna.chat.domain.exception.ChatException
import kotlinx.coroutines.delay
import kotlin.math.pow

/**
 * Utility class for implementing retry mechanisms with exponential backoff
 */
class RetryMechanism {
    
    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val BASE_DELAY_MS = 1000L
        private const val MAX_DELAY_MS = 30000L
        
        /**
         * Execute a suspending operation with retry logic and exponential backoff
         * 
         * @param maxAttempts Maximum number of retry attempts (default: 3)
         * @param operation The suspending operation to execute
         * @return Result of the operation
         * @throws ChatException The last exception if all retries fail
         */
        suspend fun <T> withRetry(
            maxAttempts: Int = MAX_RETRY_ATTEMPTS,
            operation: suspend (attemptNumber: Int) -> T
        ): T {
            var lastException: ChatException? = null
            
            repeat(maxAttempts) { attempt ->
                try {
                    return operation(attempt)
                } catch (e: ChatException) {
                    lastException = e
                    
                    // Don't retry if the exception is not retryable
                    if (!e.isRetryable()) {
                        throw e
                    }
                    
                    // Don't delay after the last attempt
                    if (attempt < maxAttempts - 1) {
                        val delayMs = e.getRetryDelayMs(attempt)
                        if (delayMs > 0) {
                            delay(delayMs)
                        }
                    }
                } catch (e: Exception) {
                    // Convert unknown exceptions to ChatException
                    val chatException = ChatException.fromThrowable(e)
                    lastException = chatException
                    
                    if (!chatException.isRetryable()) {
                        throw chatException
                    }
                    
                    if (attempt < maxAttempts - 1) {
                        val delayMs = chatException.getRetryDelayMs(attempt)
                        if (delayMs > 0) {
                            delay(delayMs)
                        }
                    }
                }
            }
            
            // All retries failed, throw the last exception
            throw lastException ?: ChatException.UnknownException("All retry attempts failed")
        }
        
        /**
         * Calculate exponential backoff delay
         * 
         * @param attemptNumber The current attempt number (0-based)
         * @param baseDelayMs Base delay in milliseconds
         * @param maxDelayMs Maximum delay in milliseconds
         * @return Delay in milliseconds
         */
        fun calculateExponentialBackoff(
            attemptNumber: Int,
            baseDelayMs: Long = BASE_DELAY_MS,
            maxDelayMs: Long = MAX_DELAY_MS
        ): Long {
            val exponentialDelay = baseDelayMs * (2.0.pow(attemptNumber.toDouble())).toLong()
            return minOf(exponentialDelay, maxDelayMs)
        }
        
        /**
         * Calculate jittered exponential backoff delay to avoid thundering herd
         * 
         * @param attemptNumber The current attempt number (0-based)
         * @param baseDelayMs Base delay in milliseconds
         * @param maxDelayMs Maximum delay in milliseconds
         * @param jitterFactor Factor for adding randomness (0.0 to 1.0)
         * @return Delay in milliseconds with jitter applied
         */
        fun calculateJitteredBackoff(
            attemptNumber: Int,
            baseDelayMs: Long = BASE_DELAY_MS,
            maxDelayMs: Long = MAX_DELAY_MS,
            jitterFactor: Double = 0.1
        ): Long {
            val baseDelay = calculateExponentialBackoff(attemptNumber, baseDelayMs, maxDelayMs)
            val jitter = (baseDelay * jitterFactor * Math.random()).toLong()
            return baseDelay + jitter
        }
    }
}

/**
 * Extension function for easier retry usage
 */
suspend fun <T> retryOnFailure(
    maxAttempts: Int = 3,
    operation: suspend (attemptNumber: Int) -> T
): T = RetryMechanism.withRetry(maxAttempts, operation)