package com.jefferson.antenas.di

import com.jefferson.antenas.data.remote.JeffersonApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://jefferson-antenas-server.onrender.com"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            // ✅ OTIMIZADO: Timeouts reduzidos para detecção rápida
            .connectTimeout(10, TimeUnit.SECONDS)  // 10s em vez de 30s
            .readTimeout(10, TimeUnit.SECONDS)     // 10s em vez de 30s
            .writeTimeout(10, TimeUnit.SECONDS)    // Adicionado para upload
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
    fun provideJeffersonApi(retrofit: Retrofit): JeffersonApi {
        return retrofit.create(JeffersonApi::class.java)
    }
}