package com.example.nutritrack.domain.model

import java.util.Date

data class Meal(
    val id: String = "",
    val foodId: String = "",
    val foodName: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val servingSize: String = "100g",
    val quantity: Float = 1f,
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val timestamp: Date = Date(),
    val imageUrl: String? = null
)

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack");

    companion object {
        fun fromString(value: String): MealType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: SNACK
        }
    }
}

data class DailyLog(
    val logId: String,
    val userId: String,
    val date: String,
    val totalNutrition: NutritionInfo,
    val meals: List<Meal>,
    val mealCount: Int
)
