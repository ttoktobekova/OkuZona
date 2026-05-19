package com.example.okuzona

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

    // Для debounce поиска
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

        // Настройка RecyclerView
        val spanCount = 2
        binding.recyclerViewBooks.layoutManager = GridLayoutManager(requireContext(), spanCount)

        adapter = BookAdapter(emptyList()) { selectedBook ->
            val bundle = Bundle().apply {
                putString("pdfUrl", selectedBook.pdfUrl)
                putString("bookTitle", selectedBook.title)
                putString("bookAuthor", selectedBook.author)
                putString("bookImageUrl", selectedBook.imageUrl)
            }
            findNavController().navigate(R.id.action_bookListFragment_to_bookReaderFragment, bundle)
        }
        binding.recyclerViewBooks.adapter = adapter

        setupSearch()
        loadBooksFromFirestore()
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Debounce: ждём 300 мс после остановки ввода
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
                // Поиск по названию ИЛИ по автору (без учёта регистра)
                book.title.contains(query, ignoreCase = true) ||
                        book.author.contains(query, ignoreCase = true)
            }
        }

        adapter.updateBooks(filtered)

        // Показываем сообщение, если ничего не найдено и поиск не пустой
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
                        val book = document.toObject(Book::class.java)
                        allBooks.add(book)
                    }
                    filterBooks(binding.searchEditText.text.toString())
                    Toast.makeText(requireContext(), "Загружено книг: ${allBooks.size}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Ошибка загрузки: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacksAndMessages(null)
    }
}