package com.example.nutritrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val name: String,
    val email: String,
    val gender: String,
    val age: Int,
    val height: Float,
    val weight: Float,
    val activityLevel: String,
    val nutritionGoal: String,
    val targetCalories: Int,
    val targetProtein: Float,
    val targetCarbs: Float,
    val targetFat: Float,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
