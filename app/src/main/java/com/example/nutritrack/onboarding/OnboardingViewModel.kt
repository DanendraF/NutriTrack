package com.example.nutritrack.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Data class untuk menampung semua data onboarding
data class OnboardingData(
    val name: String = "",
    val gender: String = "Laki-laki", // Default value
    val age: String = "",
    val height: String = "",
    val weight: String = "",
    // Opsi: "sedentary", "light", "moderate", "active", "extra"
    val activityLevel: String = "moderate", // Default value
    // Opsi: "lose", "maintain", "gain"
    val goal: String = "maintain" // Default value
)

class OnboardingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingData())
    val uiState = _uiState.asStateFlow()

    fun updateName(name: String) { _uiState.update { it.copy(name = name) } }
    fun updateGender(gender: String) { _uiState.update { it.copy(gender = gender) } }
    fun updateAge(age: String) { _uiState.update { it.copy(age = age) } }
    fun updateHeight(height: String) { _uiState.update { it.copy(height = height) } }
    fun updateWeight(weight: String) { _uiState.update { it.copy(weight = weight) } }
    fun updateActivityLevel(level: String) { _uiState.update { it.copy(activityLevel = level) } }
    fun updateGoal(goal: String) { _uiState.update { it.copy(goal = goal) } }

    fun saveUserData() {
        // TODO: Nanti di sini Anda akan memanggil repository untuk menyimpan _uiState.value ke Room Database.
        println("Data Pengguna Siap Disimpan: ${_uiState.value}")
    }
}
