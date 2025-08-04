package com.luna.chat.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.test.core.app.ApplicationProvider
import com.luna.chat.data.repository.SecureApiKeyProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30]) // Use API 30 for Robolectric tests
class ApiKeySecurityTest {

    private lateinit var context: Context
    private lateinit var apiKeyProvider: SecureApiKeyProvider
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        apiKeyProvider = SecureApiKeyProvider(context)
        
        // Clear any existing API key
        runBlocking {
            apiKeyProvider.clearApiKey()
        }
    }
    
    @Test
    fun testApiKeyStorageAndRetrieval() = runBlocking {
        // Given
        val testApiKey = "test-api-key-12345"
        
        // When
        apiKeyProvider.storeApiKey(testApiKey)
        val retrievedKey = apiKeyProvider.getApiKey()
        
        // Then
        assertEquals(testApiKey, retrievedKey)
    }
    
    @Test
    fun testApiKeyConfigurationStatus() = runBlocking {
        // Given - No API key configured
        
        // When/Then - Should return false
        assertFalse(apiKeyProvider.isApiKeyConfigured())
        
        // When - Configure an API key
        apiKeyProvider.storeApiKey("test-api-key")
        
        // Then - Should return true
        assertTrue(apiKeyProvider.isApiKeyConfigured())
    }
    
    @Test
    fun testClearApiKey() = runBlocking {
        // Given - API key is configured
        apiKeyProvider.storeApiKey("test-api-key")
        assertTrue(apiKeyProvider.isApiKeyConfigured())
        
        // When - Clear the API key
        apiKeyProvider.clearApiKey()
        
        // Then - API key should be cleared
        assertFalse(apiKeyProvider.isApiKeyConfigured())
        assertNull(apiKeyProvider.getApiKey())
    }
    
    @Test
    fun testInvalidApiKeyFormat() = runBlocking {
        // Given - Invalid API key format (too short)
        val invalidApiKey = "short"
        
        // When
        apiKeyProvider.storeApiKey(invalidApiKey)
        
        // Then - Should not be considered configured due to invalid format
        assertFalse(apiKeyProvider.isApiKeyConfigured())
    }
    
    @Test
    fun testApiKeyHashVerification() = runBlocking {
        // Given
        val testApiKey = "test-api-key-12345"
        
        // When - Store API key
        apiKeyProvider.storeApiKey(testApiKey)
        
        // Then - Hash verification should pass
        assertTrue(apiKeyProvider.isApiKeyConfigured())
        
        // When - Try to tamper with the stored key directly
        // This is a security test to verify that hash verification works
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        val prefs = EncryptedSharedPreferences.create(
            context,
            "luna_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        
        // Attempt to modify the API key without updating the hash
        prefs.edit().putString("groq_api_key", "tampered-key").apply()
        
        // Then - Hash verification should fail
        assertFalse(apiKeyProvider.isApiKeyConfigured())
    }
}