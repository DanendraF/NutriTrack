package com.example.nutritrack.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutritrack.ui.theme.DarkGreen
import com.example.nutritrack.ui.theme.NutriTrackTheme
import com.example.nutritrack.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderAgeScreen(
    viewModel: OnboardingViewModel,
    onNavigateNext: () -> Unit,
    onNavigateBack: () -> Unit,
    step: Int,
    totalSteps: Int
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
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
        bottomBar = { NavigationButtons(onBack = onNavigateBack, onNext = onNavigateNext) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Tell Us About You", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text("This helps us create your personalized plan", fontSize = 14.sp, color = TextGray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GenderCard(
                    label = "Male",
                    icon = Icons.Default.Male,
                    isSelected = uiState.gender == "male",
                    onClick = { viewModel.updateGender("male") },
                    modifier = Modifier.weight(1f)
                )
                GenderCard(
                    label = "Female",
                    icon = Icons.Default.Female,
                    isSelected = uiState.gender == "female",
                    onClick = { viewModel.updateGender("female") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GenderCard(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFFF3F4F6) else Color.White
    val contentColor = if (isSelected) DarkGreen else TextGray
    val border = if (isSelected) BorderStroke(2.dp, DarkGreen) else BorderStroke(1.dp, Color.LightGray)

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = label, fontWeight = FontWeight.SemiBold, color = contentColor)
        }
    }
}

@Preview
@Composable
fun GenderAgeScreenPreview() {
    NutriTrackTheme {
        GenderAgeScreen(viewModel(), {}, {}, 1, 5)
    }
}
