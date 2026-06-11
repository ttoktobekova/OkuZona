package com.example.okuzona

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.example.okuzona.databinding.FragmentBookListBinding

class BookListFragment : Fragment() {

    private var _binding: FragmentBookListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: BookAdapter
    private val allBooks = mutableListOf<Book>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var purchasedBooks: Set<String>

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Загружаем список купленных книг
        loadPurchasedBooks()

        val spanCount = 2
        binding.recyclerViewBooks.layoutManager = GridLayoutManager(requireContext(), spanCount)

        adapter = BookAdapter(
            books = emptyList(),
            onBookClick = { selectedBook ->
                // Проверяем, куплена ли книга
                if (purchasedBooks.contains(selectedBook.bookId)) {
                    // Если книга куплена, открываем сразу для чтения
                    openBookForReading(selectedBook)
                } else {
                    // Если не куплена, открываем информацию о книге
                    val bundle = Bundle().apply {
                        putSerializable("book", selectedBook)
                    }
                    findNavController().navigate(R.id.action_bookListFragment_to_informationFragment, bundle)
                }
            },
            onFavoriteToggle = { book, isFavorite ->
                if (isFavorite) {
                    Toast.makeText(requireContext(), "Добавлено в избранное", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Удалено из избранного", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.recyclerViewBooks.adapter = adapter

        setupSearch()
        loadBooksFromFirestore()
    }

    private fun loadPurchasedBooks() {
        val sharedPref = requireContext().getSharedPreferences("purchased", Context.MODE_PRIVATE)
        purchasedBooks = sharedPref.getStringSet("purchased_books", emptySet()) ?: emptySet()
    }

    private fun openBookForReading(book: Book) {
        val bundle = Bundle().apply {
            putString("pdfUrl", book.pdfUrl)
            putString("bookTitle", book.title)
            putString("bookAuthor", book.author)
            putString("bookImageUrl", book.imageUrl)
        }
        findNavController().navigate(R.id.action_bookListFragment_to_bookReaderFragment, bundle)
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchRunnable?.let { handler.removeCallbacks(it) }
                searchRunnable = Runnable { filterBooks(s.toString()) }
                handler.postDelayed(searchRunnable!!, 300)
            }
        })
    }

    private fun filterBooks(query: String) {
        val filtered = if (query.isEmpty()) {
            allBooks.toList()
        } else {
            allBooks.filter { book ->
                book.title.contains(query, ignoreCase = true) ||
                        book.author.contains(query, ignoreCase = true)
            }
        }

        adapter.updateBooks(filtered, requireContext())

        if (filtered.isEmpty() && query.isNotEmpty()) {
            binding.textEmptySearch.visibility = View.VISIBLE
            binding.recyclerViewBooks.visibility = View.GONE
        } else {
            binding.textEmptySearch.visibility = View.GONE
            binding.recyclerViewBooks.visibility = View.VISIBLE
        }
    }

    private fun loadBooksFromFirestore() {
        Toast.makeText(requireContext(), "Загрузка книг...", Toast.LENGTH_SHORT).show()

        db.collection("books")
            .limit(100)
            .get()
            .addOnSuccessListener { documents ->
                allBooks.clear()
                if (documents.isEmpty()) {
                    Toast.makeText(requireContext(), "Книги не найдены", Toast.LENGTH_SHORT).show()
                    binding.textEmptySearch.text = "В библиотеке пока нет книг"
                    binding.textEmptySearch.visibility = View.VISIBLE
                    binding.recyclerViewBooks.visibility = View.GONE
                } else {
                    for (document in documents) {
                        val book = document.toObject(Book::class.java).copy(bookId = document.id)
                        allBooks.add(book)
                    }
                    adapter.updateBooks(allBooks, requireContext())
                    filterBooks(binding.searchEditText.text.toString())
                    Toast.makeText(requireContext(), "Загружено книг: ${allBooks.size}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Ошибка загрузки: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onResume() {
        super.onResume()
        // Обновляем список купленных книг при возврате на экран
        loadPurchasedBooks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacksAndMessages(null)
    }
}