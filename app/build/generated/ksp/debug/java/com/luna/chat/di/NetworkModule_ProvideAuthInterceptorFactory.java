package com.luna.chat.di;

import com.luna.chat.data.repository.ApiKeyProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.Interceptor;

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
public final class NetworkModule_ProvideAuthInterceptorFactory implements Factory<Interceptor> {
  private final Provider<ApiKeyProvider> apiKeyProvider;

  public NetworkModule_ProvideAuthInterceptorFactory(Provider<ApiKeyProvider> apiKeyProvider) {
    this.apiKeyProvider = apiKeyProvider;
  }

  @Override
  public Interceptor get() {
    return provideAuthInterceptor(apiKeyProvider.get());
  }

  public static NetworkModule_ProvideAuthInterceptorFactory create(
      Provider<ApiKeyProvider> apiKeyProvider) {
    return new NetworkModule_ProvideAuthInterceptorFactory(apiKeyProvider);
  }

  public static Interceptor provideAuthInterceptor(ApiKeyProvider apiKeyProvider) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideAuthInterceptor(apiKeyProvider));
  }
}
