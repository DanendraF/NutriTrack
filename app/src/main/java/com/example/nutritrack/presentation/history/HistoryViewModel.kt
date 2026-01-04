package com.example.nutritrack.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.remote.Result
import com.example.nutritrack.data.repository.ApiMealRepository
import com.example.nutritrack.domain.model.DailyLog
import com.example.nutritrack.domain.model.Meal
import com.example.nutritrack.domain.model.MealType
import com.example.nutritrack.domain.model.NutritionInfo
import com.example.nutritrack.domain.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel(
    private val apiMealRepository: ApiMealRepository
) : ViewModel() {

    private val _dailyLogState = MutableStateFlow<UiState<DailyLog>>(UiState.Idle)
    val dailyLogState: StateFlow<UiState<DailyLog>> = _dailyLogState.asStateFlow()

    private val _selectedDate = MutableStateFlow(getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun loadTodayLog() {
        loadDailyLog(getCurrentDate())
    }

    fun loadDailyLog(date: String) {
        _dailyLogState.value = UiState.Loading

        viewModelScope.launch {
            apiMealRepository.getDailyLog(date).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _dailyLogState.value = UiState.Loading
                    }
                    is Result.Success -> {
                        val response = result.data

                        // Convert API response to domain model
                        val meals = response.meals.map { mealDto ->
                            Meal(
                                id = mealDto.id,
                                foodId = mealDto.foodId,
                                foodName = mealDto.foodName,
                                mealType = MealType.fromString(mealDto.mealType),
                                servingSize = "100g",
                                quantity = mealDto.portion.toFloat(),
                                calories = mealDto.nutrition.calories,
                                protein = mealDto.nutrition.protein.toInt(),
                                carbs = mealDto.nutrition.carbs.toInt(),
                                fat = mealDto.nutrition.fat.toInt(),
                                timestamp = parseTimestamp(mealDto.timestamp),
                                imageUrl = null
                            )
                        }

                        val dailyLog = DailyLog(
                            logId = response.id,
                            userId = response.userId,
                            date = response.date,
                            totalNutrition = NutritionInfo(
                                calories = response.totalCalories.toFloat(),
                                protein = response.totalProtein.toFloat(),
                                carbs = response.totalCarbs.toFloat(),
                                fat = response.totalFat.toFloat(),
                                fiber = 0f, // Not provided by API
                                sugar = 0f, // Not provided by API
                                sodium = 0f // Not provided by API
                            ),
                            meals = meals,
                            mealCount = meals.size
                        )

                        android.util.Log.d("HistoryViewModel", "Loaded ${meals.size} meals for $date")
                        _dailyLogState.value = UiState.Success(dailyLog)
                    }
                    is Result.Error -> {
                        android.util.Log.e("HistoryViewModel", "Failed to load daily log: ${result.message}")
                        _dailyLogState.value = UiState.Error(result.message)
                    }
                }
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun parseTimestamp(timestamp: String): Date {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(timestamp) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
}
