package com.example.myapp.ui.login

import androidx.lifecycle.viewModelScope
import com.example.myapp.data.DataState
import com.example.myapp.data.network.api.LoginResponse
import com.example.myapp.data.repository.AuthRepository
import com.example.myapp.ui.base.BaseViewModel
import com.example.myapp.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : BaseViewModel() {
    private val repository = AuthRepository.getInstance()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun updateEmail(value: String) {
        _email.value = value
        validateInput()
    }

    fun updatePassword(value: String) {
        _password.value = value
        validateInput()
    }

    private fun validateInput() {
        val emailValue = _email.value
        val passwordValue = _password.value

        when {
            emailValue.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() -> {
                _loginState.value = LoginState.ValidationError(
                    emailError = "Invalid email format",
                    passwordError = null
                )
            }
            passwordValue.isEmpty() || passwordValue.length < Constants.Validation.MIN_PASSWORD_LENGTH -> {
                _loginState.value = LoginState.ValidationError(
                    emailError = null,
                    passwordError = "Password must be at least ${Constants.Validation.MIN_PASSWORD_LENGTH} characters"
                )
            }
            else -> {
                _loginState.value = LoginState.InputValid
            }
        }
    }

    fun login() {
        if (_loginState.value !is LoginState.InputValid) {
            validateInput()
            return
        }

        viewModelScope.launch {
            executeIfNetworkAvailable(
                operation = {
                    repository.login(_email.value, _password.value).collect { dataState ->
                        when (dataState) {
                            is DataState.Loading -> {
                                _loginState.value = LoginState.Loading
                            }
                            is DataState.Success -> {
                                handleLoginSuccess(dataState.data)
                            }
                            is DataState.Error -> {
                                _loginState.value = LoginState.Error(dataState.message)
                            }
                            is DataState.Empty -> {
                                _loginState.value = LoginState.Error("Login response was empty")
                            }
                        }
                    }
                },
                onNoNetwork = {
                    _loginState.value = LoginState.Error("No internet connection")
                }
            )
        }
    }

    private fun handleLoginSuccess(response: LoginResponse) {
        // Check if first time login
        if (preferenceManager.isFirstLaunch) {
            preferenceManager.isFirstLaunch = false
            _loginState.value = LoginState.FirstTimeLogin(response)
        } else {
            _loginState.value = LoginState.Success(response)
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object InputValid : LoginState()
        data class ValidationError(
            val emailError: String? = null,
            val passwordError: String? = null
        ) : LoginState()
        data class Success(val data: LoginResponse) : LoginState()
        data class FirstTimeLogin(val data: LoginResponse) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        _email.value = ""
        _password.value = ""
    }
}
