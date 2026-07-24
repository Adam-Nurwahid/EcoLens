package com.adam.ecolens

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.adam.ecolens.data.local.SessionManager
import com.adam.ecolens.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Link BottomNavigationView with Navigation Controller
        binding.bottomNavView.setupWithNavController(navController)

        // Control bottom nav visibility based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment, R.id.quizPlayFragment -> {
                    binding.bottomNavView.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavView.visibility = View.VISIBLE
                }
            }
        }

        // Check login session on startup
        if (!sessionManager.isLoggedIn()) {
            navController.navigate(R.id.loginFragment)
        }
    }
}