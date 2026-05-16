package com.nammayantra.share.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.nammayantra.share.R
import com.nammayantra.share.databinding.ActivitySignupBinding
import com.nammayantra.share.ui.main.MainActivity
import com.nammayantra.share.util.Resource

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: AuthViewModel by viewModels()
    private var selectedRole = "farmer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back button in hero header
        binding.btnBack.setOnClickListener { finish() }

        setupRoleSelection()

        // Keyboard "Done" on password triggers signup
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptSignup()
                true
            } else false
        }

        binding.btnSignUp.setOnClickListener { attemptSignup() }

        binding.tvLogin.setOnClickListener { finish() }

        viewModel.signupResult.observe(this) { res ->
            when (res) {
                is Resource.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.btnSignUp.isEnabled = false
                    binding.tvError.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progress.visibility = View.GONE
                    binding.btnSignUp.isEnabled = true
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
                is Resource.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.btnSignUp.isEnabled = true
                    binding.tvError.text = res.message
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun attemptSignup() {
        binding.tvError.visibility = View.GONE
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        viewModel.signup(name, email, password, selectedRole)
    }

    private fun setupRoleSelection() {
        updateRoleUI()
        binding.cardFarmer.setOnClickListener {
            selectedRole = "farmer"
            updateRoleUI()
        }
        binding.cardOwner.setOnClickListener {
            selectedRole = "owner"
            updateRoleUI()
        }
    }

    private fun updateRoleUI() {
        val primaryColor = ContextCompat.getColor(this, R.color.primary)
        val dividerColor = ContextCompat.getColor(this, R.color.divider)
        val selectedBg = ContextCompat.getColor(this, R.color.primary_light)
        val defaultBg = ContextCompat.getColor(this, R.color.surface_card)

        if (selectedRole == "farmer") {
            binding.cardFarmer.strokeColor = primaryColor
            binding.cardFarmer.strokeWidth = 2.dpToPx()
            binding.cardFarmer.setCardBackgroundColor(selectedBg)

            binding.cardOwner.strokeColor = dividerColor
            binding.cardOwner.strokeWidth = 1.dpToPx()
            binding.cardOwner.setCardBackgroundColor(defaultBg)
        } else {
            binding.cardOwner.strokeColor = primaryColor
            binding.cardOwner.strokeWidth = 2.dpToPx()
            binding.cardOwner.setCardBackgroundColor(selectedBg)

            binding.cardFarmer.strokeColor = dividerColor
            binding.cardFarmer.strokeWidth = 1.dpToPx()
            binding.cardFarmer.setCardBackgroundColor(defaultBg)
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
