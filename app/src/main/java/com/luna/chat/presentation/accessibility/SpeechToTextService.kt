package com.luna.chat.presentation.accessibility

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling speech-to-text functionality with child-friendly features
 */
@Singleton
class SpeechToTextService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _speechResult = MutableStateFlow<String?>(null)
    val speechResult: StateFlow<String?> = _speechResult.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isAvailable = mutableStateOf(false)
    val isAvailable: State<Boolean> = _isAvailable
    
    init {
        checkAvailability()
    }
    
    /**
     * Check if speech recognition is available on the device
     */
    private fun checkAvailability() {
        _isAvailable.value = SpeechRecognizer.isRecognitionAvailable(context)
    }
    
    /**
     * Start listening for speech input
     */
    fun startListening() {
        if (!_isAvailable.value) {
            _error.value = "Speech recognition is not available on this device"
            return
        }
        
        if (_isListening.value) {
            return // Already listening
        }
        
        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell Luna what you want to say!")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                // Child-friendly settings
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
            }
            
            speechRecognizer?.setRecognitionListener(createRecognitionListener())
            speechRecognizer?.startListening(intent)
            
            _isListening.value = true
            _error.value = null
            _speechResult.value = null
            
        } catch (e: Exception) {
            _error.value = "Could not start voice input: ${e.message}"
            _isListening.value = false
        }
    }
    
    /**
     * Stop listening for speech input
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }
    
    /**
     * Cancel speech recognition
     */
    fun cancelListening() {
        speechRecognizer?.cancel()
        _isListening.value = false
        _speechResult.value = null
    }
    
    /**
     * Clear the current speech result
     */
    fun clearResult() {
        _speechResult.value = null
    }
    
    /**
     * Clear the current error
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Create recognition listener with child-friendly error handling
     */
    private fun createRecognitionListener() = object : RecognitionListener {
        
        override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
            _error.value = null
        }
        
        override fun onBeginningOfSpeech() {
            // Speech input detected
        }
        
        override fun onRmsChanged(rmsdB: Float) {
            // Audio level changed - could be used for visual feedback
        }
        
        override fun onBufferReceived(buffer: ByteArray?) {
            // Audio buffer received
        }
        
        override fun onEndOfSpeech() {
            _isListening.value = false
        }
        
        override fun onError(error: Int) {
            _isListening.value = false
            
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Couldn't hear you clearly. Try speaking a bit louder! 🎤"
                SpeechRecognizer.ERROR_CLIENT -> "Something went wrong. Let's try again! 🔄"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Luna needs permission to listen. Ask a grown-up to help! 🔒"
                SpeechRecognizer.ERROR_NETWORK -> "Check your internet connection and try again! 🌐"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "The connection was slow. Let's try again! ⏰"
                SpeechRecognizer.ERROR_NO_MATCH -> "I didn't catch that. Try speaking clearly! 💬"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice input is busy. Wait a moment and try again! ⏳"
                SpeechRecognizer.ERROR_SERVER -> "The voice service is having trouble. Try again later! 🛠️"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't hear anything. Try speaking into the microphone! 🎙️"
                else -> "Something unexpected happened. Let's try again! 🤔"
            }
            
            _error.value = errorMessage
        }
        
        override fun onResults(results: Bundle?) {
            _isListening.value = false
            
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val recognizedText = matches[0]
                if (recognizedText.isNotBlank()) {
                    _speechResult.value = recognizedText
                } else {
                    _error.value = "I didn't hear anything. Try speaking clearly! 💬"
                }
            } else {
                _error.value = "I didn't catch that. Try speaking again! 🎤"
            }
        }
        
        override fun onPartialResults(partialResults: Bundle?) {
            // Handle partial results for real-time feedback if needed
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                // Could show partial results in UI for better feedback
            }
        }
        
        override fun onEvent(eventType: Int, params: Bundle?) {
            // Handle other events if needed
        }
    }
    
    /**
     * Clean up resources
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        _isListening.value = false
    }
}