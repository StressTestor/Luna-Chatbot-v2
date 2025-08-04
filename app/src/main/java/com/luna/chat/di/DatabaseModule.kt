package com.luna.chat.di

import android.content.Context
import com.luna.chat.data.local.dao.ChatDao
import com.luna.chat.data.local.database.DatabaseEncryptionHelper
import com.luna.chat.data.local.database.LunaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabaseEncryptionHelper(
        @ApplicationContext context: Context
    ): DatabaseEncryptionHelper {
        return DatabaseEncryptionHelper(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        encryptionHelper: DatabaseEncryptionHelper
    ): LunaDatabase {
        return LunaDatabase.getDatabase(context, encryptionHelper)
    }

    @Provides
    fun provideChatDao(database: LunaDatabase): ChatDao {
        return database.chatDao()
    }
}