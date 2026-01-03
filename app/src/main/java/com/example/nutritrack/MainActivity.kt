package com.example.nutritrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.nutritrack.auth.LoginScreen
import com.example.nutritrack.auth.RegisterScreen
import com.example.nutritrack.data.local.preferences.AuthPreferences
import com.example.nutritrack.onboarding.OnboardingNavHost
import com.example.nutritrack.presentation.splash.SplashScreen
// --- IMPORT YANG DISESUAIKAN DENGAN STRUKTUR FOLDER ANDA ---
import com.example.nutritrack.FoodScreen
import com.example.nutritrack.HomeScreen
import com.example.nutritrack.ScanScreen
import com.example.nutritrack.TipsScreen
import com.example.nutritrack.presentation.profile.ProfileScreen
import com.example.nutritrack.presentation.settings.SettingsScreen
import com.example.nutritrack.presentation.meal.AddMealScreen
import com.example.nutritrack.presentation.food.FoodSearchScreen
import org.koin.android.ext.android.inject

import com.example.nutritrack.ui.theme.NutriTrackTheme

// Rute global untuk navigasi utama
object GlobalRoutes {
    const val SPLASH = "splash_route"
    const val AUTH = "auth_route"
    const val ONBOARDING = "onboarding_route"
    const val MAIN_APP = "main_app_route"
}

// Item untuk navigasi bawah
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Food : Screen("food", "Food", Icons.Default.Fastfood)
    data object Scan : Screen("scan", "Scan", Icons.Default.QrCodeScanner)
    data object Tips : Screen("tips", "Tips", Icons.Default.Lightbulb)

    data object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

val bottomNavItems = listOf(Screen.Home, Screen.Food, Screen.Scan, Screen.Tips, Screen.Profile)

class MainActivity : ComponentActivity() {

    private val authPreferences: AuthPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutriTrackTheme {
                val appNavController = rememberNavController()

                // Always start with splash screen
                NavHost(navController = appNavController, startDestination = GlobalRoutes.SPLASH) {
                    // 0. Splash Screen
                    composable(GlobalRoutes.SPLASH) {
                        SplashScreen(
                            onSplashFinished = {
                                // After splash, check login state
                                val destination = if (authPreferences.isLoggedIn()) {
                                    GlobalRoutes.MAIN_APP
                                } else {
                                    GlobalRoutes.AUTH
                                }
                                appNavController.navigate(destination) {
                                    popUpTo(GlobalRoutes.SPLASH) { inclusive = true }
                                }
                            }
                        )
                    }


                    // 1. Alur Autentikasi (Login/Register)
                    navigation(startDestination = "login", route = GlobalRoutes.AUTH) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    // Check if user has completed onboarding before
                                    if (authPreferences.hasCompletedOnboarding()) {
                                        // Skip onboarding, go directly to main app
                                        appNavController.navigate(GlobalRoutes.MAIN_APP) {
                                            popUpTo(GlobalRoutes.AUTH) { inclusive = true }
                                        }
                                    } else {
                                        // First time user, go to onboarding
                                        appNavController.navigate(GlobalRoutes.ONBOARDING) {
                                            popUpTo(GlobalRoutes.AUTH) { inclusive = true }
                                        }
                                    }
                                },
                                onNavigateToRegister = { appNavController.navigate("register") }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    // New users always need to complete onboarding
                                    appNavController.navigate(GlobalRoutes.ONBOARDING) {
                                        popUpTo(GlobalRoutes.AUTH) { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = { appNavController.popBackStack() }
                            )
                        }
                    }

                    // 2. Alur Onboarding
                    composable(GlobalRoutes.ONBOARDING) {
                        OnboardingNavHost(onOnboardingComplete = {
                            // Mark onboarding as completed
                            authPreferences.setOnboardingCompleted(true)

                            // Navigate to main app
                            appNavController.navigate(GlobalRoutes.MAIN_APP) {
                                popUpTo(GlobalRoutes.ONBOARDING) { inclusive = true }
                            }
                        })
                    }

                    // 3. Aplikasi Utama (Home Screen, dll.)
                    composable(GlobalRoutes.MAIN_APP) {
                        MainAppLayout()
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppLayout() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToAddMeal = {
                        navController.navigate("add_meal")
                    }
                )
            }
            composable(Screen.Food.route) {
                FoodScreen(
                    onNavigateToFoodSearch = {
                        navController.navigate("food_search_standalone")
                    }
                )
            }
            composable(Screen.Scan.route) { ScanScreen() }
            composable(Screen.Tips.route) { TipsScreen() }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToLogin = {
                        // Navigate back to login and clear backstack
                        navController.navigate(GlobalRoutes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    }
                )
            }

            // Add Meal Screen
            composable("add_meal") {
                AddMealScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToFoodSearch = {
                        navController.navigate("food_search")
                    }
                )
            }

            // Food Search Screen
            composable("food_search") {
                // Get the shared MealViewModel from the parent NavGraph
                val parentEntry = remember(navController) {
                    navController.getBackStackEntry("add_meal")
                }
                val mealViewModel: com.example.nutritrack.presentation.meal.MealViewModel =
                    org.koin.androidx.compose.koinViewModel(viewModelStoreOwner = parentEntry)

                FoodSearchScreen(
                    onFoodSelected = { food ->
                        // Fill the AddMealScreen form with selected food data
                        mealViewModel.setFoodFromDatabase(
                            foodId = food.foodId,
                            foodName = food.name,
                            servingSize = "${food.servingSize.amount} ${food.servingSize.unit}",
                            calories = food.nutrition.calories.toInt(),
                            protein = food.nutrition.protein.toInt(),
                            carbs = food.nutrition.carbs.toInt(),
                            fat = food.nutrition.fat.toInt()
                        )

                        // Navigate back to AddMealScreen with data filled
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Settings Screen
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Standalone Food Search (from FoodScreen)
            composable("food_search_standalone") {
                val userSavedFoodViewModel: com.example.nutritrack.presentation.food.UserSavedFoodViewModel =
                    org.koin.androidx.compose.koinViewModel()
                val firebaseAuth: com.google.firebase.auth.FirebaseAuth = org.koin.androidx.compose.get()

                FoodSearchScreen(
                    onFoodSelected = { food ->
                        // Create UserSavedFood object and save
                        val userId = firebaseAuth.currentUser?.uid ?: return@FoodSearchScreen
                        val savedFood = com.example.nutritrack.domain.model.UserSavedFood(
                            id = "${userId}_${food.foodId}",
                            userId = userId,
                            foodId = food.foodId,
                            foodName = food.name,
                            servingSize = "${food.servingSize.amount} ${food.servingSize.unit}",
                            calories = food.nutrition.calories.toInt(),
                            protein = food.nutrition.protein.toFloat(),
                            carbs = food.nutrition.carbs.toFloat(),
                            fat = food.nutrition.fat.toFloat()
                        )
                        userSavedFoodViewModel.saveFood(savedFood)
                        // Navigate back to FoodScreen
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
