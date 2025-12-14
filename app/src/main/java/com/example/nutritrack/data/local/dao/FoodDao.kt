package com.example.nutritrack.data.local.dao

import androidx.room.*
import com.example.nutritrack.data.local.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE foodId = :foodId")
    suspend fun getFoodById(foodId: String): FoodEntity?

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' OR nameIndonesian LIKE '%' || :query || '%'")
    fun searchFoods(query: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE category = :category")
    fun getFoodsByCategory(category: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE barcode = :barcode LIMIT 1")
    suspend fun getFoodByBarcode(barcode: String): FoodEntity?

    @Query("SELECT * FROM foods ORDER BY name ASC")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    @Update
    suspend fun updateFood(food: FoodEntity)

    @Query("DELETE FROM foods WHERE foodId = :foodId")
    suspend fun deleteFood(foodId: String)

    @Query("DELETE FROM foods")
    suspend fun deleteAllFoods()
}
