package com.example.nutritrack.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealDataInitializer @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun initializeMealsForUser(userId: String) {
        try {
            android.util.Log.d("MealDataInitializer", "🍽️ Checking if meals need initialization for user: $userId")

            // Check if user already has meals - if yes, skip initialization
            val existingMeals = firestore.collection("meals")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            if (!existingMeals.isEmpty) {
                android.util.Log.d("MealDataInitializer", "⏭️ User already has meals, skipping initialization")
                return
            }

            android.util.Log.d("MealDataInitializer", "✨ No meals found, initializing sample data...")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()

            // Create meals for the last 7 days
            for (daysAgo in 0..6) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_MONTH, -daysAgo)
                val date = dateFormat.format(calendar.time)

                android.util.Log.d("MealDataInitializer", "📅 Creating meals for: $date")

                // Create 2-3 meals per day
                val mealsForDay = when (daysAgo % 3) {
                    0 -> getMealsDay1(userId, date)
                    1 -> getMealsDay2(userId, date)
                    else -> getMealsDay3(userId, date)
                }

                mealsForDay.forEach { meal ->
                    val docId = "${userId}_${date}_${meal["mealType"]}"
                    firestore.collection("meals")
                        .document(docId)
                        .set(meal)
                        .await()

                    android.util.Log.d("MealDataInitializer", "  ✅ Added: ${meal["foodName"]} (${meal["calories"]} kcal)")
                }
            }

            android.util.Log.d("MealDataInitializer", "✅ Successfully initialized meals for 7 days!")
        } catch (e: Exception) {
            android.util.Log.e("MealDataInitializer", "❌ Error initializing meals", e)
        }
    }

    private fun getMealsDay1(userId: String, date: String): List<Map<String, Any>> {
        return listOf(
            mapOf(
                "userId" to userId,
                "foodId" to "food_banana",
                "foodName" to "Banana",
                "date" to date,
                "mealType" to "breakfast",
                "quantity" to 1.0,
                "servingSize" to "1 medium",
                "calories" to 89,
                "protein" to 1,
                "carbs" to 23,
                "fat" to 0,
                "timestamp" to Date()
            ),
            mapOf(
                "userId" to userId,
                "foodId" to "food_chicken",
                "foodName" to "Chicken Breast (Cooked)",
                "date" to date,
                "mealType" to "lunch",
                "quantity" to 1.5,
                "servingSize" to "100g",
                "calories" to 165,
                "protein" to 31,
                "carbs" to 0,
                "fat" to 4,
                "timestamp" to Date()
            ),
            mapOf(
                "userId" to userId,
                "foodId" to "food_rice",
                "foodName" to "White Rice",
                "date" to date,
                "mealType" to "dinner",
                "quantity" to 2.0,
                "servingSize" to "100g",
                "calories" to 130,
                "protein" to 3,
                "carbs" to 28,
                "fat" to 0,
                "timestamp" to Date()
            )
        )
    }

    private fun getMealsDay2(userId: String, date: String): List<Map<String, Any>> {
        return listOf(
            mapOf(
                "userId" to userId,
                "foodId" to "food_oatmeal",
                "foodName" to "Oatmeal",
                "date" to date,
                "mealType" to "breakfast",
                "quantity" to 1.0,
                "servingSize" to "100g",
                "calories" to 68,
                "protein" to 2,
                "carbs" to 12,
                "fat" to 1,
                "timestamp" to Date()
            ),
            mapOf(
                "userId" to userId,
                "foodId" to "food_salmon",
                "foodName" to "Grilled Salmon",
                "date" to date,
                "mealType" to "lunch",
                "quantity" to 1.0,
                "servingSize" to "100g",
                "calories" to 206,
                "protein" to 22,
                "carbs" to 0,
                "fat" to 13,
                "timestamp" to Date()
            )
        )
    }

    private fun getMealsDay3(userId: String, date: String): List<Map<String, Any>> {
        return listOf(
            mapOf(
                "userId" to userId,
                "foodId" to "food_egg",
                "foodName" to "Boiled Egg",
                "date" to date,
                "mealType" to "breakfast",
                "quantity" to 2.0,
                "servingSize" to "1 egg",
                "calories" to 78,
                "protein" to 6,
                "carbs" to 1,
                "fat" to 5,
                "timestamp" to Date()
            ),
            mapOf(
                "userId" to userId,
                "foodId" to "food_broccoli",
                "foodName" to "Steamed Broccoli",
                "date" to date,
                "mealType" to "lunch",
                "quantity" to 1.5,
                "servingSize" to "100g",
                "calories" to 35,
                "protein" to 2,
                "carbs" to 7,
                "fat" to 0,
                "timestamp" to Date()
            ),
            mapOf(
                "userId" to userId,
                "foodId" to "food_pasta",
                "foodName" to "Whole Wheat Pasta",
                "date" to date,
                "mealType" to "dinner",
                "quantity" to 1.0,
                "servingSize" to "100g",
                "calories" to 124,
                "protein" to 5,
                "carbs" to 26,
                "fat" to 1,
                "timestamp" to Date()
            )
        )
    }
}
