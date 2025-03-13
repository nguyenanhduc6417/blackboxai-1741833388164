package com.example.myapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityManager @Inject constructor(context: Context) {

    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isNetworkAvailable: Boolean
        get() {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        }

    fun observeNetworkState(): Flow<NetworkState> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkState.Available)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                trySend(NetworkState.Losing)
            }

            override fun onLost(network: Network) {
                trySend(NetworkState.Lost)
            }

            override fun onUnavailable() {
                trySend(NetworkState.Unavailable)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Initial state
        val currentState = if (isNetworkAvailable) {
            NetworkState.Available
        } else {
            NetworkState.Unavailable
        }
        trySend(currentState)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    fun getNetworkType(): NetworkType {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
            else -> NetworkType.NONE
        }
    }

    fun isOnWifi(): Boolean = getNetworkType() == NetworkType.WIFI
    fun isOnCellular(): Boolean = getNetworkType() == NetworkType.CELLULAR
    fun isOnEthernet(): Boolean = getNetworkType() == NetworkType.ETHERNET

    sealed class NetworkState {
        object Available : NetworkState()
        object Unavailable : NetworkState()
        object Losing : NetworkState()
        object Lost : NetworkState()
    }

    enum class NetworkType {
        WIFI,
        CELLULAR,
        ETHERNET,
        NONE
    }

    companion object {
        @Volatile
        private var instance: ConnectivityManager? = null

        fun getInstance(context: Context): ConnectivityManager {
            return instance ?: synchronized(this) {
                instance ?: ConnectivityManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
