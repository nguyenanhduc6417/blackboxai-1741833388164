package com.example.myapp.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.DataState
import com.example.myapp.data.local.SessionManager
import com.example.myapp.data.repository.SplashRepository
import com.example.myapp.ui.base.NetworkAwareViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val splashRepository: SplashRepository,
    private val sessionManager: SessionManager
) : NetworkAwareViewModel() {

    private val _navigationEvent = MutableLiveData<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    init {
        checkInitialNavigation()
    }

    private fun checkInitialNavigation() {
        viewModelScope.launch {
            // Add artificial delay for splash screen
            delay(1500)
            
            when {
                sessionManager.isLoggedIn() -> {
                    // Check if session is valid
                    when (val result = splashRepository.validateSession()) {
                        is DataState.Success -> _navigationEvent.value = NavigationEvent.NavigateToMain
                        is DataState.Error -> {
                            sessionManager.clearSession()
                            _navigationEvent.value = NavigationEvent.NavigateToLogin
                        }
                        is DataState.Loading -> { /* Handle loading state if needed */ }
                    }
                }
                else -> _navigationEvent.value = NavigationEvent.NavigateToLogin
            }
        }
    }

    sealed class NavigationEvent {
        object NavigateToMain : NavigationEvent()
        object NavigateToLogin : NavigationEvent()
    }
}
