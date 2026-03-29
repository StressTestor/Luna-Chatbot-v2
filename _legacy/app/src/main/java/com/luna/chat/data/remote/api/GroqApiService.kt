package com.luna.chat.data.remote.api

import com.luna.chat.data.remote.dto.GroqChatRequest
import com.luna.chat.data.remote.dto.GroqChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * OpenRouter-compatible API service interface for chat completions.
 *
 * Base:
 * - BASE_URL: https://api.openrouter.ai/v1/ (configured in NetworkModule)
 * - Endpoint: chat/completions
 *
 * Authorization:
 * - Call sites MUST provide a correctly formatted Authorization header using
 *   formatAuthHeader(apiKey) -> "Bearer <API_KEY>".
 * - Do not rely on network interceptors for credentials; interceptors are best‑effort only and
 *   mainly standardize headers and protect logs via redaction. Release builds disable HTTP body logging.
 */
interface GroqApiService {

    /**
     * Send a chat completion request to the OpenRouter API.
     *
     * Authorization requirements:
     * - Pass Authorization using formatAuthHeader(apiKey) which yields "Bearer <API_KEY>".
     * - Interceptors do not guarantee adding credentials; callers are the source of truth.
     *
     * Endpoint context:
     * - BASE_URL targets OpenRouter (https://api.openrouter.ai/v1/)
     * - Relative endpoint: chat/completions
     *
     * @param authorization Authorization header value, e.g., formatAuthHeader(apiKey) -> "Bearer <API_KEY>"
     * @param request The chat request containing messages and configuration
     * @return Response containing the AI's chat completion
     */
    @POST("chat/completions")
    suspend fun sendChatMessage(
        @Header("Authorization") authorization: String,
        @Body request: GroqChatRequest
    ): Response<GroqChatResponse>

    /**
     * Send a chat completion request with custom headers.
     *
     * Authorization requirements:
     * - Pass Authorization using formatAuthHeader(apiKey) which yields "Bearer <API_KEY>".
     * - Do not depend on interceptors to inject credentials.
     *
     * Endpoint context:
     * - BASE_URL targets OpenRouter (https://api.openrouter.ai/v1/)
     * - Relative endpoint: chat/completions
     *
     * @param authorization Authorization header value, e.g., formatAuthHeader(apiKey) -> "Bearer <API_KEY>"
     * @param contentType Content type header (default: application/json)
     * @param request The chat request containing messages and configuration
     * @return Response containing the AI's chat completion
     */
    @POST("chat/completions")
    suspend fun sendChatMessageWithHeaders(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: GroqChatRequest
    ): Response<GroqChatResponse>

    companion object {
        // OpenRouter base and endpoint constants (NetworkModule sets baseUrl to https://api.openrouter.ai/v1/)
        const val BASE_URL = "https://api.openrouter.ai/v1/"
        const val CHAT_COMPLETIONS_ENDPOINT = "chat/completions"

        /**
         * Format API key for Authorization header (OpenRouter: "Bearer <API_KEY>")
         */
        fun formatAuthHeader(apiKey: String): String = "Bearer $apiKey"

        /**
         * Validate API key format:
         * OpenRouter keys vary; accept non-blank Bearer tokens. Do not enforce "gsk_" prefix.
         */
        fun isValidApiKeyFormat(apiKey: String): Boolean {
            return apiKey.isNotBlank()
        }
    }
}