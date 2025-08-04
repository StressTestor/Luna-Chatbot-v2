package com.luna.chat.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ApiKeyInitializer_Factory implements Factory<ApiKeyInitializer> {
  private final Provider<ApiKeyProvider> apiKeyProvider;

  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  public ApiKeyInitializer_Factory(Provider<ApiKeyProvider> apiKeyProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    this.apiKeyProvider = apiKeyProvider;
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
  }

  @Override
  public ApiKeyInitializer get() {
    return newInstance(apiKeyProvider.get(), userPreferencesRepositoryProvider.get());
  }

  public static ApiKeyInitializer_Factory create(Provider<ApiKeyProvider> apiKeyProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    return new ApiKeyInitializer_Factory(apiKeyProvider, userPreferencesRepositoryProvider);
  }

  public static ApiKeyInitializer newInstance(ApiKeyProvider apiKeyProvider,
      UserPreferencesRepository userPreferencesRepository) {
    return new ApiKeyInitializer(apiKeyProvider, userPreferencesRepository);
  }
}
