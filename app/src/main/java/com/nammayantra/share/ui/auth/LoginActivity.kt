package com.nammayantra.share.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nammayantra.share.databinding.ActivityLoginBinding
import com.nammayantra.share.ui.main.MainActivity
import com.nammayantra.share.util.Resource

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Already logged in → skip to main
        if (viewModel.isLoggedIn()) {
            navigateToMain()
            return
        }

        // Keyboard "Done" on password triggers login
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else false
        }

        binding.btnLogin.setOnClickListener { attemptLogin() }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        viewModel.loginResult.observe(this) { res ->
            when (res) {
                is Resource.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                    binding.tvError.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progress.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    navigateToMain()
                }
                is Resource.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    // Show inline error banner instead of a Toast
                    binding.tvError.text = res.message
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun attemptLogin() {
        binding.tvError.visibility = View.GONE
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        viewModel.login(email, password)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
