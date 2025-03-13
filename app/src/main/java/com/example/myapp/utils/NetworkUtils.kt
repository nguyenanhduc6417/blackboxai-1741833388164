package com.example.myapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.myapp.MyApplication
import com.example.myapp.data.network.BaseResponse
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException

object NetworkUtils {
    private val gson = Gson()

    fun isNetworkAvailable(): Boolean {
        val context = MyApplication.getInstance()
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun <T> parseErrorResponse(errorBody: ResponseBody?): BaseResponse<T>? {
        return try {
            errorBody?.string()?.let { errorString ->
                gson.fromJson(errorString, BaseResponse::class.java) as BaseResponse<T>
            }
        } catch (e: Exception) {
            null
        }
    }

    fun <T> handleApiResponse(response: Response<BaseResponse<T>>): Result<T> {
        return try {
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status) {
                    body.data?.let {
                        Result.success(it)
                    } ?: Result.failure(IOException("Response body is null"))
                } else {
                    val errorMessage = body?.error?.getFirstError() 
                        ?: body?.message 
                        ?: "Unknown error occurred"
                    Result.failure(IOException(errorMessage))
                }
            } else {
                val errorBody = parseErrorResponse<T>(response.errorBody())
                val errorMessage = when (response.code()) {
                    401 -> "Unauthorized access"
                    403 -> "Access forbidden"
                    404 -> "Resource not found"
                    500 -> "Internal server error"
                    else -> errorBody?.error?.getFirstError() 
                        ?: errorBody?.message 
                        ?: "Unknown error occurred"
                }
                Result.failure(IOException(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is IOException -> "Network error occurred"
            is retrofit2.HttpException -> {
                when (throwable.code()) {
                    401 -> "Unauthorized access"
                    403 -> "Access forbidden"
                    404 -> "Resource not found"
                    500 -> "Internal server error"
                    else -> "Unknown error occurred"
                }
            }
            else -> throwable.message ?: "Unknown error occurred"
        }
    }
}
