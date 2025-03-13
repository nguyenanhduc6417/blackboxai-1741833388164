package com.example.myapp.utils

import com.example.myapp.BuildConfig

object Constants {
    // API Configuration
    object Api {
        // Base URLs
        val BASE_URL = if (BuildConfig.IS_PRODUCTION) {
            BuildConfig.BASE_URL_PROD
        } else {
            BuildConfig.BASE_URL_DEV
        }

        // API Versions
        const val API_VERSION = "v1"
        
        // Timeouts (in seconds)
        const val CONNECT_TIMEOUT = 30L
        const val READ_TIMEOUT = 30L
        const val WRITE_TIMEOUT = 30L

        // Cache Configuration
        const val CACHE_SIZE = 10 * 1024 * 1024L // 10 MB
        const val CACHE_MAX_AGE = 7 * 24 * 60 * 60 // 7 days
        const val CACHE_MAX_STALE = 7 * 24 * 60 * 60 // 7 days
    }

    // Shared Preferences
    object Prefs {
        const val PREF_NAME = "MyAppPrefs"
        const val KEY_TOKEN = "token"
        const val KEY_USER = "user"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_TOKEN_EXPIRY = "token_expiry"
        const val KEY_FIRST_LAUNCH = "first_launch"
    }

    // Database
    object Database {
        const val DB_NAME = "myapp_db"
        const val DB_VERSION = 1
    }

    // Network
    object Network {
        const val RETRY_COUNT = 3
        const val RETRY_DELAY = 1000L // 1 second
    }

    // Date Formats
    object DateFormat {
        const val API_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        const val DISPLAY_DATE_FORMAT = "dd MMM yyyy"
        const val DISPLAY_TIME_FORMAT = "HH:mm"
    }

    // Pagination
    object Pagination {
        const val DEFAULT_PAGE_SIZE = 20
        const val INITIAL_PAGE = 1
    }

    // Validation
    object Validation {
        const val MIN_PASSWORD_LENGTH = 6
        const val MAX_PASSWORD_LENGTH = 20
        const val MIN_USERNAME_LENGTH = 3
        const val MAX_USERNAME_LENGTH = 50
    }

    // File Upload
    object Upload {
        const val MAX_IMAGE_SIZE = 5 * 1024 * 1024 // 5 MB
        const val ALLOWED_IMAGE_TYPES = "image/jpeg,image/png"
        const val IMAGE_QUALITY = 80
    }

    // Error Messages
    object ErrorMessages {
        const val NO_INTERNET = "No internet connection"
        const val SERVER_ERROR = "Server error occurred"
        const val TIMEOUT_ERROR = "Request timed out"
        const val UNKNOWN_ERROR = "Unknown error occurred"
        const val INVALID_CREDENTIALS = "Invalid credentials"
        const val SESSION_EXPIRED = "Session expired"
    }

    // Environment
    object Environment {
        const val DEV = "development"
        const val PROD = "production"

        val CURRENT = if (BuildConfig.IS_PRODUCTION) PROD else DEV

        fun isDev() = CURRENT == DEV
        fun isProd() = CURRENT == PROD
    }
}
