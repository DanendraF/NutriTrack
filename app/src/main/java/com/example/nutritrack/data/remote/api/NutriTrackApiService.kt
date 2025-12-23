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

    // ===== MEAL ENDPOINTS =====

    @POST("api/v1/meals")
    suspend fun createMeal(
        @Body request: CreateMealRequest
    ): Response<MealResponse>

    @GET("api/v1/meals/{id}")
    suspend fun getMeal(
        @Path("id") mealId: String
    ): Response<MealResponse>

    @PUT("api/v1/meals/{id}")
    suspend fun updateMeal(
        @Path("id") mealId: String,
        @Body request: UpdateMealRequest
    ): Response<MealResponse>

    @DELETE("api/v1/meals/{id}")
    suspend fun deleteMeal(
        @Path("id") mealId: String
    ): Response<Map<String, String>>

    // ===== DAILY LOG ENDPOINTS =====

    @GET("api/v1/daily-logs")
    suspend fun getDailyLogs(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<DailyLogsResponse>

    @GET("api/v1/daily-logs/{date}")
    suspend fun getDailyLog(
        @Path("date") date: String
    ): Response<DailyLogResponse>

    // ===== HEALTH CHECK =====

    @GET("health")
    suspend fun healthCheck(): Response<Map<String, String>>

    @GET("api/v1/ping")
    suspend fun ping(): Response<Map<String, String>>
}
