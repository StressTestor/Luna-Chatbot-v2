package com.luna.chat.di

import com.luna.chat.data.repository.ApiConnectivityTester
import com.luna.chat.data.repository.ApiKeyProvider
import com.luna.chat.data.repository.ChatRepositoryImpl
import com.luna.chat.data.repository.DefaultApiConnectivityTester
import com.luna.chat.data.repository.DefaultParentAuthService
import com.luna.chat.data.repository.ParentAuthenticationService
import com.luna.chat.data.repository.SecureApiKeyProvider
import com.luna.chat.data.repository.UserPreferencesRepositoryImpl
import com.luna.chat.data.repository.VisionRepositoryImpl
import com.luna.chat.domain.repository.ChatRepository
import com.luna.chat.domain.repository.UserPreferencesRepository
import com.luna.chat.domain.repository.VisionRepository
import com.luna.chat.domain.usecase.ChatHistoryUseCase
import com.luna.chat.domain.usecase.ContentFilterUseCase
import com.luna.chat.domain.usecase.ProcessImageUseCase
import com.luna.chat.domain.usecase.SendMessageUseCase
import com.luna.chat.domain.usecase.ThemeManagementUseCase
import com.luna.chat.presentation.viewmodel.ChatViewModel
import com.luna.chat.presentation.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val commonModule = module {
    // Services
    singleOf(::SecureApiKeyProvider) bind ApiKeyProvider::class
    singleOf(::DefaultParentAuthService) bind ParentAuthenticationService::class
    singleOf(::DefaultApiConnectivityTester) bind ApiConnectivityTester::class

    // Repositories
    singleOf(::ChatRepositoryImpl) bind ChatRepository::class
    singleOf(::VisionRepositoryImpl) bind VisionRepository::class
    singleOf(::UserPreferencesRepositoryImpl) bind UserPreferencesRepository::class

    // Use cases
    singleOf(::ContentFilterUseCase)
    singleOf(::SendMessageUseCase)
    singleOf(::ChatHistoryUseCase)
    singleOf(::ThemeManagementUseCase)
    singleOf(::ProcessImageUseCase)

    // ViewModels
    viewModelOf(::ChatViewModel)
    viewModelOf(::SettingsViewModel)
}
