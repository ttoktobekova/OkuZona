package com.example.okuzona

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.okuzona.databinding.FragmentCartBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter
    private val gson = Gson()
    private var cartBooks = mutableListOf<Book>()
    private var totalPrice = 0.0
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadCartItems()
        setupButtons()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            books = cartBooks,
            onRemoveClick = { book ->
                removeFromCart(book)
            },
            onBookClick = { book ->
                val bundle = Bundle().apply {
                    putSerializable("book", book)
                }
                findNavController().navigate(R.id.action_cartFragment_to_informationFragment, bundle)
            }
        )
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecyclerView.adapter = cartAdapter
    }

    private fun loadCartItems() {
        cartBooks.clear()

        val sharedPref = requireContext().getSharedPreferences("cart", Context.MODE_PRIVATE)
        val cartIds = sharedPref.getStringSet("cart_items", emptySet())

        if (cartIds.isNullOrEmpty()) {
            showEmptyCart()
            return
        }

        var hasValidBooks = false
        for (bookId in cartIds) {
            val bookJson = sharedPref.getString("book_$bookId", null)
            if (bookJson != null) {
                val book = gson.fromJson(bookJson, Book::class.java)
                cartBooks.add(book)
                hasValidBooks = true
            }
        }

        if (hasValidBooks) {
            showCartContent()
            updateTotalPrice()
            cartAdapter.updateBooks(cartBooks)
        } else {
            showEmptyCart()
        }
    }

    private fun removeFromCart(book: Book) {
        val sharedPref = requireContext().getSharedPreferences("cart", Context.MODE_PRIVATE)
        val cartIds = sharedPref.getStringSet("cart_items", emptySet())?.toMutableSet()

        if (cartIds != null && cartIds.remove(book.bookId)) {
            sharedPref.edit().putStringSet("cart_items", cartIds).apply()
            sharedPref.edit().remove("book_${book.bookId}").apply()

            cartBooks.remove(book)
            updateTotalPrice()
            cartAdapter.updateBooks(cartBooks)

            updateCartBadge()

            Toast.makeText(requireContext(), "${book.title} удалена из корзины", Toast.LENGTH_SHORT).show()

            if (cartBooks.isEmpty()) {
                showEmptyCart()
            }
        }
    }

    private fun updateTotalPrice() {
        totalPrice = cartBooks.sumOf {
            it.cost.toDoubleOrNull() ?: 0.0
        }
        val commission = totalPrice * 0.035
        val total = totalPrice + commission

        binding.subtotalText.text = formatPrice(totalPrice)
        binding.commissionText.text = formatPrice(commission)
        binding.totalText.text = formatPrice(total)
    }

    private fun formatPrice(price: Double): String {
        return if (price == price.toInt().toDouble()) {
            "${price.toInt()} сом"
        } else {
            String.format("%.2f сом", price).replace(".00", "")
        }
    }

    private fun setupButtons() {
        binding.clearCartButton.setOnClickListener {
            if (cartBooks.isNotEmpty()) {
                clearCart()
            } else {
                Toast.makeText(requireContext(), "Корзина уже пуста", Toast.LENGTH_SHORT).show()
            }
        }

        binding.checkoutButton.setOnClickListener {
            if (cartBooks.isNotEmpty()) {
                showCheckoutDialog()
            } else {
                Toast.makeText(requireContext(), "Корзина пуста", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearCart() {
        val sharedPref = requireContext().getSharedPreferences("cart", Context.MODE_PRIVATE)
        sharedPref.edit().remove("cart_items").apply()

        for (book in cartBooks) {
            sharedPref.edit().remove("book_${book.bookId}").apply()
        }

        cartBooks.clear()
        cartAdapter.updateBooks(cartBooks)
        showEmptyCart()
        updateTotalPrice()

        updateCartBadge()

        Toast.makeText(requireContext(), "Корзина очищена", Toast.LENGTH_SHORT).show()
    }

    private fun showCheckoutDialog() {
        val total = totalPrice + (totalPrice * 0.035)

        val dialogView = layoutInflater.inflate(R.layout.dialog_payment, null)
        val cardNumberInput = dialogView.findViewById<TextInputEditText>(R.id.cardNumberInput)
        val expiryInput = dialogView.findViewById<TextInputEditText>(R.id.expiryInput)
        val cvvInput = dialogView.findViewById<TextInputEditText>(R.id.cvvInput)
        val amountText = dialogView.findViewById<TextView>(R.id.amountText)
        val commissionText = dialogView.findViewById<TextView>(R.id.commissionText)
        val totalText = dialogView.findViewById<TextView>(R.id.totalText)

        amountText.text = "Сумма: ${formatPrice(totalPrice)}"
        commissionText.text = "Комиссия (3.5%): ${formatPrice(totalPrice * 0.035)}"
        totalText.text = "Итого: ${formatPrice(total)}"

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Оплата картой")
            .setView(dialogView)
            .setPositiveButton("Оплатить") { _, _ ->
                val cardNumber = cardNumberInput.text.toString()
                val expiry = expiryInput.text.toString()
                val cvv = cvvInput.text.toString()

                if (validateCard(cardNumber, expiry, cvv)) {
                    processPayment()
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

    private fun processPayment() {
        val total = totalPrice + (totalPrice * 0.035)

        val progressDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Обработка платежа")
            .setMessage("Пожалуйста, подождите...")
            .setCancelable(false)
            .create()

        progressDialog.show()

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            progressDialog.dismiss()

            // Сохраняем все купленные книги
            for (book in cartBooks) {
                savePurchasedBook(book)
            }

            // Сохраняем копию книг для сообщения
            val purchasedCount = cartBooks.size

            // Очищаем корзину
            clearCart()

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Оплата успешна! 🎉")
                .setMessage("""
                    Оплачено книг: $purchasedCount
                    Общая сумма: ${formatPrice(total)}
                    Статус: Подтверждено
                    
                    Книги добавлены в вашу библиотеку.
                    Теперь вы можете читать их из главного экрана.
                """.trimIndent())
                .setPositiveButton("Отлично") { _, _ ->
                    findNavController().navigateUp()
                }
                .show()

        }, 2000)
    }

    private fun savePurchasedBook(book: Book) {
        val userId = auth.currentUser?.uid ?: return

        // Сохраняем в SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("purchased", Context.MODE_PRIVATE)
        val purchasedBooks = sharedPref.getStringSet("purchased_books", emptySet())?.toMutableSet() ?: mutableSetOf()

        purchasedBooks.add(book.bookId)
        sharedPref.edit().putStringSet("purchased_books", purchasedBooks).apply()

        val bookJson = gson.toJson(book)
        sharedPref.edit().putString("purchased_book_${book.bookId}", bookJson).apply()

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
                        .addOnSuccessListener {
                            println("Книга ${book.title} сохранена в Firestore")
                        }
                }
            }
    }

    private fun updateCartBadge() {
        (requireActivity() as? MainActivity)?.updateCartBadge()
    }

    private fun showEmptyCart() {
        binding.cartRecyclerView.visibility = View.GONE
        binding.emptyCartLayout.visibility = View.VISIBLE
        binding.clearCartButton.isEnabled = false
        binding.checkoutButton.isEnabled = false
        binding.subtotalText.text = "0 сом"
        binding.commissionText.text = "0 сом"
        binding.totalText.text = "0 сом"
    }

    private fun showCartContent() {
        binding.cartRecyclerView.visibility = View.VISIBLE
        binding.emptyCartLayout.visibility = View.GONE
        binding.clearCartButton.isEnabled = true
        binding.checkoutButton.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}