package com.luna.chat.di

import com.luna.chat.data.remote.api.GroqApiService
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

/**
 * Hilt module for network dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL = "https://api.groq.com/openai/v1/"
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(apiKeyProvider: ApiKeyProvider): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            
            // Check if Authorization header is already present (for dynamic API key injection)
            val existingAuth = originalRequest.header("Authorization")
            
            val newRequest = if (existingAuth == null) {
                // Only add default auth if no Authorization header is present
                originalRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .build()
            } else {
                originalRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .build()
            }
            
            chain.proceed(newRequest)
        }
    }
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        certificatePinningManager: CertificatePinningManager
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .certificatePinner(certificatePinningManager.createCertificatePinner())
            .connectTimeout(30, TimeUnit.SECONDS)
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
}