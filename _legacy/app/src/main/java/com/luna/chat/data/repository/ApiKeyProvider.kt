package com.luna.chat.data.repository

/**
 * Interface for providing and managing API keys securely
 */
interface ApiKeyProvider {
    
    /**
     * Get the stored API key
     * @return The API key if available, null otherwise
     */
    suspend fun getApiKey(): String?
    
    /**
     * Store the API key securely
     * @param apiKey The API key to store
     */
    suspend fun setApiKey(apiKey: String)
    
    /**
     * Check if an API key is configured
     * @return true if API key is configured, false otherwise
     */
    suspend fun isApiKeyConfigured(): Boolean
    
    /**
     * Clear the stored API key
     */
    suspend fun clearApiKey()
    
    /**
     * Validate if the provided API key is in correct format
     * @param apiKey The API key to validate
     * @return true if valid format, false otherwise
     */
    suspend fun validateApiKeyFormat(apiKey: String): Boolean
}