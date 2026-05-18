package com.example.okuzona

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Импортируем Glide

class BookAdapter(
    private val books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.textBookTitle)
        val author: TextView = view.findViewById(R.id.textBookAuthor)
        val cover: ImageView = view.findViewById(R.id.imageBookCover) // НАШЛИ КАРТИНКУ
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title
        holder.author.text = book.author

        // ИСПОЛЬЗУЕМ GLIDE ДЛЯ ЗАГРУЗКИ ОБЛОЖКИ
        if (book.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(book.imageUrl)
                .into(holder.cover)
        } else {
            // Если обложки нет, оставляем пустой серый цвет
            holder.cover.setImageResource(android.R.color.darker_gray)
        }
        holder.itemView.setOnClickListener { onBookClick(book) }
    }

    override fun getItemCount() = books.size
}
