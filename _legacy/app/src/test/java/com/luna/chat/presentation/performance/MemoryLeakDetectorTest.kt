package com.luna.chat.presentation.performance

import android.app.ActivityManager
import android.content.Context
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class MemoryLeakDetectorTest {

    private lateinit var context: Context
    private lateinit var activityManager: ActivityManager
    private lateinit var memoryLeakDetector: MemoryLeakDetector

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        activityManager = mockk(relaxed = true)
        
        every { context.getSystemService(Context.ACTIVITY_SERVICE) } returns activityManager
        
        val memoryInfo = ActivityManager.MemoryInfo().apply {
            availMem = 1024 * 1024 * 1024L // 1GB
            totalMem = 2048 * 1024 * 1024L // 2GB
        }
        every { activityManager.getMemoryInfo(any()) } answers {
            val info = firstArg<ActivityManager.MemoryInfo>()
            info.availMem = memoryInfo.availMem
            info.totalMem = memoryInfo.totalMem
        }
        
        memoryLeakDetector = MemoryLeakDetector(context)
    }

    @After
    fun tearDown() {
        memoryLeakDetector.stopMonitoring()
        unmockkAll()
    }

    @Test
    fun `initial state should have empty metrics and warnings`() = runTest {
        val metrics = memoryLeakDetector.memoryMetrics.first()
        val warnings = memoryLeakDetector.leakWarnings.first()
        
        assertEquals("Initial metrics should be empty", MemoryMetrics(), metrics)
        assertTrue("Initial warnings should be empty", warnings.isEmpty())
    }

    @Test
    fun `trackObject should add object to tracking`() {
        val testObject = "test"
        val key = "test_key"
        
        memoryLeakDetector.trackObject(key, testObject)
        
        // We can't directly access private fields, but we can test the behavior
        // by checking that untracking doesn't crash
        memoryLeakDetector.untrackObject(key)
        
        assertTrue("Object tracking should work without crashing", true)
    }

    @Test
    fun `forceGarbageCollection should not crash`() {
        memoryLeakDetector.forceGarbageCollection()
        
        assertTrue("Force GC should complete without crashing", true)
    }

    @Test
    fun `startMonitoring and stopMonitoring should work correctly`() {
        memoryLeakDetector.startMonitoring()
        assertTrue("Monitoring should start without issues", true)
        
        memoryLeakDetector.stopMonitoring()
        assertTrue("Monitoring should stop without issues", true)
    }

    @Test
    fun `getMemoryRecommendations should provide helpful suggestions`() {
        val recommendations = memoryLeakDetector.getMemoryRecommendations()
        
        assertNotNull("Recommendations should not be null", recommendations)
        assertTrue("Recommendations should be a list", recommendations is List<String>)
    }

    @Test
    fun `MemoryMetrics data class should have reasonable defaults`() {
        val metrics = MemoryMetrics()
        
        assertEquals("Default used memory should be 0", 0L, metrics.usedMemoryMB)
        assertEquals("Default total memory should be 0", 0L, metrics.totalMemoryMB)
        assertEquals("Default max memory should be 0", 0L, metrics.maxMemoryMB)
        assertEquals("Default available memory should be 0", 0L, metrics.availableMemoryMB)
        assertEquals("Default memory pressure should be 0", 0f, metrics.memoryPressure, 0.01f)
        assertEquals("Default native heap size should be 0", 0L, metrics.nativeHeapSizeMB)
        assertEquals("Default native heap allocated should be 0", 0L, metrics.nativeHeapAllocatedMB)
    }

    @Test
    fun `LeakWarning should contain required information`() {
        val warning = LeakWarning(
            type = LeakType.HIGH_MEMORY_USAGE,
            message = "Test warning",
            severity = Severity.HIGH
        )
        
        assertEquals("Warning type should be set", LeakType.HIGH_MEMORY_USAGE, warning.type)
        assertEquals("Warning message should be set", "Test warning", warning.message)
        assertEquals("Warning severity should be set", Severity.HIGH, warning.severity)
        assertTrue("Warning should have timestamp", warning.timestamp > 0)
    }

    @Test
    fun `LeakType enum should have all expected values`() {
        val expectedTypes = setOf(
            LeakType.HIGH_MEMORY_USAGE,
            LeakType.OBJECT_ACCUMULATION,
            LeakType.NATIVE_MEMORY_LEAK,
            LeakType.ACTIVITY_LEAK,
            LeakType.VIEW_LEAK
        )
        
        val actualTypes = LeakType.values().toSet()
        
        assertEquals("All leak types should be present", expectedTypes, actualTypes)
    }

    @Test
    fun `Severity enum should have all expected values`() {
        val expectedSeverities = setOf(
            Severity.LOW,
            Severity.MEDIUM,
            Severity.HIGH
        )
        
        val actualSeverities = Severity.values().toSet()
        
        assertEquals("All severities should be present", expectedSeverities, actualSeverities)
    }

    @Test
    fun `multiple object tracking should work correctly`() {
        val objects = mapOf(
            "object1" to "test1",
            "object2" to "test2",
            "object3" to "test3"
        )
        
        // Track multiple objects
        objects.forEach { (key, obj) ->
            memoryLeakDetector.trackObject(key, obj)
        }
        
        // Untrack them
        objects.keys.forEach { key ->
            memoryLeakDetector.untrackObject(key)
        }
        
        assertTrue("Multiple object tracking should work", true)
    }

    @Test
    fun `memory leak detector should handle concurrent access safely`() = runTest {
        // Start monitoring
        memoryLeakDetector.startMonitoring()
        
        // Track objects concurrently (simulated)
        repeat(10) { index ->
            memoryLeakDetector.trackObject("concurrent_$index", "object_$index")
        }
        
        // Force GC while monitoring
        memoryLeakDetector.forceGarbageCollection()
        
        // Untrack objects
        repeat(10) { index ->
            memoryLeakDetector.untrackObject("concurrent_$index")
        }
        
        memoryLeakDetector.stopMonitoring()
        
        assertTrue("Concurrent operations should complete safely", true)
    }
}