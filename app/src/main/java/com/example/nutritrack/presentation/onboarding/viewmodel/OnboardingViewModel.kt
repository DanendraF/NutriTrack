package com.example.nutritrack.presentation.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.remote.Result
import com.example.nutritrack.data.repository.ApiUserRepository
import com.example.nutritrack.data.repository.UserRepository
import com.example.nutritrack.domain.model.*
import com.example.nutritrack.utils.DateUtils
import com.example.nutritrack.utils.NutritionCalculator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period

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

class OnboardingViewModel(
    private val userRepository: UserRepository,
    private val apiUserRepository: ApiUserRepository,
    private val firebaseAuth: FirebaseAuth
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

        android.util.Log.d("OnboardingViewModel", "=== saveUserData called ===")
        android.util.Log.d("OnboardingViewModel", "userId: $userId")
        android.util.Log.d("OnboardingViewModel", "state: name=${state.name}, email=${state.email}, age=${state.age}, height=${state.height}, weight=${state.weight}")

        if (!validateInput(state)) {
            android.util.Log.e("OnboardingViewModel", "Validation failed!")
            _uiState.update { it.copy(saveError = "Please fill all required fields") }
            return
        }

        android.util.Log.d("OnboardingViewModel", "Validation passed, starting API call...")

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true, saveError = null) }

                val weight = state.weight.toDouble()
                val height = state.height.toDouble()
                val age = state.age.toInt()

                // Calculate date of birth from age
                val dateOfBirth = LocalDate.now().minusYears(age.toLong()).toString()

                // Map gender to lowercase for API
                val genderForApi = state.gender.lowercase()

                android.util.Log.d("OnboardingViewModel", "Calling createUser API...")
                android.util.Log.d("OnboardingViewModel", "Request: email=${state.email}, name=${state.name}, dob=$dateOfBirth, gender=$genderForApi, h=$height, w=$weight")

                // Create user via API
                apiUserRepository.createUser(
                    email = state.email,
                    name = state.name,
                    dateOfBirth = dateOfBirth,
                    gender = genderForApi,
                    height = height,
                    weight = weight
                ).collect { result ->
                    android.util.Log.d("OnboardingViewModel", "API Result received: ${result.javaClass.simpleName}")
                    when (result) {
                        is Result.Loading -> {
                            android.util.Log.d("OnboardingViewModel", "Loading state...")
                            // Already set isSaving = true
                        }
                        is Result.Success -> {
                            android.util.Log.d("OnboardingViewModel", "SUCCESS! User created: ${result.data}")
                            val userResponse = result.data

                            // Save to local Room database for offline access
                            val activityLevel = ActivityLevel.fromString(state.activityLevel)
                            val nutritionGoal = NutritionGoal.fromString(state.goal)

                            val user = User(
                                userId = userId,
                                name = state.name,
                                email = state.email,
                                gender = state.gender,
                                age = age,
                                height = height.toFloat(),
                                weight = weight.toFloat(),
                                activityLevel = activityLevel,
                                nutritionGoal = nutritionGoal,
                                targetCalories = userResponse.goals.targetCalories,
                                targetMacros = Macros(
                                    protein = userResponse.goals.targetProtein.toFloat(),
                                    carbs = userResponse.goals.targetCarbs.toFloat(),
                                    fat = userResponse.goals.targetFat.toFloat()
                                )
                            )

                            userRepository.saveUser(user)

                            _uiState.update {
                                it.copy(
                                    isSaving = false,
                                    saveSuccess = true,
                                    calculatedCalories = userResponse.goals.targetCalories,
                                    calculatedMacros = Macros(
                                        protein = userResponse.goals.targetProtein.toFloat(),
                                        carbs = userResponse.goals.targetCarbs.toFloat(),
                                        fat = userResponse.goals.targetFat.toFloat()
                                    )
                                )
                            }
                        }
                        is Result.Error -> {
                            android.util.Log.e("OnboardingViewModel", "ERROR from API: ${result.message}")
                            _uiState.update {
                                it.copy(
                                    isSaving = false,
                                    saveError = "Failed to create user: ${result.message}"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("OnboardingViewModel", "EXCEPTION in saveUserData", e)
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
