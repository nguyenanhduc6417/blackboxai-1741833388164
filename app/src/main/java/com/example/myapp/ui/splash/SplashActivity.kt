package com.example.myapp.ui.splash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapp.R
import com.example.myapp.databinding.ActivitySplashBinding
import com.example.myapp.ui.base.BaseActivity
import com.example.myapp.ui.login.LoginActivity
import com.example.myapp.ui.main.MainActivity
import com.example.myapp.utils.hide
import com.example.myapp.utils.show
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {

    override val viewModel: SplashViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    override fun getViewBinding(inflater: LayoutInflater) = ActivitySplashBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkNotificationPermission()
    }

    override fun setupViews() {
        // Initialize views if needed
    }

    override fun observeData() {
        // Observe splash state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.splashState.collect { state ->
                    handleSplashState(state)
                }
            }
        }

        // Observe permission state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.permissionState.collect { state ->
                    handlePermissionState(state)
                }
            }
        }
    }

    private fun handleSplashState(state: SplashViewModel.SplashState) {
        when (state) {
            is SplashViewModel.SplashState.Initial -> {
                // Initial state, do nothing
            }
            is SplashViewModel.SplashState.Loading -> {
                binding.progressBar.show()
                binding.tvLoadingMessage.show()
            }
            is SplashViewModel.SplashState.RequiresUpdate -> {
                binding.progressBar.hide()
                binding.tvLoadingMessage.hide()
                showForceUpdateDialog(state.message, state.storeUrl)
            }
            is SplashViewModel.SplashState.NavigateToLogin -> {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            is SplashViewModel.SplashState.NavigateToHome -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            is SplashViewModel.SplashState.Error -> {
                binding.progressBar.hide()
                showError(state.message)
            }
        }
    }

    private fun handlePermissionState(state: SplashViewModel.PermissionState) {
        when (state) {
            is SplashViewModel.PermissionState.Initial -> {
                // Initial state, do nothing
            }
            is SplashViewModel.PermissionState.RequiresPermission -> {
                requestNotificationPermission()
            }
            is SplashViewModel.PermissionState.Granted -> {
                // Permission granted, continue with splash flow
            }
            is SplashViewModel.PermissionState.Denied -> {
                showPermissionDeniedDialog()
            }
        }
    }

    private fun checkNotificationPermission() {
        viewModel.checkNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    viewModel.onPermissionResult(true)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showPermissionRationaleDialog()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            viewModel.onPermissionResult(true)
        }
    }

    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.permission_required)
            .setMessage(R.string.notification_permission_rationale)
            .setPositiveButton(R.string.grant) { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton(R.string.deny) { _, _ ->
                viewModel.onPermissionResult(false)
            }
            .show()
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.permission_denied)
            .setMessage(R.string.notification_permission_denied_message)
            .setPositiveButton(R.string.settings) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(R.string.continue_without_permission) { _, _ ->
                viewModel.onPermissionResult(false)
            }
            .show()
    }

    private fun showForceUpdateDialog(message: String, storeUrl: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.update_required)
            .setMessage(message)
            .setPositiveButton(R.string.update) { _, _ ->
                openPlayStore(storeUrl)
            }
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            startActivity(this)
        }
    }

    private fun openPlayStore(storeUrl: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl)))
        } catch (e: Exception) {
            // If Play Store app is not installed, open in browser
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(
                "https://play.google.com/store/apps/details?id=${packageName}"
            )))
        }
        finish()
    }
}
