package com.luna.chat.data.remote.api;

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
public final class ApiConnectivityTester_Factory implements Factory<ApiConnectivityTester> {
  private final Provider<GroqApiService> groqApiServiceProvider;

  public ApiConnectivityTester_Factory(Provider<GroqApiService> groqApiServiceProvider) {
    this.groqApiServiceProvider = groqApiServiceProvider;
  }

  @Override
  public ApiConnectivityTester get() {
    return newInstance(groqApiServiceProvider.get());
  }

  public static ApiConnectivityTester_Factory create(
      Provider<GroqApiService> groqApiServiceProvider) {
    return new ApiConnectivityTester_Factory(groqApiServiceProvider);
  }

  public static ApiConnectivityTester newInstance(GroqApiService groqApiService) {
    return new ApiConnectivityTester(groqApiService);
  }
}
