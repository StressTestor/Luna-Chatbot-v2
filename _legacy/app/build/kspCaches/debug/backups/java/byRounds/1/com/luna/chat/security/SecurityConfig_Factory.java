package com.luna.chat.security;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SecurityConfig_Factory implements Factory<SecurityConfig> {
  private final Provider<Context> contextProvider;

  public SecurityConfig_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SecurityConfig get() {
    return newInstance(contextProvider.get());
  }

  public static SecurityConfig_Factory create(Provider<Context> contextProvider) {
    return new SecurityConfig_Factory(contextProvider);
  }

  public static SecurityConfig newInstance(Context context) {
    return new SecurityConfig(context);
  }
}
