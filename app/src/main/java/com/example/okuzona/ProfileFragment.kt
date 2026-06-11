package com.example.okuzona

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.okuzona.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        prefs = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)

        loadUserData()
        setupClickListeners()
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user == null) {
            findNavController().navigate(R.id.action_profileFragment_to_authFragment)
            return
        }

        binding.tvProfileEmail.text = user.email ?: "не указан"
        val name = prefs.getString("user_name_${user.uid}", "Не указано")
        binding.tvProfileName.text = name
    }

    private fun setupClickListeners() {
        // Выход из аккаунта
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Выход из аккаунта")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Выйти") { _, _ ->
                    logout()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        // Информация о приложении
        binding.btnAppInfo.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("О приложении")
                .setMessage("OkuZona - приложение для чтения книг\nВерсия 1.0.0\nКонтакты: okuzona@example.com")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun logout() {
        val user = auth.currentUser
        if (user != null) {
            prefs.edit().remove("user_name_${user.uid}").apply()
        }

        // Очищаем локальные данные (корзину, покупки, избранное)
        clearLocalData()

        // Выход из Firebase Auth
        auth.signOut()

        Toast.makeText(requireContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()

        // Переход на экран регистрации
        findNavController().navigate(
            R.id.action_profileFragment_to_authFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.profileFragment, true)
                .build()
        )
    }

    private fun clearLocalData() {
        // Очищаем корзину
        val cartPref = requireContext().getSharedPreferences("cart", Context.MODE_PRIVATE)
        cartPref.edit().clear().apply()

        // Очищаем покупки
        val purchasedPref = requireContext().getSharedPreferences("purchased", Context.MODE_PRIVATE)
        purchasedPref.edit().clear().apply()

        // Очищаем избранное
        val favoritesPref = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
        favoritesPref.edit().clear().apply()

        // Обновляем бейдж корзины
        (requireActivity() as? MainActivity)?.updateCartBadge()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}