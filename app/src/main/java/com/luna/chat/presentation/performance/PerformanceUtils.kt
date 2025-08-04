package com.luna.chat.presentation.performance

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Performance utilities for optimizing UI rendering and memory usage
 */
object PerformanceUtils {
    
    /**
     * Default item height for LazyColumn performance calculations
     */
    val DefaultItemHeight = 80.dp
    
    /**
     * Buffer size for preloading items in LazyColumn
     */
    const val LazyColumnBuffer = 5
    
    /**
     * Maximum number of messages to keep in memory
     */
    const val MaxMessagesInMemory = 100
    
    /**
     * Threshold for triggering memory cleanup
     */
    const val MemoryCleanupThreshold = 150
}

/**
 * Composable that tracks whether the LazyColumn is currently scrolling
 * This can be used to optimize animations and reduce recompositions during scroll
 */
@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

/**
 * Composable that provides scroll-based visibility optimization
 * Returns true if the item should be fully rendered, false if it can use a placeholder
 */
@Composable
fun LazyListState.shouldRenderItem(
    itemIndex: Int,
    bufferSize: Int = PerformanceUtils.LazyColumnBuffer
): Boolean {
    return remember(this) {
        derivedStateOf {
            val visibleRange = firstVisibleItemIndex..(firstVisibleItemIndex + layoutInfo.visibleItemsInfo.size)
            val bufferedRange = (firstVisibleItemIndex - bufferSize)..(firstVisibleItemIndex + layoutInfo.visibleItemsInfo.size + bufferSize)
            itemIndex in bufferedRange
        }
    }.value
}

/**
 * Composable that tracks scroll performance metrics
 */
@Composable
fun LazyListState.trackScrollPerformance(): ScrollPerformanceMetrics {
    var frameDrops by remember { mutableStateOf(0) }
    var totalFrames by remember { mutableStateOf(0) }
    
    LaunchedEffect(this) {
        snapshotFlow { isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                if (isScrolling) {
                    totalFrames++
                    // In a real implementation, we would measure actual frame drops
                    // This is a simplified version for demonstration
                }
            }
    }
    
    return remember(frameDrops, totalFrames) {
        ScrollPerformanceMetrics(
            frameDrops = frameDrops,
            totalFrames = totalFrames,
            dropRate = if (totalFrames > 0) frameDrops.toFloat() / totalFrames else 0f
        )
    }
}

/**
 * Data class for scroll performance metrics
 */
data class ScrollPerformanceMetrics(
    val frameDrops: Int,
    val totalFrames: Int,
    val dropRate: Float
)

/**
 * Composable that provides memory-efficient item rendering
 * Only renders items that are visible or in the buffer zone
 */
@Composable
fun <T> LazyListState.optimizedItemContent(
    item: T,
    itemIndex: Int,
    content: @Composable (T) -> Unit,
    placeholder: @Composable () -> Unit = { /* Empty placeholder */ }
) {
    if (shouldRenderItem(itemIndex)) {
        content(item)
    } else {
        placeholder()
    }
}

/**
 * Extension function to calculate optimal item height for LazyColumn
 */
@Composable
fun calculateOptimalItemHeight(
    contentHeight: Dp,
    minHeight: Dp = 48.dp,
    maxHeight: Dp = 200.dp
): Dp {
    val density = LocalDensity.current
    
    return remember(contentHeight, density) {
        contentHeight.coerceIn(minHeight, maxHeight)
    }
}

/**
 * Memory management utilities for chat messages
 */
object MessageMemoryManager {
    
    /**
     * Determines if memory cleanup should be triggered
     */
    fun shouldCleanupMemory(messageCount: Int): Boolean {
        return messageCount > PerformanceUtils.MemoryCleanupThreshold
    }
    
    /**
     * Calculates how many messages to remove during cleanup
     */
    fun calculateCleanupCount(messageCount: Int): Int {
        return if (shouldCleanupMemory(messageCount)) {
            messageCount - PerformanceUtils.MaxMessagesInMemory
        } else {
            0
        }
    }
    
    /**
     * Estimates memory usage for a message list
     */
    fun estimateMemoryUsage(messageCount: Int, averageMessageSize: Int = 100): Long {
        // Rough estimation: each message object + content + UI state
        val baseObjectSize = 200 // bytes
        val totalSize = messageCount * (baseObjectSize + averageMessageSize)
        return totalSize.toLong()
    }
}

/**
 * Image loading and caching utilities
 */
object ImageOptimizationUtils {
    
    /**
     * Calculates optimal image size for display
     */
    fun calculateOptimalImageSize(
        originalWidth: Int,
        originalHeight: Int,
        maxDisplayWidth: Int,
        maxDisplayHeight: Int
    ): Pair<Int, Int> {
        val widthRatio = maxDisplayWidth.toFloat() / originalWidth
        val heightRatio = maxDisplayHeight.toFloat() / originalHeight
        val ratio = minOf(widthRatio, heightRatio, 1f)
        
        return Pair(
            (originalWidth * ratio).toInt(),
            (originalHeight * ratio).toInt()
        )
    }
    
    /**
     * Determines if an image should be compressed
     */
    fun shouldCompressImage(
        width: Int,
        height: Int,
        fileSizeBytes: Long,
        maxSizeBytes: Long = 1024 * 1024 // 1MB
    ): Boolean {
        return fileSizeBytes > maxSizeBytes || width > 1920 || height > 1920
    }
}

/**
 * Database query optimization utilities
 */
object DatabaseOptimizationUtils {
    
    /**
     * Calculates optimal page size for database queries
     */
    fun calculateOptimalPageSize(
        totalItems: Int,
        itemComplexity: ItemComplexity = ItemComplexity.MEDIUM
    ): Int {
        return when (itemComplexity) {
            ItemComplexity.LOW -> minOf(50, totalItems)
            ItemComplexity.MEDIUM -> minOf(25, totalItems)
            ItemComplexity.HIGH -> minOf(10, totalItems)
        }
    }
    
    /**
     * Determines if query results should be cached
     */
    fun shouldCacheQuery(
        queryFrequency: QueryFrequency,
        resultSize: Int,
        maxCacheSize: Int = 1000
    ): Boolean {
        return when (queryFrequency) {
            QueryFrequency.VERY_HIGH -> resultSize <= maxCacheSize
            QueryFrequency.HIGH -> resultSize <= maxCacheSize / 2
            QueryFrequency.MEDIUM -> resultSize <= maxCacheSize / 4
            QueryFrequency.LOW -> false
        }
    }
}

enum class ItemComplexity {
    LOW, MEDIUM, HIGH
}

enum class QueryFrequency {
    LOW, MEDIUM, HIGH, VERY_HIGH
}

/**
 * API request batching utilities
 */
object ApiOptimizationUtils {
    
    /**
     * Determines if requests should be batched
     */
    fun shouldBatchRequests(
        requestCount: Int,
        timeWindowMs: Long,
        maxBatchSize: Int = 5
    ): Boolean {
        return requestCount >= 2 && requestCount <= maxBatchSize && timeWindowMs <= 1000
    }
    
    /**
     * Calculates optimal retry delay with exponential backoff
     */
    fun calculateRetryDelay(
        attemptNumber: Int,
        baseDelayMs: Long = 1000,
        maxDelayMs: Long = 30000
    ): Long {
        val exponentialDelay = baseDelayMs * (1L shl (attemptNumber - 1))
        return minOf(exponentialDelay, maxDelayMs)
    }
    
    /**
     * Determines if request should be retried
     */
    fun shouldRetryRequest(
        attemptNumber: Int,
        maxAttempts: Int = 3,
        errorType: ApiErrorType
    ): Boolean {
        return when (errorType) {
            ApiErrorType.NETWORK -> attemptNumber < maxAttempts
            ApiErrorType.SERVER_ERROR -> attemptNumber < maxAttempts
            ApiErrorType.RATE_LIMIT -> attemptNumber < maxAttempts
            ApiErrorType.CLIENT_ERROR -> false
            ApiErrorType.AUTHENTICATION -> false
        }
    }
}

enum class ApiErrorType {
    NETWORK, SERVER_ERROR, RATE_LIMIT, CLIENT_ERROR, AUTHENTICATION
}