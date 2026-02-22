package com.luna.chat.platform

expect class TextToSpeechEngine {
    fun isAvailable(): Boolean
    fun speak(text: String)
    fun stop()
    fun shutdown()
}
