package com.luna.chat.di;

import android.content.Context;
import com.luna.chat.data.local.database.DatabaseEncryptionHelper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideDatabaseEncryptionHelperFactory implements Factory<DatabaseEncryptionHelper> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideDatabaseEncryptionHelperFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DatabaseEncryptionHelper get() {
    return provideDatabaseEncryptionHelper(contextProvider.get());
  }

  public static DatabaseModule_ProvideDatabaseEncryptionHelperFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideDatabaseEncryptionHelperFactory(contextProvider);
  }

  public static DatabaseEncryptionHelper provideDatabaseEncryptionHelper(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDatabaseEncryptionHelper(context));
  }
}
