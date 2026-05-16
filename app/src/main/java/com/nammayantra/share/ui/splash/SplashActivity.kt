package com.nammayantra.share.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nammayantra.share.data.repository.AuthRepository
import com.nammayantra.share.databinding.ActivitySplashBinding
import com.nammayantra.share.ui.auth.LoginActivity
import com.nammayantra.share.ui.main.MainActivity
import com.nammayantra.share.util.Constants

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            // Use AuthRepository.currentUserId() so DEMO_MODE is handled correctly
            val isLoggedIn = if (Constants.DEMO_MODE) {
                AuthRepository.currentUserId() != null
            } else {
                Firebase.auth.currentUser != null
            }

            val target = if (isLoggedIn) MainActivity::class.java else LoginActivity::class.java
            startActivity(Intent(this, target))
            finish()
        }, 1800)
    }
}
