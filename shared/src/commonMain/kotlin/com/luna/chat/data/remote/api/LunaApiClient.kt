package com.luna.chat.data.remote.api

import com.luna.chat.data.remote.dto.GroqChatRequest
import com.luna.chat.data.remote.dto.GroqChatResponse
import com.luna.chat.data.remote.dto.OpenRouterModelsResponse
import com.luna.chat.data.remote.dto.VisionChatRequest
import com.luna.chat.data.remote.dto.VisionChatResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class LunaApiClient(private val httpClient: HttpClient) {

    companion object {
        const val BASE_URL = "https://openrouter.ai/api/v1/"
        const val CHAT_COMPLETIONS_ENDPOINT = "chat/completions"

        fun formatAuthHeader(apiKey: String): String = "Bearer $apiKey"
    }

    suspend fun sendChatMessage(apiKey: String, request: GroqChatRequest): GroqChatResponse {
        val response = httpClient.post("$BASE_URL$CHAT_COMPLETIONS_ENDPOINT") {
            contentType(ContentType.Application.Json)
            header("Authorization", formatAuthHeader(apiKey))
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            println("Luna:API: error ${response.status.value}: $errorBody")
            throw ApiException.fromHttpCode(response.status.value, errorBody)
        }
        return response.body()
    }

    suspend fun fetchModels(): OpenRouterModelsResponse {
        val response = httpClient.get("${BASE_URL}models")
        if (!response.status.isSuccess()) {
            throw ApiException.fromHttpCode(response.status.value, response.bodyAsText())
        }
        return response.body()
    }

    suspend fun analyzeImage(apiKey: String, request: VisionChatRequest): VisionChatResponse {
        val response = httpClient.post("$BASE_URL$CHAT_COMPLETIONS_ENDPOINT") {
            contentType(ContentType.Application.Json)
            header("Authorization", formatAuthHeader(apiKey))
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            println("Luna:API: error ${response.status.value}: $errorBody")
            throw ApiException.fromHttpCode(response.status.value, errorBody)
        }
        return response.body()
    }
}
