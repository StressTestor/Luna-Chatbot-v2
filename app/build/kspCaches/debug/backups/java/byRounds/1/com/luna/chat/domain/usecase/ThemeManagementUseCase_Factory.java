package com.luna.chat.domain.usecase;

import com.luna.chat.data.repository.UserPreferencesRepository;
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
public final class ThemeManagementUseCase_Factory implements Factory<ThemeManagementUseCase> {
  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  public ThemeManagementUseCase_Factory(
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
  }

  @Override
  public ThemeManagementUseCase get() {
    return newInstance(userPreferencesRepositoryProvider.get());
  }

  public static ThemeManagementUseCase_Factory create(
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    return new ThemeManagementUseCase_Factory(userPreferencesRepositoryProvider);
  }

  public static ThemeManagementUseCase newInstance(
      UserPreferencesRepository userPreferencesRepository) {
    return new ThemeManagementUseCase(userPreferencesRepository);
  }
}
