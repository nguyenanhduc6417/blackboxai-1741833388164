package com.example.myapp.data.repository

import com.example.myapp.data.DataState
import com.example.myapp.data.network.ApiException
import com.example.myapp.data.network.BaseResponse
import com.example.myapp.data.network.NetworkBoundResource
import com.example.myapp.data.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

abstract class BaseRepository {

    protected suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<BaseResponse<T>>
    ): Flow<DataState<T>> = flow {
        emit(DataState.loading())
        
        try {
            val response = apiCall()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                if (body.status) {
                    body.data?.let {
                        emit(DataState.success(it))
                    } ?: emit(DataState.empty())
                } else {
                    val errorMessage = body.error?.getFirstError() ?: body.message ?: "Unknown error occurred"
                    emit(DataState.error(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = when (response.code()) {
                    401 -> throw ApiException.AccessDenied("Unauthorized access")
                    403 -> throw ApiException.AccessDenied("Access forbidden")
                    404 -> throw ApiException.NotFound("Resource not found")
                    500 -> "Internal server error"
                    else -> errorBody ?: "Unknown error occurred"
                }
                emit(DataState.error(errorMessage, response.code()))
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is IOException -> "No internet connection"
                is SocketTimeoutException -> "Connection timeout"
                is HttpException -> {
                    when (e.code()) {
                        401 -> throw ApiException.AccessDenied("Unauthorized access")
                        403 -> throw ApiException.AccessDenied("Access forbidden")
                        404 -> throw ApiException.NotFound("Resource not found")
                        else -> "Server error occurred"
                    }
                }
                else -> e.localizedMessage ?: "Unknown error occurred"
            }
            emit(DataState.error(errorMessage, exception = e))
        }
    }.flowOn(Dispatchers.IO)

    protected fun <ResultType, RequestType> networkBoundResource(
        fetchFromLocal: () -> Flow<ResultType>,
        fetchFromRemote: suspend () -> RequestType,
        saveRemoteData: suspend (RequestType) -> Unit,
        processRemoteData: (suspend (RequestType) -> ResultType)? = null,
        shouldFetch: (ResultType) -> Boolean = { true }
    ) = object : NetworkBoundResource<ResultType, RequestType>() {
        
        override fun loadFromDb(): Flow<ResultType> = fetchFromLocal()

        override suspend fun createNetworkRequest(): RequestType = fetchFromRemote()

        override suspend fun saveNetworkResult(data: ResultType) {
            // If we have remote data processing, we save the original response
            if (processRemoteData != null) {
                saveRemoteData(createNetworkRequest())
            } else {
                // Otherwise, we're saving the processed data
                @Suppress("UNCHECKED_CAST")
                saveRemoteData(data as RequestType)
            }
        }

        override suspend fun processResponse(response: RequestType): ResultType {
            return processRemoteData?.invoke(response) ?: run {
                @Suppress("UNCHECKED_CAST")
                response as ResultType
            }
        }

        override fun shouldFetch(data: ResultType): Boolean = shouldFetch(data)
    }.asFlow().map { result ->
        when (result) {
            is NetworkResult.Success -> DataState.success(result.data)
            is NetworkResult.Error -> DataState.error(result.message, result.code)
            is NetworkResult.Loading -> DataState.loading()
        }
    }.catch { e ->
        emit(DataState.error(e.message ?: "Unknown error occurred", exception = e))
    }.flowOn(Dispatchers.IO)

    protected fun <T> networkOnly(
        apiCall: suspend () -> T
    ): Flow<DataState<T>> = NetworkBoundResource.networkOnly(apiCall)
        .map { DataState.fromNetworkResult(it) }
        .flowOn(Dispatchers.IO)

    protected fun <T> dbOnly(
        dbQuery: () -> Flow<T>
    ): Flow<DataState<T>> = NetworkBoundResource.dbOnly(dbQuery)
        .map { DataState.fromNetworkResult(it) }
        .flowOn(Dispatchers.IO)
}
