package com.luna.chat.data.network;

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
public final class CertificatePinningManager_Factory implements Factory<CertificatePinningManager> {
  @Override
  public CertificatePinningManager get() {
    return newInstance();
  }

  public static CertificatePinningManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CertificatePinningManager newInstance() {
    return new CertificatePinningManager();
  }

  private static final class InstanceHolder {
    private static final CertificatePinningManager_Factory INSTANCE = new CertificatePinningManager_Factory();
  }
}
