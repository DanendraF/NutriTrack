package com.example.nutritrack.data.remote.dto

import com.google.gson.annotations.SerializedName

// Request DTOs
data class CreateUserRequest(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("dateOfBirth") val dateOfBirth: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("height") val height: Double,
    @SerializedName("weight") val weight: Double
)

data class UpdateUserRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
    @SerializedName("height") val height: Double? = null,
    @SerializedName("weight") val weight: Double? = null
)

data class UpdateGoalsRequest(
    @SerializedName("activityLevel") val activityLevel: String? = null,
    @SerializedName("nutritionGoal") val nutritionGoal: String? = null
)

// Response DTOs
data class UserResponse(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("dateOfBirth") val dateOfBirth: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("measurements") val measurements: MeasurementsDto,
    @SerializedName("goals") val goals: GoalsDto,
    @SerializedName("settings") val settings: SettingsDto,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class MeasurementsDto(
    @SerializedName("height") val height: Double,
    @SerializedName("weight") val weight: Double,
    @SerializedName("updatedAt") val updatedAt: String
)

data class GoalsDto(
    @SerializedName("activityLevel") val activityLevel: String,
    @SerializedName("nutritionGoal") val nutritionGoal: String,
    @SerializedName("targetCalories") val targetCalories: Int,
    @SerializedName("targetProtein") val targetProtein: Double,
    @SerializedName("targetCarbs") val targetCarbs: Double,
    @SerializedName("targetFat") val targetFat: Double,
    @SerializedName("bmr") val bmr: Double,
    @SerializedName("tdee") val tdee: Double,
    @SerializedName("updatedAt") val updatedAt: String
)

data class SettingsDto(
    @SerializedName("units") val units: String,
    @SerializedName("notifications") val notifications: Boolean,
    @SerializedName("theme") val theme: String
)
