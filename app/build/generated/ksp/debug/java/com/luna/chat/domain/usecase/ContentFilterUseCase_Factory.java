package com.luna.chat.domain.usecase;

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
public final class ContentFilterUseCase_Factory implements Factory<ContentFilterUseCase> {
  @Override
  public ContentFilterUseCase get() {
    return newInstance();
  }

  public static ContentFilterUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ContentFilterUseCase newInstance() {
    return new ContentFilterUseCase();
  }

  private static final class InstanceHolder {
    private static final ContentFilterUseCase_Factory INSTANCE = new ContentFilterUseCase_Factory();
  }
}
