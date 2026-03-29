package com.luna.chat

import android.app.Application
import android.util.Log
import com.luna.chat.data.repository.ApiKeyProvider
import com.luna.chat.di.initKoin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.java.KoinJavaComponent.get

class LunaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@LunaApplication)
        }
        seedApiKeyIfNeeded()
    }

    private fun seedApiKeyIfNeeded() {
        val builtInKey = BuildConfig.OPENROUTER_API_KEY
        Log.d("Luna", "seedApiKey: builtInKey length=${builtInKey.length}, blank=${builtInKey.isBlank()}")
        if (builtInKey.isBlank()) return

        val provider = get<ApiKeyProvider>(ApiKeyProvider::class.java)
        val hasKey = provider.hasApiKey()
        Log.d("Luna", "seedApiKey: hasKey=$hasKey")
        if (hasKey) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                provider.setApiKey(builtInKey)
                Log.d("Luna", "seedApiKey: key stored successfully")
            } catch (e: Exception) {
                Log.e("Luna", "seedApiKey: FAILED to store key", e)
            }
        }
    }
}
