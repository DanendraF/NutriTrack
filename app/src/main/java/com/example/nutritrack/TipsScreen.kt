package com.example.nutritrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nutritrack.domain.model.Tip
import com.example.nutritrack.domain.model.DailyRecommend
import com.example.nutritrack.domain.model.Article
import com.example.nutritrack.presentation.tips.TipsViewModel
import com.example.nutritrack.presentation.auth.FirebaseAuthViewModel
import com.example.nutritrack.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun TipsScreen(
    viewModel: TipsViewModel = koinViewModel(),
    authViewModel: FirebaseAuthViewModel = koinViewModel(),
    homeViewModel: com.example.nutritrack.presentation.home.HomeViewModel = koinViewModel(),
    onNavigateToArticleDetail: (Article) -> Unit = {}
) {
    val tips by viewModel.tips.collectAsState()
    val dailyRecommends by viewModel.dailyRecommends.collectAsState()
    val articles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val homeUiState by homeViewModel.uiState.collectAsState()

    // Get user info
    val userName = "Farrell" // TODO: Get from user profile

    // Calculate progress percentage
    val progressPercentage = if (homeUiState.targetCalories > 0) {
        (homeUiState.consumedCalories.toFloat() / homeUiState.targetCalories.toFloat() * 100f)
    } else {
        0f
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header with profile
        item {
            TipsScreenTopBar(
                userName = userName,
                consumedCalories = homeUiState.consumedCalories,
                progressPercentage = progressPercentage
            )
        }

        // Rekomendasi Harian Section
        item {
            RecommendationSection(dailyRecommends)
        }

        // Tips Terbaru Section
        item {
            LatestTipsSection(tips)
        }

        // Untuk Tujuanmu Section
        item {
            GoalSection()
        }

        // Artikel Hidup Sehat Section
        item {
            Text(
                text = "Artikel Hidup Sehat",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )
        }

        if (articles.isEmpty()) {
            item {
                Text(
                    text = "No articles available",
                    fontSize = 14.sp,
                    color = TextGray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(articles) { article ->
                ArticleItem(
                    article = article,
                    onClick = { onNavigateToArticleDetail(article) }
                )
            }
        }
    }
}

@Composable
private fun TipsScreenTopBar(
    userName: String,
    consumedCalories: Int,
    progressPercentage: Float
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
                "$consumedCalories kcal - ${progressPercentage.toInt()}%",
                fontSize = 12.sp,
                color = TextGray
            )
        }
        IconButton(onClick = { /* TODO: Navigate to settings */ }) {
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
private fun RecommendationSection(recommends: List<DailyRecommend>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Rekomendasi harian",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextBlack
        )
        Text(
            "ide cepat untuk nutrisi harian kamu lebih baik",
            fontSize = 14.sp,
            color = TextGray
        )

        if (recommends.isEmpty()) {
            Text(
                "Makan buah setiap hari",
                fontSize = 14.sp,
                color = DarkGreen,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                "Batasi Gula harian",
                fontSize = 14.sp,
                color = DarkGreen
            )
        } else {
            recommends.take(2).forEach { recommend ->
                Text(
                    recommend.title,
                    fontSize = 14.sp,
                    color = DarkGreen
                )
            }
        }
    }
}

@Composable
private fun LatestTipsSection(tips: List<Tip>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Tips Terbaru",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextBlack
        )

        if (tips.isEmpty()) {
            // Sample tips if empty
            val sampleTips = listOf(
                Tip("tip1", "Sarapan tinggi protein", "Pilih sayuran hijau"),
                Tip("tip2", "Sarapan tinggi protein", "Pilih sayuran hijau"),
                Tip("tip3", "Sarapan tinggi protein", "Pilih sayuran hijau")
            )
            sampleTips.forEach { tip ->
                LatestTipCard(tip)
            }
        } else {
            tips.take(3).forEach { tip ->
                LatestTipCard(tip)
            }
        }
    }
}

@Composable
private fun LatestTipCard(tip: Tip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreen.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "Tip Icon",
                tint = DarkGreen,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .padding(8.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tip.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextBlack
                )
                Text(
                    tip.description,
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
private fun GoalSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Untuk Tujuanmu",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextBlack
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GoalCard(
                label = "Kalori",
                value = "Sembung",
                modifier = Modifier.weight(1f),
                color = Color(0xFFE8F5E9)
            )
            GoalCard(
                label = "Kalori",
                value = "Sembung",
                modifier = Modifier.weight(1f),
                color = Color(0xFFE8F5E9)
            )
        }

        // "Tambah" Button
        OutlinedButton(
            onClick = { /* TODO: Add goal */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = DarkGreen
            )
        ) {
            Text(
                "Tambah",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun GoalCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                label,
                fontSize = 12.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium
            )
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )
            Text(
                "porsi 50/30/20",
                fontSize = 10.sp,
                color = TextGray
            )
        }
    }
}

@Composable
private fun ArticleItem(
    article: Article,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Article Image
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            // Article Content
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    article.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        article.readTime,
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    Text(
                        article.category,
                        fontSize = 12.sp,
                        color = DarkGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
