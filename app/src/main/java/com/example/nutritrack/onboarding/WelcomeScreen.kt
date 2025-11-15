package com.example.nutritrack.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.R // Ganti dengan ID gambar Anda jika berbeda
import com.example.nutritrack.ui.theme.DarkGreen
import com.example.nutritrack.ui.theme.NutriTrackTheme
import com.example.nutritrack.ui.theme.TextGray

@Composable
fun WelcomeScreen(onNavigateNext: () -> Unit) {
    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logont), // GANTI dengan gambar Anda
                    contentDescription = "Welcome Illustration",
                    modifier = Modifier.fillMaxWidth(0.7f),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "Selamat Datang di NutriTrack!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ayo mulai perjalananmu menuju gaya hidup yang lebih sehat.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = TextGray,
                    lineHeight = 24.sp
                )
            }
            Button(
                onClick = onNavigateNext,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Mulai Sekarang", fontSize = 18.sp)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun WelcomeScreenPreview() {
    NutriTrackTheme {
        WelcomeScreen(onNavigateNext = {})
    }
}
