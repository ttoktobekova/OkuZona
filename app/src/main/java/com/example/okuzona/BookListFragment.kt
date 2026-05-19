package com.example.okuzona

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class BookListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdapter
    private val bookList = mutableListOf<Book>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewBooks)

        // Используем GridLayoutManager с 2 колонками для отображения книг
        val spanCount = 2 // Количество колонок
        val layoutManager = GridLayoutManager(requireContext(), spanCount)
        recyclerView.layoutManager = layoutManager

        // Обновленный вызов адаптера с полной информацией о книге
        adapter = BookAdapter(bookList) { selectedBook ->
            // Переход к читалке с передачей всех необходимых аргументов через Bundle
            val bundle = Bundle().apply {
                putString("pdfUrl", selectedBook.pdfUrl)
                putString("bookTitle", selectedBook.title)
                putString("bookAuthor", selectedBook.author)
                putString("bookImageUrl", selectedBook.imageUrl)
            }
            findNavController().navigate(R.id.action_bookListFragment_to_bookReaderFragment, bundle)
        }
        recyclerView.adapter = adapter

        loadBooksFromFirestore()
        return view
    }

    private fun loadBooksFromFirestore() {
        // Показываем индикатор загрузки (можно добавить ProgressBar)
        Toast.makeText(requireContext(), "Загрузка книг...", Toast.LENGTH_SHORT).show()

        db.collection("books")
            .get()
            .addOnSuccessListener { documents ->
                bookList.clear()
                if (documents.isEmpty()) {
                    Toast.makeText(requireContext(), "Книги не найдены", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in documents) {
                        val book = document.toObject(Book::class.java)
                        bookList.add(book)
                    }
                    adapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Загружено книг: ${bookList.size}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Ошибка загрузки данных: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}