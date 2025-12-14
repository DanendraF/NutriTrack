package com.example.nutritrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey
    val mealId: String,
    val userId: String,
    val foodId: String,
    val foodName: String,
    val mealType: String,
    val quantity: Float,
    val portionUnit: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val isSynced: Boolean = false
)
