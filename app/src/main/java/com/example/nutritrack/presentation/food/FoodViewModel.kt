package com.example.nutritrack.presentation.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.repository.FoodRepository
import com.example.nutritrack.domain.model.Food
import com.example.nutritrack.domain.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodUiState(
    val searchQuery: String = "",
    val selectedFood: Food? = null,
    val portionMultiplier: Float = 1f,
    val calculatedCalories: Float = 0f,
    val calculatedProtein: Float = 0f,
    val calculatedCarbs: Float = 0f,
    val calculatedFat: Float = 0f
)

@HiltViewModel
class FoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodUiState())
    val uiState = _uiState.asStateFlow()

    private val _foodsState = MutableStateFlow<UiState<List<Food>>>(UiState.Idle)
    val foodsState = _foodsState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Food>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    init {
        loadAllFoods()
    }

    fun loadAllFoods() {
        viewModelScope.launch {
            _foodsState.value = UiState.Loading
            foodRepository.getAllFoods()
                .catch { e ->
                    _foodsState.value = UiState.Error(e.message ?: "Failed to load foods")
                }
                .collect { foods ->
                    _foodsState.value = UiState.Success(foods)
                }
        }
    }

    fun searchFoods(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            foodRepository.searchFoods(query)
                .catch { e ->
                    _searchResults.value = emptyList()
                }
                .collect { results ->
                    _searchResults.value = results
                }
        }
    }

    fun selectFood(food: Food) {
        _uiState.update {
            it.copy(
                selectedFood = food,
                portionMultiplier = 1f,
                calculatedCalories = food.nutrition.calories,
                calculatedProtein = food.nutrition.protein,
                calculatedCarbs = food.nutrition.carbs,
                calculatedFat = food.nutrition.fat
            )
        }
    }

    fun updatePortionMultiplier(multiplier: Float) {
        val food = _uiState.value.selectedFood ?: return

        _uiState.update {
            it.copy(
                portionMultiplier = multiplier,
                calculatedCalories = food.nutrition.calories * multiplier,
                calculatedProtein = food.nutrition.protein * multiplier,
                calculatedCarbs = food.nutrition.carbs * multiplier,
                calculatedFat = food.nutrition.fat * multiplier
            )
        }
    }

    fun incrementPortion() {
        val currentMultiplier = _uiState.value.portionMultiplier
        updatePortionMultiplier((currentMultiplier + 0.5f).coerceAtMost(10f))
    }

    fun decrementPortion() {
        val currentMultiplier = _uiState.value.portionMultiplier
        updatePortionMultiplier((currentMultiplier - 0.5f).coerceAtLeast(0.5f))
    }

    fun getFoodByBarcode(barcode: String) {
        viewModelScope.launch {
            try {
                val food = foodRepository.getFoodByBarcode(barcode)
                if (food != null) {
                    selectFood(food)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun clearSelection() {
        _uiState.value = FoodUiState()
    }

    fun addFoodToLog(onSuccess: () -> Unit) {
        val state = _uiState.value
        val food = state.selectedFood ?: return

        viewModelScope.launch {
            try {
                // TODO: Add meal to MealRepository
                // For now, just call success callback
                onSuccess()
                clearSelection()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
