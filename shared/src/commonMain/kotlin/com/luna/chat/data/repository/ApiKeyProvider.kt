package com.luna.chat.data.repository

interface ApiKeyProvider {
    suspend fun getApiKey(): String?
    fun hasApiKey(): Boolean
    suspend fun setApiKey(apiKey: String)
    suspend fun clearApiKey()
}
