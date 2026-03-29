package com.luna.chat.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class ApiKeyInitializerTest {

    @Mock
    private lateinit var secureApiKeyProvider: SecureApiKeyProvider

    @Mock
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    private lateinit var apiKeyInitializer: ApiKeyInitializer

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        apiKeyInitializer = ApiKeyInitializer(secureApiKeyProvider, userPreferencesRepository)
    }

    @Test
    fun `initializeApiKey should return success when API key is already configured`() = runTest {
        // Given
        whenever(secureApiKeyProvider.hasApiKey()).thenReturn(true)
        whenever(secureApiKeyProvider.getApiKey()).thenReturn("gsk_existing123456789")

        // When
        val result = apiKeyInitializer.initializeApiKey()

        // Then
        assertTrue(result.isSuccess)
        assertEquals("API key already configured", result.message)
        verify(userPreferencesRepository, never()).updateApiKeyConfigured(any())
    }

    @Test
    fun `initializeApiKey should return error when no API key is set`() = runTest {
        // Given
        whenever(secureApiKeyProvider.hasApiKey()).thenReturn(false)

        // When
        val result = apiKeyInitializer.initializeApiKey()

        // Then
        assertFalse(result.isSuccess)
        assertEquals("No API key configured. Please set up your Groq API key.", result.message)
        verify(userPreferencesRepository, never()).updateApiKeyConfigured(any())
    }

    @Test
    fun `setupApiKey should validate and store valid API key`() = runTest {
        // Given
        val validApiKey = "gsk_valid123456789abcdef"
        val parentPassword = "1234"

        // When
        val result = apiKeyInitializer.setupApiKey(validApiKey, parentPassword)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("API key configured successfully!", result.message)
        verify(secureApiKeyProvider).setApiKey(validApiKey)
        verify(userPreferencesRepository).updateApiKeyConfigured(true)
    }

    @Test
    fun `setupApiKey should reject invalid API key format`() = runTest {
        // Given
        val invalidApiKey = "invalid-key"
        val parentPassword = "1234"

        // When
        val result = apiKeyInitializer.setupApiKey(invalidApiKey, parentPassword)

        // Then
        assertFalse(result.isSuccess)
        assertEquals("Invalid API key format. Please check your Groq API key.", result.message)
        verify(secureApiKeyProvider, never()).setApiKey(any())
        verify(userPreferencesRepository, never()).updateApiKeyConfigured(any())
    }

    @Test
    fun `setupApiKey should reject invalid parent password`() = runTest {
        // Given
        val validApiKey = "gsk_valid123456789abcdef"
        val invalidPassword = "abc" // Too short, no digits

        // When
        val result = apiKeyInitializer.setupApiKey(validApiKey, invalidPassword)

        // Then
        assertFalse(result.isSuccess)
        assertEquals("Invalid parent password. Please ask a grown-up for help!", result.message)
        verify(secureApiKeyProvider, never()).setApiKey(any())
        verify(userPreferencesRepository, never()).updateApiKeyConfigured(any())
    }

    @Test
    fun `setupApiKey should handle storage exceptions`() = runTest {
        // Given
        val validApiKey = "gsk_valid123456789abcdef"
        val parentPassword = "1234"
        
        whenever(secureApiKeyProvider.setApiKey(validApiKey))
            .thenThrow(RuntimeException("Storage error"))

        // When
        val result = apiKeyInitializer.setupApiKey(validApiKey, parentPassword)

        // Then
        assertFalse(result.isSuccess)
        assertEquals("Failed to store API key securely. Please try again.", result.message)
        verify(userPreferencesRepository, never()).updateApiKeyConfigured(any())
    }

    @Test
    fun `validateApiKey should return true for valid format`() {
        // Valid API keys
        assertTrue(apiKeyInitializer.validateApiKey("gsk_1234567890abcdef1234567890"))
        assertTrue(apiKeyInitializer.validateApiKey("gsk_abcdefghijklmnopqrstuvwxyz1234567890"))
    }

    @Test
    fun `validateApiKey should return false for invalid format`() {
        // Invalid API keys
        assertFalse(apiKeyInitializer.validateApiKey(""))
        assertFalse(apiKeyInitializer.validateApiKey("invalid-key"))
        assertFalse(apiKeyInitializer.validateApiKey("sk_1234567890")) // Wrong prefix
        assertFalse(apiKeyInitializer.validateApiKey("gsk_short")) // Too short
        assertFalse(apiKeyInitializer.validateApiKey("gsk_")) // Just prefix
    }

    @Test
    fun `validateParentPassword should return true for valid password`() {
        // Valid passwords (at least 4 characters with at least one digit)
        assertTrue(apiKeyInitializer.validateParentPassword("1234"))
        assertTrue(apiKeyInitializer.validateParentPassword("abc123"))
        assertTrue(apiKeyInitializer.validateParentPassword("parent1"))
        assertTrue(apiKeyInitializer.validateParentPassword("12345"))
    }

    @Test
    fun `validateParentPassword should return false for invalid password`() {
        // Invalid passwords
        assertFalse(apiKeyInitializer.validateParentPassword("")) // Empty
        assertFalse(apiKeyInitializer.validateParentPassword("abc")) // Too short, no digits
        assertFalse(apiKeyInitializer.validateParentPassword("abcd")) // No digits
        assertFalse(apiKeyInitializer.validateParentPassword("123")) // Too short
    }

    @Test
    fun `resetApiKey should clear stored key and update preferences`() = runTest {
        // When
        val result = apiKeyInitializer.resetApiKey()

        // Then
        assertTrue(result.isSuccess)
        assertEquals("API key cleared successfully", result.message)
        verify(secureApiKeyProvider).clearApiKey()
        verify(userPreferencesRepository).updateApiKeyConfigured(false)
    }

    @Test
    fun `resetApiKey should handle clearing exceptions`() = runTest {
        // Given
        whenever(secureApiKeyProvider.clearApiKey())
            .thenThrow(RuntimeException("Clear error"))

        // When
        val result = apiKeyInitializer.resetApiKey()

        // Then
        assertFalse(result.isSuccess)
        assertEquals("Failed to clear API key. Please try again.", result.message)
        verify(userPreferencesRepository, never()).updateApiKeyConfigured(any())
    }

    @Test
    fun `getApiKeyStatus should return correct status`() = runTest {
        // Not configured
        whenever(secureApiKeyProvider.hasApiKey()).thenReturn(false)
        assertEquals(ApiKeyInitializationStatus.NOT_CONFIGURED, apiKeyInitializer.getApiKeyStatus())

        // Configured
        whenever(secureApiKeyProvider.hasApiKey()).thenReturn(true)
        whenever(secureApiKeyProvider.getApiKey()).thenReturn("gsk_valid123456789")
        assertEquals(ApiKeyInitializationStatus.CONFIGURED, apiKeyInitializer.getApiKeyStatus())

        // Invalid format (edge case)
        whenever(secureApiKeyProvider.getApiKey()).thenReturn("invalid-key")
        assertEquals(ApiKeyInitializationStatus.INVALID, apiKeyInitializer.getApiKeyStatus())
    }

    @Test
    fun `updateApiKey should replace existing key with validation`() = runTest {
        // Given
        val newApiKey = "gsk_new123456789abcdef"
        val parentPassword = "1234"
        
        whenever(secureApiKeyProvider.hasApiKey()).thenReturn(true)

        // When
        val result = apiKeyInitializer.updateApiKey(newApiKey, parentPassword)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("API key updated successfully!", result.message)
        verify(secureApiKeyProvider).setApiKey(newApiKey)
        verify(userPreferencesRepository).updateApiKeyConfigured(true)
    }

    @Test
    fun `updateApiKey should reject update with invalid credentials`() = runTest {
        // Given
        val newApiKey = "gsk_new123456789abcdef"
        val invalidPassword = "abc"

        // When
        val result = apiKeyInitializer.updateApiKey(newApiKey, invalidPassword)

        // Then
        assertFalse(result.isSuccess)
        assertEquals("Invalid parent password. Please ask a grown-up for help!", result.message)
        verify(secureApiKeyProvider, never()).setApiKey(any())
    }

    @Test
    fun `ApiKeyInitializationResult should have correct properties`() {
        // Success result
        val successResult = ApiKeyInitializationResult.success("Success message")
        assertTrue(successResult.isSuccess)
        assertEquals("Success message", successResult.message)
        assertNull(successResult.error)

        // Error result
        val errorResult = ApiKeyInitializationResult.error("Error message")
        assertFalse(errorResult.isSuccess)
        assertEquals("Error message", errorResult.message)
        assertEquals("Error message", errorResult.error)
    }

    @Test
    fun `ApiKeyInitializationStatus enum should have all expected values`() {
        val statuses = ApiKeyInitializationStatus.values()
        assertEquals(3, statuses.size)
        assertTrue(statuses.contains(ApiKeyInitializationStatus.NOT_CONFIGURED))
        assertTrue(statuses.contains(ApiKeyInitializationStatus.CONFIGURED))
        assertTrue(statuses.contains(ApiKeyInitializationStatus.INVALID))
    }
}