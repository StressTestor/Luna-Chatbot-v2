package com.luna.chat.di

import android.content.Context
import com.luna.chat.data.repository.*
import com.luna.chat.domain.repository.ChatRepository
import com.luna.chat.domain.repository.VisionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository
    
    @Binds
    @Singleton
    abstract fun bindApiKeyProvider(
        secureApiKeyProvider: SecureApiKeyProvider
    ): ApiKeyProvider

    // Bind VisionRepository to VisionRepositoryImpl
    @Binds
    @Singleton
    abstract fun bindVisionRepository(
        impl: VisionRepositoryImpl
    ): VisionRepository
    
    companion object {
        @Provides
        @Singleton
        fun provideUserPreferencesRepository(
            @ApplicationContext context: Context
        ): UserPreferencesRepository {
            return UserPreferencesRepository(context)
        }
    }
}