package com.example.myapp.di

import com.example.myapp.BuildConfig
import com.example.myapp.data.network.interceptor.NetworkInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val TIMEOUT = 60L
    private lateinit var baseUrl: String
    private var retrofit: Retrofit? = null

    fun initialize(baseUrl: String) {
        this.baseUrl = baseUrl
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder().apply {
            // Add network interceptor
            addInterceptor(NetworkInterceptor())
            
            // Add logging interceptor in debug mode
            if (BuildConfig.DEBUG) {
                addInterceptor(loggingInterceptor)
            }

            // Timeouts
            connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            readTimeout(TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(TIMEOUT, TimeUnit.SECONDS)

            // Retry on connection failure
            retryOnConnectionFailure(true)
        }.build()
    }

    @Synchronized
    fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            if (!::baseUrl.isInitialized) {
                throw IllegalStateException("NetworkModule must be initialized with baseUrl first")
            }

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return getRetrofit().create(serviceClass)
    }

    fun clearRetrofit() {
        retrofit = null
    }

    fun getBaseUrl(): String {
        if (!::baseUrl.isInitialized) {
            throw IllegalStateException("NetworkModule must be initialized with baseUrl first")
        }
        return baseUrl
    }

    fun isInitialized(): Boolean {
        return ::baseUrl.isInitialized
    }

    // Environment configuration
    object Environment {
        const val DEVELOPMENT = "development"
        const val PRODUCTION = "production"

        fun getCurrentEnvironment(): String {
            return if (BuildConfig.DEBUG) DEVELOPMENT else PRODUCTION
        }

        fun isDevelopment(): Boolean {
            return getCurrentEnvironment() == DEVELOPMENT
        }

        fun isProduction(): Boolean {
            return getCurrentEnvironment() == PRODUCTION
        }
    }
}
