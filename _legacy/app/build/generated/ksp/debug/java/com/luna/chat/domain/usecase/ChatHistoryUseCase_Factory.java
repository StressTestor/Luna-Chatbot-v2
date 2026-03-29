package com.luna.chat.domain.usecase;

import com.luna.chat.domain.repository.ChatRepository;
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
public final class ChatHistoryUseCase_Factory implements Factory<ChatHistoryUseCase> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  public ChatHistoryUseCase_Factory(Provider<ChatRepository> chatRepositoryProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
  }

  @Override
  public ChatHistoryUseCase get() {
    return newInstance(chatRepositoryProvider.get());
  }

  public static ChatHistoryUseCase_Factory create(Provider<ChatRepository> chatRepositoryProvider) {
    return new ChatHistoryUseCase_Factory(chatRepositoryProvider);
  }

  public static ChatHistoryUseCase newInstance(ChatRepository chatRepository) {
    return new ChatHistoryUseCase(chatRepository);
  }
}
