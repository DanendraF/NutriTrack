package com.example.nutritrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey
    val logId: String,
    val userId: String,
    val date: String,
    val totalCalories: Float,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val mealCount: Int,
    val updatedAt: Long = System.currentTimeMillis()
)
