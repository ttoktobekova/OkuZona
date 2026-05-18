package com.example.okuzona

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = BookAdapter(bookList) { selectedBook ->
            // Переход к читалке с передачей аргумента pdfUrl через Bundle
            val bundle = Bundle().apply {
                putString("pdfUrl", selectedBook.pdfUrl)
            }
            findNavController().navigate(R.id.action_bookListFragment_to_bookReaderFragment, bundle)
        }
        recyclerView.adapter = adapter

        loadBooksFromFirestore()
        return view
    }

    private fun loadBooksFromFirestore() {
        db.collection("books")
            .get()
            .addOnSuccessListener { documents ->
                bookList.clear()
                for (document in documents) {
                    val book = document.toObject(Book::class.java)
                    bookList.add(book)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
            }
    }
}
