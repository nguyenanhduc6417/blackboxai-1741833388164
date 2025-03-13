package com.example.myapp.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.myapp.R
import com.example.myapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup bottom navigation with nav controller
        binding.bottomNavigation.setupWithNavController(navController)

        // Handle reselection to reset fragment state
        binding.bottomNavigation.setOnItemReselectedListener { menuItem ->
            val selectedFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
            when (menuItem.itemId) {
                R.id.navigation_home -> (selectedFragment as? HomeFragment)?.scrollToTop()
                R.id.navigation_messages -> (selectedFragment as? MessagesFragment)?.scrollToTop()
                R.id.navigation_notifications -> (selectedFragment as? NotificationsFragment)?.scrollToTop()
                R.id.navigation_activities -> (selectedFragment as? ActivitiesFragment)?.scrollToTop()
                R.id.navigation_contacts -> (selectedFragment as? ContactsFragment)?.scrollToTop()
            }
        }
    }

    override fun onBackPressed() {
        if (binding.bottomNavigation.selectedItemId == R.id.navigation_home) {
            super.onBackPressed()
        } else {
            binding.bottomNavigation.selectedItemId = R.id.navigation_home
        }
    }
}
