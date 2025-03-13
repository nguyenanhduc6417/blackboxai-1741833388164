package com.example.myapp.data.network

import com.example.myapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    private const val TIMEOUT = 60L

    private val baseUrl: String
        get() = if (BuildConfig.IS_PRODUCTION) {
            BuildConfig.BASE_URL_PROD
        } else {
            BuildConfig.BASE_URL_DEV
        }

    private val okHttpClient = OkHttpClient.Builder().apply {
        if (!BuildConfig.IS_PRODUCTION) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
        addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Accept", "application/json")
                .method(original.method, original.body)
            
            chain.proceed(requestBuilder.build())
        }
        connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        readTimeout(TIMEOUT, TimeUnit.SECONDS)
        writeTimeout(TIMEOUT, TimeUnit.SECONDS)
    }.build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }

    // Function to add auth token
    fun addAuthToken(token: String) {
        okHttpClient.newBuilder().addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Authorization", "Bearer $token")
                .method(original.method, original.body)
            
            chain.proceed(requestBuilder.build())
        }.build()
    }
}
