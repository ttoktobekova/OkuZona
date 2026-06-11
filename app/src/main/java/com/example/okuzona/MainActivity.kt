package com.example.okuzona

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_language", "ru") ?: "ru"
        val locale = Locale(lang)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Настройка навигации
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val navController = navHostFragment?.findNavController()

        if (navController != null) {
            bottomNavigationView.setupWithNavController(navController)

            // Скрываем нижнюю навигацию на определенных экранах
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.authFragment, R.id.loginFragment, R.id.splashFragment,
                    R.id.bookReaderFragment, R.id.informationFragment -> {
                        bottomNavigationView.visibility = View.GONE
                    }
                    else -> {
                        bottomNavigationView.visibility = View.VISIBLE
                        // Обновляем бейдж при возврате на главные экраны
                        updateCartBadge()
                    }
                }
            }
        }

        // Первоначальное обновление бейджа
        updateCartBadge()
    }

    fun updateCartBadge() {
        BadgeHelper.updateCartBadge(this, bottomNavigationView)
    }
}