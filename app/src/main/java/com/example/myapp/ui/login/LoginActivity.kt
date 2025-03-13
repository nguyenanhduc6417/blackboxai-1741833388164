package com.example.myapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapp.R
import com.example.myapp.databinding.ActivityLoginBinding
import com.example.myapp.ui.base.BaseActivity
import com.example.myapp.ui.main.MainActivity
import com.example.myapp.utils.hide
import com.example.myapp.utils.show
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    override val viewModel: LoginViewModel by viewModels()

    override fun getViewBinding(inflater: LayoutInflater) = ActivityLoginBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if already logged in
        if (isLoggedIn()) {
            navigateToMain()
            return
        }
    }

    override fun setupViews() {
        with(binding) {
            // Setup input fields
            etEmail.addTextChangedListener { 
                viewModel.updateEmail(it?.toString() ?: "")
                tilEmail.error = null
            }
            
            etPassword.addTextChangedListener {
                viewModel.updatePassword(it?.toString() ?: "")
                tilPassword.error = null
            }

            // Setup login button
            btnLogin.setOnClickListener {
                viewModel.login()
            }

            // Setup forgot password
            tvForgotPassword.setOnClickListener {
                // Handle forgot password
                showSnackbar(
                    resourceProvider.getString(R.string.feature_coming_soon)
                )
            }

            // Setup network state observer
            setupNetworkStateObserver()
        }
    }

    override fun observeData() {
        // Observe login state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    handleLoginState(state)
                }
            }
        }

        // Observe network state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.networkState.collect { state ->
                    handleNetworkState(state)
                }
            }
        }
    }

    private fun handleLoginState(state: LoginViewModel.LoginState) {
        with(binding) {
            when (state) {
                is LoginViewModel.LoginState.Idle -> {
                    progressBar.hide()
                    btnLogin.isEnabled = true
                }
                is LoginViewModel.LoginState.Loading -> {
                    progressBar.show()
                    btnLogin.isEnabled = false
                }
                is LoginViewModel.LoginState.InputValid -> {
                    progressBar.hide()
                    btnLogin.isEnabled = true
                    tilEmail.error = null
                    tilPassword.error = null
                }
                is LoginViewModel.LoginState.ValidationError -> {
                    progressBar.hide()
                    btnLogin.isEnabled = true
                    state.emailError?.let { tilEmail.error = it }
                    state.passwordError?.let { tilPassword.error = it }
                }
                is LoginViewModel.LoginState.Success -> {
                    progressBar.hide()
                    navigateToMain()
                }
                is LoginViewModel.LoginState.FirstTimeLogin -> {
                    progressBar.hide()
                    // Handle first time login (e.g., show tutorial)
                    navigateToMain()
                }
                is LoginViewModel.LoginState.Error -> {
                    progressBar.hide()
                    btnLogin.isEnabled = true
                    showError(state.message)
                }
            }
        }
    }

    private fun handleNetworkState(state: ConnectivityManager.NetworkState) {
        when (state) {
            is ConnectivityManager.NetworkState.Available -> {
                binding.networkStatusBar.hide()
            }
            is ConnectivityManager.NetworkState.Unavailable -> {
                binding.networkStatusBar.apply {
                    show()
                    setText(R.string.no_internet_connection)
                    setBackgroundColor(resourceProvider.getColor(R.color.error))
                }
            }
            is ConnectivityManager.NetworkState.Losing -> {
                binding.networkStatusBar.apply {
                    show()
                    setText(R.string.weak_internet_connection)
                    setBackgroundColor(resourceProvider.getColor(R.color.warning))
                }
            }
            else -> { /* no-op */ }
        }
    }

    private fun setupNetworkStateObserver() {
        binding.networkStatusBar.setOnClickListener {
            if (!viewModel.isOnline.value) {
                showSnackbar(
                    message = resourceProvider.getString(R.string.check_internet_connection),
                    action = resourceProvider.getString(R.string.settings)
                ) {
                    // Open network settings
                    startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS))
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetState()
    }
}
