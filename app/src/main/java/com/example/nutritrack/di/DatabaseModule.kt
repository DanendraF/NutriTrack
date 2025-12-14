package com.example.nutritrack.di

import android.content.Context
import androidx.room.Room
import com.example.nutritrack.data.local.NutriTrackDatabase
import com.example.nutritrack.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNutriTrackDatabase(@ApplicationContext context: Context): NutriTrackDatabase {
        return Room.databaseBuilder(
            context,
            NutriTrackDatabase::class.java,
            "nutritrack_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: NutriTrackDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideFoodDao(database: NutriTrackDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    @Singleton
    fun provideMealDao(database: NutriTrackDatabase): MealDao {
        return database.mealDao()
    }

    @Provides
    @Singleton
    fun provideDailyLogDao(database: NutriTrackDatabase): DailyLogDao {
        return database.dailyLogDao()
    }
}
