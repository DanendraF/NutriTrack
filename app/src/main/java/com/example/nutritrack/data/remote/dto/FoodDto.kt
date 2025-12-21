package com.example.nutritrack.data.remote.dto

import com.google.gson.annotations.SerializedName

// Response DTOs
data class FoodResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("nameIndonesian") val nameIndonesian: String,
    @SerializedName("category") val category: String,
    @SerializedName("nutrition") val nutrition: NutritionDto,
    @SerializedName("servingSize") val servingSize: ServingSizeDto,
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("isVerified") val isVerified: Boolean,
    @SerializedName("source") val source: String,
    @SerializedName("createdAt") val createdAt: String
)

data class NutritionDto(
    @SerializedName("calories") val calories: Int,
    @SerializedName("protein") val protein: Double,
    @SerializedName("carbs") val carbs: Double,
    @SerializedName("fat") val fat: Double,
    @SerializedName("fiber") val fiber: Double,
    @SerializedName("sugar") val sugar: Double,
    @SerializedName("sodium") val sodium: Double
)

data class ServingSizeDto(
    @SerializedName("amount") val amount: Double,
    @SerializedName("unit") val unit: String
)

data class FoodSearchResponse(
    @SerializedName("foods") val foods: List<FoodResponse>,
    @SerializedName("total") val total: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("offset") val offset: Int,
    @SerializedName("hasMore") val hasMore: Boolean
)

// Request DTOs
data class CreateFoodRequest(
    @SerializedName("name") val name: String,
    @SerializedName("nameIndonesian") val nameIndonesian: String,
    @SerializedName("category") val category: String,
    @SerializedName("nutrition") val nutrition: NutritionDto,
    @SerializedName("servingSize") val servingSize: ServingSizeDto,
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null
)
