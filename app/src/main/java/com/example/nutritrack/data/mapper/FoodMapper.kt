package com.example.nutritrack.data.mapper

import com.example.nutritrack.data.local.entity.FoodEntity
import com.example.nutritrack.domain.model.Food
import com.example.nutritrack.domain.model.NutritionInfo
import com.example.nutritrack.domain.model.ServingSize

fun FoodEntity.toDomainModel(): Food {
    return Food(
        foodId = foodId,
        name = name,
        nameIndonesian = nameIndonesian,
        category = category,
        nutrition = NutritionInfo(
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            fiber = fiber,
            sugar = sugar,
            sodium = sodium
        ),
        servingSize = ServingSize(
            amount = servingSize,
            unit = servingUnit
        ),
        barcode = barcode,
        imageUrl = imageUrl,
        isVerified = isVerified,
        source = source
    )
}

fun Food.toEntity(): FoodEntity {
    return FoodEntity(
        foodId = foodId,
        name = name,
        nameIndonesian = nameIndonesian,
        category = category,
        calories = nutrition.calories,
        protein = nutrition.protein,
        carbs = nutrition.carbs,
        fat = nutrition.fat,
        fiber = nutrition.fiber,
        sugar = nutrition.sugar,
        sodium = nutrition.sodium,
        servingSize = servingSize.amount,
        servingUnit = servingSize.unit,
        barcode = barcode,
        imageUrl = imageUrl,
        isVerified = isVerified,
        source = source
    )
}
