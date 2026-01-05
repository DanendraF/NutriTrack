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
    onNavigateToCaloriesDetail: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    authViewModel: FirebaseAuthViewModel = koinViewModel(),
    mealViewModel: MealViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val deleteMealState by mealViewModel.deleteMealState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Note: User data check is now done in LoginScreen before reaching here
    // If user reaches HomeScreen, they should have data
    // But we keep this as safety check in case of edge cases
    LaunchedEffect(userState) {
        when (userState) {
            is com.example.nutritrack.domain.model.UiState.Error -> {
                val errorMsg = (userState as com.example.nutritrack.domain.model.UiState.Error).message
                android.util.Log.e("HomeScreen", "⚠️ Error loading user data: $errorMsg")
                // Don't redirect to onboarding here - user should already have data
                // This might be a network error or other issue
            }
            else -> {}
        }
    }

    // Initialize userId and refresh on every screen appearance
    LaunchedEffect(key1 = Unit) {
        authViewModel.getCurrentUserId()?.let { userId ->
            viewModel.setUserId(userId)
        }
    }

    // Refresh data when returning to this screen (e.g., after adding meal)
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            // Refresh when leaving screen (so it's fresh when coming back)
            viewModel.refreshData()
        }
    }

    // Also refresh when screen first loads
    LaunchedEffect(key1 = true) {
        viewModel.refreshData()
    }

    // Handle delete result
    LaunchedEffect(deleteMealState) {
        when (deleteMealState) {
            is com.example.nutritrack.domain.model.UiState.Success -> {
                snackbarHostState.showSnackbar("Meal deleted successfully")
                mealViewModel.resetDeleteState()
                // Refresh data after successful deletion
                viewModel.refreshData()
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
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 80.dp)
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
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
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
                        progress = uiState.progressPercentage / 100f,
                        weeklyCalories = uiState.weeklyCalories,
                        onClick = onNavigateToCaloriesDetail
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
                item {
                    ProgressTrackerCard(
                        weeklyData = uiState.weeklyCalories,
                        targetCalories = uiState.targetCalories
                    )
                }
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
        // Profile Picture Circle
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(DarkGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.firstOrNull()?.uppercase() ?: "U",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Hai $userName!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Daily Goals: $targetCalories kcal - ${progressPercentage.toInt()}% Done",
                fontSize = 11.sp,
                color = TextGray
            )
        }
        IconButton(onClick = onSyncClick) {
            Icon(
                Icons.Default.Cloud,
                contentDescription = "Sync",
                tint = TextGray,
                modifier = Modifier.size(22.dp)
            )
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = TextGray,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun CaloriesCard(
    consumed: Float,
    target: Int,
    remaining: Int,
    progress: Float,
    weeklyCalories: List<Pair<String, Int>>,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Calories", fontSize = 16.sp, color = TextGray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextBlack
                                )
                            ) {
                                append("${consumed.toInt()}")
                            }
                            withStyle(style = SpanStyle(fontSize = 20.sp, color = TextGray)) {
                                append(" / ${target} kcal")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Remaining $remaining kcal",
                        fontSize = 14.sp,
                        color = TextGray
                    )
                }
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.size(80.dp),
                        color = DarkGreen,
                        strokeWidth = 8.dp,
                        trackColor = LightGreen.copy(alpha = 0.3f),
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        "${(progress * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextBlack
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

            // Weekly Progress Chart
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Weekly Progress",
                fontSize = 14.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            WeeklyProgressChart(
                weeklyCalories = weeklyCalories,
                targetCalories = target
            )
        }
    }
}

@Composable
private fun WeeklyProgressChart(
    weeklyCalories: List<Pair<String, Int>>,
    targetCalories: Int
) {
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    // Use real data from API, if empty use empty list
    val weekDays = if (weeklyCalories.isNotEmpty()) {
        weeklyCalories.map { it.first } // "Mon", "Tue", etc from API
    } else {
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    val weekDaysShort = weekDays.map { it.first().toString() } // "M", "T", "W", etc

    val caloriesData = if (weeklyCalories.isNotEmpty()) {
        weeklyCalories.map { it.second } // Actual calories from API
    } else {
        listOf(0, 0, 0, 0, 0, 0, 0) // Empty data if no API data
    }

    val progressData = caloriesData.map {
        if (targetCalories > 0) (it.toFloat() / targetCalories).coerceIn(0f, 1f)
        else 0f
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            progressData.forEachIndexed { index, progressValue ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedDayIndex = index }
                ) {
                    // Bar
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height((progressValue * 80).dp.coerceAtLeast(4.dp))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                when {
                                    selectedDayIndex == index -> LightGreen
                                    index == progressData.lastIndex -> DarkGreen
                                    else -> DarkGreen.copy(alpha = 0.5f)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Day label
                    Text(
                        text = weekDaysShort[index],
                        fontSize = 10.sp,
                        color = when {
                            selectedDayIndex == index -> LightGreen
                            index == progressData.lastIndex -> DarkGreen
                            else -> TextGray
                        },
                        fontWeight = if (index == progressData.lastIndex || selectedDayIndex == index)
                            FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Show selected day details
        selectedDayIndex?.let { index ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = LightGreen.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, LightGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = weekDays[index],
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                        Text(
                            text = "${caloriesData[index]} kcal consumed",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                    Text(
                        text = "${(progressData[index] * 100).toInt()}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayMealsSection(
    meals: List<Meal>,
    todayDate: String,
    onDeleteMeal: (Meal) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Today's meals",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )
            Text(
                DateUtils.formatDateForDisplay(todayDate),
                fontSize = 14.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food image placeholder (circular)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = mealTypeColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Meal info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.foodName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${meal.calories} kcal",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Meal type badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(mealTypeColor)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    meal.mealType.displayName.uppercase(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProgressTrackerCard(
    weeklyData: List<Pair<String, Int>>,
    targetCalories: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Weekly Progress", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)

            // Average calories indicator
            if (weeklyData.isNotEmpty()) {
                val avgCalories = weeklyData.map { it.second }.filter { it > 0 }.average().toInt()
                if (avgCalories > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = "Average",
                            tint = DarkGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Avg: $avgCalories kcal",
                            fontSize = 12.sp,
                            color = DarkGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Daily Calories",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextBlack
                    )

                    // Target line indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .background(DarkGreen.copy(alpha = 0.3f))
                        )
                        Text(
                            "Target",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (weeklyData.isEmpty() || weeklyData.all { it.second == 0 }) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.LightGray
                            )
                            Text(
                                "No data yet",
                                fontSize = 14.sp,
                                color = TextGray
                            )
                            Text(
                                "Start logging meals to see your progress",
                                fontSize = 12.sp,
                                color = TextGray.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    WeeklyBarChart(
                        data = weeklyData,
                        targetCalories = targetCalories
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyBarChart(
    data: List<Pair<String, Int>>,
    targetCalories: Int
) {
    val maxValue = (data.maxOfOrNull { it.second } ?: targetCalories).coerceAtLeast(targetCalories) * 1.1f

    Box(modifier = Modifier.fillMaxWidth()) {
        // Target line (background)
        val targetLineHeight = if (maxValue > 0) {
            ((targetCalories.toFloat() / maxValue) * 140).dp
        } else 0.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 0.dp)
                .align(Alignment.BottomStart)
                .offset(y = -24.dp - targetLineHeight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        DarkGreen.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }

        // Bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { (day, calories) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Calories value
                    if (calories > 0) {
                        Text(
                            text = if (calories >= 1000) {
                                "${(calories / 100.0).toInt()}00"
                            } else {
                                "$calories"
                            },
                            fontSize = 10.sp,
                            color = TextBlack,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Bar
                    val barHeight = if (maxValue > 0 && calories > 0) {
                        ((calories.toFloat() / maxValue) * 140).coerceAtLeast(8f)
                    } else if (calories == 0) {
                        8f
                    } else {
                        8f
                    }

                    val barColor = when {
                        calories == 0 -> Color.LightGray.copy(alpha = 0.3f)
                        calories > targetCalories * 1.1 -> Color(0xFFE57373) // Light Red
                        calories > targetCalories -> OrangeIndicator
                        calories >= targetCalories * 0.8 -> DarkGreen
                        else -> LightGreen
                    }

                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(barHeight.dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(barColor)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Day label
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        color = if (calories > 0) TextBlack else TextGray.copy(alpha = 0.5f),
                        fontWeight = if (calories > 0) FontWeight.Medium else FontWeight.Normal
                    )
                }
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
