package com.example.nutritrack

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.presentation.food.FoodHistoryViewModel
import com.example.nutritrack.presentation.auth.FirebaseAuthViewModel
import com.example.nutritrack.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

data class FoodCategory(
    val id: String,
    val name: String,
    val color: Color,
    val icon: String = "🍎"
)

@Composable
fun FoodScreen(
    onNavigateToFoodSearch: () -> Unit = {},
    onNavigateToFoodHistory: () -> Unit = {},
    onNavigateToCategory: (String) -> Unit = {},
    onNavigateToRecipe: (String) -> Unit = {},
    authViewModel: FirebaseAuthViewModel = koinViewModel(),
    foodHistoryViewModel: FoodHistoryViewModel = koinViewModel()
) {
    val recentMeals by foodHistoryViewModel.recentMeals.collectAsStateWithLifecycle()
    val historyLoading by foodHistoryViewModel.isLoading.collectAsStateWithLifecycle()

    // Get user info
    val currentUserId = authViewModel.getCurrentUserId()
    val userName = "Farrell" // TODO: Get from user profile

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
        // Header
        item {
            FoodScreenTopBar(userName = userName)
        }

        // Food History Section
        item {
            FoodHistorySection(
                recentMeals = recentMeals,
                isLoading = historyLoading,
                onViewAll = onNavigateToFoodHistory,
                onMealClick = { meal ->
                    // Navigate to detail
                    onNavigateToFoodHistory()
                }
            )
        }

        // Category Section
        item {
            CategorySection(
                onCategoryClick = onNavigateToCategory
            )
        }
    }
}

@Composable
private fun FoodScreenTopBar(userName: String) {
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
                "Food Screen",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )
        }
        IconButton(onClick = { /* TODO: Search */ }) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = TextGray,
                modifier = Modifier.size(22.dp)
            )
        }
        IconButton(onClick = { /* TODO: Settings */ }) {
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
private fun FoodHistorySection(
    recentMeals: List<com.example.nutritrack.presentation.food.RecentMeal>,
    isLoading: Boolean,
    onViewAll: () -> Unit,
    onMealClick: (com.example.nutritrack.presentation.food.RecentMeal) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Food History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )
            TextButton(onClick = onViewAll) {
                Text(
                    "See All",
                    fontSize = 14.sp,
                    color = DarkGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkGreen)
            }
        } else if (recentMeals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
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
                        tint = TextGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No food history yet", fontSize = 14.sp, color = TextGray)
                }
            }
        } else {
            recentMeals.take(3).forEach { meal ->
                FoodHistoryItem(
                    meal = meal,
                    onClick = { onMealClick(meal) }
                )
            }
        }
    }
}

@Composable
private fun FoodHistoryItem(
    meal: com.example.nutritrack.presentation.food.RecentMeal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food icon
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
                    tint = DarkGreen,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${meal.date} • ${meal.calories} kcal",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    onCategoryClick: (String) -> Unit,
    recentMeals: List<com.example.nutritrack.presentation.food.RecentMeal>
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Meals by Type",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextBlack
        )

        // Group meals by type
        val breakfastMeals = recentMeals.filter { it.mealType?.lowercase() == "breakfast" }.take(3)
        val lunchMeals = recentMeals.filter { it.mealType?.lowercase() == "lunch" }.take(3)
        val snackMeals = recentMeals.filter { it.mealType?.lowercase() == "snack" }.take(3)
        val dinnerMeals = recentMeals.filter { it.mealType?.lowercase() == "dinner" }.take(3)

        // Show categories only if there are meals
        if (breakfastMeals.isNotEmpty()) {
            MealTypeSection(
                title = "Breakfast",
                meals = breakfastMeals,
                color = Color(0xFFFFA726)
            )
        }

        if (lunchMeals.isNotEmpty()) {
            MealTypeSection(
                title = "Lunch",
                meals = lunchMeals,
                color = Color(0xFF66BB6A)
            )
        }

        if (snackMeals.isNotEmpty()) {
            MealTypeSection(
                title = "Snack",
                meals = snackMeals,
                color = Color(0xFF42A5F5)
            )
        }

        if (dinnerMeals.isNotEmpty()) {
            MealTypeSection(
                title = "Dinner",
                meals = dinnerMeals,
                color = Color(0xFFEF5350)
            )
        }

        // Empty state
        if (recentMeals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Fastfood,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = TextGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No meals categorized yet", fontSize = 14.sp, color = TextGray)
                    Text("Start logging your meals!", fontSize = 12.sp, color = TextGray)
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(
    category: FoodCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = category.color,
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = category.name,
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Food items grid (placeholder)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5F5F5))
                            .clickable(onClick = onClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = category.color,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Salad Bush",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextBlack
                            )
                            Text(
                                "100 kcal",
                                fontSize = 9.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }
        }
    }
}
