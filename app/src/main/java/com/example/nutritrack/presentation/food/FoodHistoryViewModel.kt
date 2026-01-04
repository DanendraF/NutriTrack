package com.example.nutritrack.presentation.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.remote.Result
import com.example.nutritrack.data.repository.ApiMealRepository
import com.example.nutritrack.domain.model.Meal
import com.example.nutritrack.domain.model.MealType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FoodHistoryViewModel(
    private val apiMealRepository: ApiMealRepository
) : ViewModel() {

    private val _recentMeals = MutableStateFlow<List<RecentMeal>>(emptyList())
    val recentMeals: StateFlow<List<RecentMeal>> = _recentMeals.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadRecentMeals() {
        _isLoading.value = true

        viewModelScope.launch {
            // Get last 7 days
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val endDate = dateFormat.format(calendar.time)

            calendar.add(Calendar.DAY_OF_MONTH, -7)
            val startDate = dateFormat.format(calendar.time)

            apiMealRepository.getDailyLogs(startDate, endDate).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                    }
                    is Result.Success -> {
                        val allMeals = mutableListOf<RecentMeal>()

                        // Extract all meals from daily logs
                        result.data.logs.forEach { dailyLog ->
                            dailyLog.meals.forEach { mealDto ->
                                allMeals.add(
                                    RecentMeal(
                                        id = mealDto.id,
                                        name = mealDto.foodName,
                                        date = formatDate(mealDto.timestamp),
                                        calories = mealDto.nutrition.calories
                                    )
                                )
                            }
                        }

                        // Sort by most recent and take top 5
                        _recentMeals.value = allMeals.take(5)
                        _isLoading.value = false
                    }
                    is Result.Error -> {
                        android.util.Log.e("FoodHistoryViewModel", "Error loading recent meals: ${result.message}")
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    private fun formatDate(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(timestamp) ?: return "Unknown"

            val now = Calendar.getInstance()
            val dateCalendar = Calendar.getInstance().apply { time = date }

            val diffInDays = ((now.timeInMillis - dateCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

            when {
                diffInDays == 0 -> "Today"
                diffInDays == 1 -> "Yesterday"
                diffInDays < 7 -> "$diffInDays days ago"
                else -> {
                    val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                    outputFormat.format(date)
                }
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

data class RecentMeal(
    val id: String,
    val name: String,
    val date: String,
    val calories: Int
)
