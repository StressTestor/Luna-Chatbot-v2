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
        // Use provider-agnostic key name; keep legacy for migration in get/set
        private const val KEY_API_KEY = "openrouter_api_key"
        private const val LEGACY_KEY_API_KEY = "groq_api_key"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun getApiKey(): String? {
        // Prefer new key; fall back to legacy and migrate forward
        val current = sharedPreferences.getString(KEY_API_KEY, null)
        if (!current.isNullOrBlank()) return current
        val legacy = sharedPreferences.getString(LEGACY_KEY_API_KEY, null)
        if (!legacy.isNullOrBlank()) {
            sharedPreferences.edit()
                .putString(KEY_API_KEY, legacy)
                .remove(LEGACY_KEY_API_KEY)
                .apply()
            return legacy
        }
        return null
    }

    override suspend fun setApiKey(apiKey: String) {
        sharedPreferences.edit()
            .putString(KEY_API_KEY, apiKey)
            .remove(LEGACY_KEY_API_KEY)
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
        // OpenRouter keys vary; accept non-blank and minimal length
        return apiKey.isNotBlank() && apiKey.length >= 20
    }
}