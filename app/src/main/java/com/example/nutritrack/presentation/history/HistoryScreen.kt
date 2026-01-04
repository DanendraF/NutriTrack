package com.example.nutritrack.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.domain.model.Meal
import com.example.nutritrack.domain.model.MealType
import com.example.nutritrack.domain.model.UiState
import com.example.nutritrack.ui.theme.DarkGreen
import com.example.nutritrack.ui.theme.LightGreen
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel()
) {
    val dailyLogState by viewModel.dailyLogState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTodayLog()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal History") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGreen,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date Selector
            DateSelectorCard(
                selectedDate = selectedDate,
                onDateChange = { newDate ->
                    viewModel.selectDate(newDate)
                    viewModel.loadDailyLog(newDate)
                }
            )

            // Content based on state
            when (val state = dailyLogState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DarkGreen)
                    }
                }

                is UiState.Success -> {
                    val dailyLog = state.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Summary Card
                        item {
                            DailySummaryCard(
                                totalCalories = dailyLog.totalNutrition.calories.toInt(),
                                totalProtein = dailyLog.totalNutrition.protein.toInt(),
                                totalCarbs = dailyLog.totalNutrition.carbs.toInt(),
                                totalFat = dailyLog.totalNutrition.fat.toInt(),
                                mealCount = dailyLog.mealCount
                            )
                        }

                        // Meals grouped by type
                        val mealsByType = dailyLog.meals.groupBy { it.mealType }

                        MealType.entries.forEach { mealType ->
                            val mealsForType = mealsByType[mealType] ?: emptyList()
                            if (mealsForType.isNotEmpty()) {
                                item {
                                    Text(
                                        text = mealType.displayName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                                items(mealsForType) { meal ->
                                    MealHistoryItem(meal = meal)
                                }
                            }
                        }

                        if (dailyLog.meals.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Restaurant,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = Color.Gray
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            "No meals logged for this date",
                                            color = Color.Gray,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Red
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                state.message,
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadDailyLog(selectedDate) },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun DateSelectorCard(
    selectedDate: String,
    onDateChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreen.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val newDate = adjustDate(selectedDate, -1)
                onDateChange(newDate)
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day")
            }

            Text(
                text = formatDateDisplay(selectedDate),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = {
                val newDate = adjustDate(selectedDate, 1)
                onDateChange(newDate)
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
            }
        }
    }
}

@Composable
fun DailySummaryCard(
    totalCalories: Int,
    totalProtein: Int,
    totalCarbs: Int,
    totalFat: Int,
    mealCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkGreen.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Daily Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$mealCount meals",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                NutritionSummaryItem("Calories", "$totalCalories kcal", DarkGreen)
                NutritionSummaryItem("Protein", "${totalProtein}g", Color(0xFF2196F3))
                NutritionSummaryItem("Carbs", "${totalCarbs}g", Color(0xFFFFA726))
                NutritionSummaryItem("Fat", "${totalFat}g", Color(0xFFEF5350))
            }
        }
    }
}

@Composable
fun NutritionSummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun MealHistoryItem(meal: Meal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    meal.foodName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${meal.quantity} x ${meal.servingSize}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NutritionChip("${meal.calories} kcal", Color(0xFF4CAF50))
                    NutritionChip("P: ${meal.protein}g", Color(0xFF2196F3))
                    NutritionChip("C: ${meal.carbs}g", Color(0xFFFFA726))
                    NutritionChip("F: ${meal.fat}g", Color(0xFFEF5350))
                }
            }
        }
    }
}

@Composable
fun NutritionChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = color
        )
    }
}

// Helper functions
private fun adjustDate(dateString: String, days: Int): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = format.parse(dateString) ?: Date()
    val calendar = Calendar.getInstance().apply {
        time = date
        add(Calendar.DAY_OF_MONTH, days)
    }
    return format.format(calendar.time)
}

private fun formatDateDisplay(dateString: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
    val date = inputFormat.parse(dateString) ?: Date()

    val today = Calendar.getInstance()
    val dateCalendar = Calendar.getInstance().apply { time = date }

    return when {
        isSameDay(dateCalendar, today) -> "Today"
        isSameDay(dateCalendar, today.apply { add(Calendar.DAY_OF_MONTH, -1) }) -> "Yesterday"
        isSameDay(dateCalendar, today.apply { add(Calendar.DAY_OF_MONTH, 1) }) -> "Tomorrow"
        else -> outputFormat.format(date)
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
