package com.luna.chat.platform

import kotlinx.coroutines.flow.Flow

expect class SpeechRecognition {
    fun isAvailable(): Boolean
    fun startListening(): Flow<SpeechResult>
    fun stopListening()
}

data class SpeechResult(
    val text: String,
    val isFinal: Boolean
)
