package com.example.nutritrack.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.repository.AuthRepository
import com.example.nutritrack.data.repository.ApiUserRepository
import com.example.nutritrack.domain.model.User
import com.example.nutritrack.domain.model.UiState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.nutritrack.data.remote.Result

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val apiUserRepository: ApiUserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _logoutState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val logoutState = _logoutState.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            apiUserRepository.getCurrentUser()
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
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
                                userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
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

                            _uiState.update {
                                it.copy(
                                    user = user,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                        }
                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = result.message
                                )
                            }
                        }
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = UiState.Loading

            try {
                authRepository.logout()
                _logoutState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _logoutState.value = UiState.Error(e.message ?: "Logout failed")
            }
        }
    }

    fun resetLogoutState() {
        _logoutState.value = UiState.Idle
    }
}
