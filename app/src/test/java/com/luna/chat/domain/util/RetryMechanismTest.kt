package com.luna.chat.domain.util

import com.luna.chat.domain.exception.ChatException
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException

class RetryMechanismTest {
    
    @Test
    fun `withRetry succeeds on first attempt`() = runTest {
        var attemptCount = 0
        
        val result = RetryMechanism.withRetry {
            attemptCount++
            "success"
        }
        
        assertEquals("success", result)
        assertEquals(1, attemptCount)
    }
    
    @Test
    fun `withRetry succeeds after retries`() = runTest {
        var attemptCount = 0
        
        val result = RetryMechanism.withRetry {
            attemptCount++
            if (attemptCount < 3) {
                throw ChatException.NetworkException("Network error")
            }
            "success"
        }
        
        assertEquals("success", result)
        assertEquals(3, attemptCount)
    }
    
    @Test
    fun `withRetry fails after max attempts with retryable exception`() = runTest {
        var attemptCount = 0
        
        try {
            RetryMechanism.withRetry(maxAttempts = 3) {
                attemptCount++
                throw ChatException.NetworkException("Network error")
            }
            fail("Expected exception to be thrown")
        } catch (e: ChatException.NetworkException) {
            assertEquals(3, attemptCount)
            assertEquals("Network error", e.message)
        }
    }
    
    @Test
    fun `withRetry does not retry non-retryable exceptions`() = runTest {
        var attemptCount = 0
        
        try {
            RetryMechanism.withRetry {
                attemptCount++
                throw ChatException.ContentFilterException(
                    ChatException.ContentFilterException.FilterType.INAPPROPRIATE_INPUT,
                    "Inappropriate content"
                )
            }
            fail("Expected exception to be thrown")
        } catch (e: ChatException.ContentFilterException) {
            assertEquals(1, attemptCount) // Should not retry
            assertEquals("Inappropriate content", e.message)
        }
    }
    
    @Test
    fun `withRetry converts unknown exceptions to ChatException`() = runTest {
        var attemptCount = 0
        
        try {
            RetryMechanism.withRetry {
                attemptCount++
                throw IOException("IO error")
            }
            fail("Expected exception to be thrown")
        } catch (e: ChatException.NetworkException) {
            assertEquals(3, attemptCount) // Should retry because NetworkException is retryable
            assertTrue(e.message?.contains("IO error") == true)
        }
    }
    
    @Test
    fun `withRetry does not retry converted non-retryable exceptions`() = runTest {
        var attemptCount = 0
        
        try {
            RetryMechanism.withRetry {
                attemptCount++
                throw IllegalArgumentException("Invalid argument")
            }
            fail("Expected exception to be thrown")
        } catch (e: ChatException.UnknownException) {
            assertEquals(3, attemptCount) // UnknownException is retryable, so it should retry
            assertTrue(e.message?.contains("Invalid argument") == true)
        }
    }
    
    @Test
    fun `calculateExponentialBackoff returns correct delays`() {
        assertEquals(1000L, RetryMechanism.calculateExponentialBackoff(0, 1000L, 30000L))
        assertEquals(2000L, RetryMechanism.calculateExponentialBackoff(1, 1000L, 30000L))
        assertEquals(4000L, RetryMechanism.calculateExponentialBackoff(2, 1000L, 30000L))
        assertEquals(8000L, RetryMechanism.calculateExponentialBackoff(3, 1000L, 30000L))
        assertEquals(16000L, RetryMechanism.calculateExponentialBackoff(4, 1000L, 30000L))
        assertEquals(30000L, RetryMechanism.calculateExponentialBackoff(5, 1000L, 30000L)) // Capped at max
        assertEquals(30000L, RetryMechanism.calculateExponentialBackoff(10, 1000L, 30000L)) // Still capped
    }
    
    @Test
    fun `calculateJitteredBackoff returns delays within expected range`() {
        val baseDelay = 1000L
        val maxDelay = 30000L
        val jitterFactor = 0.1
        
        repeat(10) { attempt ->
            val jitteredDelay = RetryMechanism.calculateJitteredBackoff(
                attempt, baseDelay, maxDelay, jitterFactor
            )
            val expectedBaseDelay = RetryMechanism.calculateExponentialBackoff(
                attempt, baseDelay, maxDelay
            )
            val maxJitter = (expectedBaseDelay * jitterFactor).toLong()
            
            // Jittered delay should be between base delay and base delay + max jitter
            assertTrue("Jittered delay $jitteredDelay should be >= $expectedBaseDelay", 
                jitteredDelay >= expectedBaseDelay)
            assertTrue("Jittered delay $jitteredDelay should be <= ${expectedBaseDelay + maxJitter}", 
                jitteredDelay <= expectedBaseDelay + maxJitter)
        }
    }
    
    @Test
    fun `retryOnFailure extension function works correctly`() = runTest {
        var attemptCount = 0
        
        val result = retryOnFailure(maxAttempts = 2) {
            attemptCount++
            if (attemptCount < 2) {
                throw ChatException.NetworkException("Network error")
            }
            "success"
        }
        
        assertEquals("success", result)
        assertEquals(2, attemptCount)
    }
    
    @Test
    fun `withRetry respects custom max attempts`() = runTest {
        var attemptCount = 0
        
        try {
            RetryMechanism.withRetry(maxAttempts = 5) {
                attemptCount++
                throw ChatException.NetworkException("Network error")
            }
            fail("Expected exception to be thrown")
        } catch (e: ChatException.NetworkException) {
            assertEquals(5, attemptCount)
        }
    }
    
    @Test
    fun `withRetry passes attempt number to operation`() = runTest {
        val attemptNumbers = mutableListOf<Int>()
        
        try {
            RetryMechanism.withRetry(maxAttempts = 3) { attemptNumber ->
                attemptNumbers.add(attemptNumber)
                throw ChatException.NetworkException("Network error")
            }
        } catch (e: ChatException.NetworkException) {
            // Expected
        }
        
        assertEquals(listOf(0, 1, 2), attemptNumbers)
    }
}