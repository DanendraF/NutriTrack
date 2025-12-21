package com.example.nutritrack.data.remote.api

import com.example.nutritrack.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface NutriTrackApiService {

    // ===== USER ENDPOINTS =====

    @POST("api/v1/users")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): Response<UserResponse>

    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @PUT("api/v1/users/me")
    suspend fun updateUser(
        @Body request: UpdateUserRequest
    ): Response<UserResponse>

    @PUT("api/v1/users/me/goals")
    suspend fun updateGoals(
        @Body request: UpdateGoalsRequest
    ): Response<UserResponse>

    @DELETE("api/v1/users/me")
    suspend fun deleteUser(): Response<Map<String, String>>

    // ===== FOOD ENDPOINTS =====

    @GET("api/v1/foods")
    suspend fun searchFoods(
        @Query("q") query: String? = null,
        @Query("category") category: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<FoodSearchResponse>

    @GET("api/v1/foods/{id}")
    suspend fun getFoodById(
        @Path("id") foodId: String
    ): Response<FoodResponse>

    @GET("api/v1/foods/barcode/{barcode}")
    suspend fun getFoodByBarcode(
        @Path("barcode") barcode: String
    ): Response<FoodResponse>

    @GET("api/v1/foods/category/{category}")
    suspend fun getFoodsByCategory(
        @Path("category") category: String
    ): Response<Map<String, Any>>

    @POST("api/v1/foods")
    suspend fun createFood(
        @Body request: CreateFoodRequest
    ): Response<FoodResponse>

    @PUT("api/v1/foods/{id}")
    suspend fun updateFood(
        @Path("id") foodId: String,
        @Body request: CreateFoodRequest
    ): Response<FoodResponse>

    @DELETE("api/v1/foods/{id}")
    suspend fun deleteFood(
        @Path("id") foodId: String
    ): Response<Map<String, String>>

    // ===== HEALTH CHECK =====

    @GET("health")
    suspend fun healthCheck(): Response<Map<String, String>>

    @GET("api/v1/ping")
    suspend fun ping(): Response<Map<String, String>>
}
