package com.luna.chat.presentation.viewmodel;

import com.luna.chat.data.repository.UserPreferencesRepository;
import com.luna.chat.domain.usecase.ChatHistoryUseCase;
import com.luna.chat.domain.usecase.SendMessageUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<SendMessageUseCase> sendMessageUseCaseProvider;

  private final Provider<ChatHistoryUseCase> chatHistoryUseCaseProvider;

  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  public ChatViewModel_Factory(Provider<SendMessageUseCase> sendMessageUseCaseProvider,
      Provider<ChatHistoryUseCase> chatHistoryUseCaseProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    this.sendMessageUseCaseProvider = sendMessageUseCaseProvider;
    this.chatHistoryUseCaseProvider = chatHistoryUseCaseProvider;
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(sendMessageUseCaseProvider.get(), chatHistoryUseCaseProvider.get(), userPreferencesRepositoryProvider.get());
  }

  public static ChatViewModel_Factory create(
      Provider<SendMessageUseCase> sendMessageUseCaseProvider,
      Provider<ChatHistoryUseCase> chatHistoryUseCaseProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    return new ChatViewModel_Factory(sendMessageUseCaseProvider, chatHistoryUseCaseProvider, userPreferencesRepositoryProvider);
  }

  public static ChatViewModel newInstance(SendMessageUseCase sendMessageUseCase,
      ChatHistoryUseCase chatHistoryUseCase, UserPreferencesRepository userPreferencesRepository) {
    return new ChatViewModel(sendMessageUseCase, chatHistoryUseCase, userPreferencesRepository);
  }
}
