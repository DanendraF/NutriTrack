package com.example.nutritrack.data.local.dao

import androidx.room.*
import com.example.nutritrack.data.local.entity.MealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE mealId = :mealId")
    suspend fun getMealById(mealId: String): MealEntity?

    @Query("SELECT * FROM meals WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun getMealsByDate(userId: String, date: String): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getMealsByDateRange(userId: String, startDate: String, endDate: String): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE userId = :userId AND mealType = :mealType ORDER BY timestamp DESC LIMIT 10")
    fun getRecentMealsByType(userId: String, mealType: String): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedMeals(userId: String): List<MealEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Update
    suspend fun updateMeal(meal: MealEntity)

    @Query("UPDATE meals SET isSynced = 1 WHERE mealId = :mealId")
    suspend fun markAsSynced(mealId: String)

    @Query("DELETE FROM meals WHERE mealId = :mealId")
    suspend fun deleteMeal(mealId: String)

    @Query("DELETE FROM meals WHERE userId = :userId AND date < :beforeDate")
    suspend fun deleteOldMeals(userId: String, beforeDate: String)
}
