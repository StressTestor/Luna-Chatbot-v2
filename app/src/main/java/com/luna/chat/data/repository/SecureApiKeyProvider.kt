package com.luna.chat.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure implementation of ApiKeyProvider using EncryptedSharedPreferences
 * and Android Keystore for secure API key storage
 */
@Singleton
class SecureApiKeyProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : ApiKeyProvider {

    companion object {
        private const val ENCRYPTED_PREFS_NAME = "luna_secure_prefs"
        // Renamed to neutral OpenRouter naming; keep legacy keys for migration
        private const val KEY_API_KEY = "openrouter_api_key"
        private const val KEY_API_KEY_HASH = "openrouter_api_key_hash"
        private const val LEGACY_KEY_API_KEY = "groq_api_key"
        private const val LEGACY_KEY_API_KEY_HASH = "groq_api_key_hash"
        private const val MASTER_KEY_ALIAS = "luna_master_key"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context, MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedSharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun getApiKey(): String? = withContext(Dispatchers.IO) {
        try {
            // Try new key first
            val currentKey = encryptedSharedPreferences.getString(KEY_API_KEY, null)
            if (!currentKey.isNullOrBlank() && isApiKeyValid(currentKey)) {
                return@withContext currentKey
            }
            // Migrate legacy Groq key if present
            val legacyKey = encryptedSharedPreferences.getString(LEGACY_KEY_API_KEY, null)
            if (!legacyKey.isNullOrBlank() && isApiKeyValid(legacyKey)) {
                // migrate to new keys
                encryptedSharedPreferences.edit()
                    .putString(KEY_API_KEY, legacyKey)
                    .putString(KEY_API_KEY_HASH, encryptedSharedPreferences.getString(LEGACY_KEY_API_KEY_HASH, null))
                    .remove(LEGACY_KEY_API_KEY)
                    .remove(LEGACY_KEY_API_KEY_HASH)
                    .apply()
                return@withContext legacyKey
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun setApiKey(apiKey: String) = withContext(Dispatchers.IO) {
        try {
            // Validate API key format before storing (relaxed for OpenRouter)
            if (!isValidApiKeyFormat(apiKey)) {
                throw IllegalArgumentException("Invalid API key format")
            }

            // Store encrypted API key
            encryptedSharedPreferences.edit()
                .putString(KEY_API_KEY, apiKey)
                .putString(KEY_API_KEY_HASH, generateApiKeyHash(apiKey))
                .apply()
        } catch (e: Exception) {
            throw SecurityException("Failed to store API key securely", e)
        }
    }

    override suspend fun clearApiKey() = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.edit()
                .remove(KEY_API_KEY)
                .remove(KEY_API_KEY_HASH)
                .apply()
        } catch (e: Exception) {
            // Log error in production, but don't throw to avoid blocking user
        }
    }

    override suspend fun isApiKeyConfigured(): Boolean = withContext(Dispatchers.IO) {
        try {
            val apiKey = encryptedSharedPreferences.getString(KEY_API_KEY, null)
            !apiKey.isNullOrBlank() && isApiKeyValid(apiKey)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if API key exists (synchronous version for ViewModel usage)
     */
    fun hasApiKey(): Boolean {
        return try {
            val apiKey = encryptedSharedPreferences.getString(KEY_API_KEY, null)
            !apiKey.isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Validate stored API key integrity using hash comparison
     */
    private suspend fun isApiKeyValid(apiKey: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val storedHash = encryptedSharedPreferences.getString(KEY_API_KEY_HASH, null)
            val currentHash = generateApiKeyHash(apiKey)
            storedHash == currentHash && isValidApiKeyFormat(apiKey)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Generate a hash of the API key for integrity verification
     */
    private fun generateApiKeyHash(apiKey: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(apiKey.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Validate API key format (public implementation of interface method)
     */
    override suspend fun validateApiKeyFormat(apiKey: String): Boolean = withContext(Dispatchers.IO) {
        isValidApiKeyFormat(apiKey)
    }
    
    /**
     * Validate API key format (private helper)
     */
    private fun isValidApiKeyFormat(apiKey: String): Boolean {
        // OpenRouter keys vary; accept non-blank and minimal length
        return apiKey.isNotBlank() && apiKey.length >= 20
    }

    /**
     * Test if secure storage is available and working
     */
    suspend fun testSecureStorage(): Boolean = withContext(Dispatchers.IO) {
        try {
            val testKey = "test_key"
            val testValue = "test_value_${System.currentTimeMillis()}"
            
            // Try to write and read a test value
            encryptedSharedPreferences.edit()
                .putString(testKey, testValue)
                .apply()
            
            val retrievedValue = encryptedSharedPreferences.getString(testKey, null)
            val isWorking = retrievedValue == testValue
            
            // Clean up test data
            encryptedSharedPreferences.edit()
                .remove(testKey)
                .apply()
            
            isWorking
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get API key metadata without exposing the actual key
     */
    suspend fun getApiKeyMetadata(): ApiKeyMetadata = withContext(Dispatchers.IO) {
        try {
            val isConfigured = isApiKeyConfigured()
            val apiKey = if (isConfigured) getApiKey() else null
            
            ApiKeyMetadata(
                isConfigured = isConfigured,
                isValid = apiKey?.let { isValidApiKeyFormat(it) } ?: false,
                keyPrefix = apiKey?.take(8) ?: "",
                lastModified = getLastModifiedTime()
            )
        } catch (e: Exception) {
            ApiKeyMetadata(
                isConfigured = false,
                isValid = false,
                keyPrefix = "",
                lastModified = 0L
            )
        }
    }

    /**
     * Get the last modified time of the API key (approximate)
     */
    private fun getLastModifiedTime(): Long {
        return try {
            // This is a simple approximation - in a real app you might store this separately
            val prefsFile = context.getSharedPreferences(ENCRYPTED_PREFS_NAME, Context.MODE_PRIVATE)
            // SharedPreferences doesn't provide last modified time directly
            // This is a placeholder - you could store timestamp separately if needed
            System.currentTimeMillis()
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Metadata about the stored API key without exposing the actual key
 */
data class ApiKeyMetadata(
    val isConfigured: Boolean,
    val isValid: Boolean,
    val keyPrefix: String, // First 8 characters for identification
    val lastModified: Long
)