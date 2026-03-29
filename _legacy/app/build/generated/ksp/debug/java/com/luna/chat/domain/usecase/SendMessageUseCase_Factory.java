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
public final class SendMessageUseCase_Factory implements Factory<SendMessageUseCase> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<ContentFilterUseCase> contentFilterUseCaseProvider;

  public SendMessageUseCase_Factory(Provider<ChatRepository> chatRepositoryProvider,
      Provider<ContentFilterUseCase> contentFilterUseCaseProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.contentFilterUseCaseProvider = contentFilterUseCaseProvider;
  }

  @Override
  public SendMessageUseCase get() {
    return newInstance(chatRepositoryProvider.get(), contentFilterUseCaseProvider.get());
  }

  public static SendMessageUseCase_Factory create(Provider<ChatRepository> chatRepositoryProvider,
      Provider<ContentFilterUseCase> contentFilterUseCaseProvider) {
    return new SendMessageUseCase_Factory(chatRepositoryProvider, contentFilterUseCaseProvider);
  }

  public static SendMessageUseCase newInstance(ChatRepository chatRepository,
      ContentFilterUseCase contentFilterUseCase) {
    return new SendMessageUseCase(chatRepository, contentFilterUseCase);
  }
}
