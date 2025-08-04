package com.luna.chat.data.repository;

import com.luna.chat.data.local.dao.ChatDao;
import com.luna.chat.data.remote.api.GroqApiService;
import com.luna.chat.security.ContentFilteringService;
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
public final class ChatRepositoryImpl_Factory implements Factory<ChatRepositoryImpl> {
  private final Provider<GroqApiService> groqApiServiceProvider;

  private final Provider<ChatDao> chatDaoProvider;

  private final Provider<ApiKeyProvider> apiKeyProvider;

  private final Provider<ContentFilteringService> contentFilteringServiceProvider;

  public ChatRepositoryImpl_Factory(Provider<GroqApiService> groqApiServiceProvider,
      Provider<ChatDao> chatDaoProvider, Provider<ApiKeyProvider> apiKeyProvider,
      Provider<ContentFilteringService> contentFilteringServiceProvider) {
    this.groqApiServiceProvider = groqApiServiceProvider;
    this.chatDaoProvider = chatDaoProvider;
    this.apiKeyProvider = apiKeyProvider;
    this.contentFilteringServiceProvider = contentFilteringServiceProvider;
  }

  @Override
  public ChatRepositoryImpl get() {
    return newInstance(groqApiServiceProvider.get(), chatDaoProvider.get(), apiKeyProvider.get(), contentFilteringServiceProvider.get());
  }

  public static ChatRepositoryImpl_Factory create(Provider<GroqApiService> groqApiServiceProvider,
      Provider<ChatDao> chatDaoProvider, Provider<ApiKeyProvider> apiKeyProvider,
      Provider<ContentFilteringService> contentFilteringServiceProvider) {
    return new ChatRepositoryImpl_Factory(groqApiServiceProvider, chatDaoProvider, apiKeyProvider, contentFilteringServiceProvider);
  }

  public static ChatRepositoryImpl newInstance(GroqApiService groqApiService, ChatDao chatDao,
      ApiKeyProvider apiKeyProvider, ContentFilteringService contentFilteringService) {
    return new ChatRepositoryImpl(groqApiService, chatDao, apiKeyProvider, contentFilteringService);
  }
}
