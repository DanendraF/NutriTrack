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
import com.example.nutritrack.ui.theme.*

@Composable
fun FoodScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentPadding = PaddingValues(vertical = 24.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { FoodTopBar() }
        item { CaloriesDetectionCard() }
        item { PreviewCard() }
        item { HistorySection() }
    }
}

@Composable
private fun FoodTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Food Tracking", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextBlack)
            Text("photos of food or drinks to count calories", fontSize = 14.sp, color = TextGray)
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
private fun CaloriesDetectionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Calories Detection", fontWeight = FontWeight.SemiBold, color = TextBlack)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightGreen.copy(alpha = 0.3f))
                    .border(1.dp, LightGreen, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = TextGray, modifier = Modifier.size(40.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ActionButton(text = "open camera", icon = Icons.Default.CameraAlt, modifier = Modifier.weight(1f))
                ActionButton(text = "choose from gallery", icon = Icons.Default.Image, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ActionButton(text: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Button(
        onClick = { /*TODO*/ },
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LightGreen)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = DarkGreen)
            Text(text, color = DarkGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PreviewCard() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Preview", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = TextGray)
                    Spacer(Modifier.width(8.dp))
                    Text("Detection : Pancake with slice strawberry", color = TextGray, modifier = Modifier.weight(1f))
                    Text("- 420 kcal", color = TextGray, fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    NutrientInfo("Carbohydrate", "62 g", Modifier.weight(1f))
                    NutrientInfo("Fat", "12 g", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    NutrientInfo("Protein", "18 g", Modifier.weight(1f))
                    NutrientInfo("Carbohydrate", "24 g", Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    var count by remember { mutableStateOf(1) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Portion", color = TextGray)
                        Spacer(Modifier.width(16.dp))
                        IconButton(onClick = { if (count > 1) count-- }) { Icon(Icons.Default.Remove, null) }
                        Text("$count", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { count++ }) { Icon(Icons.Default.Add, null) }
                    }
                    Button(onClick = { /*TODO*/ }, colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)) { Text("Save") }
                }
            }
        }
    }
}

@Composable
private fun NutrientInfo(label: String, value: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .clip(RoundedCornerShape(12.dp))
        .background(LightGreen)
        .padding(12.dp)) {
        Column {
            Text(label, fontSize = 12.sp, color = TextGray)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlack)
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
