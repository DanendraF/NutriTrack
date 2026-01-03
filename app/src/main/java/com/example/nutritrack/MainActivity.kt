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
import com.example.nutritrack.onboarding.OnboardingNavHost
// --- IMPORT YANG DISESUAIKAN DENGAN STRUKTUR FOLDER ANDA ---
import com.example.nutritrack.FoodScreen
import com.example.nutritrack.HomeScreen
import com.example.nutritrack.ScanScreen
import com.example.nutritrack.TipsScreen
import com.example.nutritrack.presentation.profile.ProfileScreen
import com.example.nutritrack.presentation.settings.SettingsScreen
import com.example.nutritrack.presentation.meal.AddMealScreen
import com.example.nutritrack.presentation.food.FoodSearchScreen

import com.example.nutritrack.ui.theme.NutriTrackTheme

// Rute global untuk navigasi utama
object GlobalRoutes {
    // Hapus SPLASH untuk sementara jika tidak digunakan
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutriTrackTheme {
                val appNavController = rememberNavController()

                NavHost(navController = appNavController, startDestination = GlobalRoutes.AUTH) {
                    // 1. Alur Autentikasi (Login/Register)
                    navigation(startDestination = "login", route = GlobalRoutes.AUTH) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    appNavController.navigate(GlobalRoutes.ONBOARDING) {
                                        // Hapus riwayat navigasi Auth agar tidak bisa kembali
                                        popUpTo(GlobalRoutes.AUTH) { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = { appNavController.navigate("register") }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = {
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
            composable(Screen.Food.route) { FoodScreen() }
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
                    }
                )
            }

            // Food Search Screen
            composable("food_search") {
                FoodSearchScreen(
                    onFoodSelected = { food ->
                        // Navigate to food details/logging
                        // For now, just go back
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
        }
    }
}
