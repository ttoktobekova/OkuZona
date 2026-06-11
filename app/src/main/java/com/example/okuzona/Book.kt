package com.example.okuzona

import java.io.Serializable

data class Book(
    val bookId: String = "",
    val title: String = "",
    val author: String = "",
    val pdfUrl: String = "",
    val imageUrl: String = "",
    val info: String = "",
    val cost: String = "",
    val readersCount: String = ""
) : Serializable