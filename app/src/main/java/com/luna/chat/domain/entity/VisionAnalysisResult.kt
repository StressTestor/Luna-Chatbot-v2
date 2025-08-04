package com.luna.chat.domain.entity

// Sealed result used by repository and use case
sealed class VisionAnalysisResult {
    data class Success(val summary: String) : VisionAnalysisResult()
    data class Unavailable(val reason: String) : VisionAnalysisResult()
    data class Error(val message: String) : VisionAnalysisResult()
}