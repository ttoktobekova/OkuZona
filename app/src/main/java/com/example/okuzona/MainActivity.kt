package com.example.okuzona

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val navController = navHostFragment?.findNavController()!!

        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.authFragment, R.id.loginFragment, R.id.splashFragment -> {
                    // Скрываем bottom navigation на экранах авторизации, входа и заставки
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    // Показываем bottom navigation на всех остальных экранах (список книг, читалка, профиль, избранное)
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }
}