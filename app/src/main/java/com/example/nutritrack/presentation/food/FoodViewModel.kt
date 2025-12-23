package com.example.nutritrack.presentation.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.remote.Result
import com.example.nutritrack.data.repository.ApiFoodRepository
import com.example.nutritrack.data.repository.FoodRepository
import com.example.nutritrack.domain.model.Food
import com.example.nutritrack.domain.model.NutritionInfo
import com.example.nutritrack.domain.model.ServingSize
import com.example.nutritrack.domain.model.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FoodUiState(
    val searchQuery: String = "",
    val selectedFood: Food? = null,
    val portionMultiplier: Float = 1f,
    val calculatedCalories: Float = 0f,
    val calculatedProtein: Float = 0f,
    val calculatedCarbs: Float = 0f,
    val calculatedFat: Float = 0f,
    val isSearching: Boolean = false,
    val searchError: String? = null
)

class FoodViewModel(
    private val foodRepository: FoodRepository,
    private val apiFoodRepository: ApiFoodRepository
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

            // Try to load from Room first for offline support
            foodRepository.getAllFoods()
                .catch { e ->
                    android.util.Log.e("FoodViewModel", "Failed to load from Room", e)
                }
                .collect { localFoods ->
                    if (localFoods.isNotEmpty()) {
                        _foodsState.value = UiState.Success(localFoods)
                    }
                }

            // Also fetch from backend to get latest data
            fetchFoodsFromBackend()
        }
    }

    private fun fetchFoodsFromBackend() {
        viewModelScope.launch {
            apiFoodRepository.searchFoods(limit = 100)
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            android.util.Log.d("FoodViewModel", "Loading foods from backend...")
                        }
                        is Result.Success -> {
                            android.util.Log.d("FoodViewModel", "Fetched ${result.data.foods.size} foods from backend")

                            // Convert DTOs to domain models
                            val foods = result.data.foods.map { dto ->
                                Food(
                                    foodId = dto.id,
                                    name = dto.name,
                                    nameIndonesian = dto.nameIndonesian,
                                    category = dto.category,
                                    nutrition = NutritionInfo(
                                        calories = dto.nutrition.calories.toFloat(),
                                        protein = dto.nutrition.protein.toFloat(),
                                        carbs = dto.nutrition.carbs.toFloat(),
                                        fat = dto.nutrition.fat.toFloat(),
                                        fiber = dto.nutrition.fiber.toFloat(),
                                        sugar = dto.nutrition.sugar.toFloat(),
                                        sodium = dto.nutrition.sodium.toFloat()
                                    ),
                                    servingSize = ServingSize(
                                        amount = dto.servingSize.amount.toFloat(),
                                        unit = dto.servingSize.unit
                                    ),
                                    barcode = dto.barcode,
                                    imageUrl = dto.imageUrl,
                                    isVerified = dto.isVerified,
                                    source = dto.source
                                )
                            }

                            _foodsState.value = UiState.Success(foods)
                        }
                        is Result.Error -> {
                            android.util.Log.e("FoodViewModel", "Failed to fetch from backend: ${result.message}")
                            // Keep showing local data if available
                        }
                    }
                }
        }
    }

    fun searchFoods(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearching = true, searchError = null) }

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _uiState.update { it.copy(isSearching = false) }
            return
        }

        viewModelScope.launch {
            android.util.Log.d("FoodViewModel", "Searching foods: query=$query")

            // Try backend API first
            apiFoodRepository.searchFoods(query = query, limit = 20)
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            android.util.Log.d("FoodViewModel", "Searching...")
                        }
                        is Result.Success -> {
                            android.util.Log.d("FoodViewModel", "Found ${result.data.foods.size} foods")

                            val foods = result.data.foods.map { dto ->
                                Food(
                                    foodId = dto.id,
                                    name = dto.name,
                                    nameIndonesian = dto.nameIndonesian,
                                    category = dto.category,
                                    nutrition = NutritionInfo(
                                        calories = dto.nutrition.calories.toFloat(),
                                        protein = dto.nutrition.protein.toFloat(),
                                        carbs = dto.nutrition.carbs.toFloat(),
                                        fat = dto.nutrition.fat.toFloat(),
                                        fiber = dto.nutrition.fiber.toFloat(),
                                        sugar = dto.nutrition.sugar.toFloat(),
                                        sodium = dto.nutrition.sodium.toFloat()
                                    ),
                                    servingSize = ServingSize(
                                        amount = dto.servingSize.amount.toFloat(),
                                        unit = dto.servingSize.unit
                                    ),
                                    barcode = dto.barcode,
                                    imageUrl = dto.imageUrl,
                                    isVerified = dto.isVerified,
                                    source = dto.source
                                )
                            }

                            _searchResults.value = foods
                            _uiState.update { it.copy(isSearching = false) }
                        }
                        is Result.Error -> {
                            android.util.Log.e("FoodViewModel", "Search error: ${result.message}")

                            // Fallback to local search
                            foodRepository.searchFoods(query)
                                .catch { e ->
                                    android.util.Log.e("FoodViewModel", "Local search failed", e)
                                    _searchResults.value = emptyList()
                                    _uiState.update {
                                        it.copy(
                                            isSearching = false,
                                            searchError = "Search failed: ${result.message}"
                                        )
                                    }
                                }
                                .collect { localResults ->
                                    _searchResults.value = localResults
                                    _uiState.update { it.copy(isSearching = false) }
                                }
                        }
                    }
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
