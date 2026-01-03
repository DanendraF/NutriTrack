package com.example.nutritrack.presentation.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.repository.UserSavedFoodRepository
import com.example.nutritrack.domain.model.UserSavedFood
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserSavedFoodViewModel(
    private val userSavedFoodRepository: UserSavedFoodRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _savedFoods = MutableStateFlow<List<UserSavedFood>>(emptyList())
    val savedFoods: StateFlow<List<UserSavedFood>> = _savedFoods.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadSavedFoods()
    }

    private fun loadSavedFoods() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            _error.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                userSavedFoodRepository.getUserSavedFoods(userId)
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = emptyList()
                    )
                    .collect { foods ->
                        _savedFoods.value = foods
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun saveFood(userSavedFood: UserSavedFood) {
        viewModelScope.launch {
            val result = userSavedFoodRepository.saveFood(userSavedFood)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun deleteFood(foodId: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            val result = userSavedFoodRepository.deleteFood(foodId, userId)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
