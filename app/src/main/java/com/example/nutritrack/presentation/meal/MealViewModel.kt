package com.example.nutritrack.presentation.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.remote.Result
import com.example.nutritrack.data.remote.dto.NutritionDto
import com.example.nutritrack.data.repository.ApiMealRepository
import com.example.nutritrack.domain.model.Meal
import com.example.nutritrack.domain.model.MealType
import com.example.nutritrack.domain.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MealViewModel(
    private val apiMealRepository: ApiMealRepository
) : ViewModel() {

    private val _addMealState = MutableStateFlow<AddMealUiState>(AddMealUiState())
    val addMealState: StateFlow<AddMealUiState> = _addMealState.asStateFlow()

    private val _saveMealState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val saveMealState: StateFlow<UiState<String>> = _saveMealState.asStateFlow()

    private val _deleteMealState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteMealState: StateFlow<UiState<Unit>> = _deleteMealState.asStateFlow()

    /**
     * Update food name
     */
    fun updateFoodName(name: String) {
        _addMealState.value = _addMealState.value.copy(foodName = name)
    }

    /**
     * Update meal type
     */
    fun updateMealType(mealType: MealType) {
        _addMealState.value = _addMealState.value.copy(mealType = mealType)
    }

    /**
     * Update serving size
     */
    fun updateServingSize(servingSize: String) {
        _addMealState.value = _addMealState.value.copy(servingSize = servingSize)
    }

    /**
     * Update quantity
     */
    fun updateQuantity(quantity: Float) {
        _addMealState.value = _addMealState.value.copy(quantity = quantity)
        recalculateNutrition()
    }

    /**
     * Update calories (per serving)
     */
    fun updateCaloriesPerServing(calories: Int) {
        _addMealState.value = _addMealState.value.copy(caloriesPerServing = calories)
        recalculateNutrition()
    }

    /**
     * Update protein (per serving)
     */
    fun updateProteinPerServing(protein: Int) {
        _addMealState.value = _addMealState.value.copy(proteinPerServing = protein)
        recalculateNutrition()
    }

    /**
     * Update carbs (per serving)
     */
    fun updateCarbsPerServing(carbs: Int) {
        _addMealState.value = _addMealState.value.copy(carbsPerServing = carbs)
        recalculateNutrition()
    }

    /**
     * Update fat (per serving)
     */
    fun updateFatPerServing(fat: Int) {
        _addMealState.value = _addMealState.value.copy(fatPerServing = fat)
        recalculateNutrition()
    }

    /**
     * Recalculate total nutrition based on quantity
     */
    private fun recalculateNutrition() {
        val state = _addMealState.value
        _addMealState.value = state.copy(
            totalCalories = (state.caloriesPerServing * state.quantity).toInt(),
            totalProtein = (state.proteinPerServing * state.quantity).toInt(),
            totalCarbs = (state.carbsPerServing * state.quantity).toInt(),
            totalFat = (state.fatPerServing * state.quantity).toInt()
        )
    }

    /**
     * Set food from database (when user selects from search)
     */
    fun setFoodFromDatabase(
        foodId: String,
        foodName: String,
        servingSize: String,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int
    ) {
        _addMealState.value = AddMealUiState(
            foodId = foodId,
            foodName = foodName,
            servingSize = servingSize,
            quantity = 1f,
            caloriesPerServing = calories,
            proteinPerServing = protein,
            carbsPerServing = carbs,
            fatPerServing = fat,
            totalCalories = calories,
            totalProtein = protein,
            totalCarbs = carbs,
            totalFat = fat
        )
    }

    /**
     * Save meal via API (backend will update Firestore + DailyLog)
     */
    fun saveMeal(userId: String) {
        val state = _addMealState.value

        // Validation
        if (state.foodName.isBlank()) {
            _saveMealState.value = UiState.Error("Food name is required")
            return
        }
        if (state.quantity <= 0) {
            _saveMealState.value = UiState.Error("Quantity must be greater than 0")
            return
        }

        _saveMealState.value = UiState.Loading

        viewModelScope.launch {
            // Format date as YYYY-MM-DD
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            // Convert MealType enum to API string format
            val mealTypeString = when (state.mealType) {
                MealType.BREAKFAST -> "breakfast"
                MealType.LUNCH -> "lunch"
                MealType.DINNER -> "dinner"
                MealType.SNACK -> "snack"
            }

            val nutritionDto = NutritionDto(
                calories = state.totalCalories,
                protein = state.totalProtein.toDouble(),
                carbs = state.totalCarbs.toDouble(),
                fat = state.totalFat.toDouble(),
                fiber = 0.0, // Default value, not tracked in manual entry
                sugar = 0.0, // Default value, not tracked in manual entry
                sodium = 0.0 // Default value, not tracked in manual entry
            )

            android.util.Log.d("MealViewModel", "Saving meal: ${state.foodName}, date=$currentDate, type=$mealTypeString")

            apiMealRepository.createMeal(
                foodId = state.foodId.ifEmpty { "manual_entry" },
                foodName = state.foodName,
                date = currentDate,
                mealType = mealTypeString,
                portion = state.quantity.toDouble(),
                nutrition = nutritionDto
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _saveMealState.value = UiState.Loading
                    }
                    is Result.Success -> {
                        android.util.Log.d("MealViewModel", "Meal saved successfully: ${result.data.id}")
                        _saveMealState.value = UiState.Success(result.data.id)
                    }
                    is Result.Error -> {
                        android.util.Log.e("MealViewModel", "Failed to save meal: ${result.message}")
                        _saveMealState.value = UiState.Error(result.message)
                    }
                }
            }
        }
    }

    /**
     * Update existing meal via API
     */
    fun updateMeal(userId: String, mealId: String) {
        val state = _addMealState.value

        // Validation
        if (state.foodName.isBlank()) {
            _saveMealState.value = UiState.Error("Food name is required")
            return
        }

        _saveMealState.value = UiState.Loading

        viewModelScope.launch {
            val mealTypeString = when (state.mealType) {
                MealType.BREAKFAST -> "breakfast"
                MealType.LUNCH -> "lunch"
                MealType.DINNER -> "dinner"
                MealType.SNACK -> "snack"
            }

            val nutritionDto = NutritionDto(
                calories = state.totalCalories,
                protein = state.totalProtein.toDouble(),
                carbs = state.totalCarbs.toDouble(),
                fat = state.totalFat.toDouble(),
                fiber = 0.0,
                sugar = 0.0,
                sodium = 0.0
            )

            apiMealRepository.updateMeal(
                mealId = mealId,
                mealType = mealTypeString,
                portion = state.quantity.toDouble(),
                nutrition = nutritionDto
            ).collect { result ->
                when (result) {
                    is Result.Loading -> _saveMealState.value = UiState.Loading
                    is Result.Success -> _saveMealState.value = UiState.Success(mealId)
                    is Result.Error -> _saveMealState.value = UiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Delete meal via API
     */
    fun deleteMeal(userId: String, mealId: String, timestamp: Date) {
        _deleteMealState.value = UiState.Loading

        viewModelScope.launch {
            apiMealRepository.deleteMeal(mealId).collect { result ->
                when (result) {
                    is Result.Loading -> _deleteMealState.value = UiState.Loading
                    is Result.Success -> _deleteMealState.value = UiState.Success(Unit)
                    is Result.Error -> _deleteMealState.value = UiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Load meal for editing
     */
    fun loadMealForEdit(meal: Meal) {
        _addMealState.value = AddMealUiState(
            foodId = meal.foodId,
            foodName = meal.foodName,
            mealType = meal.mealType,
            servingSize = meal.servingSize,
            quantity = meal.quantity,
            caloriesPerServing = (meal.calories / meal.quantity).toInt(),
            proteinPerServing = (meal.protein / meal.quantity).toInt(),
            carbsPerServing = (meal.carbs / meal.quantity).toInt(),
            fatPerServing = (meal.fat / meal.quantity).toInt(),
            totalCalories = meal.calories,
            totalProtein = meal.protein,
            totalCarbs = meal.carbs,
            totalFat = meal.fat
        )
    }

    /**
     * Reset form
     */
    fun resetForm() {
        _addMealState.value = AddMealUiState()
        _saveMealState.value = UiState.Idle
    }

    /**
     * Reset save state
     */
    fun resetSaveState() {
        _saveMealState.value = UiState.Idle
    }

    /**
     * Reset delete state
     */
    fun resetDeleteState() {
        _deleteMealState.value = UiState.Idle
    }

    /**
     * Save meal from scan result
     */
    fun saveMealFromScan(food: com.example.nutritrack.domain.model.Food, mealType: MealType) {
        // Set food data
        setFoodFromDatabase(
            foodId = food.foodId,
            foodName = food.name,
            servingSize = "100g",
            calories = food.nutrition.calories.toInt(),
            protein = food.nutrition.protein.toInt(),
            carbs = food.nutrition.carbs.toInt(),
            fat = food.nutrition.fat.toInt()
        )

        // Update meal type
        updateMealType(mealType)

        // Save to backend
        _saveMealState.value = UiState.Loading

        viewModelScope.launch {
            // Format date as YYYY-MM-DD
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            // Convert MealType enum to API string format
            val mealTypeString = when (mealType) {
                MealType.BREAKFAST -> "breakfast"
                MealType.LUNCH -> "lunch"
                MealType.DINNER -> "dinner"
                MealType.SNACK -> "snack"
            }

            val nutritionDto = NutritionDto(
                calories = food.nutrition.calories.toInt(),
                protein = food.nutrition.protein.toDouble(),
                carbs = food.nutrition.carbs.toDouble(),
                fat = food.nutrition.fat.toDouble(),
                fiber = food.nutrition.fiber.toDouble(),
                sugar = food.nutrition.sugar.toDouble(),
                sodium = food.nutrition.sodium.toDouble()
            )

            android.util.Log.d("MealViewModel", "Saving scanned meal: ${food.name}, type=$mealTypeString")

            apiMealRepository.createMeal(
                foodId = food.foodId,
                foodName = food.name,
                date = currentDate,
                mealType = mealTypeString,
                portion = 1.0,
                nutrition = nutritionDto
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _saveMealState.value = UiState.Loading
                    }
                    is Result.Success -> {
                        android.util.Log.d("MealViewModel", "Scanned meal saved successfully: ${result.data.id}")
                        _saveMealState.value = UiState.Success(result.data.id)
                    }
                    is Result.Error -> {
                        android.util.Log.e("MealViewModel", "Failed to save scanned meal: ${result.message}")
                        _saveMealState.value = UiState.Error(result.message)
                    }
                }
            }
        }
    }
}

/**
 * UI State for Add/Edit Meal Screen
 */
data class AddMealUiState(
    val foodId: String = "",
    val foodName: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val servingSize: String = "100g",
    val quantity: Float = 1f,
    val caloriesPerServing: Int = 0,
    val proteinPerServing: Int = 0,
    val carbsPerServing: Int = 0,
    val fatPerServing: Int = 0,
    val totalCalories: Int = 0,
    val totalProtein: Int = 0,
    val totalCarbs: Int = 0,
    val totalFat: Int = 0
)
