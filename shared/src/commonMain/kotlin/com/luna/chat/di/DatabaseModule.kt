package com.luna.chat.di

import com.luna.chat.data.local.DatabaseDriverFactory
import com.luna.chat.db.LunaDatabase
import org.koin.dsl.module

val databaseModule = module {
    single { get<DatabaseDriverFactory>().createDriver() }
    single { LunaDatabase(get()) }
}
