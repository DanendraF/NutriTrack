package com.example.nutritrack.domain.model

data class Meal(
    val mealId: String,
    val userId: String,
    val food: Food,
    val mealType: MealType,
    val quantity: Float,
    val portionUnit: String,
    val totalNutrition: NutritionInfo,
    val date: String,
    val timestamp: Long,
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
