package com.luna.chat.domain.repository

import com.luna.chat.domain.entity.VisionAnalysisResult

// Contract for vision analysis
interface VisionRepository {
    suspend fun analyze(imageBase64: String, mimeType: String, userPrompt: String?): VisionAnalysisResult
}