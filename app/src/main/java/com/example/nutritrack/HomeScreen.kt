package com.example.nutritrack

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutritrack.presentation.home.HomeViewModel
import com.example.nutritrack.ui.theme.*
import com.example.nutritrack.utils.DateUtils

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentPadding = PaddingValues(vertical = 24.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            HomeTopBar(
                userName = uiState.userName,
                targetCalories = uiState.targetCalories,
                progressPercentage = uiState.progressPercentage,
                onSyncClick = { viewModel.refreshData() },
                onSettingsClick = { /* TODO: Navigate to settings */ }
            )
        }
        item {
            CaloriesCard(
                consumed = uiState.consumedCalories,
                target = uiState.targetCalories,
                remaining = uiState.remainingCalories,
                progress = uiState.progressPercentage / 100f
            )
        }
        item {
            TodayMealsSection(
                todayDate = uiState.todayDate
            )
        }
        item { ProgressTrackerCard() }
        item {
            InsightAndTipsCard(
                targetProtein = uiState.targetProtein,
                consumedProtein = uiState.consumedProtein,
                targetCarbs = uiState.targetCarbs,
                consumedCarbs = uiState.consumedCarbs,
                targetFat = uiState.targetFat,
                consumedFat = uiState.consumedFat
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    userName: String,
    targetCalories: Int,
    progressPercentage: Float,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(DarkGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.firstOrNull()?.uppercase() ?: "U",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Hai $userName!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )
            Text(
                "Daily Goals : $targetCalories kcal - ${progressPercentage.toInt()}% Done",
                fontSize = 12.sp,
                color = TextGray
            )
        }
        IconButton(onClick = onSyncClick) {
            Icon(Icons.Default.Cloud, contentDescription = "Sync", tint = TextGray)
        }
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextGray)
        }
    }
}

@Composable
private fun CaloriesCard(
    consumed: Float,
    target: Int,
    remaining: Int,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Calories", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        ) {
                            append("${consumed.toInt()}")
                        }
                        withStyle(style = SpanStyle(fontSize = 18.sp, color = Color.Gray)) {
                            append(" / $target kcal")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.size(60.dp),
                        color = DarkGreen,
                        strokeWidth = 6.dp,
                        trackColor = LightGreen,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        "${(progress * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = DarkGreen,
                trackColor = LightGreen,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Remaining $remaining kcal",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun TodayMealsSection(todayDate: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Today's meals", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(
                DateUtils.formatDateForDisplay(todayDate),
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        // TODO: Display real meals from database
        MealItem(tagText = "Breakfast", tagColor = Color(0xFFF44336))
        MealItem(tagText = "Lunch", tagColor = Color(0xFF2196F3))
        MealItem(tagText = "Dinner", tagColor = Color(0xFFFFC107))
    }
}

@Composable
private fun MealItem(tagText: String, tagColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(LightGreen)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("No meals logged", fontWeight = FontWeight.Bold)
                Text(
                    "Tap to add $tagText",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(tagColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    tagText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ProgressTrackerCard() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Progress Tracker's", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Charts will be added later", color = Color.Gray)
            }
        }
    }
}

@Composable
private fun InsightAndTipsCard(
    targetProtein: Float,
    consumedProtein: Float,
    targetCarbs: Float,
    consumedCarbs: Float,
    targetFat: Float,
    consumedFat: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Insight & Tips", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

        // Protein insight
        val proteinPercent = if (targetProtein > 0) (consumedProtein / targetProtein) * 100 else 0f
        if (proteinPercent < 50) {
            InsightCard(
                message = "Protein intake rendah hari ini",
                tip = "Tambahkan telur, ayam, atau ikan di makan berikutnya",
                icon = Icons.Default.Lightbulb
            )
        }

        // Carbs insight
        val carbsPercent = if (targetCarbs > 0) (consumedCarbs / targetCarbs) * 100 else 0f
        if (carbsPercent < 50) {
            InsightCard(
                message = "Karbohidrat masih kurang",
                tip = "Nasi, roti, atau pasta bisa jadi pilihan",
                icon = Icons.Default.Lightbulb
            )
        }

        // Default insight if all good
        if (proteinPercent >= 50 && carbsPercent >= 50) {
            InsightCard(
                message = "Nutrisi hari ini sudah baik!",
                tip = "Terus pertahankan pola makan sehat",
                icon = Icons.Default.Lightbulb
            )
        }
    }
}

@Composable
private fun InsightCard(
    message: String,
    tip: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreen)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = "Tips", tint = DarkGreen)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        message,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(OrangeIndicator)
                    )
                }
                Text(
                    tip,
                    fontSize = 12.sp,
                    color = DarkGreen.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    NutriTrackTheme {
        HomeScreen()
    }
}
