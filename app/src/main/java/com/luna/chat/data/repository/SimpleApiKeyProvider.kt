package com.luna.chat.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple implementation of ApiKeyProvider using SharedPreferences
 * This is a temporary implementation - will be replaced with secure storage
 * in the next subtask (3.3)
 */
@Singleton
class SimpleApiKeyProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : ApiKeyProvider {

    companion object {
        private const val PREFS_NAME = "luna_api_prefs"
        private const val KEY_API_KEY = "groq_api_key"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun getApiKey(): String? {
        return sharedPreferences.getString(KEY_API_KEY, null)
    }

    override suspend fun setApiKey(apiKey: String) {
        sharedPreferences.edit()
            .putString(KEY_API_KEY, apiKey)
            .apply()
    }

    override suspend fun clearApiKey() {
        sharedPreferences.edit()
            .remove(KEY_API_KEY)
            .apply()
    }

    override suspend fun isApiKeyConfigured(): Boolean {
        val apiKey = getApiKey()
        return !apiKey.isNullOrBlank()
    }

    override suspend fun validateApiKeyFormat(apiKey: String): Boolean {
        return apiKey.isNotBlank() && 
               apiKey.startsWith("gsk_") && 
               apiKey.length >= 20 &&
               apiKey.matches(Regex("^gsk_[a-zA-Z0-9]+$"))
    }
}