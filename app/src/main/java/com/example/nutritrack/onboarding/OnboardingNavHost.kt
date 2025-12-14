package com.example.nutritrack.onboarding

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutritrack.presentation.onboarding.viewmodel.OnboardingViewModel

object OnboardingRoutes {
    const val WELCOME = "welcome"
    const val GENDER_AGE = "gender_age"
    const val MEASUREMENTS = "measurements"
    const val ACTIVITY_LEVEL = "activity_level"
    const val NUTRITION_GOAL = "nutrition_goal"
    const val RESULT = "result"
}

@Composable
fun OnboardingNavHost(onOnboardingComplete: () -> Unit) {
    val navController = rememberNavController()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val slideSpec = tween<androidx.compose.ui.unit.IntOffset>(350)
    val totalSteps = 5

    NavHost(navController = navController, startDestination = OnboardingRoutes.WELCOME) {
        composable(OnboardingRoutes.WELCOME) {
            WelcomeScreen(
                onNavigateNext = {
                    navController.navigate(OnboardingRoutes.GENDER_AGE)
                }
            )
        }

        composable(
            route = OnboardingRoutes.GENDER_AGE,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    slideSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    slideSpec
                )
            }
        ) {
            GenderAgeScreen(
                viewModel = onboardingViewModel,
                onNavigateNext = { navController.navigate(OnboardingRoutes.MEASUREMENTS) },
                onNavigateBack = { navController.popBackStack() },
                step = 1,
                totalSteps = totalSteps
            )
        }

        composable(
            route = OnboardingRoutes.MEASUREMENTS,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    slideSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    slideSpec
                )
            }
        ) {
            MeasurementScreen(
                viewModel = onboardingViewModel,
                onNavigateNext = { navController.navigate(OnboardingRoutes.ACTIVITY_LEVEL) },
                onNavigateBack = { navController.popBackStack() },
                step = 2,
                totalSteps = totalSteps
            )
        }

        composable(
            route = OnboardingRoutes.ACTIVITY_LEVEL,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    slideSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    slideSpec
                )
            }
        ) {
            ActivityLevelScreen(
                viewModel = onboardingViewModel,
                onNavigateNext = { navController.navigate(OnboardingRoutes.NUTRITION_GOAL) },
                onNavigateBack = { navController.popBackStack() },
                step = 3,
                totalSteps = totalSteps
            )
        }

        composable(
            route = OnboardingRoutes.NUTRITION_GOAL,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    slideSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    slideSpec
                )
            }
        ) {
            NutritionGoalScreen(
                viewModel = onboardingViewModel,
                onNavigateNext = { navController.navigate(OnboardingRoutes.RESULT) },
                onNavigateBack = { navController.popBackStack() },
                step = 4,
                totalSteps = totalSteps
            )
        }

        composable(
            route = OnboardingRoutes.RESULT,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    slideSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    slideSpec
                )
            }
        ) {
            CalculationResultScreen(
                onComplete = onOnboardingComplete,
                onNavigateBack = { navController.popBackStack() },
                step = 5,
                totalSteps = totalSteps,
                onboardingViewModel = onboardingViewModel
            )
        }
    }
}
