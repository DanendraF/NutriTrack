package com.example.nutritrack.onboarding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationResultScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    step: Int,
    totalSteps: Int
) {
    val uiState by viewModel.uiState.collectAsState()

    val calories = remember(uiState) {
        // Kalkulasi kalori sederhana (gantilah dengan logika TDEE Anda)
        val height = uiState.height.toFloatOrNull() ?: 0f
        val weight = uiState.weight.toFloatOrNull() ?: 0f
        if (height > 0 && weight > 0) 2000 else 0
    }

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
        bottomBar = {
            // Tombol di sini hanya untuk menyelesaikan
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("Complete & Start Journey", color = Color.White)
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
                text = "$calories kcal / hari",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
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

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun CalculationResultScreenPreview() {
    NutriTrackTheme {
        CalculationResultScreen(viewModel(), {}, {}, 5, 5)
    }
}
