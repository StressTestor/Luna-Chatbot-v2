package com.luna.chat.di;

import com.luna.chat.data.local.dao.ChatDao;
import com.luna.chat.data.local.database.LunaDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class DatabaseModule_ProvideChatDaoFactory implements Factory<ChatDao> {
  private final Provider<LunaDatabase> databaseProvider;

  public DatabaseModule_ProvideChatDaoFactory(Provider<LunaDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ChatDao get() {
    return provideChatDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideChatDaoFactory create(
      Provider<LunaDatabase> databaseProvider) {
    return new DatabaseModule_ProvideChatDaoFactory(databaseProvider);
  }

  public static ChatDao provideChatDao(LunaDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideChatDao(database));
  }
}
