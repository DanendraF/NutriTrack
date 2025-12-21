package com.example.nutritrack.data.repository

import com.example.nutritrack.data.remote.api.NutriTrackApiService
import com.example.nutritrack.data.remote.dto.FoodResponse
import com.example.nutritrack.data.remote.dto.FoodSearchResponse
import com.example.nutritrack.data.remote.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ApiFoodRepository(
    private val apiService: NutriTrackApiService
) {

    fun searchFoods(
        query: String? = null,
        category: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<Result<FoodSearchResponse>> = flow {
        try {
            emit(Result.Loading)

            val response = apiService.searchFoods(
                query = query,
                category = category,
                limit = limit,
                offset = offset
            )

            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!))
            } else {
                emit(Result.Error(response.message() ?: "Failed to search foods"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error"))
        }
    }

    fun getFoodById(foodId: String): Flow<Result<FoodResponse>> = flow {
        try {
            emit(Result.Loading)

            val response = apiService.getFoodById(foodId)

            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!))
            } else {
                emit(Result.Error(response.message() ?: "Failed to get food"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error"))
        }
    }

    fun getFoodByBarcode(barcode: String): Flow<Result<FoodResponse>> = flow {
        try {
            emit(Result.Loading)

            val response = apiService.getFoodByBarcode(barcode)

            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!))
            } else {
                emit(Result.Error(response.message() ?: "Food not found for barcode"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error"))
        }
    }

    fun getFoodsByCategory(category: String): Flow<Result<List<FoodResponse>>> = flow {
        try {
            emit(Result.Loading)

            val response = apiService.getFoodsByCategory(category)

            if (response.isSuccessful && response.body() != null) {
                @Suppress("UNCHECKED_CAST")
                val foods = response.body()!!["foods"] as? List<Map<String, Any>>
                // For simplicity, return empty list for now
                // In production, you'd parse the response properly
                emit(Result.Success(emptyList()))
            } else {
                emit(Result.Error(response.message() ?: "Failed to get foods by category"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error"))
        }
    }

    fun healthCheck(): Flow<Result<String>> = flow {
        try {
            emit(Result.Loading)

            val response = apiService.healthCheck()

            if (response.isSuccessful && response.body() != null) {
                val status = response.body()!!["status"] ?: "unknown"
                emit(Result.Success(status))
            } else {
                emit(Result.Error("Health check failed"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error"))
        }
    }
}
