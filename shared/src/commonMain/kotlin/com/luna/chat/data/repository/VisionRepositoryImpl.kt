package com.luna.chat.data.repository

import com.luna.chat.data.remote.api.LunaApiClient
import com.luna.chat.data.remote.dto.ContentPart
import com.luna.chat.data.remote.dto.ImageUrl
import com.luna.chat.data.remote.dto.VisionChatRequest
import com.luna.chat.data.remote.dto.VisionMessage
import com.luna.chat.domain.entity.VisionAnalysisResult
import com.luna.chat.domain.repository.VisionRepository

class VisionRepositoryImpl(
    private val apiClient: LunaApiClient,
    private val apiKeyProvider: ApiKeyProvider
) : VisionRepository {

    override suspend fun analyze(imageBase64: String, mimeType: String, userPrompt: String?): VisionAnalysisResult {
        if (mimeType !in setOf("image/jpeg", "image/png", "image/webp")) {
            return VisionAnalysisResult.Error("Unsupported image type. Please choose JPG, PNG, or WEBP.")
        }

        val apiKey = apiKeyProvider.getApiKey()
            ?: return VisionAnalysisResult.Error("API key is not configured.")

        val dataUrl = "data:$mimeType;base64,$imageBase64"
        val safePrompt = userPrompt?.take(200)
            ?: "Provide a brief, child-safe description of this image without personal details."

        val request = VisionChatRequest(
            model = "meta-llama/llama-3.2-11b-vision-instruct:free",
            messages = listOf(
                VisionMessage(
                    role = "user",
                    content = listOf(
                        ContentPart.TextPart(text = safePrompt),
                        ContentPart.ImageUrlPart(imageUrl = ImageUrl(url = dataUrl))
                    )
                )
            )
        )

        return try {
            val response = apiClient.analyzeImage(apiKey, request)
            val text = response.choices.firstOrNull()?.message?.content?.trim().orEmpty()
            if (text.isBlank()) {
                VisionAnalysisResult.Error("No analysis returned for this image.")
            } else {
                VisionAnalysisResult.Success(text)
            }
        } catch (t: Throwable) {
            VisionAnalysisResult.Error("Image analysis failed due to a network error.")
        }
    }
}
