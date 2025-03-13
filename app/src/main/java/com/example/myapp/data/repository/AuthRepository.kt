package com.example.myapp.data.repository

import com.example.myapp.MyApplication
import com.example.myapp.data.DataState
import com.example.myapp.data.local.PreferenceManager
import com.example.myapp.data.local.SessionManager
import com.example.myapp.data.network.api.AuthApi
import com.example.myapp.data.network.api.LoginRequest
import com.example.myapp.data.network.api.LoginResponse
import com.example.myapp.data.network.api.User
import com.example.myapp.di.NetworkModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() : BaseRepository() {
    private val authApi: AuthApi = NetworkModule.createService(AuthApi::class.java)
    private val sessionManager: SessionManager = MyApplication.getInstance().sessionManager
    private val preferenceManager: PreferenceManager = MyApplication.getInstance().preferenceManager

    suspend fun login(email: String, password: String): Flow<DataState<LoginResponse>> {
        return safeApiCall {
            authApi.login(LoginRequest(email, password))
        }.onEach { dataState ->
            when (dataState) {
                is DataState.Success -> {
                    // Save session data
                    sessionManager.saveSession(
                        token = dataState.data.token,
                        user = dataState.data.user
                    )
                    // Update last login time
                    preferenceManager.lastSyncTime = System.currentTimeMillis()
                }
                is DataState.Error -> {
                    // Clear any existing session on error
                    sessionManager.clearSession()
                }
                else -> { /* no-op */ }
            }
        }
    }

    suspend fun refreshToken(): Flow<DataState<String>> {
        return safeApiCall {
            authApi.refreshToken()
        }.onEach { dataState ->
            when (dataState) {
                is DataState.Success -> {
                    sessionManager.token = dataState.data
                }
                is DataState.Error -> {
                    // Token refresh failed, clear session
                    sessionManager.clearSession()
                }
                else -> { /* no-op */ }
            }
        }
    }

    suspend fun logout(): Flow<DataState<Unit>> {
        return safeApiCall {
            authApi.logout()
        }.onEach { dataState ->
            when (dataState) {
                is DataState.Success, is DataState.Error -> {
                    // Clear session regardless of success or failure
                    sessionManager.clearSession()
                    NetworkModule.clearRetrofit()
                }
                else -> { /* no-op */ }
            }
        }
    }

    suspend fun updateProfile(user: User): Flow<DataState<User>> {
        return safeApiCall {
            authApi.updateProfile(user)
        }.onEach { dataState ->
            when (dataState) {
                is DataState.Success -> {
                    // Update stored user data
                    sessionManager.user = dataState.data
                }
                else -> { /* no-op */ }
            }
        }
    }

    // Session management helpers
    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn
    fun getToken(): String? = sessionManager.token
    fun getUser(): User? = sessionManager.user
    
    fun clearSession() {
        sessionManager.clearSession()
        NetworkModule.clearRetrofit()
    }

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository().also { instance = it }
            }
        }
    }
}
