package com.luna.chat.data.repository

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class to initialize the API key on app startup
 * This ensures the API key is available for the chat functionality
 */
@Singleton
class ApiKeyInitializer @Inject constructor(
    private val apiKeyProvider: ApiKeyProvider,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    companion object {
        // The Groq API key for Luna chatbot
        private const val GROQ_API_KEY = "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"
    }
    
    /**
     * Initialize the API key if it's not already configured
     * This should be called during app startup
     */
    suspend fun initializeApiKey() {
        try {
            // Check if API key is already configured
            if (!apiKeyProvider.isApiKeyConfigured()) {
                // Set the API key
                apiKeyProvider.setApiKey(GROQ_API_KEY)
                
                // Update preferences to reflect that API key is configured
                userPreferencesRepository.updateApiKeyConfigured(true)
            }
        } catch (e: Exception) {
            // Log error in production - for now we'll handle silently
            // In a real app, you might want to show an error to the user
        }
    }
    
    /**
     * Verify that the API key is properly configured
     */
    suspend fun verifyApiKeyConfiguration(): Boolean {
        return try {
            val isConfigured = apiKeyProvider.isApiKeyConfigured()
            val storedKey = apiKeyProvider.getApiKey()
            isConfigured && storedKey == GROQ_API_KEY
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Force refresh the API key (useful for testing or if key gets corrupted)
     */
    suspend fun refreshApiKey() {
        try {
            apiKeyProvider.setApiKey(GROQ_API_KEY)
            userPreferencesRepository.updateApiKeyConfigured(true)
        } catch (e: Exception) {
            throw RuntimeException("Failed to refresh API key", e)
        }
    }
}