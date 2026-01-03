package com.example.nutritrack.onboarding

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutritrack.presentation.onboarding.viewmodel.OnboardingViewModel

object OnboardingRoutes {
    const val SIMPLE_ONBOARDING = "simple_onboarding"
    // Old routes (commented for reference)
    // const val WELCOME = "welcome"
    // const val GENDER_AGE = "gender_age"
    // const val MEASUREMENTS = "measurements"
    // const val ACTIVITY_LEVEL = "activity_level"
    // const val NUTRITION_GOAL = "nutrition_goal"
    // const val RESULT = "result"
}

@Composable
fun OnboardingNavHost(onOnboardingComplete: () -> Unit) {
    val navController = rememberNavController()
    val onboardingViewModel: OnboardingViewModel = koinViewModel()

    NavHost(navController = navController, startDestination = OnboardingRoutes.SIMPLE_ONBOARDING) {
        composable(OnboardingRoutes.SIMPLE_ONBOARDING) {
            SimpleOnboardingScreen(
                onOnboardingComplete = onOnboardingComplete,
                viewModel = onboardingViewModel
            )
        }
    }

    /* OLD MULTI-STEP FLOW (kept for reference)
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
        // ... other screens
    }
    */
}
