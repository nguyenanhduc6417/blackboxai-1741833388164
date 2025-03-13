package com.example.myapp.data.network

import com.example.myapp.MyApplication
import com.example.myapp.utils.ConnectivityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * A generic class that can provide a resource backed by both the local database and the network.
 *
 * @param ResultType Type for the Resource data.
 * @param RequestType Type for the API response.
 */
abstract class NetworkBoundResource<ResultType, RequestType> {

    private val connectivityManager = MyApplication.getInstance().connectivityManager

    fun asFlow() = flow<NetworkResult<ResultType>> {
        emit(NetworkResult.loading())

        // First, load data from the local database
        val dbValue = loadFromDb().first()

        if (shouldFetch(dbValue)) {
            // Need to fetch from network
            emit(NetworkResult.loading())

            if (connectivityManager.isNetworkAvailable) {
                try {
                    // Make network request
                    val apiResponse = createNetworkRequest()
                    
                    // Parse and save the response
                    val resultData = processResponse(apiResponse)
                    
                    // Save the result to database
                    saveNetworkResult(resultData)
                    
                    // Emit the saved data from database
                    emitAll(loadFromDb().map { NetworkResult.success(it) })
                } catch (e: Exception) {
                    // If network request failed, emit the last known good data from database
                    onFetchFailed(e)
                    emitAll(loadFromDb().map { 
                        NetworkResult.error(
                            e.message ?: "Network error occurred",
                            it
                        )
                    })
                }
            } else {
                // No network available
                emitAll(loadFromDb().map { 
                    NetworkResult.error(
                        "No internet connection",
                        it
                    )
                })
            }
        } else {
            // Don't need to fetch from network, just return database data
            emitAll(loadFromDb().map { NetworkResult.success(it) })
        }
    }

    /**
     * Load data from local database
     */
    protected abstract fun loadFromDb(): Flow<ResultType>

    /**
     * Make network request to fetch fresh data
     */
    protected abstract suspend fun createNetworkRequest(): RequestType

    /**
     * Process the network response and convert it to the ResultType
     */
    protected abstract suspend fun processResponse(response: RequestType): ResultType

    /**
     * Save the result of the network request to the local database
     */
    protected abstract suspend fun saveNetworkResult(data: ResultType)

    /**
     * Decide whether to fetch data from network
     * @param data Current data in database
     * @return true if data should be fetched from network, false otherwise
     */
    protected abstract fun shouldFetch(data: ResultType): Boolean

    /**
     * Called when a network request fails
     */
    protected open fun onFetchFailed(error: Throwable) {
        // Override if needed
    }

    companion object {
        /**
         * Helper function for cases where we only need network data
         */
        fun <T> networkOnly(
            fetch: suspend () -> T
        ): Flow<NetworkResult<T>> = flow {
            emit(NetworkResult.loading())
            try {
                val data = fetch()
                emit(NetworkResult.success(data))
            } catch (e: Exception) {
                emit(NetworkResult.error(e.message ?: "Network error occurred"))
            }
        }

        /**
         * Helper function for cases where we only need database data
         */
        fun <T> dbOnly(
            query: () -> Flow<T>
        ): Flow<NetworkResult<T>> = flow {
            emit(NetworkResult.loading())
            emitAll(query().map { NetworkResult.success(it) })
        }
    }
}
