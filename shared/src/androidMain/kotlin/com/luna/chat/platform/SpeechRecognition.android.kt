package com.luna.chat.platform

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

actual class SpeechRecognition(private val context: Context) {

    private var currentRecognizer: SpeechRecognizer? = null

    actual fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    actual fun startListening(): Flow<SpeechResult> = callbackFlow {
        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            currentRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onEvent(eventType: Int, params: Bundle?) {}

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!partial.isNullOrEmpty()) {
                        trySend(SpeechResult(text = partial[0], isFinal = false))
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        trySend(SpeechResult(text = matches[0], isFinal = true))
                    }
                    close()
                }

                override fun onError(error: Int) {
                    close(SpeechRecognitionException(error))
                }
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            recognizer.startListening(intent)
        }

        awaitClose {
            mainHandler.post {
                currentRecognizer?.stopListening()
                currentRecognizer?.destroy()
                currentRecognizer = null
            }
        }
    }

    actual fun stopListening() {
        Handler(Looper.getMainLooper()).post {
            currentRecognizer?.stopListening()
        }
    }
}

class SpeechRecognitionException(val errorCode: Int) : Exception(
    "Speech recognition error $errorCode: ${describeError(errorCode)}",
)

private fun describeError(code: Int): String = when (code) {
    SpeechRecognizer.ERROR_AUDIO -> "audio recording error"
    SpeechRecognizer.ERROR_CLIENT -> "client-side error"
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "missing RECORD_AUDIO permission"
    SpeechRecognizer.ERROR_NETWORK -> "network error"
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "network timeout"
    SpeechRecognizer.ERROR_NO_MATCH -> "no speech match"
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "recognizer busy"
    SpeechRecognizer.ERROR_SERVER -> "server error"
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "no speech input"
    else -> "unknown"
}
