package com.example.myapp.ui.base

import androidx.lifecycle.viewModelScope
import com.example.myapp.MyApplication
import com.example.myapp.utils.ConnectivityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class NetworkAwareViewModel : BaseViewModel() {
    
    private val connectivityManager = ConnectivityManager.getInstance(MyApplication.getContext())

    private val _networkState = MutableStateFlow<ConnectivityManager.NetworkState>(
        if (connectivityManager.isNetworkAvailable) {
            ConnectivityManager.NetworkState.Available
        } else {
            ConnectivityManager.NetworkState.Unavailable
        }
    )
    val networkState: StateFlow<ConnectivityManager.NetworkState> = _networkState

    private val _isOnline = MutableStateFlow(connectivityManager.isNetworkAvailable)
    val isOnline: StateFlow<Boolean> = _isOnline

    init {
        observeNetworkState()
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            connectivityManager.observeNetworkState().collect { state ->
                _networkState.value = state
                _isOnline.value = state is ConnectivityManager.NetworkState.Available
                onNetworkStateChanged(state)
            }
        }
    }

    /**
     * Override this method to handle network state changes
     */
    protected open fun onNetworkStateChanged(state: ConnectivityManager.NetworkState) {
        when (state) {
            is ConnectivityManager.NetworkState.Available -> {
                // Network became available
                onNetworkAvailable()
            }
            is ConnectivityManager.NetworkState.Unavailable,
            is ConnectivityManager.NetworkState.Lost -> {
                // Network became unavailable
                onNetworkLost()
            }
            is ConnectivityManager.NetworkState.Losing -> {
                // Network is about to be lost
                onNetworkLosing()
            }
        }
    }

    protected open fun onNetworkAvailable() {
        // Override in child classes if needed
    }

    protected open fun onNetworkLost() {
        // Override in child classes if needed
    }

    protected open fun onNetworkLosing() {
        // Override in child classes if needed
    }

    /**
     * Helper method to check if network is available before making API calls
     */
    protected fun isNetworkAvailable(): Boolean = connectivityManager.isNetworkAvailable

    /**
     * Helper method to get current network type
     */
    protected fun getNetworkType(): ConnectivityManager.NetworkType = connectivityManager.getNetworkType()

    /**
     * Helper methods to check specific network types
     */
    protected fun isOnWifi(): Boolean = connectivityManager.isOnWifi()
    protected fun isOnCellular(): Boolean = connectivityManager.isOnCellular()
    protected fun isOnEthernet(): Boolean = connectivityManager.isOnEthernet()

    /**
     * Execute a network operation only if network is available
     */
    protected suspend fun executeIfNetworkAvailable(
        operation: suspend () -> Unit,
        onNoNetwork: () -> Unit = { showError("No network connection") }
    ) {
        if (isNetworkAvailable()) {
            operation()
        } else {
            onNoNetwork()
        }
    }
}
