package com.example.nutritrack.presentation.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun FoodRecipeScreen(
    recipeId: String,
    onNavigateBack: () -> Unit
) {
    // Sample recipe data - in real app, this would come from ViewModel
    val recipe = RecipeDetail(
        name = "Nasi Goreng Spesial",
        calories = "300 kcal",
        servingSize = "1 porsi",
        prepTime = "15 menit",
        ingredients = listOf(
            "200g nasi putih",
            "2 butir telur",
            "100g ayam fillet",
            "2 siung bawang putih",
            "3 siung bawang merah",
            "2 sdm kecap manis",
            "1 sdt garam",
            "Minyak goreng secukupnya"
        ),
        instructions = listOf(
            "Panaskan minyak, tumis bawang putih dan bawang merah hingga harum",
            "Masukkan ayam fillet yang sudah dipotong kecil, masak hingga matang",
            "Masukkan telur, orak-arik hingga setengah matang",
            "Masukkan nasi putih, aduk rata dengan bumbu",
            "Tambahkan kecap manis dan garam, aduk hingga merata",
            "Masak dengan api sedang sambil diaduk hingga nasi matang sempurna",
            "Angkat dan sajikan selagi hangat"
        ),
        nutrition = mapOf(
            "Protein" to "25g",
            "Karbohidrat" to "45g",
            "Lemak" to "8g"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Resep",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(LightGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = DarkGreen,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Text(
                        text = recipe.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Kalori", fontSize = 12.sp, color = TextGray)
                            Text(recipe.calories, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Porsi", fontSize = 12.sp, color = TextGray)
                            Text(recipe.servingSize, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Waktu", fontSize = 12.sp, color = TextGray)
                            Text(recipe.prepTime, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                        }
                    }
                }
            }

            // Nutrition Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Nutrisi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )

                    recipe.nutrition.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(key, fontSize = 14.sp, color = TextGray)
                            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextBlack)
                        }
                    }
                }
            }

            // Ingredients Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Bahan-Bahan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )

                    recipe.ingredients.forEach { ingredient ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("•", fontSize = 14.sp, color = DarkGreen)
                            Text(ingredient, fontSize = 14.sp, color = TextBlack, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Instructions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Cara Membuat",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )

                    recipe.instructions.forEachIndexed { index, instruction ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = DarkGreen,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                instruction,
                                fontSize = 14.sp,
                                color = TextBlack,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class RecipeDetail(
    val name: String,
    val calories: String,
    val servingSize: String,
    val prepTime: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val nutrition: Map<String, String>
)
