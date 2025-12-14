package com.example.nutritrack.data.mapper

import com.example.nutritrack.data.local.entity.UserEntity
import com.example.nutritrack.domain.model.*

fun UserEntity.toDomainModel(): User {
    return User(
        userId = userId,
        name = name,
        email = email,
        gender = gender,
        age = age,
        height = height,
        weight = weight,
        activityLevel = ActivityLevel.fromString(activityLevel),
        nutritionGoal = NutritionGoal.fromString(nutritionGoal),
        targetCalories = targetCalories,
        targetMacros = Macros(
            protein = targetProtein,
            carbs = targetCarbs,
            fat = targetFat
        )
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        userId = userId,
        name = name,
        email = email,
        gender = gender,
        age = age,
        height = height,
        weight = weight,
        activityLevel = activityLevel.name,
        nutritionGoal = nutritionGoal.name,
        targetCalories = targetCalories,
        targetProtein = targetMacros.protein,
        targetCarbs = targetMacros.carbs,
        targetFat = targetMacros.fat
    )
}
