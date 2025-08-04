package com.luna.chat.presentation.viewmodel;

import com.luna.chat.data.remote.api.ApiConnectivityTester;
import com.luna.chat.data.repository.ParentAuthenticationService;
import com.luna.chat.data.repository.SecureApiKeyProvider;
import com.luna.chat.data.repository.UserPreferencesRepository;
import com.luna.chat.domain.usecase.ChatHistoryUseCase;
import com.luna.chat.domain.usecase.ThemeManagementUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<ThemeManagementUseCase> themeManagementUseCaseProvider;

  private final Provider<ChatHistoryUseCase> chatHistoryUseCaseProvider;

  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  private final Provider<SecureApiKeyProvider> secureApiKeyProvider;

  private final Provider<ParentAuthenticationService> parentAuthenticationServiceProvider;

  private final Provider<ApiConnectivityTester> apiConnectivityTesterProvider;

  public SettingsViewModel_Factory(Provider<ThemeManagementUseCase> themeManagementUseCaseProvider,
      Provider<ChatHistoryUseCase> chatHistoryUseCaseProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider,
      Provider<SecureApiKeyProvider> secureApiKeyProvider,
      Provider<ParentAuthenticationService> parentAuthenticationServiceProvider,
      Provider<ApiConnectivityTester> apiConnectivityTesterProvider) {
    this.themeManagementUseCaseProvider = themeManagementUseCaseProvider;
    this.chatHistoryUseCaseProvider = chatHistoryUseCaseProvider;
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
    this.secureApiKeyProvider = secureApiKeyProvider;
    this.parentAuthenticationServiceProvider = parentAuthenticationServiceProvider;
    this.apiConnectivityTesterProvider = apiConnectivityTesterProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(themeManagementUseCaseProvider.get(), chatHistoryUseCaseProvider.get(), userPreferencesRepositoryProvider.get(), secureApiKeyProvider.get(), parentAuthenticationServiceProvider.get(), apiConnectivityTesterProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<ThemeManagementUseCase> themeManagementUseCaseProvider,
      Provider<ChatHistoryUseCase> chatHistoryUseCaseProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider,
      Provider<SecureApiKeyProvider> secureApiKeyProvider,
      Provider<ParentAuthenticationService> parentAuthenticationServiceProvider,
      Provider<ApiConnectivityTester> apiConnectivityTesterProvider) {
    return new SettingsViewModel_Factory(themeManagementUseCaseProvider, chatHistoryUseCaseProvider, userPreferencesRepositoryProvider, secureApiKeyProvider, parentAuthenticationServiceProvider, apiConnectivityTesterProvider);
  }

  public static SettingsViewModel newInstance(ThemeManagementUseCase themeManagementUseCase,
      ChatHistoryUseCase chatHistoryUseCase, UserPreferencesRepository userPreferencesRepository,
      SecureApiKeyProvider secureApiKeyProvider,
      ParentAuthenticationService parentAuthenticationService,
      ApiConnectivityTester apiConnectivityTester) {
    return new SettingsViewModel(themeManagementUseCase, chatHistoryUseCase, userPreferencesRepository, secureApiKeyProvider, parentAuthenticationService, apiConnectivityTester);
  }
}
