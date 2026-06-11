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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.okuzona.databinding.FragmentFavoritesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences
    private lateinit var adapter: FavoriteBookAdapter
    private val favoriteBooks = mutableListOf<Book>()
    private val gson = Gson()
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadFavorites()
        updateEmptyState()
    }

    private fun setupRecyclerView() {
        adapter = FavoriteBookAdapter(
            books = favoriteBooks,
            onBookClick = { book ->
                val bundle = Bundle().apply {
                    putString("pdfUrl", book.pdfUrl)
                    putString("bookTitle", book.title)
                    putString("bookAuthor", book.author)
                    putString("bookImageUrl", book.imageUrl)
                }
                findNavController().navigate(R.id.action_favoritesFragment_to_bookReaderFragment, bundle)
            },
            onRemoveClick = { book ->
                removeFromFavorites(book)
            }
        )
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavorites.adapter = adapter
    }

    private fun loadFavorites() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            favoriteBooks.clear()
            adapter.updateBooks(favoriteBooks)
            updateEmptyState()
            return
        }

        // Загружаем избранное из Firestore
        db.collection("favorites").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val favoriteBooksIds = document.get("favoriteBooks") as? List<String> ?: emptyList()
                    loadFavoriteBooksDetails(favoriteBooksIds, userId)
                } else {
                    favoriteBooks.clear()
                    adapter.updateBooks(favoriteBooks)
                    updateEmptyState()
                }
            }
            .addOnFailureListener {
                loadFromLocal()
            }
    }

    private fun loadFavoriteBooksDetails(bookIds: List<String>, userId: String) {
        if (bookIds.isEmpty()) {
            favoriteBooks.clear()
            adapter.updateBooks(favoriteBooks)
            updateEmptyState()
            return
        }

        // Загружаем детали книг из Firestore
        favoriteBooks.clear()

        for (bookId in bookIds) {
            db.collection("favorites_details").document(userId)
                .collection("books").document(bookId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val bookJson = document.getString("bookData")
                        if (bookJson != null) {
                            val book = gson.fromJson(bookJson, Book::class.java)
                            if (!favoriteBooks.any { it.bookId == book.bookId }) {
                                favoriteBooks.add(book)
                            }
                            adapter.updateBooks(favoriteBooks.toList())
                            updateEmptyState()
                        }
                    }
                }
        }

        // Также сохраняем в локальное хранилище
        saveToLocal()
    }

    private fun loadFromLocal() {
        val json = prefs.getString("favorite_books", "[]")
        val type = object : TypeToken<List<Book>>() {}.type
        val books: List<Book> = gson.fromJson(json, type)
        favoriteBooks.clear()
        favoriteBooks.addAll(books)
        adapter.updateBooks(favoriteBooks)
        updateEmptyState()
    }

    private fun saveToLocal() {
        val json = gson.toJson(favoriteBooks)
        prefs.edit().putString("favorite_books", json).apply()
    }

    private fun removeFromFavorites(book: Book) {
        favoriteBooks.removeAll { it.bookId == book.bookId }
        saveToLocal()

        // Также удаляем из Firestore через BookAdapter
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("favorites").document(userId).get()
                .addOnSuccessListener { document ->
                    val currentFavorites = if (document.exists()) {
                        (document.get("favoriteBooks") as? List<String>)?.toMutableList() ?: mutableListOf()
                    } else {
                        mutableListOf()
                    }
                    currentFavorites.remove(book.bookId)
                    db.collection("favorites").document(userId)
                        .set(mapOf("favoriteBooks" to currentFavorites))

                    // Удаляем детали книги
                    db.collection("favorites_details").document(userId)
                        .collection("books").document(book.bookId)
                        .delete()
                }
        }

        adapter.updateBooks(favoriteBooks)
        updateEmptyState()
        Toast.makeText(requireContext(), "Книга удалена из избранного", Toast.LENGTH_SHORT).show()
    }

    private fun updateEmptyState() {
        if (favoriteBooks.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.recyclerViewFavorites.visibility = View.GONE
            binding.tvFavoriteCount.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.recyclerViewFavorites.visibility = View.VISIBLE
            binding.tvFavoriteCount.visibility = View.VISIBLE
            binding.tvFavoriteCount.text = "${favoriteBooks.size} ${getBooksText(favoriteBooks.size)}"
        }
    }

    private fun getBooksText(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "книга"
            count % 10 in 2..4 && (count % 100 < 10 || count % 100 > 20) -> "книги"
            else -> "книг"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}