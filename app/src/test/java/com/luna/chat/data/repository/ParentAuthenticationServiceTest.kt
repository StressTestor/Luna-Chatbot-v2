package com.luna.chat.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class ParentAuthenticationServiceTest {

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var context: Context
    private lateinit var parentAuthenticationService: ParentAuthenticationService

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = RuntimeEnvironment.getApplication()
        
        // Mock SharedPreferences behavior
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putInt(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putLong(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.remove(any())).thenReturn(mockEditor)
        
        // Create service with real context (Robolectric will handle EncryptedSharedPreferences)
        parentAuthenticationService = ParentAuthenticationService(context)
    }

    @Test
    fun `setupParentPassword returns success for valid password`() = runTest {
        // Given
        val password = "test1234"

        // When
        val result = parentAuthenticationService.setupParentPassword(password)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result is ParentAuthResult.Success)
        assertTrue(result.message.contains("successfully"))
    }

    @Test
    fun `setupParentPassword returns WeakPassword for short password`() = runTest {
        // Given
        val weakPassword = "123"

        // When
        val result = parentAuthenticationService.setupParentPassword(weakPassword)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result is ParentAuthResult.WeakPassword)
        assertTrue(result.message.contains("at least"))
    }

    @Test
    fun `setupParentPassword returns WeakPassword for password without numbers`() = runTest {
        // Given
        val passwordWithoutNumbers = "testpassword"

        // When
        val result = parentAuthenticationService.setupParentPassword(passwordWithoutNumbers)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result is ParentAuthResult.WeakPassword)
        assertTrue(result.message.contains("number"))
    }

    @Test
    fun `authenticateParent returns NotSetup when password not configured`() = runTest {
        // Given
        val password = "test1234"
        // Service starts with no password setup

        // When
        val result = parentAuthenticationService.authenticateParent(password)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result is ParentAuthResult.NotSetup)
    }

    @Test
    fun `authenticateParent returns Success for correct password after setup`() = runTest {
        // Given
        val password = "test1234"
        
        // Setup password first
        val setupResult = parentAuthenticationService.setupParentPassword(password)
        assertTrue(setupResult.isSuccess)

        // When
        val authResult = parentAuthenticationService.authenticateParent(password)

        // Then
        assertTrue(authResult.isSuccess)
        assertTrue(authResult is ParentAuthResult.Success)
    }

    @Test
    fun `authenticateParent returns InvalidPassword for wrong password`() = runTest {
        // Given
        val correctPassword = "test1234"
        val wrongPassword = "wrong5678"
        
        // Setup password first
        parentAuthenticationService.setupParentPassword(correctPassword)

        // When
        val result = parentAuthenticationService.authenticateParent(wrongPassword)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result is ParentAuthResult.InvalidPassword)
        assertTrue(result.message.contains("Incorrect"))
    }

    @Test
    fun `changeParentPassword returns Success when current password is correct`() = runTest {
        // Given
        val currentPassword = "test1234"
        val newPassword = "new5678"
        
        // Setup initial password
        parentAuthenticationService.setupParentPassword(currentPassword)

        // When
        val result = parentAuthenticationService.changeParentPassword(currentPassword, newPassword)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result is ParentAuthResult.Success)
        assertTrue(result.message.contains("changed"))
    }

    @Test
    fun `changeParentPassword returns InvalidPassword when current password is wrong`() = runTest {
        // Given
        val currentPassword = "test1234"
        val wrongPassword = "wrong5678"
        val newPassword = "new5678"
        
        // Setup initial password
        parentAuthenticationService.setupParentPassword(currentPassword)

        // When
        val result = parentAuthenticationService.changeParentPassword(wrongPassword, newPassword)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result is ParentAuthResult.InvalidPassword)
    }

    @Test
    fun `resetParentAuth clears all authentication data`() = runTest {
        // Given
        val password = "test1234"
        parentAuthenticationService.setupParentPassword(password)
        assertTrue(parentAuthenticationService.isParentPasswordSetup())

        // When
        val result = parentAuthenticationService.resetParentAuth()

        // Then
        assertTrue(result.isSuccess)
        assertFalse(parentAuthenticationService.isParentPasswordSetup())
    }

    @Test
    fun `isParentPasswordSetup returns false initially`() {
        // When
        val isSetup = parentAuthenticationService.isParentPasswordSetup()

        // Then
        assertFalse(isSetup)
    }

    @Test
    fun `isParentPasswordSetup returns true after setup`() = runTest {
        // Given
        val password = "test1234"

        // When
        parentAuthenticationService.setupParentPassword(password)
        val isSetup = parentAuthenticationService.isParentPasswordSetup()

        // Then
        assertTrue(isSetup)
    }

    @Test
    fun `getAuthStatus returns correct status`() {
        // When
        val status = parentAuthenticationService.getAuthStatus()

        // Then
        assertFalse(status.isSetup)
        assertFalse(status.isLockedOut)
        assertEquals(0, status.failedAttempts)
        assertEquals(0L, status.remainingLockoutTime)
    }

    @Test
    fun `multiple failed attempts trigger lockout`() = runTest {
        // Given
        val correctPassword = "test1234"
        val wrongPassword = "wrong5678"
        
        parentAuthenticationService.setupParentPassword(correctPassword)

        // When - attempt authentication multiple times with wrong password
        repeat(6) {
            parentAuthenticationService.authenticateParent(wrongPassword)
        }

        // Then
        val status = parentAuthenticationService.getAuthStatus()
        assertTrue(status.isLockedOut)
        assertTrue(status.failedAttempts >= 5)
    }
}