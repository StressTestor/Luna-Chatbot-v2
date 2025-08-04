package com.luna.chat.data.remote.api

import com.luna.chat.data.remote.dto.GroqChatRequest
import com.luna.chat.data.remote.dto.GroqChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Groq API service interface for chat completions
 * Provides methods to interact with the Groq API for AI chat functionality
 */
interface GroqApiService {
    
    /**
     * Send a chat completion request to the Groq API
     * 
     * @param authorization Bearer token for API authentication
     * @param request The chat request containing messages and configuration
     * @return Response containing the AI's chat completion
     */
    @POST("chat/completions")
    suspend fun sendChatMessage(
        @Header("Authorization") authorization: String,
        @Body request: GroqChatRequest
    ): Response<GroqChatResponse>
    
    /**
     * Send a chat completion request with custom headers
     * 
     * @param authorization Bearer token for API authentication
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
        const val BASE_URL = "https://api.groq.com/"
        const val CHAT_COMPLETIONS_ENDPOINT = "openai/v1/chat/completions"
        
        /**
         * Format API key for Authorization header
         * 
         * @param apiKey The raw API key
         * @return Formatted authorization header value
         */
        fun formatAuthHeader(apiKey: String): String {
            return "Bearer $apiKey"
        }
        
        /**
         * Validate API key format
         * 
         * @param apiKey The API key to validate
         * @return true if the API key appears to be valid format
         */
        fun isValidApiKeyFormat(apiKey: String): Boolean {
            return apiKey.isNotBlank() && 
                   apiKey.startsWith("gsk_") && 
                   apiKey.length >= 20
        }
    }
}