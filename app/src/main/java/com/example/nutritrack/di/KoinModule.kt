package com.example.nutritrack.di

import androidx.room.Room
import com.example.nutritrack.BuildConfig
import com.example.nutritrack.data.local.NutriTrackDatabase
import com.example.nutritrack.data.remote.api.NutriTrackApiService
import com.example.nutritrack.data.remote.interceptor.AuthInterceptor
import com.example.nutritrack.data.repository.*
import com.example.nutritrack.data.repository.ApiUserRepository
import com.example.nutritrack.data.repository.ApiFoodRepository
import com.example.nutritrack.presentation.auth.AuthViewModel
import com.example.nutritrack.presentation.auth.FirebaseAuthViewModel
import com.example.nutritrack.presentation.food.FoodViewModel
import com.example.nutritrack.presentation.home.HomeViewModel
import com.example.nutritrack.presentation.meal.MealViewModel
import com.example.nutritrack.presentation.onboarding.viewmodel.OnboardingViewModel
import com.example.nutritrack.presentation.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {

    // ===== FIREBASE =====
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }

    // ===== NETWORK =====
    // HTTP Logging Interceptor
    single {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    // Auth Interceptor
    single { AuthInterceptor(get()) }

    // OkHttp Client
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Gson
    single {
        GsonBuilder()
            .setLenient()
            .create()
    }

    // Retrofit
    single {
        Retrofit.Builder()
            .baseUrl("http://192.168.0.196:8080/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    // API Service
    single { get<Retrofit>().create(NutriTrackApiService::class.java) }

    // ===== DATABASE =====
    single {
        Room.databaseBuilder(
            androidContext(),
            NutriTrackDatabase::class.java,
            "nutritrack_database"
        ).fallbackToDestructiveMigration()
            .build()
    }
    single { get<NutriTrackDatabase>().userDao() }
    single { get<NutriTrackDatabase>().foodDao() }
    single { get<NutriTrackDatabase>().mealDao() }
    single { get<NutriTrackDatabase>().dailyLogDao() }

    // ===== REPOSITORIES (Concrete implementations) =====
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<FoodRepository> { FoodRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single { FirestoreUserRepository(get(), get()) }
    single { FirestoreMealRepository(get()) }

    // API Repositories
    single { ApiUserRepository(get()) }
    single { ApiFoodRepository(get()) }

    // ===== VIEWMODELS =====
    viewModel { AuthViewModel() }
    viewModel { FirebaseAuthViewModel(get()) }
    viewModel { OnboardingViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { FoodViewModel(get()) }
    viewModel { MealViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
}
