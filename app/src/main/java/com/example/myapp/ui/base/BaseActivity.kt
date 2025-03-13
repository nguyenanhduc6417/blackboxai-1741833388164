package com.example.myapp.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.example.myapp.data.local.PreferenceManager
import com.example.myapp.data.local.SessionManager
import com.example.myapp.utils.ResourceProvider
import com.example.myapp.utils.showSnackbar
import com.example.myapp.utils.showToast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class BaseActivity<VB : ViewBinding, VM : BaseViewModel> : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected abstract val viewModel: VM
    
    // Managers
    protected lateinit var sessionManager: SessionManager
    protected lateinit var preferenceManager: PreferenceManager
    protected lateinit var resourceProvider: ResourceProvider

    // Abstract methods that must be implemented by child activities
    protected abstract fun getViewBinding(inflater: LayoutInflater): VB
    protected abstract fun setupViews()
    protected abstract fun observeData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeManagers()
        setupTheme()
        
        _binding = getViewBinding(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeData()
        observeBaseData()
    }

    private fun initializeManagers() {
        sessionManager = SessionManager.getInstance(this)
        preferenceManager = PreferenceManager.getInstance(this)
        resourceProvider = ResourceProvider.getInstance(this)
    }

    private fun setupTheme() {
        // Apply theme based on preferences
        if (preferenceManager.isDarkMode) {
            // Apply dark theme
            // setTheme(R.style.Theme_App_Dark)
        } else {
            // Apply light theme
            // setTheme(R.style.Theme_App_Light)
        }
    }

    private fun observeBaseData() {
        // Observe loading state
        lifecycleScope {
            viewModel.loading.collect { isLoading ->
                handleLoading(isLoading)
            }
        }

        // Observe error state
        lifecycleScope {
            viewModel.error.collect { error ->
                error?.let { handleError(it) }
            }
        }
    }

    protected open fun handleLoading(isLoading: Boolean) {
        // Override in child activities if needed
    }

    protected open fun handleError(error: String) {
        binding.root.showSnackbar(error)
    }

    protected fun showToast(message: String) {
        showToast(message)
    }

    protected fun showToast(@StringRes messageResId: Int) {
        showToast(resourceProvider.getString(messageResId))
    }

    protected fun showSnackbar(
        message: String,
        action: String? = null,
        actionListener: (() -> Unit)? = null
    ) {
        binding.root.showSnackbar(message, action = action, actionListener = actionListener)
    }

    protected fun showSnackbar(
        @StringRes messageResId: Int,
        @StringRes actionResId: Int? = null,
        actionListener: (() -> Unit)? = null
    ) {
        binding.root.showSnackbar(
            resourceProvider.getString(messageResId),
            action = actionResId?.let { resourceProvider.getString(it) },
            actionListener = actionListener
        )
    }

    protected fun <T> Flow<T>.collectWhenStarted(action: suspend (T) -> Unit) {
        lifecycleScope {
            this@collectWhenStarted.collect { value ->
                action(value)
            }
        }
    }

    private fun lifecycleScope(block: suspend () -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                block()
            }
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
