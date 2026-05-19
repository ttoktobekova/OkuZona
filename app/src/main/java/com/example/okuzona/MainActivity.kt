package com.example.okuzona

import android.os.Bundle
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

        // Получаем NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val navController = navHostFragment?.findNavController()!!

        // Настраиваем BottomNavigationView с NavController
        bottomNavigationView.setupWithNavController(navController)

        // Следим за изменениями destination, чтобы скрывать/показывать bottom navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.authFragment, R.id.loginFragment -> {
                    // Скрываем bottom navigation на экранах авторизации и входа
                    bottomNavigationView.visibility = android.view.View.GONE
                }
                else -> {
                    // Показываем bottom navigation на всех остальных экранах
                    bottomNavigationView.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
}