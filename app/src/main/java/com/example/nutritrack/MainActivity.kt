package com.example.nutritrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
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
import com.example.nutritrack.presentation.article.ArticleDetailScreen
import com.example.nutritrack.domain.model.Article
import org.koin.android.ext.android.inject

import com.example.nutritrack.ui.theme.NutriTrackTheme
import com.example.nutritrack.ui.theme.DarkGreen
import com.example.nutritrack.ui.theme.LightGreen

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
                                // Always navigate to auth to force login
                                appNavController.navigate(GlobalRoutes.AUTH) {
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
                                    // User has data in backend, go to main app
                                    appNavController.navigate(GlobalRoutes.MAIN_APP) {
                                        popUpTo(GlobalRoutes.AUTH) { inclusive = true }
                                    }
                                },
                                onNavigateToOnboarding = {
                                    // User logged in but no data in backend, need onboarding
                                    appNavController.navigate(GlobalRoutes.ONBOARDING) {
                                        popUpTo(GlobalRoutes.AUTH) { inclusive = true }
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
                        OnboardingNavHost(
                            onOnboardingComplete = {
                                // Mark onboarding as completed
                                authPreferences.setOnboardingCompleted(true)

                                // Navigate to main app
                                appNavController.navigate(GlobalRoutes.MAIN_APP) {
                                    popUpTo(GlobalRoutes.ONBOARDING) { inclusive = true }
                                }
                            },
                            onBackToLogin = {
                                // Navigate back to login
                                appNavController.navigate(GlobalRoutes.AUTH) {
                                    popUpTo(GlobalRoutes.ONBOARDING) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 3. Aplikasi Utama (Home Screen, dll.)
                    composable(GlobalRoutes.MAIN_APP) {
                        MainAppLayout(appNavController = appNavController)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        authPreferences.clearLoginState()
    }
}

@Composable
fun MainAppLayout(appNavController: NavHostController) {
    val navController = rememberNavController()
    var selectedArticle by remember { mutableStateOf<Article?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = { }
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
                    },
                    onNavigateToCaloriesDetail = {
                        navController.navigate("calories_detail")
                    }
                )
            }

            // Calories Detail Screen
            composable("calories_detail") {
                com.example.nutritrack.presentation.calories.CaloriesDetailScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Food.route) {
                FoodScreen(
                    onNavigateToFoodSearch = {
                        navController.navigate("food_search_standalone")
                    },
                    onNavigateToFoodHistory = {
                        navController.navigate("food_history_detail")
                    },
                    onNavigateToCategory = { categoryId ->
                        navController.navigate("category_food/$categoryId")
                    },
                    onNavigateToRecipe = { recipeId ->
                        navController.navigate("food_recipe/$recipeId")
                    }
                )
            }

            // History screen (accessible from elsewhere, not in bottom nav)
            composable("history") {
                com.example.nutritrack.presentation.history.HistoryScreen()
            }
            composable(Screen.Scan.route) { ScanScreen() }
            composable(Screen.Tips.route) {
                TipsScreen(
                    onNavigateToArticleDetail = { article ->
                        selectedArticle = article
                        navController.navigate("article_detail")
                    }
                )
            }

            // Article Detail Screen
            composable("article_detail") {
                selectedArticle?.let { article ->
                    ArticleDetailScreen(
                        article = article,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }


            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToLogin = {
                        // Navigate back to login and clear backstack - use appNavController to exit MainAppLayout
                        appNavController.navigate(GlobalRoutes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    }
                )
            }

            // Food Screen (separate route for browsing saved foods)
            composable("food") {
                FoodScreen(
                    onNavigateToFoodSearch = {
                        navController.navigate("food_search_standalone")
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
                    },
                    onNavigateToEditProfile = {
                        navController.navigate("edit_profile")
                    }
                )
            }

            // Edit Profile Screen
            composable("edit_profile") {
                com.example.nutritrack.presentation.profile.EditProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Standalone Food Search (from FoodScreen)
            composable("food_search_standalone") {
                val userSavedFoodViewModel: com.example.nutritrack.presentation.food.UserSavedFoodViewModel =
                    org.koin.androidx.compose.koinViewModel()
                val firebaseAuth: com.google.firebase.auth.FirebaseAuth = org.koin.compose.koinInject()

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

            // Food History Detail Screen
            composable("food_history_detail") {
                com.example.nutritrack.presentation.food.FoodHistoryDetailScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Category Food Screen
            composable("category_food/{categoryId}") { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                com.example.nutritrack.presentation.food.CategoryFoodScreen(
                    categoryId = categoryId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Food Recipe Screen
            composable("food_recipe/{recipeId}") { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                com.example.nutritrack.presentation.food.FoodRecipeScreen(
                    recipeId = recipeId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }

        // Floating Bottom Navigation Bar
        FloatingBottomNav(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun FloatingBottomNav(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Card(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                selected = currentDestination?.route == Screen.Home.route,
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            // Food
            BottomNavItem(
                icon = Icons.Default.Search,
                label = "Food",
                selected = currentDestination?.route == Screen.Food.route,
                onClick = {
                    navController.navigate(Screen.Food.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            // Scan (FAB in center)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = (-12).dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.Scan.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    containerColor = DarkGreen,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Scan",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Tips
            BottomNavItem(
                icon = Icons.Default.Star,
                label = "Tips",
                selected = currentDestination?.route == Screen.Tips.route,
                onClick = {
                    navController.navigate(Screen.Tips.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            // Profile
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                selected = currentDestination?.route == Screen.Profile.route,
                onClick = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) DarkGreen else Color(0xFF9E9E9E),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (selected) DarkGreen else Color(0xFF9E9E9E),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
