package com.example.nutritrack.data.repository

import com.example.nutritrack.domain.model.Meal
import com.example.nutritrack.domain.model.MealType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreMealRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val MEALS_COLLECTION = "meals"
        private const val DAILY_LOGS_COLLECTION = "dailyLogs"
    }

    /**
     * Add a new meal to Firestore
     */
    suspend fun addMeal(userId: String, meal: Meal): Result<String> {
        return try {
            val mealData = hashMapOf(
                "foodId" to meal.foodId,
                "foodName" to meal.foodName,
                "mealType" to meal.mealType.name,
                "servingSize" to meal.servingSize,
                "quantity" to meal.quantity,
                "calories" to meal.calories,
                "protein" to meal.protein,
                "carbs" to meal.carbs,
                "fat" to meal.fat,
                "timestamp" to meal.timestamp,
                "date" to getDateString(meal.timestamp),
                "createdAt" to Date()
            )

            val docRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(MEALS_COLLECTION)
                .add(mealData)
                .await()

            // Update daily log
            updateDailyLog(userId, meal.timestamp)

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all meals for a specific date
     */
    fun getMealsForDate(userId: String, date: String): Flow<List<Meal>> = callbackFlow {
        val listener = firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(MEALS_COLLECTION)
            .whereEqualTo("date", date)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val meals = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Meal(
                            id = doc.id,
                            foodId = doc.getString("foodId") ?: "",
                            foodName = doc.getString("foodName") ?: "",
                            mealType = MealType.valueOf(doc.getString("mealType") ?: "BREAKFAST"),
                            servingSize = doc.getString("servingSize") ?: "",
                            quantity = doc.getDouble("quantity")?.toFloat() ?: 1f,
                            calories = doc.getLong("calories")?.toInt() ?: 0,
                            protein = doc.getLong("protein")?.toInt() ?: 0,
                            carbs = doc.getLong("carbs")?.toInt() ?: 0,
                            fat = doc.getLong("fat")?.toInt() ?: 0,
                            timestamp = doc.getDate("timestamp") ?: Date()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(meals)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get today's meals
     */
    fun getTodaysMeals(userId: String): Flow<List<Meal>> {
        val today = getDateString(Date())
        return getMealsForDate(userId, today)
    }

    /**
     * Update a meal
     */
    suspend fun updateMeal(userId: String, meal: Meal): Result<Unit> {
        return try {
            val mealData = hashMapOf(
                "foodName" to meal.foodName,
                "mealType" to meal.mealType.name,
                "servingSize" to meal.servingSize,
                "quantity" to meal.quantity,
                "calories" to meal.calories,
                "protein" to meal.protein,
                "carbs" to meal.carbs,
                "fat" to meal.fat,
                "timestamp" to meal.timestamp,
                "date" to getDateString(meal.timestamp),
                "updatedAt" to Date()
            )

            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(MEALS_COLLECTION)
                .document(meal.id)
                .update(mealData as Map<String, Any>)
                .await()

            // Update daily log
            updateDailyLog(userId, meal.timestamp)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a meal
     */
    suspend fun deleteMeal(userId: String, mealId: String, timestamp: Date): Result<Unit> {
        return try {
            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(MEALS_COLLECTION)
                .document(mealId)
                .delete()
                .await()

            // Update daily log
            updateDailyLog(userId, timestamp)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get meal history (last 30 days)
     */
    suspend fun getMealHistory(userId: String, limit: Int = 100): Result<List<Meal>> {
        return try {
            val snapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(MEALS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val meals = snapshot.documents.mapNotNull { doc ->
                try {
                    Meal(
                        id = doc.id,
                        foodId = doc.getString("foodId") ?: "",
                        foodName = doc.getString("foodName") ?: "",
                        mealType = MealType.valueOf(doc.getString("mealType") ?: "BREAKFAST"),
                        servingSize = doc.getString("servingSize") ?: "",
                        quantity = doc.getDouble("quantity")?.toFloat() ?: 1f,
                        calories = doc.getLong("calories")?.toInt() ?: 0,
                        protein = doc.getLong("protein")?.toInt() ?: 0,
                        carbs = doc.getLong("carbs")?.toInt() ?: 0,
                        fat = doc.getLong("fat")?.toInt() ?: 0,
                        timestamp = doc.getDate("timestamp") ?: Date()
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(meals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update daily log totals
     */
    private suspend fun updateDailyLog(userId: String, date: Date) {
        try {
            val dateString = getDateString(date)

            // Get all meals for this date
            val snapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(MEALS_COLLECTION)
                .whereEqualTo("date", dateString)
                .get()
                .await()

            // Calculate totals
            var totalCalories = 0
            var totalProtein = 0
            var totalCarbs = 0
            var totalFat = 0

            snapshot.documents.forEach { doc ->
                totalCalories += doc.getLong("calories")?.toInt() ?: 0
                totalProtein += doc.getLong("protein")?.toInt() ?: 0
                totalCarbs += doc.getLong("carbs")?.toInt() ?: 0
                totalFat += doc.getLong("fat")?.toInt() ?: 0
            }

            // Update or create daily log
            val dailyLogData = hashMapOf(
                "date" to dateString,
                "totalCalories" to totalCalories,
                "totalProtein" to totalProtein,
                "totalCarbs" to totalCarbs,
                "totalFat" to totalFat,
                "mealCount" to snapshot.size(),
                "updatedAt" to Date()
            )

            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(DAILY_LOGS_COLLECTION)
                .document(dateString)
                .set(dailyLogData)
                .await()
        } catch (e: Exception) {
            // Log error but don't fail the operation
            e.printStackTrace()
        }
    }

    /**
     * Get daily log totals for a specific date
     */
    fun getDailyLog(userId: String, date: String): Flow<DailyLogData> = callbackFlow {
        val listener = firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(DAILY_LOGS_COLLECTION)
            .document(date)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val dailyLog = if (snapshot?.exists() == true) {
                    DailyLogData(
                        date = snapshot.getString("date") ?: date,
                        totalCalories = snapshot.getLong("totalCalories")?.toInt() ?: 0,
                        totalProtein = snapshot.getLong("totalProtein")?.toInt() ?: 0,
                        totalCarbs = snapshot.getLong("totalCarbs")?.toInt() ?: 0,
                        totalFat = snapshot.getLong("totalFat")?.toInt() ?: 0,
                        mealCount = snapshot.getLong("mealCount")?.toInt() ?: 0
                    )
                } else {
                    DailyLogData(date = date)
                }

                trySend(dailyLog)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get today's daily log
     */
    fun getTodaysDailyLog(userId: String): Flow<DailyLogData> {
        val today = getDateString(Date())
        return getDailyLog(userId, today)
    }

    /**
     * Helper function to format date as YYYY-MM-DD
     */
    private fun getDateString(date: Date): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }
}

/**
 * Daily log data class
 */
data class DailyLogData(
    val date: String,
    val totalCalories: Int = 0,
    val totalProtein: Int = 0,
    val totalCarbs: Int = 0,
    val totalFat: Int = 0,
    val mealCount: Int = 0
)
