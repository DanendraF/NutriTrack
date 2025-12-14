package com.example.nutritrack.utils

import com.example.nutritrack.domain.model.ActivityLevel
import com.example.nutritrack.domain.model.Macros
import com.example.nutritrack.domain.model.NutritionGoal
import kotlin.math.roundToInt

object NutritionCalculator {

    /**
     * Calculate Basal Metabolic Rate (BMR) using Harris-Benedict Equation
     * @param weight in kg
     * @param height in cm
     * @param age in years
     * @param gender "Male" or "Female"
     * @return BMR in calories
     */
    fun calculateBMR(
        weight: Float,
        height: Float,
        age: Int,
        gender: String
    ): Int {
        return if (gender.lowercase() == "male") {
            (88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)).roundToInt()
        } else {
            (447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age)).roundToInt()
        }
    }

    /**
     * Calculate Total Daily Energy Expenditure (TDEE)
     * @param bmr Basal Metabolic Rate
     * @param activityLevel Activity level enum
     * @return TDEE in calories
     */
    fun calculateTDEE(
        bmr: Int,
        activityLevel: ActivityLevel
    ): Int {
        return (bmr * activityLevel.multiplier).roundToInt()
    }

    /**
     * Calculate target calories based on nutrition goal
     * @param tdee Total Daily Energy Expenditure
     * @param goal Nutrition goal (lose/maintain/gain weight)
     * @return Target calories
     */
    fun calculateTargetCalories(
        tdee: Int,
        goal: NutritionGoal
    ): Int {
        return when (goal) {
            NutritionGoal.LOSE_WEIGHT -> (tdee * 0.8).roundToInt() // 20% deficit
            NutritionGoal.MAINTAIN_WEIGHT -> tdee
            NutritionGoal.GAIN_WEIGHT -> (tdee * 1.1).roundToInt() // 10% surplus
        }
    }

    /**
     * Calculate macro distribution based on goal
     * Protein: 30% for lose/maintain, 25% for gain
     * Fat: 25% for all goals
     * Carbs: Remaining calories
     *
     * @param targetCalories Daily calorie target
     * @param goal Nutrition goal
     * @return Macros object with protein, carbs, and fat in grams
     */
    fun calculateMacros(
        targetCalories: Int,
        goal: NutritionGoal
    ): Macros {
        val proteinPercentage = if (goal == NutritionGoal.GAIN_WEIGHT) 0.25f else 0.30f
        val fatPercentage = 0.25f
        val carbsPercentage = 1f - proteinPercentage - fatPercentage

        val proteinCalories = targetCalories * proteinPercentage
        val fatCalories = targetCalories * fatPercentage
        val carbsCalories = targetCalories * carbsPercentage

        // Convert to grams (protein: 4 cal/g, fat: 9 cal/g, carbs: 4 cal/g)
        val proteinGrams = (proteinCalories / 4f)
        val fatGrams = (fatCalories / 9f)
        val carbsGrams = (carbsCalories / 4f)

        return Macros(
            protein = proteinGrams.roundToInt().toFloat(),
            carbs = carbsGrams.roundToInt().toFloat(),
            fat = fatGrams.roundToInt().toFloat()
        )
    }

    /**
     * Calculate all nutrition targets at once
     */
    fun calculateNutritionTargets(
        weight: Float,
        height: Float,
        age: Int,
        gender: String,
        activityLevel: ActivityLevel,
        goal: NutritionGoal
    ): NutritionTargets {
        val bmr = calculateBMR(weight, height, age, gender)
        val tdee = calculateTDEE(bmr, activityLevel)
        val targetCalories = calculateTargetCalories(tdee, goal)
        val macros = calculateMacros(targetCalories, goal)

        return NutritionTargets(
            bmr = bmr,
            tdee = tdee,
            targetCalories = targetCalories,
            macros = macros
        )
    }

    /**
     * Calculate macro percentage from actual intake
     */
    fun calculateMacroPercentage(
        calories: Float,
        protein: Float,
        carbs: Float,
        fat: Float
    ): MacroPercentages {
        val totalCalories = if (calories > 0) calories else (protein * 4 + carbs * 4 + fat * 9)

        return MacroPercentages(
            proteinPercent = if (totalCalories > 0) ((protein * 4 / totalCalories) * 100) else 0f,
            carbsPercent = if (totalCalories > 0) ((carbs * 4 / totalCalories) * 100) else 0f,
            fatPercent = if (totalCalories > 0) ((fat * 9 / totalCalories) * 100) else 0f
        )
    }

    /**
     * Calculate remaining calories for the day
     */
    fun calculateRemainingCalories(
        targetCalories: Int,
        consumedCalories: Float
    ): Int {
        return (targetCalories - consumedCalories).roundToInt().coerceAtLeast(0)
    }

    /**
     * Calculate progress percentage
     */
    fun calculateProgress(
        consumed: Float,
        target: Float
    ): Float {
        return if (target > 0) {
            ((consumed / target) * 100).coerceAtMost(100f)
        } else {
            0f
        }
    }
}

data class NutritionTargets(
    val bmr: Int,
    val tdee: Int,
    val targetCalories: Int,
    val macros: Macros
)

data class MacroPercentages(
    val proteinPercent: Float,
    val carbsPercent: Float,
    val fatPercent: Float
)
