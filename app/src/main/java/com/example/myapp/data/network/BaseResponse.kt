package com.example.myapp.data.network

import com.google.gson.annotations.SerializedName

open class BaseResponse<T> {
    @SerializedName("status")
    var status: Boolean = false

    @SerializedName("message")
    var message: String? = null

    @SerializedName("data")
    var data: T? = null

    @SerializedName("error")
    var error: ErrorResponse? = null
}

data class ErrorResponse(
    @SerializedName("code")
    val code: Int? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("errors")
    val errors: Map<String, List<String>>? = null
) {
    fun getFirstError(): String {
        return errors?.values?.firstOrNull()?.firstOrNull() 
            ?: message 
            ?: "Unknown error occurred"
    }
}

sealed class ApiException : Exception() {
    data class Network(override val message: String) : ApiException()
    data class NotFound(override val message: String) : ApiException()
    data class AccessDenied(override val message: String) : ApiException()
    data class ServiceUnavailable(override val message: String) : ApiException()
    data class Unknown(override val message: String) : ApiException()
}
