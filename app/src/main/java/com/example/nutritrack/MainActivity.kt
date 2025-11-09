package com.example.nutritrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope // <-- Import this
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.nutritrack.ui.theme.*

// --- Definisi Rute Navigasi (Best Practice) ---
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Food : Screen("food", "Food", Icons.Default.Fastfood)
    object Scan : Screen("scan", "Scan", Icons.Default.QrCodeScanner)
    object Tips : Screen("tips", "Tips", Icons.Default.Lightbulb)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

val bottomNavItems = listOf(Screen.Home, Screen.Food, Screen.Scan, Screen.Tips, Screen.Profile)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutriTrackTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkGreen,
                            unselectedIconColor = TextGray,
                            selectedTextColor = DarkGreen,
                            unselectedTextColor = TextGray,
                            indicatorColor = LightGreen
                        )
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
            val animationSpec = tween<IntOffset>(durationMillis = 350)

            composable(
                Screen.Home.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec) }
            ) { HomeScreen() }

            composable(
                Screen.Food.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Screen.Home.route -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec)
                        else -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec)
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Screen.Home.route -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec)
                        else -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec)
                    }
                }
            ) { FoodScreen() }

            composable(
                Screen.Tips.route,
                enterTransition = {
                    when (initialState.destination.route) {
                        Screen.Profile.route, Screen.Scan.route -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec)
                        else -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec)
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Screen.Profile.route, Screen.Scan.route -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec)
                        else -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec)
                    }
                }
            ) { TipsScreen() }

            composable(Screen.Scan.route) { ScanScreen() }
            composable(Screen.Profile.route) { /* TODO: Buat Composable untuk layar Profile di sini */ }
        }
    }
}
