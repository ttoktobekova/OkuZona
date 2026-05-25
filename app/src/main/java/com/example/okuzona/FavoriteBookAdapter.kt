package com.example.okuzona

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.okuzona.databinding.ItemFavoriteBookBinding

class FavoriteBookAdapter(
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit,
    private val onRemoveClick: (Book) -> Unit
) : RecyclerView.Adapter<FavoriteBookAdapter.FavoriteViewHolder>() {

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding =
            ItemFavoriteBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding, onBookClick, onRemoveClick)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size

    class FavoriteViewHolder(
        private val binding: ItemFavoriteBookBinding,
        private val onBookClick: (Book) -> Unit,
        private val onRemoveClick: (Book) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.tvTitle.text = book.title
            binding.tvAuthor.text = book.author

            Glide.with(binding.root.context)
                .load(book.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(binding.ivCover)

            binding.root.setOnClickListener { onBookClick(book) }
            binding.btnRemove.setOnClickListener { onRemoveClick(book) }
        }
    }
}