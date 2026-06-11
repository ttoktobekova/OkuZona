package com.example.okuzona

import android.content.Context
import android.view.View
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView

object BadgeHelper {

    fun updateCartBadge(context: Context, bottomNav: BottomNavigationView) {
        val sharedPref = context.getSharedPreferences("cart", Context.MODE_PRIVATE)
        val cartItems = sharedPref.getStringSet("cart_items", emptySet())
        val itemCount = cartItems?.size ?: 0

        val badge = bottomNav.getOrCreateBadge(R.id.cartFragment)
        if (itemCount > 0) {
            badge.number = itemCount
            badge.isVisible = true
        } else {
            badge.isVisible = false
        }
    }
}