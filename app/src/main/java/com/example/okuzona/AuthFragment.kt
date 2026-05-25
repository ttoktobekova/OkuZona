package com.example.okuzona

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class AuthFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signUpButton: Button
    private lateinit var signInButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auth, container, false)

        auth = FirebaseAuth.getInstance()
        prefs = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)

        nameInput = view.findViewById(R.id.editTextName)
        emailInput = view.findViewById(R.id.editTextEmail)
        passwordInput = view.findViewById(R.id.editTextPassword)
        signUpButton = view.findViewById(R.id.buttonSignUp)
        signInButton = view.findViewById(R.id.buttonSignIn)

        signUpButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (validateInputs(name, email, password)) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val user = result.user
                        if (user != null) {
                            // Сохраняем имя локально с ключом по uid
                            prefs.edit().putString("user_name_${user.uid}", name).apply()
                            Toast.makeText(requireContext(), "Аккаунт создан!", Toast.LENGTH_SHORT).show()
                            navigateToBooks()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Ошибка: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        signInButton.setOnClickListener {
            findNavController().navigate(R.id.action_authFragment_to_loginFragment)
        }

        return view
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
}