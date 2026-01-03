package com.example.nutritrack.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutritrack.presentation.onboarding.viewmodel.OnboardingViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleOnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
    firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Auto-fill email from Firebase
    LaunchedEffect(Unit) {
        firebaseAuth.currentUser?.email?.let { email ->
            viewModel.updateEmail(email)
        }
    }

    // Handle success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onOnboardingComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Your Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Let's get to know you",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "We need some basic information to calculate your nutrition goals",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Email (read-only)
            OutlinedTextField(
                value = uiState.email,
                onValueChange = {},
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                singleLine = true
            )

            // Gender
            Text(
                text = "Gender",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.gender == "Male",
                    onClick = { viewModel.updateGender("Male") },
                    label = { Text("Male") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.gender == "Female",
                    onClick = { viewModel.updateGender("Female") },
                    label = { Text("Female") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Age
            OutlinedTextField(
                value = uiState.age,
                onValueChange = { viewModel.updateAge(it) },
                label = { Text("Age (years)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Height
            OutlinedTextField(
                value = uiState.height,
                onValueChange = { viewModel.updateHeight(it) },
                label = { Text("Height (cm)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // Weight
            OutlinedTextField(
                value = uiState.weight,
                onValueChange = { viewModel.updateWeight(it) },
                label = { Text("Weight (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // Activity Level
            Text(
                text = "Activity Level",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "Sedentary" to "Little or no exercise",
                    "Lightly Active" to "Exercise 1-3 days/week",
                    "Moderately Active" to "Exercise 3-5 days/week",
                    "Very Active" to "Exercise 6-7 days/week"
                ).forEach { (level, desc) ->
                    FilterChip(
                        selected = uiState.activityLevel == level,
                        onClick = { viewModel.updateActivityLevel(level) },
                        label = {
                            Column {
                                Text(level, fontWeight = FontWeight.Medium)
                                Text(desc, style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Goal
            Text(
                text = "Your Goal",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "Lose Weight" to "Calorie deficit",
                    "Maintain Weight" to "Balanced calories",
                    "Gain Weight" to "Calorie surplus"
                ).forEach { (goal, desc) ->
                    FilterChip(
                        selected = uiState.goal == goal,
                        onClick = { viewModel.updateGoal(goal) },
                        label = {
                            Column {
                                Text(goal, fontWeight = FontWeight.Medium)
                                Text(desc, style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Error message
            if (uiState.saveError != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.saveError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit button
            Button(
                onClick = {
                    firebaseAuth.currentUser?.uid?.let { userId ->
                        viewModel.saveUserData(userId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Complete Setup")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
