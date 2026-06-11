package com.example.okuzona

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.okuzona.databinding.FragmentAuthBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences
    private lateinit var db: FirebaseFirestore

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        prefs = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)

        binding.buttonSignUp.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (validateInputs(name, email, password)) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val user = result.user
                        if (user != null) {
                            // Сохраняем имя пользователя
                            prefs.edit().putString("user_name_${user.uid}", name).apply()

                            // Создаём документ покупок (пустой)
                            db.collection("purchases").document(user.uid)
                                .set(mapOf("purchasedBooks" to emptyList<String>()))

                            // Создаём документ избранного (пустой)
                            db.collection("favorites").document(user.uid)
                                .set(mapOf("favoriteBooks" to emptyList<String>()))

                            // Очищаем локальные данные (новый пользователь)
                            clearLocalData()

                            Toast.makeText(requireContext(), "Аккаунт создан!", Toast.LENGTH_SHORT).show()
                            navigateToBooks()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Ошибка: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        binding.buttonSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_authFragment_to_loginFragment)
        }
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

    private fun validateInputs(name: String, email: String, password: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Введите имя", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Введите Email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(requireContext(), "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun navigateToBooks() {
        findNavController().navigate(
            R.id.action_authFragment_to_bookListFragment,
            null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.authFragment, true)
                .build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}