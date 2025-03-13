package com.example.myapp

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import com.example.myapp.data.local.PreferenceManager
import com.example.myapp.data.local.SessionManager
import com.example.myapp.di.NetworkModule
import com.example.myapp.utils.ConnectivityManager
import com.example.myapp.utils.Constants
import com.example.myapp.utils.ResourceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyApplication : Application() {
    
    // Application scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Managers
    lateinit var sessionManager: SessionManager
        private set
        
    lateinit var preferenceManager: PreferenceManager
        private set
        
    lateinit var resourceProvider: ResourceProvider
        private set
        
    lateinit var connectivityManager: ConnectivityManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        initializeManagers()
        initializeNetworkModule()
        setupTheme()
        observeNetworkState()
    }

    private fun initializeManagers() {
        // Initialize managers
        sessionManager = SessionManager.getInstance(this)
        preferenceManager = PreferenceManager.getInstance(this)
        resourceProvider = ResourceProvider.getInstance(this)
        connectivityManager = ConnectivityManager.getInstance(this)
    }

    private fun initializeNetworkModule() {
        // Initialize network module with appropriate base URL based on build type
        NetworkModule.initialize(
            baseUrl = if (BuildConfig.DEBUG) {
                BuildConfig.BASE_URL_DEV
            } else {
                BuildConfig.BASE_URL_PROD
            }
        )
    }

    private fun setupTheme() {
        // Set app theme based on preferences
        val nightMode = if (preferenceManager.isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun observeNetworkState() {
        applicationScope.launch {
            connectivityManager.observeNetworkState().collect { state ->
                when (state) {
                    is ConnectivityManager.NetworkState.Available -> {
                        // Network became available
                        handleNetworkAvailable()
                    }
                    is ConnectivityManager.NetworkState.Lost,
                    is ConnectivityManager.NetworkState.Unavailable -> {
                        // Network became unavailable
                        handleNetworkLost()
                    }
                    is ConnectivityManager.NetworkState.Losing -> {
                        // Network is about to be lost
                        handleNetworkLosing()
                    }
                }
            }
        }
    }

    private fun handleNetworkAvailable() {
        // Handle network available state
        // For example, retry failed requests, sync data, etc.
    }

    private fun handleNetworkLost() {
        // Handle network lost state
        // For example, show offline mode UI, pause background tasks, etc.
    }

    private fun handleNetworkLosing() {
        // Handle network losing state
        // For example, save important data, pause uploads, etc.
    }

    fun restartApp(context: Context) {
        // Helper method to restart the app (useful after language change, etc.)
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }

    companion object {
        private lateinit var instance: MyApplication

        fun getInstance(): MyApplication {
            if (!::instance.isInitialized) {
                throw IllegalStateException("Application not initialized")
            }
            return instance
        }

        fun getContext(): Context = instance.applicationContext

        // Environment helpers
        fun isDebug() = BuildConfig.DEBUG
        fun isProduction() = !BuildConfig.DEBUG
        fun getCurrentEnvironment() = if (isDebug()) {
            Constants.Environment.DEV
        } else {
            Constants.Environment.PROD
        }

        // Network helpers
        fun isNetworkAvailable() = getInstance().connectivityManager.isNetworkAvailable
        fun getNetworkType() = getInstance().connectivityManager.getNetworkType()
    }
}
