package com.example.nutritrack.data.repository

import com.example.nutritrack.data.remote.api.NutriTrackApiService
import com.example.nutritrack.data.remote.dto.CreateUserRequest
import com.example.nutritrack.data.remote.dto.UpdateGoalsRequest
import com.example.nutritrack.data.remote.dto.UpdateUserRequest
import com.example.nutritrack.data.remote.dto.UserResponse
import com.example.nutritrack.data.remote.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ApiUserRepository(
    private val apiService: NutriTrackApiService
) {

    fun createUser(
        email: String,
        name: String,
        dateOfBirth: String,
        gender: String,
        height: Double,
        weight: Double
    ): Flow<Result<UserResponse>> = flow {
        try {
            android.util.Log.d("ApiUserRepository", "=== createUser called ===")
            android.util.Log.d("ApiUserRepository", "Parameters: email=$email, name=$name, dob=$dateOfBirth, gender=$gender, h=$height, w=$weight")

            emit(Result.Loading)

            val request = CreateUserRequest(
                email = email,
                name = name,
                dateOfBirth = dateOfBirth,
                gender = gender,
                height = height,
                weight = weight
            )

            android.util.Log.d("ApiUserRepository", "Sending POST request to backend...")
            val response = apiService.createUser(request)

            android.util.Log.d("ApiUserRepository", "Response received: code=${response.code()}, message=${response.message()}")
            android.util.Log.d("ApiUserRepository", "Response body: ${response.body()}")
            android.util.Log.d("ApiUserRepository", "Response errorBody: ${response.errorBody()?.string()}")

            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("ApiUserRepository", "SUCCESS! Emitting success result")
                emit(Result.Success(response.body()!!))
            } else {
                val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                android.util.Log.e("ApiUserRepository", "FAILED: $errorMsg")
                emit(Result.Error(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiUserRepository", "EXCEPTION in createUser", e)
            emit(Result.Error(e.message ?: "Network error"))
        }
    }

    fun getCurrentUser(): Flow<Result<UserResponse>> = flow {
        try {
            emit(Result.Loading)

            val response = apiService.getCurrentUser()

            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!))
            } else {
                emit(Result.Error(response.message() ?: "Failed to get user"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error"))
        }
    }

    fun updateUser(
        name: String? = null,
        dateOfBirth: String? = null,
        height: Double? = null,
        weight: Double? = null
    ): Flow<Result<UserResponse>> = flow {
        try {
            emit(Result.Loading)

            val request = UpdateUserRequest(
                name = name,
                dateOfBirth = dateOfBirth,
                height = height,
                weight = weight
            )

            val response = apiService.updateUser(request)

            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!))
            } else {
                emit(Result.Error(response.message() ?: "Failed to update user"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error"))
        }
    }

    fun updateGoals(
        activityLevel: String? = null,
        nutritionGoal: String? = null
    ): Flow<Result<UserResponse>> = flow {
        try {
            emit(Result.Loading)

            val request = UpdateGoalsRequest(
                activityLevel = activityLevel,
                nutritionGoal = nutritionGoal
            )

            val response = apiService.updateGoals(request)

            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!))
            } else {
                emit(Result.Error(response.message() ?: "Failed to update goals"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error"))
        }
    }

    fun deleteUser(): Flow<Result<String>> = flow {
        try {
            emit(Result.Loading)

            val response = apiService.deleteUser()

            if (response.isSuccessful) {
                emit(Result.Success("User deleted successfully"))
            } else {
                emit(Result.Error(response.message() ?: "Failed to delete user"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network error"))
        }
    }
}
