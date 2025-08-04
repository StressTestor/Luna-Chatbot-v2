package com.luna.chat.data.repository

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class SimpleApiKeyProviderTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var apiKeyProvider: SimpleApiKeyProvider

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        whenever(context.getSharedPreferences(any(), any())).thenReturn(sharedPreferences)
        whenever(sharedPreferences.edit()).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)
        whenever(editor.remove(any())).thenReturn(editor)
        
        apiKeyProvider = SimpleApiKeyProvider(context)
    }

    @Test
    fun `getApiKey should return stored API key`() {
        // Given
        val expectedApiKey = "gsk_test123456789"
        whenever(sharedPreferences.getString(SimpleApiKeyProvider.API_KEY_PREF, null))
            .thenReturn(expectedApiKey)

        // When
        val result = apiKeyProvider.getApiKey()

        // Then
        assertEquals(expectedApiKey, result)
    }

    @Test
    fun `getApiKey should return null when no key stored`() {
        // Given
        whenever(sharedPreferences.getString(SimpleApiKeyProvider.API_KEY_PREF, null))
            .thenReturn(null)

        // When
        val result = apiKeyProvider.getApiKey()

        // Then
        assertNull(result)
    }

    @Test
    fun `setApiKey should store API key`() {
        // Given
        val apiKey = "gsk_test123456789"

        // When
        apiKeyProvider.setApiKey(apiKey)

        // Then
        verify(editor).putString(SimpleApiKeyProvider.API_KEY_PREF, apiKey)
        verify(editor).apply()
    }

    @Test
    fun `setApiKey should validate API key format`() {
        // Given
        val invalidApiKey = "invalid-key"

        // When & Then
        try {
            apiKeyProvider.setApiKey(invalidApiKey)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid API key format", e.message)
        }

        verify(editor, never()).putString(any(), any())
        verify(editor, never()).apply()
    }

    @Test
    fun `setApiKey should handle null input`() {
        // When & Then
        try {
            apiKeyProvider.setApiKey(null)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("API key cannot be null or empty", e.message)
        }
    }

    @Test
    fun `setApiKey should handle empty input`() {
        // When & Then
        try {
            apiKeyProvider.setApiKey("")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("API key cannot be null or empty", e.message)
        }
    }

    @Test
    fun `hasApiKey should return true when key exists`() {
        // Given
        whenever(sharedPreferences.getString(SimpleApiKeyProvider.API_KEY_PREF, null))
            .thenReturn("gsk_test123456789")

        // When
        val result = apiKeyProvider.hasApiKey()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasApiKey should return false when no key exists`() {
        // Given
        whenever(sharedPreferences.getString(SimpleApiKeyProvider.API_KEY_PREF, null))
            .thenReturn(null)

        // When
        val result = apiKeyProvider.hasApiKey()

        // Then
        assertFalse(result)
    }

    @Test
    fun `hasApiKey should return false when key is empty`() {
        // Given
        whenever(sharedPreferences.getString(SimpleApiKeyProvider.API_KEY_PREF, null))
            .thenReturn("")

        // When
        val result = apiKeyProvider.hasApiKey()

        // Then
        assertFalse(result)
    }

    @Test
    fun `clearApiKey should remove stored key`() {
        // When
        apiKeyProvider.clearApiKey()

        // Then
        verify(editor).remove(SimpleApiKeyProvider.API_KEY_PREF)
        verify(editor).apply()
    }

    @Test
    fun `isValidApiKey should validate correct format`() {
        // Valid API keys
        assertTrue(SimpleApiKeyProvider.isValidApiKey("gsk_1234567890abcdef1234567890"))
        assertTrue(SimpleApiKeyProvider.isValidApiKey("gsk_abcdefghijklmnopqrstuvwxyz1234567890"))

        // Invalid API keys
        assertFalse(SimpleApiKeyProvider.isValidApiKey(""))
        assertFalse(SimpleApiKeyProvider.isValidApiKey("invalid-key"))
        assertFalse(SimpleApiKeyProvider.isValidApiKey("sk_1234567890")) // Wrong prefix
        assertFalse(SimpleApiKeyProvider.isValidApiKey("gsk_short")) // Too short
        assertFalse(SimpleApiKeyProvider.isValidApiKey("gsk_")) // Just prefix
        assertFalse(SimpleApiKeyProvider.isValidApiKey("GSK_1234567890abcdef1234567890")) // Wrong case
    }

    @Test
    fun `getApiKeyStatus should return correct status`() {
        // No key
        whenever(sharedPreferences.getString(SimpleApiKeyProvider.API_KEY_PREF, null))
            .thenReturn(null)
        assertEquals(ApiKeyStatus.NOT_SET, apiKeyProvider.getApiKeyStatus())

        // Invalid key
        whenever(sharedPreferences.getString(SimpleApiKeyProvider.API_KEY_PREF, null))
            .thenReturn("invalid-key")
        assertEquals(ApiKeyStatus.INVALID, apiKeyProvider.getApiKeyStatus())

        // Valid key
        whenever(sharedPreferences.getString(SimpleApiKeyProvider.API_KEY_PREF, null))
            .thenReturn("gsk_1234567890abcdef1234567890")
        assertEquals(ApiKeyStatus.VALID, apiKeyProvider.getApiKeyStatus())
    }

    @Test
    fun `updateApiKey should replace existing key`() {
        // Given
        val oldKey = "gsk_old123456789"
        val newKey = "gsk_new123456789"
        
        whenever(sharedPreferences.getString(SimpleApiKeyProvider.API_KEY_PREF, null))
            .thenReturn(oldKey)

        // When
        apiKeyProvider.setApiKey(newKey)

        // Then
        verify(editor).putString(SimpleApiKeyProvider.API_KEY_PREF, newKey)
        verify(editor).apply()
    }

    @Test
    fun `constants should have correct values`() {
        assertEquals("api_key", SimpleApiKeyProvider.API_KEY_PREF)
        assertEquals("luna_api_prefs", SimpleApiKeyProvider.PREFS_NAME)
        assertEquals(20, SimpleApiKeyProvider.MIN_API_KEY_LENGTH)
    }
}