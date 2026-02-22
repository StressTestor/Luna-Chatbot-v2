package com.luna.chat.data.remote.api

import com.luna.chat.data.remote.dto.GroqChatRequest
import com.luna.chat.data.remote.dto.GroqChatResponse
import com.luna.chat.data.remote.dto.VisionChatRequest
import com.luna.chat.data.remote.dto.VisionChatResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class LunaApiClient(private val httpClient: HttpClient) {

    companion object {
        const val BASE_URL = "https://gateway.openrouter.ai/v1/"
        const val CHAT_COMPLETIONS_ENDPOINT = "chat/completions"

        fun formatAuthHeader(apiKey: String): String = "Bearer $apiKey"
    }

    suspend fun sendChatMessage(apiKey: String, request: GroqChatRequest): GroqChatResponse {
        return httpClient.post("$BASE_URL$CHAT_COMPLETIONS_ENDPOINT") {
            contentType(ContentType.Application.Json)
            header("Authorization", formatAuthHeader(apiKey))
            setBody(request)
        }.body()
    }

    suspend fun analyzeImage(apiKey: String, request: VisionChatRequest): VisionChatResponse {
        return httpClient.post("$BASE_URL$CHAT_COMPLETIONS_ENDPOINT") {
            contentType(ContentType.Application.Json)
            header("Authorization", formatAuthHeader(apiKey))
            setBody(request)
        }.body()
    }
}
