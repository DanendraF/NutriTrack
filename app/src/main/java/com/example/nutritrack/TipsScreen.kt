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
    onNavigateToArticleDetail: (Article) -> Unit = {}
) {
    val tips by viewModel.tips.collectAsState()
    val dailyRecommends by viewModel.dailyRecommends.collectAsState()
    val articles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Get user info
    val userName = "Farrell" // TODO: Get from user profile

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentPadding = PaddingValues(vertical = 24.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header with profile
        item {
            TipsScreenTopBar(userName = userName)
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
private fun TipsScreenTopBar(userName: String) {
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
                "Hoi $userName!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )
            Text(
                "180 kcal - 85% from",
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

        // "Lihat Selengkapnya" Button
        Button(
            onClick = { /* TODO: Navigate to all tips */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
        ) {
            Text(
                "Lihat Selengkapnya",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )
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
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkGreen,
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    "Baca",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
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
                Text(
                    "Please add you content here. keep it short and simple. And smile :)",
                    fontSize = 12.sp,
                    color = TextGray,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Tags
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LightGreen.copy(alpha = 0.7f),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            "Low Fats",
                            fontSize = 10.sp,
                            color = DarkGreen,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE3F2FD),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            "Tricks Body",
                            fontSize = 10.sp,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            "Whole Veg",
                            fontSize = 10.sp,
                            color = Color(0xFFFF6F00),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
