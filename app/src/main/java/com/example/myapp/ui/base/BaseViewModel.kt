package com.example.myapp.ui.base

import androidx.lifecycle.viewModelScope
import com.example.myapp.MyApplication
import com.example.myapp.data.local.PreferenceManager
import com.example.myapp.data.local.SessionManager
import com.example.myapp.utils.ResourceProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : NetworkAwareViewModel() {

    // Managers
    protected val sessionManager: SessionManager = MyApplication.getInstance().sessionManager
    protected val preferenceManager: PreferenceManager = MyApplication.getInstance().preferenceManager
    protected val resourceProvider: ResourceProvider = MyApplication.getInstance().resourceProvider

    // UI State
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Navigation
    private val _navigation = MutableSharedFlow<NavigationEvent>()
    val navigation: SharedFlow<NavigationEvent> = _navigation

    // UI Events
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    protected fun showLoading() {
        _loading.value = true
    }

    protected fun hideLoading() {
        _loading.value = false
    }

    protected fun showError(message: String) {
        _error.value = message
    }

    protected fun clearError() {
        _error.value = null
    }

    protected fun navigate(event: NavigationEvent) {
        viewModelScope.launch {
            _navigation.emit(event)
        }
    }

    protected fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    // Session management
    protected fun isLoggedIn(): Boolean = sessionManager.isLoggedIn
    protected fun getToken(): String? = sessionManager.token
    protected fun getUser() = sessionManager.user
    protected fun clearSession() {
        sessionManager.clearSession()
    }

    // Preference management
    protected fun isDarkMode(): Boolean = preferenceManager.isDarkMode
    protected fun isNotificationsEnabled(): Boolean = preferenceManager.isNotificationsEnabled
    protected fun isFirstLaunch(): Boolean = preferenceManager.isFirstLaunch
    protected fun getAppLanguage(): String = preferenceManager.appLanguage

    // Common sealed classes for events
    sealed class NavigationEvent {
        data class ToLogin(val clearStack: Boolean = true) : NavigationEvent()
        data class ToMain(val clearStack: Boolean = true) : NavigationEvent()
        data class ToScreen(val route: String) : NavigationEvent()
        object Back : NavigationEvent()
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data class ShowSnackbar(
            val message: String,
            val action: String? = null,
            val actionListener: (() -> Unit)? = null
        ) : UiEvent()
        data class ShowDialog(
            val title: String,
            val message: String,
            val positiveButton: String? = null,
            val negativeButton: String? = null,
            val positiveAction: (() -> Unit)? = null,
            val negativeAction: (() -> Unit)? = null
        ) : UiEvent()
        object ShowNoInternetConnection : UiEvent()
        object ShowSessionExpired : UiEvent()
        object ShowServerError : UiEvent()
    }

    // Override NetworkAwareViewModel methods
    override fun onNetworkLost() {
        super.onNetworkLost()
        emitUiEvent(UiEvent.ShowNoInternetConnection)
    }

    // Helper method to handle common API errors
    protected fun handleApiError(error: Throwable) {
        when (error) {
            is retrofit2.HttpException -> {
                when (error.code()) {
                    401 -> {
                        clearSession()
                        emitUiEvent(UiEvent.ShowSessionExpired)
                        navigate(NavigationEvent.ToLogin())
                    }
                    500 -> emitUiEvent(UiEvent.ShowServerError)
                    else -> showError(error.message())
                }
            }
            else -> showError(error.message ?: "Unknown error occurred")
        }
    }
}
