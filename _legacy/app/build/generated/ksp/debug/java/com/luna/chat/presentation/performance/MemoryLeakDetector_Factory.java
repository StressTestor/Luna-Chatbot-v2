package com.luna.chat.presentation.performance;

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
public final class MemoryLeakDetector_Factory implements Factory<MemoryLeakDetector> {
  private final Provider<Context> contextProvider;

  public MemoryLeakDetector_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MemoryLeakDetector get() {
    return newInstance(contextProvider.get());
  }

  public static MemoryLeakDetector_Factory create(Provider<Context> contextProvider) {
    return new MemoryLeakDetector_Factory(contextProvider);
  }

  public static MemoryLeakDetector newInstance(Context context) {
    return new MemoryLeakDetector(context);
  }
}
