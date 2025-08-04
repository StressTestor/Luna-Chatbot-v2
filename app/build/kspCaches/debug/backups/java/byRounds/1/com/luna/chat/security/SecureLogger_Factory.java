package com.luna.chat.security;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class SecureLogger_Factory implements Factory<SecureLogger> {
  @Override
  public SecureLogger get() {
    return newInstance();
  }

  public static SecureLogger_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SecureLogger newInstance() {
    return new SecureLogger();
  }

  private static final class InstanceHolder {
    private static final SecureLogger_Factory INSTANCE = new SecureLogger_Factory();
  }
}
