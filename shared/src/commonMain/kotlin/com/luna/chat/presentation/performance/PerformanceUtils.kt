package com.luna.chat.presentation.performance

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

object PerformanceUtils {
    val DefaultItemHeight = 80.dp
    const val LazyColumnBuffer = 5
    const val MaxMessagesInMemory = 100
    const val MemoryCleanupThreshold = 150
}

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

@Composable
fun LazyListState.shouldRenderItem(
    itemIndex: Int,
    bufferSize: Int = PerformanceUtils.LazyColumnBuffer
): Boolean {
    return remember(this) {
        derivedStateOf {
            val bufferedRange = (firstVisibleItemIndex - bufferSize)..(firstVisibleItemIndex + layoutInfo.visibleItemsInfo.size + bufferSize)
            itemIndex in bufferedRange
        }
    }.value
}

data class ScrollPerformanceMetrics(
    val frameDrops: Int,
    val totalFrames: Int,
    val dropRate: Float
)

object MessageMemoryManager {
    fun shouldCleanupMemory(messageCount: Int): Boolean =
        messageCount > PerformanceUtils.MemoryCleanupThreshold

    fun calculateCleanupCount(messageCount: Int): Int =
        if (shouldCleanupMemory(messageCount)) messageCount - PerformanceUtils.MaxMessagesInMemory else 0

    fun estimateMemoryUsage(messageCount: Int, averageMessageSize: Int = 100): Long {
        val baseObjectSize = 200
        return (messageCount * (baseObjectSize + averageMessageSize)).toLong()
    }
}

object ImageOptimizationUtils {
    fun calculateOptimalImageSize(
        originalWidth: Int, originalHeight: Int,
        maxDisplayWidth: Int, maxDisplayHeight: Int
    ): Pair<Int, Int> {
        val ratio = minOf(maxDisplayWidth.toFloat() / originalWidth, maxDisplayHeight.toFloat() / originalHeight, 1f)
        return Pair((originalWidth * ratio).toInt(), (originalHeight * ratio).toInt())
    }

    fun shouldCompressImage(width: Int, height: Int, fileSizeBytes: Long, maxSizeBytes: Long = 1024 * 1024): Boolean =
        fileSizeBytes > maxSizeBytes || width > 1920 || height > 1920
}
