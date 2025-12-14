package com.example.nutritrack.domain.model

data class Food(
    val foodId: String,
    val name: String,
    val nameIndonesian: String?,
    val category: String,
    val nutrition: NutritionInfo,
    val servingSize: ServingSize,
    val barcode: String?,
    val imageUrl: String?,
    val isVerified: Boolean = false,
    val source: String = "local"
)

data class NutritionInfo(
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float,
    val sugar: Float,
    val sodium: Float
)

data class ServingSize(
    val amount: Float,
    val unit: String
)

enum class FoodCategory(val displayName: String) {
    FRUITS("Fruits"),
    VEGETABLES("Vegetables"),
    GRAINS("Grains"),
    PROTEIN("Protein"),
    DAIRY("Dairy"),
    SNACKS("Snacks"),
    BEVERAGES("Beverages"),
    OTHERS("Others");

    companion object {
        fun fromString(value: String): FoodCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHERS
        }
    }
}
