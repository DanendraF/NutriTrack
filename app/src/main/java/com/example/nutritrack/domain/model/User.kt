package com.example.nutritrack.domain.model

data class User(
    val userId: String,
    val name: String,
    val email: String,
    val gender: String,
    val age: Int,
    val height: Float,
    val weight: Float,
    val activityLevel: ActivityLevel,
    val nutritionGoal: NutritionGoal,
    val targetCalories: Int,
    val targetMacros: Macros
)

data class Macros(
    val protein: Float,
    val carbs: Float,
    val fat: Float
)

enum class ActivityLevel(val multiplier: Float, val displayName: String) {
    SEDENTARY(1.2f, "Sedentary"),
    LIGHTLY_ACTIVE(1.375f, "Lightly Active"),
    MODERATELY_ACTIVE(1.55f, "Moderately Active"),
    VERY_ACTIVE(1.725f, "Very Active"),
    EXTREMELY_ACTIVE(1.9f, "Extremely Active");

    companion object {
        fun fromString(value: String): ActivityLevel {
            return when (value.lowercase()) {
                "sedentary" -> SEDENTARY
                "lightly active", "lightly_active" -> LIGHTLY_ACTIVE
                "moderately active", "moderately_active" -> MODERATELY_ACTIVE
                "very active", "very_active" -> VERY_ACTIVE
                "extremely active", "extremely_active" -> EXTREMELY_ACTIVE
                else -> SEDENTARY
            }
        }
    }
}

enum class NutritionGoal(val displayName: String) {
    LOSE_WEIGHT("Lose Weight"),
    MAINTAIN_WEIGHT("Maintain Weight"),
    GAIN_WEIGHT("Gain Weight");

    companion object {
        fun fromString(value: String): NutritionGoal {
            return when (value.lowercase()) {
                "lose weight", "lose_weight" -> LOSE_WEIGHT
                "maintain weight", "maintain_weight" -> MAINTAIN_WEIGHT
                "gain weight", "gain_weight" -> GAIN_WEIGHT
                else -> MAINTAIN_WEIGHT
            }
        }
    }
}
