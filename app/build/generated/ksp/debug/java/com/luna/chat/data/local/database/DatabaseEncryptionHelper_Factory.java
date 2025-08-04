package com.luna.chat.data.local.database;

import android.content.Context;
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
public final class DatabaseEncryptionHelper_Factory implements Factory<DatabaseEncryptionHelper> {
  private final Provider<Context> contextProvider;

  public DatabaseEncryptionHelper_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DatabaseEncryptionHelper get() {
    return newInstance(contextProvider.get());
  }

  public static DatabaseEncryptionHelper_Factory create(Provider<Context> contextProvider) {
    return new DatabaseEncryptionHelper_Factory(contextProvider);
  }

  public static DatabaseEncryptionHelper newInstance(Context context) {
    return new DatabaseEncryptionHelper(context);
  }
}
