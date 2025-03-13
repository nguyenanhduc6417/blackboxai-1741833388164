package com.example.myapp.data

sealed class DataState<out T> {
    data class Success<out T>(val data: T) : DataState<T>()
    data class Error(
        val message: String,
        val code: Int? = null,
        val exception: Throwable? = null
    ) : DataState<Nothing>()
    object Loading : DataState<Nothing>()
    object Empty : DataState<Nothing>()

    companion object {
        fun <T> success(data: T) = Success(data)
        fun error(message: String, code: Int? = null, exception: Throwable? = null) = 
            Error(message, code, exception)
        fun loading() = Loading
        fun empty() = Empty

        fun <T> fromNetworkResult(result: NetworkResult<T>): DataState<T> {
            return when (result) {
                is NetworkResult.Success -> success(result.data)
                is NetworkResult.Error -> error(result.message, result.code)
                is NetworkResult.Loading -> loading()
            }
        }
    }

    /**
     * Helper methods to handle state
     */
    inline fun onSuccess(action: (T) -> Unit): DataState<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (Error) -> Unit): DataState<T> {
        if (this is Error) action(this)
        return this
    }

    inline fun onLoading(action: () -> Unit): DataState<T> {
        if (this is Loading) action()
        return this
    }

    inline fun onEmpty(action: () -> Unit): DataState<T> {
        if (this is Empty) action()
        return this
    }

    /**
     * Transform data if state is Success
     */
    fun <R> map(transform: (T) -> R): DataState<R> {
        return when (this) {
            is Success -> success(transform(data))
            is Error -> error(message, code, exception)
            is Loading -> loading()
            is Empty -> empty()
        }
    }

    /**
     * Get data or null
     */
    fun getDataOrNull(): T? {
        return when (this) {
            is Success -> data
            else -> null
        }
    }

    /**
     * Get data or throw error
     */
    fun getDataOrThrow(): T {
        return when (this) {
            is Success -> data
            is Error -> throw exception ?: Exception(message)
            is Loading -> throw IllegalStateException("Data is loading")
            is Empty -> throw IllegalStateException("Data is empty")
        }
    }

    /**
     * Check states
     */
    fun isSuccess() = this is Success
    fun isError() = this is Error
    fun isLoading() = this is Loading
    fun isEmpty() = this is Empty

    /**
     * Combine with another DataState
     */
    fun <R> combine(other: DataState<R>): DataState<Pair<T, R>> {
        return when {
            this is Success && other is Success -> success(Pair(this.data, other.data))
            this is Error -> error(this.message, this.code, this.exception)
            other is Error -> error(other.message, other.code, other.exception)
            this is Loading || other is Loading -> loading()
            else -> empty()
        }
    }
}

/**
 * Extension functions for Flow<DataState>
 */
fun <T> Flow<DataState<T>>.onSuccessFlow(action: suspend (T) -> Unit): Flow<DataState<T>> =
    transform { value ->
        if (value is DataState.Success) {
            action(value.data)
        }
        emit(value)
    }

fun <T> Flow<DataState<T>>.onErrorFlow(action: suspend (DataState.Error) -> Unit): Flow<DataState<T>> =
    transform { value ->
        if (value is DataState.Error) {
            action(value)
        }
        emit(value)
    }

fun <T> Flow<DataState<T>>.onLoadingFlow(action: suspend () -> Unit): Flow<DataState<T>> =
    transform { value ->
        if (value is DataState.Loading) {
            action()
        }
        emit(value)
    }

fun <T> Flow<DataState<T>>.onEmptyFlow(action: suspend () -> Unit): Flow<DataState<T>> =
    transform { value ->
        if (value is DataState.Empty) {
            action()
        }
        emit(value)
    }
