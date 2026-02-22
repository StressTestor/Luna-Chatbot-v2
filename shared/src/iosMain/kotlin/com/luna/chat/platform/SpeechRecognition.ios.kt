package com.luna.chat.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

actual class SpeechRecognition {
    // TODO: Implement using Speech framework (SFSpeechRecognizer)
    actual fun isAvailable(): Boolean = false
    actual fun startListening(): Flow<SpeechResult> = flow { }
    actual fun stopListening() { }
}
