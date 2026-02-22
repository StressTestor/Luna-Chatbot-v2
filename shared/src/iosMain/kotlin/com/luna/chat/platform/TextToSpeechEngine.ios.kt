package com.luna.chat.platform

actual class TextToSpeechEngine {
    // TODO: Implement using AVSpeechSynthesizer
    actual fun isAvailable(): Boolean = false
    actual fun speak(text: String) { }
    actual fun stop() { }
    actual fun shutdown() { }
}
