package com.example.okuzona

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var backToRegisterButton: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = FirebaseAuth.getInstance()

        emailInput = view.findViewById(R.id.editTextLoginEmail)
        passwordInput = view.findViewById(R.id.editTextLoginPassword)
        loginButton = view.findViewById(R.id.buttonDoLogin)
        backToRegisterButton = view.findViewById(R.id.textViewBackToRegister)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (validateInputs(email, password)) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Вход выполнен!", Toast.LENGTH_SHORT).show()
                        navigateToBooks()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Ошибка: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        backToRegisterButton.setOnClickListener {
            findNavController().navigateUp()
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
        findNavController().navigate(
            R.id.action_loginFragment_to_bookListFragment,
            null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.authFragment, true)
                .build()
        )
    }
}