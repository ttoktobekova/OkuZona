package com.example.okuzona

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.okuzona.databinding.FragmentInformationBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InformationFragment : Fragment() {

    private var _binding: FragmentInformationBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentBook: Book? = null
    private var isPurchased = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val book = arguments?.getSerializable("book") as? Book

        if (book != null) {
            currentBook = book
            displayBookInfo(book)

            checkIfPurchased(book)

            binding.addToCartButton.setOnClickListener {
                addToCart(book)
            }

            binding.buyNowButton.setOnClickListener {
                showPaymentDialog(book)
            }
        } else {
            Toast.makeText(requireContext(), "Ошибка загрузки информации о книге", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun checkIfPurchased(book: Book) {
        val sharedPref = requireContext().getSharedPreferences("purchased", Context.MODE_PRIVATE)
        val purchasedBooks = sharedPref.getStringSet("purchased_books", emptySet())
        isPurchased = purchasedBooks?.contains(book.bookId) == true

        if (isPurchased) {
            binding.addToCartButton.visibility = View.GONE
            binding.buyNowButton.visibility = View.GONE

            val readButton = Button(requireContext()).apply {
                text = "Читать книгу"
                textSize = 16f
                setTextColor(android.graphics.Color.WHITE)
                setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            readButton.setOnClickListener {
                openBookForReading(book)
            }
            binding.buttonContainer.addView(readButton)
        }
    }

    private fun addToCart(book: Book) {
        val sharedPref = requireContext().getSharedPreferences("cart", Context.MODE_PRIVATE)
        val cartItems = sharedPref.getStringSet("cart_items", emptySet())?.toMutableSet() ?: mutableSetOf()

        cartItems.add(book.bookId)
        sharedPref.edit().putStringSet("cart_items", cartItems).apply()

        val bookJson = gson.toJson(book)
        sharedPref.edit().putString("book_${book.bookId}", bookJson).apply()

        updateCartBadge()

        Toast.makeText(requireContext(), "${book.title} добавлена в корзину", Toast.LENGTH_SHORT).show()
    }

    private fun updateCartBadge() {
        (requireActivity() as? MainActivity)?.updateCartBadge()
    }

    private fun showPaymentDialog(book: Book) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_payment, null)
        val cardNumberInput = dialogView.findViewById<TextInputEditText>(R.id.cardNumberInput)
        val expiryInput = dialogView.findViewById<TextInputEditText>(R.id.expiryInput)
        val cvvInput = dialogView.findViewById<TextInputEditText>(R.id.cvvInput)
        val amountText = dialogView.findViewById<TextView>(R.id.amountText)
        val commissionText = dialogView.findViewById<TextView>(R.id.commissionText)
        val totalText = dialogView.findViewById<TextView>(R.id.totalText)

        val price = book.cost.toDoubleOrNull() ?: 0.0
        val commission = price * 0.035
        val total = price + commission

        amountText.text = "Сумма: ${String.format("%.2f", price)} сом"
        commissionText.text = "Комиссия (3.5%): ${String.format("%.2f", commission)} сом"
        totalText.text = "Итого: ${String.format("%.2f", total)} сом"

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Оплата картой")
            .setView(dialogView)
            .setPositiveButton("Оплатить") { _, _ ->
                val cardNumber = cardNumberInput.text.toString()
                val expiry = expiryInput.text.toString()
                val cvv = cvvInput.text.toString()

                if (validateCard(cardNumber, expiry, cvv)) {
                    processPayment(book, total)
                } else {
                    Toast.makeText(requireContext(), "Неверные данные карты", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()
    }

    private fun validateCard(cardNumber: String, expiry: String, cvv: String): Boolean {
        return cardNumber.length >= 16 &&
                expiry.matches(Regex("\\d{2}/\\d{2}")) &&
                cvv.length >= 3
    }

    private fun processPayment(book: Book, amount: Double) {
        val progressDialog = AlertDialog.Builder(requireContext())
            .setTitle("Обработка платежа")
            .setMessage("Пожалуйста, подождите...")
            .setCancelable(false)
            .create()

        progressDialog.show()

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            progressDialog.dismiss()

            savePurchasedBook(book)
            removeFromCartIfExists(book)

            AlertDialog.Builder(requireContext())
                .setTitle("Оплата успешна!")
                .setMessage("""
                    Книга "${book.title}" успешно оплачена!
                    
                    Сумма: ${String.format("%.2f", amount)} сом
                    Статус: Подтверждено
                    
                    Сейчас откроется книга для чтения.
                """.trimIndent())
                .setPositiveButton("Читать сейчас") { _, _ ->
                    openBookForReading(book)
                }
                .setCancelable(false)
                .show()

        }, 2000)
    }

    private fun removeFromCartIfExists(book: Book) {
        val sharedPref = requireContext().getSharedPreferences("cart", Context.MODE_PRIVATE)
        val cartIds = sharedPref.getStringSet("cart_items", emptySet())?.toMutableSet()

        if (cartIds != null && cartIds.remove(book.bookId)) {
            sharedPref.edit().putStringSet("cart_items", cartIds).apply()
            sharedPref.edit().remove("book_${book.bookId}").apply()
            updateCartBadge()
        }
    }

    private fun openBookForReading(book: Book) {
        val bundle = Bundle().apply {
            putString("pdfUrl", book.pdfUrl)
            putString("bookTitle", book.title)
            putString("bookAuthor", book.author)
            putString("bookImageUrl", book.imageUrl)
        }
        findNavController().navigate(R.id.action_informationFragment_to_bookReaderFragment, bundle)
    }

    private fun savePurchasedBook(book: Book) {
        val userId = auth.currentUser?.uid ?: return

        // Сохраняем в SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("purchased", Context.MODE_PRIVATE)
        val purchasedBooks = sharedPref.getStringSet("purchased_books", emptySet())?.toMutableSet() ?: mutableSetOf()
        purchasedBooks.add(book.bookId)
        sharedPref.edit().putStringSet("purchased_books", purchasedBooks).apply()

        // Сохраняем в Firestore
        db.collection("purchases").document(userId).get()
            .addOnSuccessListener { document ->
                val currentPurchases = if (document.exists()) {
                    (document.get("purchasedBooks") as? List<String>)?.toMutableList() ?: mutableListOf()
                } else {
                    mutableListOf()
                }

                if (!currentPurchases.contains(book.bookId)) {
                    currentPurchases.add(book.bookId)
                    db.collection("purchases").document(userId)
                        .set(mapOf("purchasedBooks" to currentPurchases))
                }
            }
    }

    private fun displayBookInfo(book: Book) {
        binding.titleText.text = book.title
        binding.authorText.text = book.author
        binding.infoText.text = if (book.info.isNotEmpty()) book.info else "Описание отсутствует"
        binding.costText.text = if (book.cost.isNotEmpty()) "Цена: ${book.cost} сом" else "Цена не указана"
        binding.readersCountText.text = if (book.readersCount.isNotEmpty()) "👥 Читателей: ${book.readersCount}" else "👥 Количество читателей неизвестно"

        if (book.imageUrl.isNotEmpty()) {
            Glide.with(requireContext())
                .load(book.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(binding.coverImage)
        } else {
            binding.coverImage.setImageResource(android.R.color.darker_gray)
            binding.coverImage.setBackgroundColor(android.graphics.Color.LTGRAY)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val gson = com.google.gson.Gson()
    }
}