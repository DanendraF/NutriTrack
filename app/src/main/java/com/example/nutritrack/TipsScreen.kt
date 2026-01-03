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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.domain.model.Tip
import com.example.nutritrack.domain.model.DailyRecommend
import com.example.nutritrack.domain.model.Article
import com.example.nutritrack.presentation.tips.TipsViewModel
import com.example.nutritrack.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun TipsScreen(
    viewModel: TipsViewModel = koinViewModel()
) {
    val tips by viewModel.tips.collectAsState()
    val dailyRecommends by viewModel.dailyRecommends.collectAsState()
    val articles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentPadding = PaddingValues(vertical = 24.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TipsTopBar()
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            DailyRecommendsCard(dailyRecommends)
        }

        item {
            Text(
                text = "Health Tips",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = TextBlack,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DarkGreen)
                }
            }
        } else if (tips.isEmpty()) {
            item {
                Text(
                    text = "No tips available",
                    fontSize = 14.sp,
                    color = TextGray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(tips) { tip ->
                TipItem(tip = tip)
            }
        }

        item {
            Text(
                text = "Healthy Living Articles",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextBlack,
                modifier = Modifier.padding(top = 8.dp)
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
                ArticleItem(article = article)
            }
        }
    }
}

@Composable
private fun TipsTopBar() {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Tips", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextBlack)
            Text("Daily tips and articles for a healthy life", fontSize = 14.sp, color = TextGray)
        }
    }
}

@Composable
private fun DailyRecommendsCard(recommends: List<DailyRecommend>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Daily Recommends", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = TextBlack)
            Text("Quick ideas to improve daily nutrition", fontSize = 12.sp, color = TextGray)
            Spacer(modifier = Modifier.height(16.dp))

            if (recommends.isEmpty()) {
                Text(
                    "Loading recommendations...",
                    fontSize = 12.sp,
                    color = TextGray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommends.take(4)) { recommend ->
                        ChipInfo(recommend.title, modifier = Modifier.width(160.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipInfo(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(LightGreen.copy(alpha = 0.7f))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = DarkGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TipItem(tip: Tip) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LightGreen.copy(alpha = 0.7f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = "Tip Icon",
            tint = DarkGreen,
            modifier = Modifier
                .size(32.dp)
                .background(Color.White, CircleShape)
                .padding(6.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(tip.title, fontWeight = FontWeight.Bold, color = TextBlack)
            Text(tip.description, fontSize = 12.sp, color = TextGray)
        }
    }
}

@Composable
private fun ArticleItem(article: Article) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { /* TODO: Handle article click */ }
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(article.category, fontSize = 12.sp, color = DarkGreen, fontWeight = FontWeight.SemiBold)
                Text(
                    article.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(article.readTime, fontSize = 12.sp, color = TextGray)
            }
        }
    }
}
