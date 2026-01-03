package com.example.nutritrack.onboarding

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutritrack.presentation.onboarding.viewmodel.OnboardingViewModel

object OnboardingRoutes {
    const val SIMPLE_ONBOARDING = "simple_onboarding"
}

@Composable
fun OnboardingNavHost(
    onOnboardingComplete: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val navController = rememberNavController()
    val onboardingViewModel: OnboardingViewModel = koinViewModel()

    NavHost(navController = navController, startDestination = OnboardingRoutes.SIMPLE_ONBOARDING) {
        composable(OnboardingRoutes.SIMPLE_ONBOARDING) {
            SimpleOnboardingScreen(
                onOnboardingComplete = onOnboardingComplete,
                onBackToLogin = onBackToLogin,
                viewModel = onboardingViewModel
            )
        }
    }
}
