package com.example.okuzona

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailEditText = view.findViewById<android.widget.EditText>(R.id.editTextLoginEmail)
        val passwordEditText = view.findViewById<android.widget.EditText>(R.id.editTextLoginPassword)
        val loginButton = view.findViewById<android.widget.Button>(R.id.buttonDoLogin)
        val backToRegisterButton = view.findViewById<android.widget.TextView>(R.id.textViewBackToRegister)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        // Загружаем покупки пользователя
                        loadUserPurchases(user.uid)
                        // Загружаем избранное пользователя
                        loadUserFavorites(user.uid)
                        Toast.makeText(requireContext(), "Вход выполнен успешно!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_bookListFragment)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Ошибка: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }

        backToRegisterButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadUserPurchases(userId: String) {
        db.collection("purchases").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val purchasedBooksIds = document.get("purchasedBooks") as? List<String> ?: emptyList()
                    val purchasedPref = requireContext().getSharedPreferences("purchased", Context.MODE_PRIVATE)
                    purchasedPref.edit().putStringSet("purchased_books", purchasedBooksIds.toSet()).apply()
                } else {
                    val purchasedPref = requireContext().getSharedPreferences("purchased", Context.MODE_PRIVATE)
                    purchasedPref.edit().clear().apply()
                }
            }
    }

    private fun loadUserFavorites(userId: String) {
        db.collection("favorites").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val favoriteBooksIds = document.get("favoriteBooks") as? List<String> ?: emptyList()
                    // Сохраняем в SharedPreferences
                    val favoritesPref = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
                    favoritesPref.edit().putStringSet("favorite_books_ids", favoriteBooksIds.toSet()).apply()
                } else {
                    val favoritesPref = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
                    favoritesPref.edit().clear().apply()
                }
            }
    }
}