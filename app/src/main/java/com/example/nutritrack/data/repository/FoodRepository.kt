package com.example.nutritrack.data.repository

import com.example.nutritrack.data.local.dao.FoodDao
import com.example.nutritrack.data.mapper.toDomainModel
import com.example.nutritrack.data.mapper.toEntity
import com.example.nutritrack.domain.model.Food
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface FoodRepository {
    suspend fun getFoodById(foodId: String): Food?
    fun searchFoods(query: String): Flow<List<Food>>
    fun getFoodsByCategory(category: String): Flow<List<Food>>
    suspend fun getFoodByBarcode(barcode: String): Food?
    fun getAllFoods(): Flow<List<Food>>
    suspend fun saveFood(food: Food)
    suspend fun saveFoods(foods: List<Food>)
    suspend fun updateFood(food: Food)
    suspend fun deleteFood(foodId: String)
}

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao
) : FoodRepository {

    override suspend fun getFoodById(foodId: String): Food? {
        return foodDao.getFoodById(foodId)?.toDomainModel()
    }

    override fun searchFoods(query: String): Flow<List<Food>> {
        return foodDao.searchFoods(query).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getFoodsByCategory(category: String): Flow<List<Food>> {
        return foodDao.getFoodsByCategory(category).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun getFoodByBarcode(barcode: String): Food? {
        return foodDao.getFoodByBarcode(barcode)?.toDomainModel()
    }

    override fun getAllFoods(): Flow<List<Food>> {
        return foodDao.getAllFoods().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun saveFood(food: Food) {
        foodDao.insertFood(food.toEntity())
    }

    override suspend fun saveFoods(foods: List<Food>) {
        foodDao.insertFoods(foods.map { it.toEntity() })
    }

    override suspend fun updateFood(food: Food) {
        foodDao.updateFood(food.toEntity())
    }

    override suspend fun deleteFood(foodId: String) {
        foodDao.deleteFood(foodId)
    }
}
