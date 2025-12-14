package com.example.nutritrack.presentation.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.repository.UserRepository
import com.example.nutritrack.domain.model.*
import com.example.nutritrack.utils.DateUtils
import com.example.nutritrack.utils.NutritionCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val name: String = "",
    val email: String = "",
    val gender: String = "Male",
    val age: String = "",
    val height: String = "",
    val weight: String = "",
    val activityLevel: String = "Moderately Active",
    val goal: String = "Maintain Weight",
    val calculatedCalories: Int = 0,
    val calculatedMacros: Macros? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingState())
    val uiState = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updateGender(gender: String) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun updateAge(age: String) {
        _uiState.update { it.copy(age = age) }
    }

    fun updateHeight(height: String) {
        _uiState.update { it.copy(height = height) }
    }

    fun updateWeight(weight: String) {
        _uiState.update { it.copy(weight = weight) }
    }

    fun updateActivityLevel(level: String) {
        _uiState.update { it.copy(activityLevel = level) }
    }

    fun updateGoal(goal: String) {
        _uiState.update { it.copy(goal = goal) }
    }

    fun calculateNutritionTargets() {
        val state = _uiState.value

        try {
            val weight = state.weight.toFloatOrNull() ?: return
            val height = state.height.toFloatOrNull() ?: return
            val age = state.age.toIntOrNull() ?: return

            val activityLevel = ActivityLevel.fromString(state.activityLevel)
            val nutritionGoal = NutritionGoal.fromString(state.goal)

            val targets = NutritionCalculator.calculateNutritionTargets(
                weight = weight,
                height = height,
                age = age,
                gender = state.gender,
                activityLevel = activityLevel,
                goal = nutritionGoal
            )

            _uiState.update {
                it.copy(
                    calculatedCalories = targets.targetCalories,
                    calculatedMacros = targets.macros
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(saveError = "Error calculating nutrition targets: ${e.message}")
            }
        }
    }

    fun saveUserData(userId: String) {
        val state = _uiState.value

        if (!validateInput(state)) {
            _uiState.update { it.copy(saveError = "Please fill all required fields") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true, saveError = null) }

                val weight = state.weight.toFloat()
                val height = state.height.toFloat()
                val age = state.age.toInt()
                val activityLevel = ActivityLevel.fromString(state.activityLevel)
                val nutritionGoal = NutritionGoal.fromString(state.goal)

                val targets = NutritionCalculator.calculateNutritionTargets(
                    weight = weight,
                    height = height,
                    age = age,
                    gender = state.gender,
                    activityLevel = activityLevel,
                    goal = nutritionGoal
                )

                val user = User(
                    userId = userId,
                    name = state.name,
                    email = state.email,
                    gender = state.gender,
                    age = age,
                    height = height,
                    weight = weight,
                    activityLevel = activityLevel,
                    nutritionGoal = nutritionGoal,
                    targetCalories = targets.targetCalories,
                    targetMacros = targets.macros
                )

                userRepository.saveUser(user)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        calculatedCalories = targets.targetCalories,
                        calculatedMacros = targets.macros
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = "Failed to save user data: ${e.message}"
                    )
                }
            }
        }
    }

    private fun validateInput(state: OnboardingState): Boolean {
        return state.name.isNotBlank() &&
                state.email.isNotBlank() &&
                state.age.toIntOrNull() != null &&
                state.height.toFloatOrNull() != null &&
                state.weight.toFloatOrNull() != null
    }

    fun clearError() {
        _uiState.update { it.copy(saveError = null) }
    }
}
