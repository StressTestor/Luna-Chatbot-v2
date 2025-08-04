package com.luna.chat.presentation.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Memory leak detection and monitoring utility for the Luna chat app
 */
@Singleton
class MemoryLeakDetector @Inject constructor(
    private val context: Context
) {
    
    private val _memoryMetrics = MutableStateFlow(MemoryMetrics())
    val memoryMetrics: StateFlow<MemoryMetrics> = _memoryMetrics.asStateFlow()
    
    private val _leakWarnings = MutableStateFlow<List<LeakWarning>>(emptyList())
    val leakWarnings: StateFlow<List<LeakWarning>> = _leakWarnings.asStateFlow()
    
    private val monitoringScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val trackedObjects = mutableMapOf<String, WeakReference<Any>>()
    private val objectCounts = mutableMapOf<String, Int>()
    
    private var isMonitoring = false
    
    /**
     * Start memory monitoring
     */
    fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        monitoringScope.launch {
            while (isMonitoring) {
                updateMemoryMetrics()
                checkForLeaks()
                delay(5000) // Check every 5 seconds
            }
        }
    }
    
    /**
     * Stop memory monitoring
     */
    fun stopMonitoring() {
        isMonitoring = false
        monitoringScope.cancel()
    }
    
    /**
     * Track an object for potential memory leaks
     */
    fun trackObject(key: String, obj: Any) {
        trackedObjects[key] = WeakReference(obj)
        objectCounts[key] = (objectCounts[key] ?: 0) + 1
    }
    
    /**
     * Remove tracking for an object
     */
    fun untrackObject(key: String) {
        trackedObjects.remove(key)
        objectCounts[key] = maxOf(0, (objectCounts[key] ?: 0) - 1)
    }
    
    /**
     * Force garbage collection and update metrics
     */
    fun forceGarbageCollection() {
        System.gc()
        Runtime.getRuntime().runFinalization()
        System.gc()
        
        monitoringScope.launch {
            delay(1000) // Wait for GC to complete
            updateMemoryMetrics()
            checkForLeaks()
        }
    }
    
    /**
     * Update memory metrics
     */
    private fun updateMemoryMetrics() {
        val runtime = Runtime.getRuntime()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val metrics = MemoryMetrics(
            usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
            totalMemoryMB = runtime.totalMemory() / (1024 * 1024),
            maxMemoryMB = runtime.maxMemory() / (1024 * 1024),
            availableMemoryMB = memoryInfo.availMem / (1024 * 1024),
            memoryPressure = calculateMemoryPressure(runtime, memoryInfo),
            nativeHeapSizeMB = Debug.getNativeHeapSize() / (1024 * 1024),
            nativeHeapAllocatedMB = Debug.getNativeHeapAllocatedSize() / (1024 * 1024)
        )
        
        _memoryMetrics.value = metrics
    }
    
    /**
     * Check for potential memory leaks
     */
    private fun checkForLeaks() {
        val warnings = mutableListOf<LeakWarning>()
        val currentMetrics = _memoryMetrics.value
        
        // Check for high memory usage
        if (currentMetrics.memoryPressure > 0.8f) {
            warnings.add(
                LeakWarning(
                    type = LeakType.HIGH_MEMORY_USAGE,
                    message = "Memory usage is high (${(currentMetrics.memoryPressure * 100).toInt()}%)",
                    severity = if (currentMetrics.memoryPressure > 0.9f) Severity.HIGH else Severity.MEDIUM
                )
            )
        }
        
        // Check tracked objects for potential leaks
        val staleObjects = trackedObjects.entries.filter { (_, ref) -> ref.get() == null }
        if (staleObjects.isNotEmpty()) {
            staleObjects.forEach { (key, _) ->
                trackedObjects.remove(key)
            }
        }
        
        // Check for objects that should have been garbage collected
        objectCounts.forEach { (key, count) ->
            if (count > 10) { // Threshold for potential leak
                warnings.add(
                    LeakWarning(
                        type = LeakType.OBJECT_ACCUMULATION,
                        message = "High count of $key objects: $count",
                        severity = if (count > 50) Severity.HIGH else Severity.MEDIUM
                    )
                )
            }
        }
        
        // Check native heap growth
        if (currentMetrics.nativeHeapAllocatedMB > currentMetrics.nativeHeapSizeMB * 0.8f) {
            warnings.add(
                LeakWarning(
                    type = LeakType.NATIVE_MEMORY_LEAK,
                    message = "Native heap usage is high",
                    severity = Severity.MEDIUM
                )
            )
        }
        
        _leakWarnings.value = warnings
    }
    
    /**
     * Calculate memory pressure as a percentage
     */
    private fun calculateMemoryPressure(runtime: Runtime, memoryInfo: ActivityManager.MemoryInfo): Float {
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        return usedMemory.toFloat() / maxMemory.toFloat()
    }
    
    /**
     * Get memory recommendations based on current state
     */
    fun getMemoryRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        val metrics = _memoryMetrics.value
        
        if (metrics.memoryPressure > 0.7f) {
            recommendations.add("Consider reducing the number of cached messages")
            recommendations.add("Clear unused image caches")
        }
        
        if (metrics.nativeHeapAllocatedMB > 50) {
            recommendations.add("Check for native memory leaks in image processing")
        }
        
        val highCountObjects = objectCounts.filter { it.value > 20 }
        if (highCountObjects.isNotEmpty()) {
            recommendations.add("Review object lifecycle for: ${highCountObjects.keys.joinToString()}")
        }
        
        return recommendations
    }
}

/**
 * Data class representing memory metrics
 */
data class MemoryMetrics(
    val usedMemoryMB: Long = 0,
    val totalMemoryMB: Long = 0,
    val maxMemoryMB: Long = 0,
    val availableMemoryMB: Long = 0,
    val memoryPressure: Float = 0f,
    val nativeHeapSizeMB: Long = 0,
    val nativeHeapAllocatedMB: Long = 0
)

/**
 * Data class representing a memory leak warning
 */
data class LeakWarning(
    val type: LeakType,
    val message: String,
    val severity: Severity,
    val timestamp: Long = System.currentTimeMillis()
)

enum class LeakType {
    HIGH_MEMORY_USAGE,
    OBJECT_ACCUMULATION,
    NATIVE_MEMORY_LEAK,
    ACTIVITY_LEAK,
    VIEW_LEAK
}

enum class Severity {
    LOW, MEDIUM, HIGH
}

/**
 * Composable that provides memory monitoring for the current composition
 */
@Composable
fun rememberMemoryMonitor(
    memoryLeakDetector: MemoryLeakDetector,
    key: String = "compose_scope"
): MemoryMetrics {
    val metrics by memoryLeakDetector.memoryMetrics.collectAsState()
    
    DisposableEffect(key) {
        memoryLeakDetector.trackObject(key, this)
        onDispose {
            memoryLeakDetector.untrackObject(key)
        }
    }
    
    return metrics
}

/**
 * Lifecycle-aware memory monitoring
 */
@Composable
fun LifecycleMemoryMonitor(
    memoryLeakDetector: MemoryLeakDetector,
    lifecycleOwner: LifecycleOwner
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> memoryLeakDetector.startMonitoring()
                Lifecycle.Event.ON_STOP -> memoryLeakDetector.stopMonitoring()
                Lifecycle.Event.ON_DESTROY -> memoryLeakDetector.forceGarbageCollection()
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}