package com.luna.chat.data.remote.api

import com.luna.chat.data.remote.dto.VisionChatRequest
import com.luna.chat.data.remote.dto.VisionChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Retrofit service for OpenRouter-compatible Vision Chat endpoints.
 *
 * Security posture and Authorization handling:
 * - Callers MUST supply the Authorization header explicitly via [analyzeImage]'s `authorization` parameter.
 * - Interceptors in the networking stack are BEST-EFFORT ONLY and MUST NOT be relied upon to inject secrets.
 * - Use [formatAuthHeader] to format the bearer token value at the call site.
 *
 * Logging policy:
 * - Downstream logging is redacted by interceptors; do not log secrets in this interface or call sites.
 * - Release builds use HttpLoggingInterceptor.Level.NONE.
 *
 * In-memory constraints:
 * - Images must be processed in-memory only. Supply images as data URLs (data:<mime>;base64,...) in the request.
 */
interface VisionApiService {

    @POST(CHAT_COMPLETIONS_ENDPOINT)
    @Headers("Content-Type: application/json")
    suspend fun analyzeImage(
        @Header("Authorization") authorization: String,
        @Body request: VisionChatRequest
    ): Response<VisionChatResponse>

    companion object {
        const val CHAT_COMPLETIONS_ENDPOINT: String = "chat/completions"
        fun formatAuthHeader(apiKey: String): String = "Bearer $apiKey"
    }
}