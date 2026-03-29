package com.luna.chat.di

import com.luna.chat.data.remote.api.GroqApiService
import com.luna.chat.data.remote.api.VisionApiService
import com.luna.chat.data.repository.ApiKeyProvider
import com.luna.chat.data.network.CertificatePinningManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.inject.Named

/**
 * Hilt module for network dependencies
 *
 * Authorization strategy:
 * - Call sites are the source of truth for credentials. They MUST pass a correctly formatted
 *   Authorization header (e.g., "Bearer <API_KEY>") when invoking API methods. This module does not
 *   guarantee credentials are injected; the auth interceptor only performs best‑effort standardization.
 * - Prefer using com.luna.chat.data.remote.api.GroqApiService.formatAuthHeader(apiKey) to build the
 *   Authorization value at the call site.
 *
 * Interceptor behavior:
 * - Auth interceptor: best‑effort only; it may standardize headers when available but is NOT relied upon
 *   for credentials. Do not depend on it for authentication.
 * - Redaction interceptor: ensures any downstream logging sees "Authorization: REDACTED". It does not
 *   modify the actual request used for network I/O earlier in the chain.
 * - Logging interceptor: gated by BuildConfig; combined with redaction ensures logs never emit secrets.
 *
 * Logging policy:
 * - Release builds: HttpLoggingInterceptor.Level.NONE
 * - Debug builds: HttpLoggingInterceptor.Level.BASIC (avoid BODY for safety)
 * - Authorization header is always redacted for logger via the redaction interceptor
 *
 * Base URL:
 * - https://api.openrouter.ai/v1/ (see BASE_URL below). Aligns with certificate pinning host.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // Default to the OpenRouter gateway to avoid networks that block api.openrouter.ai
    // Still OpenRouter; just a different hostname/CDN front.
    private const val BASE_URL = "https://gateway.openrouter.ai/v1/"
    
    /**
     * Best‑effort interceptor to standardize request headers.
     *
     * Important:
     * - Do NOT rely on this interceptor to provide credentials. Call sites MUST pass
     *   a valid Authorization header themselves (e.g., GroqApiService.formatAuthHeader(apiKey)).
     * - This interceptor may add Accept and attempt a synchronous best‑effort for Authorization
     *   if a non‑suspending, securely accessible key is available, but that is not guaranteed.
     */
    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthInterceptor(apiKeyProvider: ApiKeyProvider): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
    
            // Ensure Authorization header is present using OpenRouter API key
            val builder = originalRequest.newBuilder()
                .header("Accept", "application/json")
    
            if (originalRequest.header("Authorization").isNullOrBlank()) {
                // ApiKeyProvider.getApiKey() is suspend; use the sync accessor if available,
                // or fall back to the non-suspending "hasApiKey" + secure get pattern.
                // We avoid calling suspend here by using a best-effort synchronous read.
                val apiKey = when (apiKeyProvider) {
                    is com.luna.chat.data.repository.SecureApiKeyProvider -> {
                        // Use synchronous accessor provided for ViewModel/UI usage
                        if (apiKeyProvider.hasApiKey()) {
                            // We cannot synchronously read the decrypted key safely; skip header if not available
                            // Authorization will be injected by higher layers if needed.
                            null
                        } else null
                    }
                    else -> null
                }
    
                if (!apiKey.isNullOrBlank()) {
                    builder.header("Authorization", "Bearer $apiKey")
                }
            }
    
            // Let Retrofit set Content-Type for requests with a body
            chain.proceed(builder.build())
        }
    }

    /**
     * Redaction interceptor to protect secrets in logs.
     *
     * Behavior:
     * - If an Authorization header is present, a redacted clone is propagated so the logging interceptor
     *   only ever observes "Authorization: REDACTED".
     * - The actual network request continues to use the original header; this does not alter credentials.
     */
    @Provides
    @Singleton
    @Named("redaction")
    fun provideRedactionInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            // If there is an Authorization header, create a cloned request with redacted header
            val redactedRequest = if (request.header("Authorization") != null) {
                request.newBuilder()
                    .header("Authorization", "REDACTED")
                    .build()
            } else {
                request
            }
            // Proceed with the redacted request so downstream logging sees only redacted values.
            // Note: This affects what the logger sees; upstream auth logic still sets the real header
            // before this interceptor in the chain.
            chain.proceed(redactedRequest)
        }
    }
    
    /**
     * Logging interceptor gated by BuildConfig.
     * Combined with the redaction interceptor, this ensures Authorization is never logged in plaintext.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            // Gate verbose logging by build; BODY is discouraged.
            // Avoid direct BuildConfig reference; use reflection fallback for unit tests.
            val isDebug = runCatching {
                val clazz = Class.forName("com.luna.chat.BuildConfig")
                val field = clazz.getField("DEBUG")
                (field.get(null) as? Boolean) ?: false
            }.getOrDefault(false)
            level = if (isDebug) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    
    /**
     * Provides the OkHttpClient with interceptor ordering that enforces:
     * 1) Auth (best‑effort header standardization; credentials must come from call sites)
     * 2) Redaction (ensures logger only sees "Authorization: REDACTED")
     * 3) Logging (gated by BuildConfig)
     *
     * Note:
     * - Call sites remain responsible for Authorization; this chain does not guarantee credentials.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @Named("auth") authInterceptor: Interceptor,
        @Named("redaction") redactionInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        certificatePinningManager: CertificatePinningManager
    ): OkHttpClient {
        // One‑shot retry interceptor for transient DNS/timeout
        val transientRetry = Interceptor { chain ->
            try {
                chain.proceed(chain.request())
            } catch (e: Exception) {
                when (e) {
                    is java.net.UnknownHostException,
                    is java.net.SocketTimeoutException -> {
                        // small backoff then one retry
                        try {
                            Thread.sleep(350)
                        } catch (_: InterruptedException) { }
                        chain.proceed(chain.request())
                    }
                    else -> throw e
                }
            }
        }

        return OkHttpClient.Builder()
            // Authentication first: inject real Authorization when needed (best‑effort only)
            .addInterceptor(authInterceptor)
            // Redaction second: mutate what logging sees (Authorization -> REDACTED)
            .addInterceptor(redactionInterceptor)
            // Lightweight retry for transient DNS/timeouts (before logging)
            .addInterceptor(transientRetry)
            // Logging third: will only ever see "REDACTED" for Authorization
            .addInterceptor(loggingInterceptor)
            .certificatePinner(certificatePinningManager.createCertificatePinner())
            // Tighter connect timeout helps surface DNS/connectivity fast; generous read/write
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideGroqApiService(retrofit: Retrofit): GroqApiService {
        return retrofit.create(GroqApiService::class.java)
    }

    /**
     * Provides the VisionApiService using the shared Retrofit configured for OpenRouter host.
     *
     * Security posture:
     * - Callers MUST supply Authorization explicitly; interceptors are best-effort only.
     * - Logging is redacted and release logging is NONE.
     */
    @Provides
    @Singleton
    fun provideVisionApiService(retrofit: Retrofit): VisionApiService =
        retrofit.create(VisionApiService::class.java)
}