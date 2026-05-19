package com.example.okuzona

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BookAdapter(
    private val books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book)
        holder.itemView.setOnClickListener { onBookClick(book) }
    }

    override fun getItemCount() = books.size

    override fun onViewRecycled(holder: BookViewHolder) {
        super.onViewRecycled(holder)
        holder.clearImage()
    }

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.textBookTitle)
        private val author: TextView = view.findViewById(R.id.textBookAuthor)
        private val cover: ImageView = view.findViewById(R.id.imageBookCover)

        fun bind(book: Book) {
            title.text = book.title
            author.text = book.author

            // Загрузка обложки книги с помощью Glide
            if (book.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(book.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(cover)
            } else {
                cover.setImageResource(android.R.color.darker_gray)
                cover.setBackgroundColor(android.graphics.Color.LTGRAY)
            }
        }

        fun clearImage() {
            Glide.with(itemView.context).clear(cover)
            cover.setImageDrawable(null)
        }
    }
}