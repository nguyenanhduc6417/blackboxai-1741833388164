package com.example.myapp.data.network.api

import com.example.myapp.data.network.BaseResponse
import retrofit2.Response
import retrofit2.http.GET

interface SplashApi : BaseApi {
    @GET("version")
    suspend fun checkVersion(): Response<BaseResponse<VersionResponse>>

    @GET("profile")
    suspend fun getProfile(): Response<BaseResponse<UserProfile>>
}

data class VersionResponse(
    val currentVersion: String,
    val minVersion: String,
    val forceUpdate: Boolean,
    val updateMessage: String? = null,
    val storeUrl: String? = null
)

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val avatar: String? = null,
    val phone: String? = null,
    // Add other user profile fields as needed
    val createdAt: String,
    val updatedAt: String
)
