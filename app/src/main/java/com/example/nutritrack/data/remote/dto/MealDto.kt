package com.example.nutritrack.data.remote.dto

import com.google.gson.annotations.SerializedName

// Request DTO for creating a meal
data class CreateMealRequest(
    @SerializedName("foodId") val foodId: String,
    @SerializedName("foodName") val foodName: String,
    @SerializedName("date") val date: String, // YYYY-MM-DD
    @SerializedName("mealType") val mealType: String, // breakfast, lunch, dinner, snack
    @SerializedName("portion") val portion: Double,
    @SerializedName("nutrition") val nutrition: NutritionDto
)

// Response DTO for a meal
data class MealResponse(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("foodId") val foodId: String,
    @SerializedName("foodName") val foodName: String,
    @SerializedName("date") val date: String,
    @SerializedName("mealType") val mealType: String,
    @SerializedName("portion") val portion: Double,
    @SerializedName("nutrition") val nutrition: NutritionDto,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("createdAt") val createdAt: String
)

// Request DTO for updating a meal
data class UpdateMealRequest(
    @SerializedName("mealType") val mealType: String? = null,
    @SerializedName("portion") val portion: Double? = null,
    @SerializedName("nutrition") val nutrition: NutritionDto? = null
)

// Daily log response
data class DailyLogResponse(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("date") val date: String,
    @SerializedName("meals") val meals: List<MealResponse>,
    @SerializedName("totalCalories") val totalCalories: Double,
    @SerializedName("totalProtein") val totalProtein: Double,
    @SerializedName("totalCarbs") val totalCarbs: Double,
    @SerializedName("totalFat") val totalFat: Double,
    @SerializedName("updatedAt") val updatedAt: String
)

// Daily logs list response
data class DailyLogsResponse(
    @SerializedName("logs") val logs: List<DailyLogResponse>,
    @SerializedName("count") val count: Int
)
