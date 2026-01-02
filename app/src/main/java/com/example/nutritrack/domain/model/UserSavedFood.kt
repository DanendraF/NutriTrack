package com.example.nutritrack.domain.model

data class UserSavedFood(
    val id: String = "",
    val userId: String = "",
    val foodId: String = "",
    val foodName: String = "",
    val note: String = "", // e.g., "My regular portion", "Breakfast size"
    val customPortion: Float = 1.0f, // Custom portion multiplier
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val servingSize: String = "", // e.g., "100 g", "1 potong"
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis(),
    val useCount: Int = 0 // Track how often user logs this food
)
