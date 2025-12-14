package com.example.nutritrack.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutritrack.ui.theme.DarkGreen
import com.example.nutritrack.ui.theme.NutriTrackTheme
import com.example.nutritrack.ui.theme.TextGray
import com.example.nutritrack.presentation.onboarding.viewmodel.OnboardingViewModel

private val activityOptions = listOf(
    "Sedentary" to "Banyak duduk, jarang olahraga.",
    "Lightly Active" to "Olahraga ringan 1-3 kali/minggu.",
    "Moderately Active" to "Olahraga sedang 3-5 kali/minggu.",
    "Very Active" to "Olahraga berat hampir setiap hari."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLevelScreen(
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))
            Text("Your Activity Level?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Berapa sering kamu beraktivitas fisik dalam seminggu?", fontSize = 14.sp, color = TextGray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(activityOptions) { (title, subtitle) ->
                    val key = title.replace(" ", "").lowercase()
                    ActivityCard(
                        title = title,
                        subtitle = subtitle,
                        isSelected = uiState.activityLevel == key,
                        onClick = { viewModel.updateActivityLevel(key) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFF3F4F6) else Color.White
    val borderColor = if (isSelected) DarkGreen else Color.LightGray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, color = TextGray, fontSize = 14.sp)
            }
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = DarkGreen)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ActivityLevelScreenPreview() {
    NutriTrackTheme {
        ActivityLevelScreen(viewModel(), {}, {}, 3, 5)
    }
}
