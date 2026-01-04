package com.example.nutritrack.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.remote.Result
import com.example.nutritrack.data.repository.ApiUserRepository
import com.example.nutritrack.domain.model.UiState
import com.example.nutritrack.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val apiUserRepository: ApiUserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val updateState: StateFlow<UiState<String>> = _updateState.asStateFlow()

    fun loadUserProfile() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            apiUserRepository.getCurrentUser().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is Result.Success -> {
                        val userResponse = result.data
                        // Calculate age from dateOfBirth
                        val age = try {
                            val birthYear = userResponse.dateOfBirth.substring(0, 4).toInt()
                            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                            currentYear - birthYear
                        } catch (e: Exception) {
                            25 // default age
                        }

                        val user = User(
                            userId = firebaseAuth.currentUser?.uid ?: "",
                            name = userResponse.name,
                            email = userResponse.email,
                            gender = userResponse.gender,
                            age = age,
                            height = userResponse.measurements.height.toFloat(),
                            weight = userResponse.measurements.weight.toFloat(),
                            activityLevel = com.example.nutritrack.domain.model.ActivityLevel.fromString(
                                userResponse.goals.activityLevel
                            ),
                            nutritionGoal = com.example.nutritrack.domain.model.NutritionGoal.fromString(
                                userResponse.goals.nutritionGoal
                            ),
                            targetCalories = userResponse.goals.targetCalories,
                            targetMacros = com.example.nutritrack.domain.model.Macros(
                                protein = userResponse.goals.targetProtein.toFloat(),
                                carbs = userResponse.goals.targetCarbs.toFloat(),
                                fat = userResponse.goals.targetFat.toFloat()
                            )
                        )
                        _uiState.value = _uiState.value.copy(
                            user = user,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun updateProfile(name: String, age: Int, height: Int, weight: Double) {
        if (name.isBlank()) {
            _updateState.value = UiState.Error("Name cannot be empty")
            return
        }
        if (age <= 0 || age > 150) {
            _updateState.value = UiState.Error("Please enter a valid age")
            return
        }
        if (height <= 0 || height > 300) {
            _updateState.value = UiState.Error("Please enter a valid height")
            return
        }
        if (weight <= 0 || weight > 500) {
            _updateState.value = UiState.Error("Please enter a valid weight")
            return
        }

        _updateState.value = UiState.Loading

        viewModelScope.launch {
            apiUserRepository.updateUserProfile(
                name = name,
                age = age,
                height = height,
                weight = weight
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _updateState.value = UiState.Loading
                    }
                    is Result.Success -> {
                        android.util.Log.d("EditProfileViewModel", "Profile updated successfully")
                        _updateState.value = UiState.Success("Profile updated successfully")
                    }
                    is Result.Error -> {
                        android.util.Log.e("EditProfileViewModel", "Failed to update profile: ${result.message}")
                        _updateState.value = UiState.Error(result.message)
                    }
                }
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UiState.Idle
    }
}

data class EditProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
