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
import com.example.nutritrack.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun FoodScreen(
    onNavigateToFoodSearch: () -> Unit = {},
    viewModel: UserSavedFoodViewModel = koinViewModel()
) {
    val savedFoods by viewModel.savedFoods.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

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
                onNavigateToFoodSearch = onNavigateToFoodSearch
            )
        }
        item { HistorySection() }
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
                        onDelete = { onDeleteFood(food.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedFoodItem(
    food: UserSavedFood,
    onDelete: () -> Unit
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
            IconButton(onClick = { /*TODO: Quick add*/ }) {
                Icon(Icons.Default.Add, contentDescription = "Add to meal", tint = DarkGreen)
            }
        }
    }
}

@Composable
private fun HistorySection() {
    val dummyHistory = remember { listOf(FoodHistory(1, "Avocado Toast", "2 days ago", 300), FoodHistory(2, "Pancake Stack", "5 days ago", 420)) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("History", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
        dummyHistory.forEach { food -> HistoryItem(food) }
    }
}

@Composable
private fun HistoryItem(food: FoodHistory) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(food.name, fontWeight = FontWeight.SemiBold, color = TextBlack)
            Text("${food.date} • ${food.calories} kcal", fontSize = 12.sp, color = TextGray)
        }
        IconButton(onClick = { /*TODO*/ }) {
            Icon(Icons.Outlined.Visibility, contentDescription = "View", tint = TextGray)
        }
    }
}

private data class FoodHistory(val id: Int, val name: String, val date: String, val calories: Int)

@Preview(showBackground = true)
@Composable
private fun FoodScreenPreview() {
    NutriTrackTheme { FoodScreen() }
}
