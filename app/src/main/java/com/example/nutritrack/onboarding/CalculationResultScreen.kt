package com.example.nutritrack.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutritrack.presentation.auth.FirebaseAuthViewModel
import com.example.nutritrack.presentation.onboarding.viewmodel.OnboardingViewModel
import com.example.nutritrack.ui.theme.DarkGreen
import com.example.nutritrack.ui.theme.NutriTrackTheme
import com.example.nutritrack.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationResultScreen(
    onComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    step: Int,
    totalSteps: Int,
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
    authViewModel: FirebaseAuthViewModel = hiltViewModel()
) {
    val onboardingState by onboardingViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Calculate nutrition targets when screen loads
    LaunchedEffect(Unit) {
        onboardingViewModel.calculateNutritionTargets()
    }

    // Handle save result
    LaunchedEffect(onboardingState.saveSuccess) {
        if (onboardingState.saveSuccess) {
            onComplete()
        }
    }

    // Handle errors
    LaunchedEffect(onboardingState.saveError) {
        onboardingState.saveError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            onboardingViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White,
        topBar = {
            Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextGray)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OnboardingProgressBar(currentStep = step, totalSteps = totalSteps)
            }
        },
        bottomBar = {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                Button(
                    onClick = {
                        // Get current user ID from Firebase
                        val userId = authViewModel.getCurrentUserId()
                        if (userId != null) {
                            onboardingViewModel.saveUserData(userId)
                        } else {
                            // If no user logged in, show error
                            // This shouldn't happen in normal flow
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                    enabled = !onboardingState.isSaving
                ) {
                    if (onboardingState.isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Complete & Start Journey", color = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                color = DarkGreen.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = DarkGreen,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("You're All Set!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Berdasarkan data Anda, target kalori harian Anda adalah sekitar:",
                fontSize = 14.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${onboardingState.calculatedCalories} kcal / hari",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Show macro breakdown
            if (onboardingState.calculatedMacros != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Target Nutrisi Harian:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextGray
                    )
                    MacroRow("Protein", "${onboardingState.calculatedMacros?.protein?.toInt() ?: 0}g")
                    MacroRow("Karbohidrat", "${onboardingState.calculatedMacros?.carbs?.toInt() ?: 0}g")
                    MacroRow("Lemak", "${onboardingState.calculatedMacros?.fat?.toInt() ?: 0}g")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Anda dapat menyesuaikan target ini nanti di profil Anda.",
                fontSize = 12.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MacroRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = TextGray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun CalculationResultScreenPreview() {
    NutriTrackTheme {
        CalculationResultScreen(
            onComplete = {},
            onNavigateBack = {},
            step = 5,
            totalSteps = 5
        )
    }
}
