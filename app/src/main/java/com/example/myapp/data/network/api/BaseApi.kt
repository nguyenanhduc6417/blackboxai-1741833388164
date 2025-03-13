package com.example.myapp.data.network.api

import com.example.myapp.data.network.BaseResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface BaseApi {
    @GET("ping")
    suspend fun ping(): Response<BaseResponse<String>>

    @POST("auth/refresh-token")
    suspend fun refreshToken(): Response<BaseResponse<RefreshTokenResponse>>

    @POST("auth/logout")
    suspend fun logout(): Response<BaseResponse<Unit>>
}

data class RefreshTokenResponse(
    val token: String,
    val expiresIn: Long
)

// Common request/response models
data class PaginationRequest(
    val page: Int = 1,
    val limit: Int = 20
)

data class PaginationResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val hasMore: Boolean
)

// Common error models
data class ValidationError(
    val field: String,
    val message: String
)

// Common API endpoints
object ApiEndpoints {
    // Auth endpoints
    const val LOGIN = "auth/login"
    const val LOGOUT = "auth/logout"
    const val REFRESH_TOKEN = "auth/refresh-token"
    
    // User endpoints
    const val USER_PROFILE = "user/profile"
    const val UPDATE_PROFILE = "user/profile/update"
    
    // Common endpoints
    const val PING = "ping"
    const val UPLOAD = "upload"
}

// Common API headers
object ApiHeaders {
    const val AUTHORIZATION = "Authorization"
    const val ACCEPT = "Accept"
    const val CONTENT_TYPE = "Content-Type"
    const val USER_AGENT = "User-Agent"
}

// Common API response codes
object ApiResponseCodes {
    const val SUCCESS = 200
    const val CREATED = 201
    const val BAD_REQUEST = 400
    const val UNAUTHORIZED = 401
    const val FORBIDDEN = 403
    const val NOT_FOUND = 404
    const val INTERNAL_SERVER_ERROR = 500
}
