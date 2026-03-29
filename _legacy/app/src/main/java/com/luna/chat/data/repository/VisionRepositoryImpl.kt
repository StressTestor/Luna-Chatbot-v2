package com.luna.chat.data.repository

import com.luna.chat.data.remote.api.VisionApiService
import com.luna.chat.data.remote.dto.ContentPart
import com.luna.chat.data.remote.dto.ImageUrl
import com.luna.chat.data.remote.dto.Message
import com.luna.chat.data.remote.dto.VisionChatRequest
import com.luna.chat.domain.entity.VisionAnalysisResult
import com.luna.chat.domain.repository.VisionRepository
import com.luna.chat.security.ContentFilteringService
import com.luna.chat.security.SecureLogger
import com.luna.chat.security.SecurityConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisionRepositoryImpl @Inject constructor(
    private val visionApi: VisionApiService,
    private val apiKeyProvider: ApiKeyProvider,
    private val contentFilteringService: ContentFilteringService,
    private val securityConfig: SecurityConfig,
    private val secureLogger: SecureLogger
) : VisionRepository {

    // Governance gate sourced from SecurityConfig (default false until explicitly approved).
    private val isVisionApproved: Boolean
        get() = securityConfig.isVisionModelApproved()

    override suspend fun analyze(imageBase64: String, mimeType: String, userPrompt: String?): VisionAnalysisResult {
        // Limited mode gate
        if (securityConfig.isLimitedModeEnabled()) {
            secureLogger.info("Vision disabled due to limited mode")
            return VisionAnalysisResult.Unavailable("Vision analysis unavailable right now.")
        }
        // Governance approval gate
        if (!isVisionApproved) {
            secureLogger.info("Vision disabled by governance flag")
            return VisionAnalysisResult.Unavailable("Vision analysis unavailable right now.")
        }

        // Validate MIME; imageBase64 is already a base64 string; skip bytes length parsing here
        if (mimeType !in setOf("image/jpeg", "image/png", "image/webp")) {
            secureLogger.info("Vision image rejected: unsupported mimeType=$mimeType")
            return VisionAnalysisResult.Error("Unsupported image type. Please choose JPG, PNG, or WEBP.")
        }

        val apiKey = apiKeyProvider.getApiKey()
            ?: return VisionAnalysisResult.Error("OpenRouter API key is not configured.")

        // Build data URL for image using already-provided base64 (no disk writes)
        val dataUrl = "data:$mimeType;base64,$imageBase64"

        // Instrumentation: log model parameters available from ContentFilteringService.
        val rawParams = runCatching { contentFilteringService.getModelParameters() }
            .getOrElse { emptyMap() }
        secureLogger.debug("Vision modelParams keys=${rawParams.keys.joinToString()} " +
                "temperature=${rawParams["temperature"]} top_p=${rawParams["top_p"]}")

        // Construct minimal, child-safe prompt
        val safePrompt = (userPrompt?.take(200)
            ?: "Provide a brief, child-safe description of this image without personal details.")

        // Resolve numeric params defensively from map or fall back to SecurityConfig
        val resolvedTemperature = (rawParams["temperature"] as? Number)?.toFloat()
            ?: securityConfig.getTemperatureParameter()
        val resolvedTopP = (rawParams["top_p"] as? Number)?.toFloat()
            ?: securityConfig.getTopPParameter()

        val request = VisionChatRequest(
            model = "meta-llama/llama-3.2-11b-vision-instruct:free", // placeholder vision model; feature remains gated
            messages = listOf(
                Message(
                    role = "user",
                    content = listOf(
                        ContentPart.TextPart(text = safePrompt),
                        ContentPart.ImageUrlPart(imageUrl = ImageUrl(url = dataUrl))
                    )
                )
            ),
            temperature = resolvedTemperature,
            topP = resolvedTopP
        )

        return try {
            // Use existing network patterns; callers must pass Authorization header
            val authHeader = VisionApiService.Companion.formatAuthHeader(apiKey)
            val response = visionApi.analyzeImage(authHeader, request)
            val result = if (response.isSuccessful) {
                val body = response.body()
                val text = body?.choices?.firstOrNull()?.message?.content?.trim().orEmpty()
                if (text.isBlank()) {
                    VisionAnalysisResult.Error("No analysis returned for this image.")
                } else {
                    // PII scrub + content filtering
                    val scrubbed = com.luna.chat.domain.util.PiiScrubber.scrub(text)
                    val filtered = contentFilteringService.filterAiResponse(scrubbed)
                    // Instrumentation: log filtering outcome without leaking content.
                    secureLogger.debug("Vision filter outcome wasFiltered=${filtered.wasFiltered} reason=${filtered.reason}")
                    if (filtered.wasFiltered) {
                        VisionAnalysisResult.Error("The description was not suitable. Please try another image.")
                    } else {
                        VisionAnalysisResult.Success(filtered.input.ifBlank { scrubbed })
                    }
                }
            } else {
                secureLogger.info("Vision HTTP error code=${response.code()}")
                VisionAnalysisResult.Error("Could not analyze the image at this time.")
            }
            result
        } catch (t: Throwable) {
            secureLogger.logException(t)
            VisionAnalysisResult.Error("Image analysis failed due to a network error.")
        }
    }
}