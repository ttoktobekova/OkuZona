package com.example.okuzona

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.example.okuzona.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Анимация появления элементов
        animateSplash()

        // Задержка перед переходом
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded) {
                if (auth.currentUser == null) {
                    findNavController().navigate(R.id.action_splashFragment_to_authFragment)
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_bookListFragment)
                }
            }
        }, 2500)
    }

    private fun animateSplash() {
        // Анимация для логотипа: масштабирование и прозрачность
        binding.imageSplash.alpha = 0f
        binding.imageSplash.scaleX = 0.8f
        binding.imageSplash.scaleY = 0.8f

        binding.imageSplash.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Анимация для текста "Добро пожаловать в"
        binding.textWelcome.alpha = 0f
        binding.textWelcome.animate()
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(300)
            .start()

        // Анимация для названия приложения
        binding.textAppName.alpha = 0f
        binding.textAppName.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(500)
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}