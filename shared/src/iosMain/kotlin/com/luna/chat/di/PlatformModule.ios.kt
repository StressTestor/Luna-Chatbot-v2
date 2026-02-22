package com.luna.chat.di

import com.luna.chat.data.local.DatabaseDriverFactory
import com.luna.chat.platform.SecureStorage
import com.luna.chat.platform.SpeechRecognition
import com.luna.chat.platform.TextToSpeechEngine
import org.koin.dsl.module

actual val platformModule = module {
    single { DatabaseDriverFactory() }
    single { SecureStorage() }
    single { SpeechRecognition() }
    single { TextToSpeechEngine() }
}
