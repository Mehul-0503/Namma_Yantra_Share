package com.nammayantra.share.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nammayantra.share.R
import com.nammayantra.share.databinding.ActivityMainBinding
import com.nammayantra.share.ui.bookings.MyBookingsFragment
import com.nammayantra.share.ui.home.HomeFragment
import com.nammayantra.share.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val bookingsFragment = MyBookingsFragment()
    private val profileFragment = ProfileFragment()
    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFragments()
        setupBottomNav()
    }

    private fun setupFragments() {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentContainer, profileFragment, "profile").hide(profileFragment)
            add(R.id.fragmentContainer, bookingsFragment, "bookings").hide(bookingsFragment)
            add(R.id.fragmentContainer, homeFragment, "home")
        }.commit()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            val target = when (item.itemId) {
                R.id.nav_home -> homeFragment
                R.id.nav_bookings -> bookingsFragment
                R.id.nav_profile -> profileFragment
                else -> homeFragment
            }
            switchFragment(target)
            true
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh home list whenever MainActivity returns to foreground
        // (e.g. after AddMachineActivity or EquipmentDetailActivity finishes)
        homeFragment.refreshIfVisible()
    }

    private fun switchFragment(target: Fragment) {
        if (target == activeFragment) return
        // Fix: setCustomAnimations must come BEFORE show/hide operations
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            hide(activeFragment)
            show(target)
        }.commit()
        activeFragment = target
    }
}
