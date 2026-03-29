package com.luna.chat;

import com.luna.chat.data.repository.ApiKeyInitializer;
import com.luna.chat.security.AppIntegrityChecker;
import com.luna.chat.security.SecureLogger;
import com.luna.chat.security.SecurityConfig;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class LunaApplication_MembersInjector implements MembersInjector<LunaApplication> {
  private final Provider<ApiKeyInitializer> apiKeyInitializerProvider;

  private final Provider<AppIntegrityChecker> appIntegrityCheckerProvider;

  private final Provider<SecurityConfig> securityConfigProvider;

  private final Provider<SecureLogger> secureLoggerProvider;

  public LunaApplication_MembersInjector(Provider<ApiKeyInitializer> apiKeyInitializerProvider,
      Provider<AppIntegrityChecker> appIntegrityCheckerProvider,
      Provider<SecurityConfig> securityConfigProvider,
      Provider<SecureLogger> secureLoggerProvider) {
    this.apiKeyInitializerProvider = apiKeyInitializerProvider;
    this.appIntegrityCheckerProvider = appIntegrityCheckerProvider;
    this.securityConfigProvider = securityConfigProvider;
    this.secureLoggerProvider = secureLoggerProvider;
  }

  public static MembersInjector<LunaApplication> create(
      Provider<ApiKeyInitializer> apiKeyInitializerProvider,
      Provider<AppIntegrityChecker> appIntegrityCheckerProvider,
      Provider<SecurityConfig> securityConfigProvider,
      Provider<SecureLogger> secureLoggerProvider) {
    return new LunaApplication_MembersInjector(apiKeyInitializerProvider, appIntegrityCheckerProvider, securityConfigProvider, secureLoggerProvider);
  }

  @Override
  public void injectMembers(LunaApplication instance) {
    injectApiKeyInitializer(instance, apiKeyInitializerProvider.get());
    injectAppIntegrityChecker(instance, appIntegrityCheckerProvider.get());
    injectSecurityConfig(instance, securityConfigProvider.get());
    injectSecureLogger(instance, secureLoggerProvider.get());
  }

  @InjectedFieldSignature("com.luna.chat.LunaApplication.apiKeyInitializer")
  public static void injectApiKeyInitializer(LunaApplication instance,
      ApiKeyInitializer apiKeyInitializer) {
    instance.apiKeyInitializer = apiKeyInitializer;
  }

  @InjectedFieldSignature("com.luna.chat.LunaApplication.appIntegrityChecker")
  public static void injectAppIntegrityChecker(LunaApplication instance,
      AppIntegrityChecker appIntegrityChecker) {
    instance.appIntegrityChecker = appIntegrityChecker;
  }

  @InjectedFieldSignature("com.luna.chat.LunaApplication.securityConfig")
  public static void injectSecurityConfig(LunaApplication instance, SecurityConfig securityConfig) {
    instance.securityConfig = securityConfig;
  }

  @InjectedFieldSignature("com.luna.chat.LunaApplication.secureLogger")
  public static void injectSecureLogger(LunaApplication instance, SecureLogger secureLogger) {
    instance.secureLogger = secureLogger;
  }
}
