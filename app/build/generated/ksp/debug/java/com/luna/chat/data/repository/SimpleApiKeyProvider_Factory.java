package com.luna.chat.data.repository;

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
public final class SimpleApiKeyProvider_Factory implements Factory<SimpleApiKeyProvider> {
  private final Provider<Context> contextProvider;

  public SimpleApiKeyProvider_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SimpleApiKeyProvider get() {
    return newInstance(contextProvider.get());
  }

  public static SimpleApiKeyProvider_Factory create(Provider<Context> contextProvider) {
    return new SimpleApiKeyProvider_Factory(contextProvider);
  }

  public static SimpleApiKeyProvider newInstance(Context context) {
    return new SimpleApiKeyProvider(context);
  }
}
