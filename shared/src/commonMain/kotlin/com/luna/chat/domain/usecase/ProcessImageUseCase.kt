package com.luna.chat.domain.usecase

import com.luna.chat.domain.repository.VisionRepository
import com.luna.chat.domain.entity.VisionAnalysisResult
import com.luna.chat.domain.util.ImageValidator
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class ProcessImageUseCase(
    private val visionRepository: VisionRepository
) {
    @OptIn(ExperimentalEncodingApi::class)
    suspend operator fun invoke(imageBytes: ByteArray, mimeType: String, userPrompt: String?): Result<String> {
        if (!ImageValidator.validate(imageBytes, mimeType)) {
            return Result.failure(IllegalArgumentException("Image not accepted. Please choose a smaller JPG, PNG, or WEBP."))
        }
        val base64 = Base64.encode(imageBytes)
        val visionResult = visionRepository.analyze(base64, mimeType, userPrompt)
        return when (visionResult) {
            is VisionAnalysisResult.Success -> Result.success(visionResult.summary)
            is VisionAnalysisResult.Unavailable -> Result.failure(IllegalStateException(visionResult.reason))
            is VisionAnalysisResult.Error -> Result.failure(IllegalStateException(visionResult.message))
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun invokeAndGenerateReply(imageBytes: ByteArray, mimeType: String, userPrompt: String? = null): Result<String> {
        if (!ImageValidator.validate(imageBytes, mimeType)) {
            return Result.failure(IllegalArgumentException("Image not accepted. Please choose a smaller JPG, PNG, or WEBP."))
        }
        val base64 = Base64.encode(imageBytes)
        return when (val vision = visionRepository.analyze(base64, mimeType, userPrompt)) {
            is VisionAnalysisResult.Success -> {
                val contextForAi = "Image summary: ${vision.summary}"
                Result.success(vision.summary)
            }
            is VisionAnalysisResult.Unavailable -> Result.failure(IllegalStateException(vision.reason))
            is VisionAnalysisResult.Error -> Result.failure(IllegalStateException(vision.message))
        }
    }
}
