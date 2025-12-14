package com.example.nutritrack.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.repository.UserRepository
import com.example.nutritrack.domain.model.User
import com.example.nutritrack.domain.model.UiState
import com.example.nutritrack.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val targetCalories: Int = 2000,
    val consumedCalories: Float = 0f,
    val remainingCalories: Int = 2000,
    val progressPercentage: Float = 0f,
    val todayDate: String = DateUtils.getCurrentDate(),
    val targetProtein: Float = 0f,
    val targetCarbs: Float = 0f,
    val targetFat: Float = 0f,
    val consumedProtein: Float = 0f,
    val consumedCarbs: Float = 0f,
    val consumedFat: Float = 0f
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _userState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val userState = _userState.asStateFlow()

    init {
        loadUserData()
        loadTodayMeals()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .catch { e ->
                    _userState.value = UiState.Error(e.message ?: "Failed to load user data")
                }
                .collect { user ->
                    if (user != null) {
                        _userState.value = UiState.Success(user)
                        _uiState.update {
                            it.copy(
                                userName = user.name,
                                targetCalories = user.targetCalories,
                                remainingCalories = user.targetCalories,
                                targetProtein = user.targetMacros.protein,
                                targetCarbs = user.targetMacros.carbs,
                                targetFat = user.targetMacros.fat
                            )
                        }
                    } else {
                        _userState.value = UiState.Error("User not found")
                    }
                }
        }
    }

    private fun loadTodayMeals() {
        // TODO: Load meals from MealRepository when implemented
        // For now, using mock data
        viewModelScope.launch {
            // Simulate loading meals
            val mockConsumedCalories = 1280f
            val mockConsumedProtein = 45f
            val mockConsumedCarbs = 150f
            val mockConsumedFat = 40f

            _uiState.update { state ->
                val remaining = (state.targetCalories - mockConsumedCalories).coerceAtLeast(0f).toInt()
                val progress = ((mockConsumedCalories / state.targetCalories) * 100).coerceAtMost(100f)

                state.copy(
                    consumedCalories = mockConsumedCalories,
                    remainingCalories = remaining,
                    progressPercentage = progress,
                    consumedProtein = mockConsumedProtein,
                    consumedCarbs = mockConsumedCarbs,
                    consumedFat = mockConsumedFat
                )
            }
        }
    }

    fun refreshData() {
        loadUserData()
        loadTodayMeals()
    }

    fun addMealCalories(calories: Float, protein: Float, carbs: Float, fat: Float) {
        _uiState.update { state ->
            val newConsumed = state.consumedCalories + calories
            val remaining = (state.targetCalories - newConsumed).coerceAtLeast(0f).toInt()
            val progress = ((newConsumed / state.targetCalories) * 100).coerceAtMost(100f)

            state.copy(
                consumedCalories = newConsumed,
                remainingCalories = remaining,
                progressPercentage = progress,
                consumedProtein = state.consumedProtein + protein,
                consumedCarbs = state.consumedCarbs + carbs,
                consumedFat = state.consumedFat + fat
            )
        }
    }
}
