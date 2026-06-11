package com.example.okuzona

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(
    private var books: List<Book>,
    private val onRemoveClick: (Book) -> Unit,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
 
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view, onRemoveClick, onBookClick)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size

    class CartViewHolder(
        itemView: View,
        private val onRemoveClick: (Book) -> Unit,
        private val onBookClick: (Book) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val bookCover: ImageView = itemView.findViewById(R.id.bookCover)
        private val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
        private val bookAuthor: TextView = itemView.findViewById(R.id.bookAuthor)
        private val bookPrice: TextView = itemView.findViewById(R.id.bookPrice)
        private val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)

        fun bind(book: Book) {
            bookTitle.text = book.title
            bookAuthor.text = book.author
            // Форматируем цену для отображения
            val price = book.cost.toDoubleOrNull() ?: 0.0
            bookPrice.text = if (price == price.toInt().toDouble()) {
                "${price.toInt()} сом"
            } else {
                "${String.format("%.2f", price)} сом".replace(".00", "")
            }

            if (book.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(book.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(bookCover)
            } else {
                bookCover.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            removeButton.setOnClickListener {
                onRemoveClick(book)
            }

            itemView.setOnClickListener {
                onBookClick(book)
            }
        }
    }
}