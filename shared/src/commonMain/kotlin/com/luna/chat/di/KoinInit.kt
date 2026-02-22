package com.luna.chat.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule, networkModule, databaseModule, platformModule)
    }
