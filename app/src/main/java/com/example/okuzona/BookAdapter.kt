package com.example.okuzona

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.okuzona.databinding.ItemBookBinding

class BookAdapter(
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    fun updateBooks(newBooks: List<Book>) {
        val diffCallback = BookDiffCallback(books, newBooks)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        books = newBooks
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding, onBookClick)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size

    override fun onViewRecycled(holder: BookViewHolder) {
        super.onViewRecycled(holder)
        holder.clearImage()
    }

    class BookViewHolder(
        private val binding: ItemBookBinding,
        private val onBookClick: (Book) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.textBookTitle.text = book.title
            binding.textBookAuthor.text = book.author

            if (book.imageUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(book.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(binding.imageBookCover)
            } else {
                binding.imageBookCover.setImageResource(android.R.color.darker_gray)
                binding.imageBookCover.setBackgroundColor(android.graphics.Color.LTGRAY)
            }

            binding.root.setOnClickListener { onBookClick(book) }
        }

        fun clearImage() {
            Glide.with(binding.root.context).clear(binding.imageBookCover)
            binding.imageBookCover.setImageDrawable(null)
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
        oldList[oldItemPosition].pdfUrl == newList[newItemPosition].pdfUrl

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}