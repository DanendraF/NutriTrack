package com.example.nutritrack

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.domain.model.UserSavedFood
import com.example.nutritrack.presentation.food.UserSavedFoodViewModel
import com.example.nutritrack.presentation.food.FoodHistoryViewModel
import com.example.nutritrack.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun FoodScreen(
    onNavigateToFoodSearch: () -> Unit = {},
    viewModel: UserSavedFoodViewModel = koinViewModel(),
    foodHistoryViewModel: FoodHistoryViewModel = koinViewModel()
) {
    val savedFoods by viewModel.savedFoods.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val recentMeals by foodHistoryViewModel.recentMeals.collectAsState()
    val historyLoading by foodHistoryViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        foodHistoryViewModel.loadRecentMeals()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentPadding = PaddingValues(vertical = 24.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { FoodTopBar() }
        item {
            SavedFoodsSection(
                savedFoods = savedFoods,
                isLoading = isLoading,
                onDeleteFood = { foodId -> viewModel.deleteFood(foodId) },
                onQuickAddFood = { foodId -> viewModel.markFoodAsUsed(foodId) },
                onNavigateToFoodSearch = onNavigateToFoodSearch
            )
        }
        item {
            HistorySection(
                recentMeals = recentMeals,
                isLoading = historyLoading
            )
        }
    }

    // Show error snackbar if there's an error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // TODO: Show snackbar with error message
            viewModel.clearError()
        }
    }
}

@Composable
private fun FoodTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("My Foods", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextBlack)
            Text("Your saved and frequently eaten foods", fontSize = 14.sp, color = TextGray)
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
    }
}

@Composable
private fun SavedFoodsSection(
    savedFoods: List<UserSavedFood>,
    isLoading: Boolean,
    onDeleteFood: (String) -> Unit,
    onQuickAddFood: (String) -> Unit,
    onNavigateToFoodSearch: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Saved Foods", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
            TextButton(onClick = onNavigateToFoodSearch) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Food", fontSize = 14.sp)
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            savedFoods.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextGray
                        )
                        Text("No saved foods yet", color = TextGray, fontSize = 14.sp)
                        Text("Add foods you eat frequently", color = TextGray, fontSize = 12.sp)
                    }
                }
            }
            else -> {
                savedFoods.forEach { food ->
                    SavedFoodItem(
                        food = food,
                        onDelete = { onDeleteFood(food.id) },
                        onQuickAdd = { onQuickAddFood(food.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedFoodItem(
    food: UserSavedFood,
    onDelete: () -> Unit,
    onQuickAdd: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = DarkGreen
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(food.foodName, fontWeight = FontWeight.SemiBold, color = TextBlack)
                if (food.note.isNotEmpty()) {
                    Text(food.note, fontSize = 12.sp, color = TextGray)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${food.calories} kcal",
                        fontSize = 12.sp,
                        color = DarkGreen,
                        fontWeight = FontWeight.Medium
                    )
                    Text("•", fontSize = 12.sp, color = TextGray)
                    Text(food.servingSize, fontSize = 12.sp, color = TextGray)
                    if (food.customPortion != 1.0f) {
                        Text("•", fontSize = 12.sp, color = TextGray)
                        Text("${food.customPortion}x", fontSize = 12.sp, color = DarkGreen, fontWeight = FontWeight.Medium)
                    }
                }
            }
            IconButton(onClick = onQuickAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add to meal", tint = DarkGreen)
            }
        }
    }
}

@Composable
private fun HistorySection(
    recentMeals: List<com.example.nutritrack.presentation.food.RecentMeal>,
    isLoading: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Recent Foods", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = DarkGreen
                )
            }
        } else if (recentMeals.isEmpty()) {
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
                        modifier = Modifier.size(48.dp),
                        tint = TextGray.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No recent meals",
                        fontSize = 14.sp,
                        color = TextGray
                    )
                }
            }
        } else {
            recentMeals.forEach { meal ->
                HistoryItem(meal)
            }
        }
    }
}

@Composable
private fun HistoryItem(meal: com.example.nutritrack.presentation.food.RecentMeal) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = DarkGreen
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(meal.name, fontWeight = FontWeight.SemiBold, color = TextBlack)
            Text("${meal.date} • ${meal.calories} kcal", fontSize = 12.sp, color = TextGray)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodScreenPreview() {
    NutriTrackTheme { FoodScreen() }
}
