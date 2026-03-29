package com.luna.chat.presentation.accessibility

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for text-to-speech functionality with child-friendly voice settings
 */
@Singleton
class TextToSpeechService @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {
    
    private var textToSpeech: TextToSpeech? = null
    
    private val _isInitialized = mutableStateOf(false)
    val isInitialized: State<Boolean> = _isInitialized
    
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        initializeTextToSpeech()
    }
    
    /**
     * Initialize the TextToSpeech engine
     */
    private fun initializeTextToSpeech() {
        try {
            textToSpeech = TextToSpeech(context, this)
        } catch (e: Exception) {
            _error.value = "Could not initialize text-to-speech: ${e.message}"
        }
    }
    
    /**
     * Called when TextToSpeech initialization is complete
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                // Set language to US English
                val result = tts.setLanguage(Locale.US)
                
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    _error.value = "Language not supported for text-to-speech"
                    return
                }
                
                // Configure child-friendly voice settings
                configureTtsSettings(tts)
                
                // Set up progress listener
                tts.setOnUtteranceProgressListener(createUtteranceProgressListener())
                
                _isInitialized.value = true
                _error.value = null
            }
        } else {
            _error.value = "Text-to-speech initialization failed"
        }
    }
    
    /**
     * Configure TTS settings for child-friendly speech
     */
    private fun configureTtsSettings(tts: TextToSpeech) {
        // Set speech rate (slightly slower for children)
        tts.setSpeechRate(0.8f)
        
        // Set pitch (slightly higher for friendlier sound)
        tts.setPitch(1.1f)
    }
    
    /**
     * Speak the given text with child-friendly settings
     */
    fun speak(text: String, priority: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!_isInitialized.value) {
            _error.value = "Text-to-speech is not ready yet"
            return
        }
        
        textToSpeech?.let { tts ->
            try {
                // Clean up text for better speech
                val cleanText = cleanTextForSpeech(text)
                
                val utteranceId = "luna_speech_${System.currentTimeMillis()}"
                val params = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                }
                
                val result = tts.speak(cleanText, priority, params, utteranceId)
                
                if (result == TextToSpeech.ERROR) {
                    _error.value = "Could not speak the text"
                } else {
                    _isSpeaking.value = true
                    _error.value = null
                }
                
            } catch (e: Exception) {
                _error.value = "Error while speaking: ${e.message}"
            }
        }
    }
    
    /**
     * Speak Luna's message with appropriate introduction
     */
    fun speakLunaMessage(message: String) {
        val lunaMessage = "Luna says: $message"
        speak(lunaMessage)
    }
    
    /**
     * Speak user's message for confirmation
     */
    fun speakUserMessage(message: String) {
        val userMessage = "You said: $message"
        speak(userMessage)
    }
    
    /**
     * Speak error messages in a child-friendly way
     */
    fun speakError(error: String) {
        val friendlyError = "Oops! $error"
        speak(friendlyError, TextToSpeech.QUEUE_ADD)
    }
    
    /**
     * Stop current speech
     */
    fun stop() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }
    
    /**
     * Check if TTS is currently speaking
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking == true
    }
    
    /**
     * Clean text for better speech synthesis
     */
    private fun cleanTextForSpeech(text: String): String {
        return text
            // Replace emojis with words for better pronunciation
            .replace("🌙", "moon")
            .replace("✨", "sparkles")
            .replace("📚", "books")
            .replace("🎨", "art")
            .replace("🎮", "games")
            .replace("🔬", "science")
            .replace("🧮", "math")
            .replace("📖", "reading")
            .replace("🎲", "dice")
            .replace("🌟", "star")
            .replace("💬", "chat")
            .replace("🎤", "microphone")
            .replace("🔄", "refresh")
            .replace("🌐", "internet")
            .replace("⏰", "clock")
            .replace("⏳", "hourglass")
            .replace("🛠️", "tools")
            .replace("🤔", "thinking")
            .replace("😅", "oops")
            .replace("🎉", "celebration")
            // Remove multiple spaces
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    /**
     * Create utterance progress listener for tracking speech state
     */
    private fun createUtteranceProgressListener() = object : UtteranceProgressListener() {
        
        override fun onStart(utteranceId: String?) {
            _isSpeaking.value = true
        }
        
        override fun onDone(utteranceId: String?) {
            _isSpeaking.value = false
        }
        
        override fun onError(utteranceId: String?) {
            _isSpeaking.value = false
            _error.value = "Error occurred while speaking"
        }
        
        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            _isSpeaking.value = false
        }
    }
    
    /**
     * Clear current error
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Clean up resources
     */
    fun destroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _isInitialized.value = false
        _isSpeaking.value = false
    }
}