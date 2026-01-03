package com.example.nutritrack.domain.model

data class Article(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val readTime: String = "",
    val imageUrl: String = "",
    val content: String = "",
    val author: String = "",
    val publishedAt: String = "",
    val order: Int = 0
)
