package com.example.nutritrack.data.repository

import com.example.nutritrack.data.remote.Result
import com.example.nutritrack.data.remote.api.NutriTrackApiService
import com.example.nutritrack.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ApiMealRepository(
    private val apiService: NutriTrackApiService
) {

    fun createMeal(
        foodId: String,
        foodName: String,
        date: String,
        mealType: String,
        portion: Double,
        nutrition: NutritionDto
    ): Flow<Result<MealResponse>> = flow {
        try {
            android.util.Log.d("ApiMealRepository", "=== createMeal called ===")
            android.util.Log.d("ApiMealRepository", "Food: $foodName, Date: $date, Type: $mealType, Portion: $portion")

            emit(Result.Loading)

            val request = CreateMealRequest(
                foodId = foodId,
                foodName = foodName,
                date = date,
                mealType = mealType,
                portion = portion,
                nutrition = nutrition
            )

            android.util.Log.d("ApiMealRepository", "Sending POST request to /api/v1/meals...")
            val response = apiService.createMeal(request)

            android.util.Log.d("ApiMealRepository", "Response: code=${response.code()}, message=${response.message()}")

            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("ApiMealRepository", "SUCCESS! Meal created: ${response.body()!!.id}")
                emit(Result.Success(response.body()!!))
            } else {
                val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                android.util.Log.e("ApiMealRepository", "FAILED: $errorMsg")
                android.util.Log.e("ApiMealRepository", "Error body: ${response.errorBody()?.string()}")
                emit(Result.Error(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiMealRepository", "EXCEPTION in createMeal", e)
            emit(Result.Error(e.message ?: "Network error"))
        }
    }

    fun getMeal(mealId: String): Flow<Result<MealResponse>> = flow {
        try {
            emit(Result.Loading)

            val response = apiService.getMeal(mealId)

            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!))
            } else {
                emit(Result.Error("Failed to get meal"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error"))
        }
    }

    fun updateMeal(
        mealId: String,
        mealType: String? = null,
        portion: Double? = null,
        nutrition: NutritionDto? = null
    ): Flow<Result<MealResponse>> = flow {
        try {
            emit(Result.Loading)

            val request = UpdateMealRequest(
                mealType = mealType,
                portion = portion,
                nutrition = nutrition
            )

            val response = apiService.updateMeal(mealId, request)

            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!))
            } else {
                emit(Result.Error("Failed to update meal"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error"))
        }
    }

    fun deleteMeal(mealId: String): Flow<Result<String>> = flow {
        try {
            emit(Result.Loading)

            val response = apiService.deleteMeal(mealId)

            if (response.isSuccessful) {
                emit(Result.Success("Meal deleted successfully"))
            } else {
                emit(Result.Error("Failed to delete meal"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error"))
        }
    }

    fun getDailyLog(date: String): Flow<Result<DailyLogResponse>> = flow {
        try {
            android.util.Log.d("ApiMealRepository", "=== getDailyLog called ===")
            android.util.Log.d("ApiMealRepository", "Date: $date")

            emit(Result.Loading)

            val response = apiService.getDailyLog(date)

            android.util.Log.d("ApiMealRepository", "Response: code=${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val dailyLog = response.body()!!
                android.util.Log.d("ApiMealRepository", "SUCCESS! Got daily log with ${dailyLog.meals.size} meals")
                android.util.Log.d("ApiMealRepository", "Total calories: ${dailyLog.totalCalories}")
                emit(Result.Success(dailyLog))
            } else {
                val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                android.util.Log.e("ApiMealRepository", "FAILED: $errorMsg")
                emit(Result.Error(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiMealRepository", "EXCEPTION in getDailyLog", e)
            emit(Result.Error(e.message ?: "Network error"))
        }
    }

    fun getDailyLogs(
        startDate: String? = null,
        endDate: String? = null
    ): Flow<Result<DailyLogsResponse>> = flow {
        try {
            android.util.Log.d("ApiMealRepository", "=== getDailyLogs called ===")
            android.util.Log.d("ApiMealRepository", "Range: $startDate to $endDate")

            emit(Result.Loading)

            val response = apiService.getDailyLogs(startDate, endDate)

            if (response.isSuccessful && response.body() != null) {
                val logs = response.body()!!
                android.util.Log.d("ApiMealRepository", "SUCCESS! Got ${logs.count} daily logs")
                emit(Result.Success(logs))
            } else {
                val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                android.util.Log.e("ApiMealRepository", "FAILED: $errorMsg")
                emit(Result.Error(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiMealRepository", "EXCEPTION in getDailyLogs", e)
            emit(Result.Error(e.message ?: "Network error"))
        }
    }
}
