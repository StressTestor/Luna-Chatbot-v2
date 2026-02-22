package com.luna.chat.data.repository

interface ApiConnectivityTester {
    suspend fun testApiConnectivity(apiKey: String): ApiConnectivityResult
    fun validateApiKeyFormat(apiKey: String): ApiKeyValidationResult
}

data class ApiConnectivityResult(
    val isSuccess: Boolean,
    val message: String
)

data class ApiKeyValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
