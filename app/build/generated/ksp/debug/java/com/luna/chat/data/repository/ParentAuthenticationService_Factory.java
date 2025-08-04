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
public final class ParentAuthenticationService_Factory implements Factory<ParentAuthenticationService> {
  private final Provider<Context> contextProvider;

  public ParentAuthenticationService_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ParentAuthenticationService get() {
    return newInstance(contextProvider.get());
  }

  public static ParentAuthenticationService_Factory create(Provider<Context> contextProvider) {
    return new ParentAuthenticationService_Factory(contextProvider);
  }

  public static ParentAuthenticationService newInstance(Context context) {
    return new ParentAuthenticationService(context);
  }
}
