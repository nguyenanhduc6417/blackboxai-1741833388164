package com.example.myapp.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
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

abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel> : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected abstract val viewModel: VM
    
    // Managers
    protected lateinit var sessionManager: SessionManager
    protected lateinit var preferenceManager: PreferenceManager
    protected lateinit var resourceProvider: ResourceProvider

    // Abstract methods that must be implemented by child fragments
    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    protected abstract fun setupViews()
    protected abstract fun observeData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeManagers()
    }

    private fun initializeManagers() {
        sessionManager = SessionManager.getInstance(requireContext())
        preferenceManager = PreferenceManager.getInstance(requireContext())
        resourceProvider = ResourceProvider.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        observeData()
        observeBaseData()
    }

    private fun observeBaseData() {
        // Observe loading state
        viewLifecycleScope {
            viewModel.loading.collect { isLoading ->
                handleLoading(isLoading)
            }
        }

        // Observe error state
        viewLifecycleScope {
            viewModel.error.collect { error ->
                error?.let { handleError(it) }
            }
        }
    }

    protected open fun handleLoading(isLoading: Boolean) {
        // Override in child fragments if needed
    }

    protected open fun handleError(error: String) {
        binding.root.showSnackbar(error)
    }

    protected fun showToast(message: String) {
        requireContext().showToast(message)
    }

    protected fun showToast(@StringRes messageResId: Int) {
        requireContext().showToast(resourceProvider.getString(messageResId))
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
        viewLifecycleScope {
            this@collectWhenStarted.collect { value ->
                action(value)
            }
        }
    }

    private fun viewLifecycleScope(block: suspend () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
