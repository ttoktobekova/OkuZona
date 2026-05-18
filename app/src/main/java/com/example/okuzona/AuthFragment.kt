package com.example.okuzona

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
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signInButton: Button
    private lateinit var signUpButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auth, container, false)

        auth = FirebaseAuth.getInstance()

        emailInput = view.findViewById(R.id.editTextEmail)
        passwordInput = view.findViewById(R.id.editTextPassword)
        signInButton = view.findViewById(R.id.buttonSignIn)
        signUpButton = view.findViewById(R.id.buttonSignUp)

        // АВТО-ВХОД: Если старый пользователь уже авторизован, сразу пускаем к книгам
        if (auth.currentUser != null) {
            navigateToBooks()
        }

        // Кнопка входа для СТАРЫХ пользователей
        signInButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (validateInputs(email, password)) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        navigateToBooks()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Ошибка входа: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        // Кнопка регистрации для НОВЫХ пользователей
        signUpButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (validateInputs(email, password)) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Аккаунт успешно создан!", Toast.LENGTH_SHORT).show()
                        navigateToBooks()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Ошибка регистрации: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        return view
    }

    private fun validateInputs(email: String, password: String): Boolean {
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
        findNavController().navigate(R.id.action_authFragment_to_bookListFragment)
    }
}
