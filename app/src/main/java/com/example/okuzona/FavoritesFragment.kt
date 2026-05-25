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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences
    private lateinit var adapter: FavoriteBookAdapter
    private val favoriteBooks = mutableListOf<Book>()
    private val gson = Gson()

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
        val json = prefs.getString("favorite_books", "[]")
        val type = object : TypeToken<List<Book>>() {}.type
        val books: List<Book> = gson.fromJson(json, type)
        favoriteBooks.clear()
        favoriteBooks.addAll(books)
        adapter.updateBooks(favoriteBooks)
        updateFavoriteCount()
    }

    private fun saveFavorites() {
        val json = gson.toJson(favoriteBooks)
        prefs.edit().putString("favorite_books", json).apply()
        updateFavoriteCount()
        updateEmptyState()
    }

    private fun removeFromFavorites(book: Book) {
        favoriteBooks.removeAll { it.bookId == book.bookId }
        saveFavorites()
        adapter.updateBooks(favoriteBooks)
        Toast.makeText(requireContext(), "Книга удалена из избранного", Toast.LENGTH_SHORT).show()
    }

    private fun updateFavoriteCount() {
        binding.tvFavoriteCount.text = "${favoriteBooks.size} ${getBooksText(favoriteBooks.size)}"
    }

    private fun getBooksText(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "книга"
            count % 10 in 2..4 && (count % 100 < 10 || count % 100 > 20) -> "книги"
            else -> "книг"
        }
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
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}