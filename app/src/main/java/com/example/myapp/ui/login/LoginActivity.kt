package com.example.myapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.myapp.R
import com.example.myapp.databinding.ActivityLoginBinding
import com.example.myapp.ui.main.MainActivity
import android.util.Patterns

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // Clear errors on text change
        binding.etEmail.addTextChangedListener {
            binding.tilEmail.error = null
        }
        binding.etPassword.addTextChangedListener {
            binding.tilPassword.error = null
        }

        // Login button click
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                showLoading(true)
                // Simulate login process
                binding.root.postDelayed({
                    showLoading(false)
                    navigateToMain()
                }, 1500)
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        
        // Validate email
        val email = binding.etEmail.text.toString()
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        }

        // Validate password
        val password = binding.etPassword.text.toString()
        if (password.isEmpty() || password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_invalid_password)
            isValid = false
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.etEmail.isEnabled = !show
        binding.etPassword.isEnabled = !show
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
