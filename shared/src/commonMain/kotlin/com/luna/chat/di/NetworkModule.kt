package com.luna.chat.di

import com.luna.chat.data.remote.api.LunaApiClient
import com.luna.chat.data.remote.api.createHttpClient
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient() }
    single { LunaApiClient(get()) }
}
