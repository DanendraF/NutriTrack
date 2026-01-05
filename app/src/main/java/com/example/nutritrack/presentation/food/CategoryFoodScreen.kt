package com.example.nutritrack.presentation.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFoodScreen(
    categoryId: String,
    onNavigateBack: () -> Unit
) {
    val categoryName = when(categoryId) {
        "breakfast" -> "Sarapan"
        "lunch" -> "Makan Siang"
        "snack" -> "Camilan"
        else -> "Makanan"
    }

    // Sample food items - in real app, this would come from ViewModel
    val foodItems = listOf(
        CategoryFoodItem("Nasi Goreng", "300 kcal", "breakfast"),
        CategoryFoodItem("Roti Bakar", "150 kcal", "breakfast"),
        CategoryFoodItem("Telur Dadar", "90 kcal", "breakfast"),
        CategoryFoodItem("Bubur Ayam", "180 kcal", "breakfast"),
        CategoryFoodItem("Nasi Uduk", "250 kcal", "breakfast"),
        CategoryFoodItem("Pisang Goreng", "120 kcal", "snack")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        categoryName,
                        color = TextBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(paddingValues)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(foodItems) { food ->
                    CategoryFoodCard(food = food)
                }
            }
        }
    }
}

@Composable
private fun CategoryFoodCard(food: CategoryFoodItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Food icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(LightGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = DarkGreen,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = food.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextBlack,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = food.calories,
                fontSize = 12.sp,
                color = TextGray
            )
        }
    }
}

data class CategoryFoodItem(
    val name: String,
    val calories: String,
    val category: String
)
