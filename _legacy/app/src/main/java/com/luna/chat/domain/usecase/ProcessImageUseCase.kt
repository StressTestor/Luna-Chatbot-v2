package com.luna.chat.domain.usecase

import com.luna.chat.domain.repository.VisionRepository
import com.luna.chat.security.ContentFilteringService
import com.luna.chat.data.repository.ApiKeyProvider
import com.luna.chat.security.SecureLogger
import com.luna.chat.domain.entity.VisionAnalysisResult
import com.luna.chat.domain.util.ImageValidator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessImageUseCase @Inject constructor(
    private val visionRepository: VisionRepository,
    private val contentFilteringService: ContentFilteringService,
    private val apiKeyProvider: ApiKeyProvider, // reserved for future chaining, not used directly here
    private val secureLogger: SecureLogger
) {
    // This use case returns a summary only; integration to deepseek text generation will be done in Phase 2b.
    suspend operator fun invoke(imageBytes: ByteArray, mimeType: String, userPrompt: String?): Result<String> {
        // Validate image conservatively with ImageValidator
        if (!ImageValidator.validate(imageBytes, mimeType)) {
            return Result.failure(IllegalArgumentException("Image not accepted. Please choose a smaller JPG, PNG, or WEBP."))
        }
        // Convert to base64 (in-memory)
        val base64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
        val visionResult = visionRepository.analyze(base64, mimeType, userPrompt)
        return when (visionResult) {
            is VisionAnalysisResult.Success -> Result.success(visionResult.summary)
            is VisionAnalysisResult.Unavailable -> Result.failure(IllegalStateException(visionResult.reason))
            is VisionAnalysisResult.Error -> Result.failure(IllegalStateException(visionResult.message))
        }
    }

    /**
     * Phase 2b: Provide an API that prepares a child-safe prompt for combined response generation.
     * To preserve layering and avoid blocking collection in domain, this currently returns the summary.
     * The UI/ViewModel will route the composed prompt through the existing SendMessageUseCase path.
     */
    suspend fun invokeAndGenerateReply(imageBytes: ByteArray, mimeType: String, userPrompt: String? = null): Result<String> {
        // Reuse same validation and analysis
        if (!ImageValidator.validate(imageBytes, mimeType)) {
            return Result.failure(IllegalArgumentException("Image not accepted. Please choose a smaller JPG, PNG, or WEBP."))
        }
        val base64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
        return when (val vision = visionRepository.analyze(base64, mimeType, userPrompt)) {
            is VisionAnalysisResult.Success -> {
                // Compose conservative, child-safe context (not persisted)
                val contextForAi = "Image summary: ${vision.summary}"
                // The composed prompt to be sent by UI layer via SendMessageUseCase:
                // val prompt = "Using the following summary of an image, respond kindly and helpfully for a child.\n\n$contextForAi"
                // Minimal placeholder behavior: return the summary and let VM decide to call sendMessage()
                Result.success(vision.summary)
            }
            is VisionAnalysisResult.Unavailable -> Result.failure(IllegalStateException(vision.reason))
            is VisionAnalysisResult.Error -> Result.failure(IllegalStateException(vision.message))
        }
    }
}