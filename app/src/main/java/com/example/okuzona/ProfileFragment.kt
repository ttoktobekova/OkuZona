package com.example.okuzona

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.example.okuzona.databinding.FragmentProfileBinding
import java.util.Locale

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
        setupLanguageSelector()
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

        val savedLanguage = prefs.getString("app_language", "ru") ?: "ru"
        binding.radioRussian.isChecked = savedLanguage == "ru"
        binding.radioKyrgyz.isChecked = savedLanguage == "ky"
    }

    private fun setupLanguageSelector() {
        binding.radioGroupLanguage.setOnCheckedChangeListener { _, checkedId ->
            val newLang = when (checkedId) {
                R.id.radioKyrgyz -> "ky"
                else -> "ru"
            }
            prefs.edit().putString("app_language", newLang).apply()
            setAppLocale(newLang)
            requireActivity().recreate()
        }
    }

    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            requireContext().createConfigurationContext(config)
        }
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun setupClickListeners() {
        // Выход из аккаунта (очистка локальных данных и Firebase Auth)
        binding.btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Выход из аккаунта")
                .setMessage("Вы уверены? Вы выйдете из аккаунта, и локальные данные будут очищены.")
                .setPositiveButton("Выйти") { _, _ ->
                    val user = auth.currentUser
                    if (user != null) {
                        // Удаляем локально сохранённое имя
                        prefs.edit().remove("user_name_${user.uid}").apply()
                    }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}