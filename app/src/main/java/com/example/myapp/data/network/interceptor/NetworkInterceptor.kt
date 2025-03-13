package com.example.myapp.data.network.interceptor

import com.example.myapp.MyApplication
import com.example.myapp.data.network.api.ApiHeaders
import com.example.myapp.data.network.api.ApiResponseCodes
import com.example.myapp.data.network.api.BaseApi
import com.example.myapp.di.NetworkModule
import com.example.myapp.utils.NetworkUtils
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

class NetworkInterceptor : Interceptor {
    private val sessionManager = MyApplication.getInstance().sessionManager
    private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!NetworkUtils.isNetworkAvailable()) {
            throw NoNetworkException()
        }

        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .addHeader(ApiHeaders.ACCEPT, "application/json")
            .addHeader(ApiHeaders.CONTENT_TYPE, "application/json")

        // Add auth token if available
        sessionManager.token?.let { token ->
            requestBuilder.addHeader(ApiHeaders.AUTHORIZATION, "Bearer $token")
        }

        var response = chain.proceed(requestBuilder.build())

        // Handle 401 (Unauthorized) response
        if (response.code == ApiResponseCodes.UNAUTHORIZED && !isRefreshing) {
            response.close()
            synchronized(this) {
                return handleUnauthorizedResponse(chain, originalRequest)
            }
        }

        return response
    }

    private fun handleUnauthorizedResponse(chain: Interceptor.Chain, originalRequest: Request): Response {
        isRefreshing = true
        
        return try {
            // Try to refresh token
            val newToken = runBlocking {
                val baseApi = NetworkModule.createService(BaseApi::class.java)
                val refreshResponse = baseApi.refreshToken()
                
                if (refreshResponse.isSuccessful && refreshResponse.body()?.status == true) {
                    refreshResponse.body()?.data?.token
                } else {
                    null
                }
            }

            if (newToken != null) {
                // Save new token
                sessionManager.token = newToken

                // Retry original request with new token
                val newRequest = originalRequest.newBuilder()
                    .header(ApiHeaders.AUTHORIZATION, "Bearer $newToken")
                    .build()
                
                chain.proceed(newRequest)
            } else {
                // Token refresh failed, clear session and return 401
                sessionManager.clearSession()
                Response.Builder()
                    .request(originalRequest)
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(ApiResponseCodes.UNAUTHORIZED)
                    .message("Unauthorized")
                    .body(okhttp3.ResponseBody.create(null, ByteArray(0)))
                    .build()
            }
        } catch (e: Exception) {
            // Handle refresh token error
            sessionManager.clearSession()
            throw TokenRefreshException()
        } finally {
            isRefreshing = false
        }
    }

    class NoNetworkException : Exception("No network connection available")
    class TokenRefreshException : Exception("Failed to refresh token")

    companion object {
        private const val TIMEOUT = 30L // seconds
    }
}
