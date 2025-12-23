package com.example.nutritrack.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.remote.Result
import com.example.nutritrack.data.repository.ApiMealRepository
import com.example.nutritrack.data.repository.FirestoreMealRepository
import com.example.nutritrack.data.repository.UserRepository
import com.example.nutritrack.domain.model.Meal
import com.example.nutritrack.domain.model.User
import com.example.nutritrack.domain.model.UiState
import com.example.nutritrack.utils.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

data class HomeUiState(
    val userName: String = "",
    val targetCalories: Int = 2000,
    val consumedCalories: Int = 0,
    val remainingCalories: Int = 2000,
    val progressPercentage: Int = 0,
    val todayDate: String = DateUtils.getCurrentDate(),
    val targetProtein: Int = 0,
    val targetCarbs: Int = 0,
    val targetFat: Int = 0,
    val consumedProtein: Int = 0,
    val consumedCarbs: Int = 0,
    val consumedFat: Int = 0,
    val todayMeals: List<Meal> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
    private val userRepository: UserRepository,
    private val firestoreMealRepository: FirestoreMealRepository,
    private val apiMealRepository: ApiMealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _userState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val userState = _userState.asStateFlow()

    private var currentUserId: String? = null

    fun setUserId(userId: String) {
        if (currentUserId != userId) {
            currentUserId = userId
            loadUserData()
            loadTodayMeals(userId)
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .catch { e ->
                    _userState.value = UiState.Error(e.message ?: "Failed to load user data")
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { user ->
                    if (user != null) {
                        _userState.value = UiState.Success(user)
                        _uiState.update {
                            it.copy(
                                userName = user.name,
                                targetCalories = user.targetCalories,
                                targetProtein = user.targetMacros.protein.toInt(),
                                targetCarbs = user.targetMacros.carbs.toInt(),
                                targetFat = user.targetMacros.fat.toInt(),
                                isLoading = false
                            )
                        }
                    } else {
                        _userState.value = UiState.Error("User not found")
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
        }
    }

    private fun loadTodayMeals(userId: String) {
        viewModelScope.launch {
            android.util.Log.d("HomeViewModel", "=== loadTodayMeals called ===")
            android.util.Log.d("HomeViewModel", "userId: $userId")

            // Get today's date in YYYY-MM-DD format
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            android.util.Log.d("HomeViewModel", "Fetching daily log for: $today")

            // Fetch from backend API
            apiMealRepository.getDailyLog(today)
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            android.util.Log.d("HomeViewModel", "Loading daily log...")
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is Result.Success -> {
                            val dailyLog = result.data
                            android.util.Log.d("HomeViewModel", "SUCCESS! Got ${dailyLog.meals.size} meals")
                            android.util.Log.d("HomeViewModel", "Total calories: ${dailyLog.totalCalories}")

                            val totalCalories = dailyLog.totalCalories.toInt()
                            val totalProtein = dailyLog.totalProtein.toInt()
                            val totalCarbs = dailyLog.totalCarbs.toInt()
                            val totalFat = dailyLog.totalFat.toInt()

                            _uiState.update { state ->
                                val remaining = (state.targetCalories - totalCalories).coerceAtLeast(0)
                                val progress = if (state.targetCalories > 0) {
                                    ((totalCalories.toFloat() / state.targetCalories) * 100).toInt().coerceAtMost(100)
                                } else 0

                                state.copy(
                                    consumedCalories = totalCalories,
                                    consumedProtein = totalProtein,
                                    consumedCarbs = totalCarbs,
                                    consumedFat = totalFat,
                                    remainingCalories = remaining,
                                    progressPercentage = progress,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                        is Result.Error -> {
                            android.util.Log.e("HomeViewModel", "ERROR loading daily log: ${result.message}")

                            // Fallback to Firestore/Room
                            loadTodayMealsFromLocal(userId)
                        }
                    }
                }
        }
    }

    private fun loadTodayMealsFromLocal(userId: String) {
        viewModelScope.launch {
            android.util.Log.d("HomeViewModel", "Falling back to local data...")

            firestoreMealRepository.getTodaysMeals(userId)
                .catch { e: Throwable ->
                    android.util.Log.e("HomeViewModel", "Local fetch also failed", e)
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load meals") }
                }
                .collect { meals: List<Meal> ->
                    val totalCalories = meals.sumOf { meal -> meal.calories }
                    val totalProtein = meals.sumOf { meal -> meal.protein }
                    val totalCarbs = meals.sumOf { meal -> meal.carbs }
                    val totalFat = meals.sumOf { meal -> meal.fat }

                    _uiState.update { state ->
                        val remaining = (state.targetCalories - totalCalories).coerceAtLeast(0)
                        val progress = if (state.targetCalories > 0) {
                            ((totalCalories.toFloat() / state.targetCalories) * 100).toInt().coerceAtMost(100)
                        } else 0

                        state.copy(
                            consumedCalories = totalCalories,
                            consumedProtein = totalProtein,
                            consumedCarbs = totalCarbs,
                            consumedFat = totalFat,
                            remainingCalories = remaining,
                            progressPercentage = progress,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun refreshData() {
        currentUserId?.let { userId ->
            loadUserData()
            loadTodayMeals(userId)
        }
    }
}
