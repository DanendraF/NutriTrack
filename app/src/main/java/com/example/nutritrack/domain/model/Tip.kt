package com.example.nutritrack.domain.model

data class Tip(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val icon: String = "",
    val order: Int = 0
)
