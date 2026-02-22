package com.luna.chat.data.repository

import com.luna.chat.platform.SecureStorage

class SecureApiKeyProvider(private val secureStorage: SecureStorage) : ApiKeyProvider {
    companion object {
        private const val KEY_API_KEY = "luna_api_key"
    }

    override suspend fun getApiKey(): String? = secureStorage.getString(KEY_API_KEY)
    override fun hasApiKey(): Boolean = secureStorage.contains(KEY_API_KEY)
    override suspend fun setApiKey(apiKey: String) { secureStorage.putString(KEY_API_KEY, apiKey) }
    override suspend fun clearApiKey() { secureStorage.remove(KEY_API_KEY) }
}
