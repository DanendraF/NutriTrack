package com.example.nutritrack.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.repository.FirestoreMealRepository
import com.example.nutritrack.data.repository.UserRepository
import com.example.nutritrack.domain.model.Meal
import com.example.nutritrack.domain.model.User
import com.example.nutritrack.domain.model.UiState
import com.example.nutritrack.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

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
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firestoreMealRepository: FirestoreMealRepository
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
            firestoreMealRepository.getTodaysMeals(userId)
                .catch { e: Throwable ->
                    // Handle error but don't crash
                    e.printStackTrace()
                }
                .collect { meals: List<Meal> ->
                    // Calculate totals from meals
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
                            todayMeals = meals,
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
