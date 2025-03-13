package com.example.myapp.data.repository

import com.example.myapp.data.DataState
import com.example.myapp.data.network.api.SplashApi
import com.example.myapp.data.network.api.UserProfile
import com.example.myapp.data.network.api.VersionResponse
import com.example.myapp.di.NetworkModule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SplashRepository @Inject constructor() : BaseRepository() {
    private val splashApi: SplashApi = NetworkModule.createService(SplashApi::class.java)

    suspend fun checkVersion(): Flow<DataState<VersionResponse>> {
        return safeApiCall {
            splashApi.checkVersion()
        }
    }

    suspend fun getProfile(): Flow<DataState<UserProfile>> {
        return safeApiCall {
            splashApi.getProfile()
        }
    }

    companion object {
        @Volatile
        private var instance: SplashRepository? = null

        fun getInstance(): SplashRepository {
            return instance ?: synchronized(this) {
                instance ?: SplashRepository().also { instance = it }
            }
        }
    }
}
