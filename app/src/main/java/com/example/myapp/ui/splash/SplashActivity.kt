package com.example.myapp.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.example.myapp.ui.login.LoginActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Simulate splash screen delay
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if user is logged in here
            // For now, always navigate to LoginActivity
            navigateToLogin()
        }, SPLASH_DELAY)
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    companion object {
        private const val SPLASH_DELAY = 2000L // 2 seconds
    }
}
