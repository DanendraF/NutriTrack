package com.example.nutritrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nutritrack.data.local.dao.*
import com.example.nutritrack.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        FoodEntity::class,
        MealEntity::class,
        DailyLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NutriTrackDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun mealDao(): MealDao
    abstract fun dailyLogDao(): DailyLogDao
}
