package com.luna.chat.data.repository

import com.luna.chat.data.remote.api.LunaApiClient
import com.luna.chat.data.remote.dto.GroqChatRequest
import com.luna.chat.data.remote.dto.GroqMessage

class DefaultApiConnectivityTester(private val apiClient: LunaApiClient) : ApiConnectivityTester {

    override suspend fun testApiConnectivity(apiKey: String): ApiConnectivityResult {
        return try {
            val testRequest = GroqChatRequest.create(
                messages = listOf(
                    GroqMessage.createSystemMessage("You are a test assistant."),
                    GroqMessage.createUserMessage("Hello, this is a test.")
                ),
                maxTokens = 50
            )
            val response = apiClient.sendChatMessage(apiKey, testRequest)
            if (response.getAssistantMessage() != null) {
                ApiConnectivityResult(isSuccess = true, message = "API connection successful! Luna is ready to chat!")
            } else {
                ApiConnectivityResult(isSuccess = false, message = "API responded but no message was returned.")
            }
        } catch (e: Exception) {
            ApiConnectivityResult(isSuccess = false, message = "Connection failed: ${e.message}")
        }
    }

    override fun validateApiKeyFormat(apiKey: String): ApiKeyValidationResult {
        return if (apiKey.isNotBlank() && apiKey.length >= 10) {
            ApiKeyValidationResult(isValid = true)
        } else {
            ApiKeyValidationResult(isValid = false, errorMessage = "API key is too short or empty.")
        }
    }
}
