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
public final class AppIntegrityChecker_Factory implements Factory<AppIntegrityChecker> {
  private final Provider<Context> contextProvider;

  public AppIntegrityChecker_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AppIntegrityChecker get() {
    return newInstance(contextProvider.get());
  }

  public static AppIntegrityChecker_Factory create(Provider<Context> contextProvider) {
    return new AppIntegrityChecker_Factory(contextProvider);
  }

  public static AppIntegrityChecker newInstance(Context context) {
    return new AppIntegrityChecker(context);
  }
}
