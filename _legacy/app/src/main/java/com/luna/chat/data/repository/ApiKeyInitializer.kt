package com.luna.chat.data.repository

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Initializes the API key on app startup using the configured provider.
 * NOTE: No hardcoded keys. Keys must be provided securely (e.g., encrypted prefs).
 */
@Singleton
class ApiKeyInitializer @Inject constructor(
    private val apiKeyProvider: ApiKeyProvider,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    /**
     * Initialize the API key if it's not already configured.
     * This should be called during app startup.
     */
    suspend fun initializeApiKey() {
        try {
            if (!apiKeyProvider.isApiKeyConfigured()) {
                // Do not set any default/hardcoded key. Leave unconfigured until user/parent config.
                userPreferencesRepository.updateApiKeyConfigured(false)
            }
        } catch (e: Exception) {
            // Intentionally swallow here; upstream flows can surface errors to UI.
        }
    }

    /**
     * Verify that the API key is properly configured.
     */
    suspend fun verifyApiKeyConfiguration(): Boolean {
        return try {
            apiKeyProvider.isApiKeyConfigured() && !apiKeyProvider.getApiKey().isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Force refresh the API key (useful for testing or if key gets corrupted).
     * This should only mark configured state true if a non-blank key is present.
     */
    suspend fun refreshApiKey() {
        try {
            val key = apiKeyProvider.getApiKey()
            if (!key.isNullOrBlank()) {
                userPreferencesRepository.updateApiKeyConfigured(true)
            } else {
                userPreferencesRepository.updateApiKeyConfigured(false)
                throw RuntimeException("No API key available to refresh")
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to refresh API key", e)
        }
    }
}