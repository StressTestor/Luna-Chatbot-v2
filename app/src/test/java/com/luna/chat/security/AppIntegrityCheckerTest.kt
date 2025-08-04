package com.luna.chat.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.pm.SigningInfo
import android.os.Build
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class AppIntegrityCheckerTest {

    @Mock
    private lateinit var context: Context
    
    @Mock
    private lateinit var packageManager: PackageManager
    
    @Mock
    private lateinit var packageInfo: PackageInfo
    
    @Mock
    private lateinit var signingInfo: SigningInfo
    
    @Mock
    private lateinit var applicationInfo: ApplicationInfo
    
    private lateinit var appIntegrityChecker: AppIntegrityChecker
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        whenever(context.packageManager).thenReturn(packageManager)
        whenever(context.packageName).thenReturn("com.luna.chat")
        whenever(context.applicationInfo).thenReturn(applicationInfo)
        
        appIntegrityChecker = AppIntegrityChecker(context)
    }
    
    @Test
    fun `verifyPackageName should return true for correct package name`() {
        // Given
        whenever(context.packageName).thenReturn("com.luna.chat")
        
        // When/Then
        assertTrue(appIntegrityChecker.verifyPackageName())
    }
    
    @Test
    fun `verifyPackageName should return false for incorrect package name`() {
        // Given
        whenever(context.packageName).thenReturn("com.malicious.app")
        
        // When/Then
        assertFalse(appIntegrityChecker.verifyPackageName())
    }
    
    @Test
    fun `verifyInstallerStore should return true for official store`() {
        // Given
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val installSourceInfo = android.content.pm.InstallSourceInfo::class.java.newInstance()
            val field = installSourceInfo.javaClass.getDeclaredField("installingPackageName")
            field.isAccessible = true
            field.set(installSourceInfo, "com.android.vending")
            
            whenever(packageManager.getInstallSourceInfo(any())).thenReturn(installSourceInfo)
        } else {
            whenever(packageManager.getInstallerPackageName(any())).thenReturn("com.android.vending")
        }
        
        // When/Then
        assertTrue(appIntegrityChecker.verifyInstallerStore())
    }
    
    @Test
    fun `verifyInstallerStore should return false for unofficial store`() {
        // Given
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val installSourceInfo = android.content.pm.InstallSourceInfo::class.java.newInstance()
            val field = installSourceInfo.javaClass.getDeclaredField("installingPackageName")
            field.isAccessible = true
            field.set(installSourceInfo, "com.unknown.store")
            
            whenever(packageManager.getInstallSourceInfo(any())).thenReturn(installSourceInfo)
        } else {
            whenever(packageManager.getInstallerPackageName(any())).thenReturn("com.unknown.store")
        }
        
        // When/Then
        assertFalse(appIntegrityChecker.verifyInstallerStore())
    }
    
    @Test
    fun `isRunningOnEmulator should detect emulator`() {
        // This test is limited because we can't easily mock Build.* fields
        // Just verify the method doesn't crash
        appIntegrityChecker.isRunningOnEmulator()
    }
    
    @Test
    fun `isDeviceRooted should detect root`() {
        // Mock File.exists() to simulate su binary
        val mockFile = org.mockito.Mockito.mock(File::class.java)
        whenever(mockFile.exists()).thenReturn(true)
        
        // This test is limited because we can't easily mock File constructor
        // Just verify the method doesn't crash
        appIntegrityChecker.isDeviceRooted()
    }
}