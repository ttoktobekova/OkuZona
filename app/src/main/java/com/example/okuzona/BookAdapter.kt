package com.example.okuzona

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BookAdapter(
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit,
    private val onFavoriteToggle: (Book, Boolean) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    private var favoriteIds = mutableSetOf<String>()

    fun updateBooks(newBooks: List<Book>, context: Context) {
        prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
        loadFavoriteIds()
        val diffCallback = BookDiffCallback(books, newBooks)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        books = newBooks
        diffResult.dispatchUpdatesTo(this)
    }

    private fun loadFavoriteIds() {
        val json = prefs.getString("favorite_books", "[]")
        val type = object : TypeToken<List<Book>>() {}.type
        val favorites: List<Book> = gson.fromJson(json, type)
        favoriteIds = favorites.map { it.bookId }.toMutableSet()
    }

    private fun isFavorite(bookId: String): Boolean = favoriteIds.contains(bookId)

    fun addFavorite(book: Book) {
        if (!favoriteIds.contains(book.bookId)) {
            favoriteIds.add(book.bookId)
            saveFavorites()
        }
    }

    fun removeFavorite(bookId: String) {
        if (favoriteIds.contains(bookId)) {
            favoriteIds.remove(bookId)
            saveFavorites()
        }
    }

    private fun saveFavorites() {
        val json = prefs.getString("favorite_books", "[]")
        val type = object : TypeToken<List<Book>>() {}.type
        val currentFavorites: List<Book> = gson.fromJson(json, type)
        val currentFavoritesMutable: MutableList<Book> = currentFavorites.toMutableList()

        val updatedFavorites = currentFavoritesMutable.filter { favoriteIds.contains(it.bookId) }
        val newFavorites = books.filter { favoriteIds.contains(it.bookId) && !updatedFavorites.contains(it) }
        val finalFavorites = updatedFavorites + newFavorites

        val newJson = gson.toJson(finalFavorites)
        prefs.edit().putString("favorite_books", newJson).apply()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        prefs = parent.context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
        loadFavoriteIds()
        return BookViewHolder(view, onBookClick, onFavoriteToggle, this)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position], isFavorite(books[position].bookId))
    }

    override fun getItemCount() = books.size

    override fun onViewRecycled(holder: BookViewHolder) {
        super.onViewRecycled(holder)
        holder.clearImage()
    }

    class BookViewHolder(
        view: View,
        private val onBookClick: (Book) -> Unit,
        private val onFavoriteToggle: (Book, Boolean) -> Unit,
        private val adapter: BookAdapter
    ) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.textBookTitle)
        private val author: TextView = view.findViewById(R.id.textBookAuthor)
        private val cover: ImageView = view.findViewById(R.id.imageBookCover)
        private val btnFavorite: ImageButton = view.findViewById(R.id.btnFavorite)

        fun bind(book: Book, isFavorite: Boolean) {
            title.text = book.title
            author.text = book.author

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

            // Устанавливаем иконку сердечка
            if (isFavorite) {
                btnFavorite.setImageResource(R.drawable.ic_favorite_filled)
            } else {
                btnFavorite.setImageResource(R.drawable.ic_favorite_border)
            }

            btnFavorite.setOnClickListener {
                val newState = !isFavorite
                if (newState) {
                    btnFavorite.setImageResource(R.drawable.ic_favorite_filled)
                    adapter.addFavorite(book)
                } else {
                    btnFavorite.setImageResource(R.drawable.ic_favorite_border)
                    adapter.removeFavorite(book.bookId)
                }
                onFavoriteToggle(book, newState)
            }

            itemView.setOnClickListener { onBookClick(book) }
        }

        fun clearImage() {
            Glide.with(itemView.context).clear(cover)
            cover.setImageDrawable(null)
        }
    }
}

class BookDiffCallback(
    private val oldList: List<Book>,
    private val newList: List<Book>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].bookId == newList[newItemPosition].bookId

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}