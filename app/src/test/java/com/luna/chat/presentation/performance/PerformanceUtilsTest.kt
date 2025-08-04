package com.luna.chat.presentation.performance

import org.junit.Test
import org.junit.Assert.*

class PerformanceUtilsTest {

    @Test
    fun `MessageMemoryManager should detect when cleanup is needed`() {
        val lowCount = 50
        val highCount = 200
        
        assertFalse("Low message count should not trigger cleanup", 
                   MessageMemoryManager.shouldCleanupMemory(lowCount))
        assertTrue("High message count should trigger cleanup", 
                  MessageMemoryManager.shouldCleanupMemory(highCount))
    }

    @Test
    fun `MessageMemoryManager should calculate correct cleanup count`() {
        val messageCount = 200
        val cleanupCount = MessageMemoryManager.calculateCleanupCount(messageCount)
        val expectedCleanup = messageCount - PerformanceUtils.MaxMessagesInMemory
        
        assertEquals("Cleanup count should be correct", expectedCleanup, cleanupCount)
    }

    @Test
    fun `MessageMemoryManager should not cleanup when below threshold`() {
        val messageCount = 50
        val cleanupCount = MessageMemoryManager.calculateCleanupCount(messageCount)
        
        assertEquals("No cleanup should be needed", 0, cleanupCount)
    }

    @Test
    fun `MessageMemoryManager should estimate memory usage correctly`() {
        val messageCount = 100
        val averageSize = 150
        val estimatedUsage = MessageMemoryManager.estimateMemoryUsage(messageCount, averageSize)
        
        assertTrue("Memory usage should be positive", estimatedUsage > 0)
        assertTrue("Memory usage should scale with message count", 
                  estimatedUsage > messageCount * 100) // Base object size
    }

    @Test
    fun `ImageOptimizationUtils should calculate optimal size correctly`() {
        val originalWidth = 2000
        val originalHeight = 1500
        val maxWidth = 800
        val maxHeight = 600
        
        val (optimalWidth, optimalHeight) = ImageOptimizationUtils.calculateOptimalImageSize(
            originalWidth, originalHeight, maxWidth, maxHeight
        )
        
        assertTrue("Optimal width should not exceed max", optimalWidth <= maxWidth)
        assertTrue("Optimal height should not exceed max", optimalHeight <= maxHeight)
        assertTrue("Aspect ratio should be preserved", 
                  Math.abs((optimalWidth.toFloat() / optimalHeight) - (originalWidth.toFloat() / originalHeight)) < 0.1f)
    }

    @Test
    fun `ImageOptimizationUtils should detect when compression is needed`() {
        val largeSize = 2 * 1024 * 1024L // 2MB
        val smallSize = 500 * 1024L // 500KB
        val largeWidth = 3000
        val largeHeight = 2000
        val smallWidth = 800
        val smallHeight = 600
        
        assertTrue("Large file should need compression", 
                  ImageOptimizationUtils.shouldCompressImage(smallWidth, smallHeight, largeSize))
        assertTrue("Large dimensions should need compression", 
                  ImageOptimizationUtils.shouldCompressImage(largeWidth, largeHeight, smallSize))
        assertFalse("Small file and dimensions should not need compression", 
                   ImageOptimizationUtils.shouldCompressImage(smallWidth, smallHeight, smallSize))
    }

    @Test
    fun `DatabaseOptimizationUtils should calculate appropriate page sizes`() {
        val totalItems = 1000
        
        val lowComplexityPageSize = DatabaseOptimizationUtils.calculateOptimalPageSize(
            totalItems, ItemComplexity.LOW
        )
        val highComplexityPageSize = DatabaseOptimizationUtils.calculateOptimalPageSize(
            totalItems, ItemComplexity.HIGH
        )
        
        assertTrue("Low complexity should have larger page size", 
                  lowComplexityPageSize > highComplexityPageSize)
        assertTrue("Page size should not exceed total items", 
                  lowComplexityPageSize <= totalItems)
    }

    @Test
    fun `DatabaseOptimizationUtils should make appropriate caching decisions`() {
        val smallResult = 100
        val largeResult = 2000
        
        assertTrue("High frequency small results should be cached", 
                  DatabaseOptimizationUtils.shouldCacheQuery(QueryFrequency.VERY_HIGH, smallResult))
        assertFalse("Low frequency should not be cached", 
                   DatabaseOptimizationUtils.shouldCacheQuery(QueryFrequency.LOW, smallResult))
        assertFalse("Large results should not be cached even with high frequency", 
                   DatabaseOptimizationUtils.shouldCacheQuery(QueryFrequency.VERY_HIGH, largeResult))
    }

    @Test
    fun `ApiOptimizationUtils should determine batching appropriately`() {
        assertTrue("Multiple requests in short time should be batched", 
                  ApiOptimizationUtils.shouldBatchRequests(3, 500))
        assertFalse("Single request should not be batched", 
                   ApiOptimizationUtils.shouldBatchRequests(1, 500))
        assertFalse("Requests over long time should not be batched", 
                   ApiOptimizationUtils.shouldBatchRequests(3, 5000))
        assertFalse("Too many requests should not be batched", 
                   ApiOptimizationUtils.shouldBatchRequests(10, 500))
    }

    @Test
    fun `ApiOptimizationUtils should calculate exponential backoff correctly`() {
        val baseDelay = 1000L
        val maxDelay = 30000L
        
        val delay1 = ApiOptimizationUtils.calculateRetryDelay(1, baseDelay, maxDelay)
        val delay2 = ApiOptimizationUtils.calculateRetryDelay(2, baseDelay, maxDelay)
        val delay3 = ApiOptimizationUtils.calculateRetryDelay(3, baseDelay, maxDelay)
        
        assertEquals("First attempt should use base delay", baseDelay, delay1)
        assertEquals("Second attempt should double delay", baseDelay * 2, delay2)
        assertEquals("Third attempt should quadruple delay", baseDelay * 4, delay3)
        
        val delayHigh = ApiOptimizationUtils.calculateRetryDelay(10, baseDelay, maxDelay)
        assertEquals("Delay should not exceed maximum", maxDelay, delayHigh)
    }

    @Test
    fun `ApiOptimizationUtils should make appropriate retry decisions`() {
        assertTrue("Network errors should be retried", 
                  ApiOptimizationUtils.shouldRetryRequest(1, 3, ApiErrorType.NETWORK))
        assertTrue("Server errors should be retried", 
                  ApiOptimizationUtils.shouldRetryRequest(1, 3, ApiErrorType.SERVER_ERROR))
        assertTrue("Rate limit errors should be retried", 
                  ApiOptimizationUtils.shouldRetryRequest(1, 3, ApiErrorType.RATE_LIMIT))
        
        assertFalse("Client errors should not be retried", 
                   ApiOptimizationUtils.shouldRetryRequest(1, 3, ApiErrorType.CLIENT_ERROR))
        assertFalse("Authentication errors should not be retried", 
                   ApiOptimizationUtils.shouldRetryRequest(1, 3, ApiErrorType.AUTHENTICATION))
        
        assertFalse("Should not retry after max attempts", 
                   ApiOptimizationUtils.shouldRetryRequest(4, 3, ApiErrorType.NETWORK))
    }

    @Test
    fun `Performance constants should be reasonable`() {
        assertTrue("Default item height should be reasonable", 
                  PerformanceUtils.DefaultItemHeight.value > 0)
        assertTrue("Lazy column buffer should be positive", 
                  PerformanceUtils.LazyColumnBuffer > 0)
        assertTrue("Max messages should be reasonable", 
                  PerformanceUtils.MaxMessagesInMemory > 0)
        assertTrue("Cleanup threshold should be higher than max messages", 
                  PerformanceUtils.MemoryCleanupThreshold > PerformanceUtils.MaxMessagesInMemory)
    }
}