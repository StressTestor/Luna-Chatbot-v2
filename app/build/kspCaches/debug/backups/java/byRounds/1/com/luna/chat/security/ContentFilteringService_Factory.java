package com.luna.chat.security;

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
public final class ContentFilteringService_Factory implements Factory<ContentFilteringService> {
  private final Provider<SecurityConfig> securityConfigProvider;

  private final Provider<SecureLogger> secureLoggerProvider;

  public ContentFilteringService_Factory(Provider<SecurityConfig> securityConfigProvider,
      Provider<SecureLogger> secureLoggerProvider) {
    this.securityConfigProvider = securityConfigProvider;
    this.secureLoggerProvider = secureLoggerProvider;
  }

  @Override
  public ContentFilteringService get() {
    return newInstance(securityConfigProvider.get(), secureLoggerProvider.get());
  }

  public static ContentFilteringService_Factory create(
      Provider<SecurityConfig> securityConfigProvider,
      Provider<SecureLogger> secureLoggerProvider) {
    return new ContentFilteringService_Factory(securityConfigProvider, secureLoggerProvider);
  }

  public static ContentFilteringService newInstance(SecurityConfig securityConfig,
      SecureLogger secureLogger) {
    return new ContentFilteringService(securityConfig, secureLogger);
  }
}
