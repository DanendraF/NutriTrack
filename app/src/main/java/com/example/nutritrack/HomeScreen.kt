package com.example.nutritrack

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutritrack.domain.model.Meal
import com.example.nutritrack.domain.model.MealType
import com.example.nutritrack.presentation.auth.FirebaseAuthViewModel
import com.example.nutritrack.presentation.home.HomeViewModel
import com.example.nutritrack.presentation.meal.MealViewModel
import com.example.nutritrack.ui.theme.*
import com.example.nutritrack.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddMeal: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    authViewModel: FirebaseAuthViewModel = koinViewModel(),
    mealViewModel: MealViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val deleteMealState by mealViewModel.deleteMealState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Initialize userId
    LaunchedEffect(Unit) {
        authViewModel.getCurrentUserId()?.let { userId ->
            viewModel.setUserId(userId)
        }
    }

    // Handle delete result
    LaunchedEffect(deleteMealState) {
        when (deleteMealState) {
            is com.example.nutritrack.domain.model.UiState.Success -> {
                snackbarHostState.showSnackbar("Meal deleted successfully")
                mealViewModel.resetDeleteState()
            }
            is com.example.nutritrack.domain.model.UiState.Error -> {
                snackbarHostState.showSnackbar(
                    (deleteMealState as com.example.nutritrack.domain.model.UiState.Error).message
                )
                mealViewModel.resetDeleteState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddMeal,
                containerColor = DarkGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Meal")
            }
        }
    ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundGray)
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 24.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    HomeTopBar(
                        userName = uiState.userName,
                        targetCalories = uiState.targetCalories,
                        progressPercentage = uiState.progressPercentage.toFloat(),
                        onSyncClick = { viewModel.refreshData() },
                        onSettingsClick = { /* TODO: Navigate to settings */ }
                    )
                }
                item {
                    CaloriesCard(
                        consumed = uiState.consumedCalories.toFloat(),
                        target = uiState.targetCalories,
                        remaining = uiState.remainingCalories,
                        progress = uiState.progressPercentage / 100f
                    )
                }
                item {
                    TodayMealsSection(
                        meals = uiState.todayMeals,
                        todayDate = uiState.todayDate,
                        onDeleteMeal = { meal ->
                            authViewModel.getCurrentUserId()?.let { userId ->
                                mealViewModel.deleteMeal(userId, meal.id, meal.timestamp)
                            }
                        }
                    )
                }
                item { ProgressTrackerCard() }
                item {
                    InsightAndTipsCard(
                        targetProtein = uiState.targetProtein.toFloat(),
                        consumedProtein = uiState.consumedProtein.toFloat(),
                        targetCarbs = uiState.targetCarbs.toFloat(),
                        consumedCarbs = uiState.consumedCarbs.toFloat(),
                        targetFat = uiState.targetFat.toFloat(),
                        consumedFat = uiState.consumedFat.toFloat()
                    )
                }
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
private fun TodayMealsSection(
    meals: List<Meal>,
    todayDate: String,
    onDeleteMeal: (Meal) -> Unit
) {
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

        if (meals.isEmpty()) {
            // Empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No meals logged yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        "Tap + button to add your first meal",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Display meals
            meals.forEach { meal ->
                MealItemCard(
                    meal = meal,
                    onDelete = { onDeleteMeal(meal) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealItemCard(
    meal: Meal,
    onDelete: () -> Unit
) {
    val mealTypeColor = when (meal.mealType) {
        MealType.BREAKFAST -> Color(0xFFF44336)
        MealType.LUNCH -> Color(0xFF2196F3)
        MealType.DINNER -> Color(0xFFFFC107)
        MealType.SNACK -> Color(0xFF4CAF50)
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val mealTime = timeFormat.format(meal.timestamp)

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Meal") },
            text = { Text("Are you sure you want to delete this meal?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(mealTypeColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = mealTypeColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Meal info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.foodName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${meal.quantity} × ${meal.servingSize}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = " • ",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = mealTime,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${meal.calories} kcal • P:${meal.protein}g C:${meal.carbs}g F:${meal.fat}g",
                    fontSize = 12.sp,
                    color = DarkGreen,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Meal type badge & delete
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(mealTypeColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        meal.mealType.displayName,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
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
