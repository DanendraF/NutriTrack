package com.example.nutritrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey
    val foodId: String,
    val name: String,
    val nameIndonesian: String?,
    val category: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float,
    val sugar: Float,
    val sodium: Float,
    val servingSize: Float,
    val servingUnit: String,
    val barcode: String?,
    val imageUrl: String?,
    val isVerified: Boolean = false,
    val source: String = "local",
    val createdAt: Long = System.currentTimeMillis()
)
