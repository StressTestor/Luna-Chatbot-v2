package com.luna.chat.di;

import android.content.Context;
import com.luna.chat.data.local.database.DatabaseEncryptionHelper;
import com.luna.chat.data.local.database.LunaDatabase;
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
public final class DatabaseModule_ProvideDatabaseFactory implements Factory<LunaDatabase> {
  private final Provider<Context> contextProvider;

  private final Provider<DatabaseEncryptionHelper> encryptionHelperProvider;

  public DatabaseModule_ProvideDatabaseFactory(Provider<Context> contextProvider,
      Provider<DatabaseEncryptionHelper> encryptionHelperProvider) {
    this.contextProvider = contextProvider;
    this.encryptionHelperProvider = encryptionHelperProvider;
  }

  @Override
  public LunaDatabase get() {
    return provideDatabase(contextProvider.get(), encryptionHelperProvider.get());
  }

  public static DatabaseModule_ProvideDatabaseFactory create(Provider<Context> contextProvider,
      Provider<DatabaseEncryptionHelper> encryptionHelperProvider) {
    return new DatabaseModule_ProvideDatabaseFactory(contextProvider, encryptionHelperProvider);
  }

  public static LunaDatabase provideDatabase(Context context,
      DatabaseEncryptionHelper encryptionHelper) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDatabase(context, encryptionHelper));
  }
}
