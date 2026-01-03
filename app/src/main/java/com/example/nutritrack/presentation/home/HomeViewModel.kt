package com.example.nutritrack.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.remote.Result
import com.example.nutritrack.data.repository.ApiMealRepository
import com.example.nutritrack.data.repository.ApiUserRepository
import com.example.nutritrack.data.repository.FirestoreMealRepository
import com.example.nutritrack.data.repository.UserRepository
import com.example.nutritrack.domain.model.Meal
import com.example.nutritrack.domain.model.MealType
import com.example.nutritrack.domain.model.User
import com.example.nutritrack.domain.model.UiState
import com.example.nutritrack.utils.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

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
    val weeklyCalories: List<Pair<String, Int>> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
    private val userRepository: UserRepository,
    private val firestoreMealRepository: FirestoreMealRepository,
    private val apiMealRepository: ApiMealRepository,
    private val apiUserRepository: ApiUserRepository
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
            loadWeeklyData()
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            android.util.Log.d("HomeViewModel", "=== loadUserData called ===")

            // Try to fetch from backend API first
            apiUserRepository.getCurrentUser()
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            android.util.Log.d("HomeViewModel", "Loading user from API...")
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is Result.Success -> {
                            android.util.Log.d("HomeViewModel", "SUCCESS! Got user from API")
                            val userResponse = result.data
                            android.util.Log.d("HomeViewModel", "🔍 API Response - targetCalories: ${userResponse.goals.targetCalories}")
                            android.util.Log.d("HomeViewModel", "🔍 API Response - targetProtein: ${userResponse.goals.targetProtein}")
                            android.util.Log.d("HomeViewModel", "🔍 API Response - name: ${userResponse.name}, email: ${userResponse.email}")

                            // Calculate age from dateOfBirth
                            val dob = try {
                                LocalDate.parse(userResponse.dateOfBirth)
                            } catch (e: Exception) {
                                LocalDate.now().minusYears(25) // Default fallback
                            }
                            val age = java.time.Period.between(dob, LocalDate.now()).years

                            // Update local database with fresh data from backend
                            currentUserId?.let { userId ->
                                val user = User(
                                    userId = userId,
                                    name = userResponse.name,
                                    email = userResponse.email,
                                    gender = userResponse.gender.replaceFirstChar { it.uppercase() },
                                    age = age,
                                    height = userResponse.measurements.height.toFloat(),
                                    weight = userResponse.measurements.weight.toFloat(),
                                    activityLevel = com.example.nutritrack.domain.model.ActivityLevel.fromString(userResponse.goals.activityLevel),
                                    nutritionGoal = com.example.nutritrack.domain.model.NutritionGoal.fromString(userResponse.goals.nutritionGoal),
                                    targetCalories = userResponse.goals.targetCalories,
                                    targetMacros = com.example.nutritrack.domain.model.Macros(
                                        protein = userResponse.goals.targetProtein.toFloat(),
                                        carbs = userResponse.goals.targetCarbs.toFloat(),
                                        fat = userResponse.goals.targetFat.toFloat()
                                    )
                                )

                                // Save to local database
                                userRepository.saveUser(user)

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

                                android.util.Log.d("HomeViewModel", "✅ User data synced to state: targetCalories=${user.targetCalories}")
                                android.util.Log.d("HomeViewModel", "✅ UI State updated with calories: ${_uiState.value.targetCalories}")
                            }
                        }
                        is Result.Error -> {
                            android.util.Log.e("HomeViewModel", "API ERROR: ${result.message}, falling back to local")

                            // Fallback to local Room database
                            loadUserDataFromLocal()
                        }
                    }
                }
        }
    }

    private fun loadUserDataFromLocal() {
        viewModelScope.launch {
            android.util.Log.d("HomeViewModel", "Loading user from local database...")

            userRepository.getCurrentUser()
                .catch { e ->
                    android.util.Log.e("HomeViewModel", "Local fetch failed", e)
                    _userState.value = UiState.Error(e.message ?: "Failed to load user data")
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { user ->
                    if (user != null) {
                        android.util.Log.d("HomeViewModel", "Got user from local: targetCalories=${user.targetCalories}")
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

                            // Convert MealResponse list to Meal list
                            val meals = dailyLog.meals.map { mealResponse ->
                                // Parse RFC3339 timestamp from backend
                                val timestamp = try {
                                    val zonedDateTime = ZonedDateTime.parse(mealResponse.timestamp)
                                    Date.from(zonedDateTime.toInstant())
                                } catch (e: Exception) {
                                    android.util.Log.e("HomeViewModel", "Failed to parse timestamp: ${mealResponse.timestamp}", e)
                                    Date() // Fallback to current time
                                }

                                Meal(
                                    id = mealResponse.id,
                                    foodId = mealResponse.foodId,
                                    foodName = mealResponse.foodName,
                                    mealType = MealType.fromString(mealResponse.mealType),
                                    quantity = mealResponse.portion.toFloat(),
                                    servingSize = "100g", // Default
                                    calories = mealResponse.nutrition.calories.toInt(),
                                    protein = mealResponse.nutrition.protein.toInt(),
                                    carbs = mealResponse.nutrition.carbs.toInt(),
                                    fat = mealResponse.nutrition.fat.toInt(),
                                    timestamp = timestamp
                                )
                            }

                            android.util.Log.d("HomeViewModel", "Converted ${meals.size} meals to domain model")

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
                                    todayMeals = meals,
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
                            todayMeals = meals,
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
            loadWeeklyData()
        }
    }

    private fun loadWeeklyData() {
        viewModelScope.launch {
            android.util.Log.d("HomeViewModel", "=== loadWeeklyData called ===")

            // Calculate date range for last 7 days
            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(6)

            val startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

            android.util.Log.d("HomeViewModel", "Fetching weekly data from $startDateStr to $endDateStr")

            // Fetch from backend API
            apiMealRepository.getDailyLogs(startDateStr, endDateStr)
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            android.util.Log.d("HomeViewModel", "Loading weekly data...")
                        }
                        is Result.Success -> {
                            android.util.Log.d("HomeViewModel", "SUCCESS! Got ${result.data.logs.size} daily logs")

                            // Create map of date to calories
                            val dailyLogsMap = result.data.logs.associate { log ->
                                log.date to log.totalCalories.toInt()
                            }

                            // Build weekly data with proper day labels
                            val weeklyData = mutableListOf<Pair<String, Int>>()
                            val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH) // Mon, Tue, etc

                            for (i in 0..6) {
                                val date = startDate.plusDays(i.toLong())
                                val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                val dayLabel = date.format(dayFormatter)
                                val calories = dailyLogsMap[dateStr] ?: 0

                                weeklyData.add(dayLabel to calories)
                            }

                            android.util.Log.d("HomeViewModel", "Weekly data: $weeklyData")

                            _uiState.update { it.copy(weeklyCalories = weeklyData) }
                        }
                        is Result.Error -> {
                            android.util.Log.e("HomeViewModel", "ERROR loading weekly data: ${result.message}")
                            // Use empty data on error
                            _uiState.update {
                                it.copy(weeklyCalories = emptyList())
                            }
                        }
                    }
                }
        }
    }
}
