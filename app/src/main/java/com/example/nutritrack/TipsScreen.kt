package com.example.nutritrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.ui.theme.*

@Composable
fun TipsScreen() {
    // Definisikan data di awal agar bisa diakses oleh beberapa 'items' block
    val dummyNewTips = remember { listOf(Tip(1, "Add vegetables to each portion", "choose fresh green vegetables"), Tip(2, "Drink more water", "at least 8 glasses a day")) }
    val dummyArticles = remember { listOf(Article(1, "The Benefits of a Balanced Diet", "Nutrition", "5 min read", ""), Article(2, "Simple Exercises for a Healthy Heart", "Fitness", "7 min read", "")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentPadding = PaddingValues(vertical = 24.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Mengurangi spasi agar lebih konsisten
    ) {
        // Item 1: Top Bar
        item {
            TipsTopBar()
            Spacer(modifier = Modifier.height(8.dp)) // Menambah spasi tambahan setelah TopBar
        }

        // Item 2: Daily Recommends
        item { DailyRecommendsCard() }

        // --- PERBAIKAN STRUKTUR DAFTAR ---

        // Item 3: Judul untuk "New Tips"
        item {
            Text(
                text = "New Tips",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = TextBlack,
                modifier = Modifier.padding(top = 8.dp) // Beri sedikit spasi atas
            )
        }

        // Item 4: Daftar "New Tips" menggunakan items()
        items(dummyNewTips) { tip ->
            NewTipItem(tip = tip)
        }

        // ---------------------------------

        // Item 5: Judul untuk Artikel
        item { HealthyLivingArticlesSection() }

        // Item 6: Daftar Artikel
        items(dummyArticles) { article ->
            ArticleItem(article = article)
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
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray))
    }
}

@Composable
private fun DailyRecommendsCard() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardBackground), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Daily Recommends", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = TextBlack)
            Text("Quick ideas to improve daily nutrition", fontSize = 12.sp, color = TextGray)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChipInfo("Ganti soda dengan air putih", modifier = Modifier.weight(1f))
                ChipInfo("1 buah sebelum makan", modifier = Modifier.weight(1f))
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
        Text(text, color = DarkGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 14.sp, textAlign = TextAlign.Center)
    }
}

// Fungsi NewTipsCard() sudah tidak diperlukan lagi, jadi bisa dihapus.
// Logikanya sudah diintegrasikan ke dalam LazyColumn.

@Composable
private fun NewTipItem(tip: Tip) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LightGreen.copy(alpha = 0.7f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Eco, contentDescription = "Tip Icon", tint = DarkGreen, modifier = Modifier.size(32.dp).background(Color.White, CircleShape).padding(6.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(tip.title, fontWeight = FontWeight.Bold, color = TextBlack)
            Text(tip.subtitle, fontSize = 12.sp, color = TextGray)
        }
    }
}

@Composable
private fun HealthyLivingArticlesSection() {
    Text(text = "Healthy Living Articles", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextBlack, modifier = Modifier.padding(top = 8.dp))
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
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(article.category, fontSize = 12.sp, color = DarkGreen, fontWeight = FontWeight.SemiBold)
                Text(article.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextBlack, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(article.readTime, fontSize = 12.sp, color = TextGray)
            }
        }
    }
}

private data class Tip(val id: Int, val title: String, val subtitle: String)
private data class Article(val id: Int, val title: String, val category: String, val readTime: String, val imageUrl: String)

@Preview(showBackground = true)
@Composable
private fun TipsScreenPreview() {
    NutriTrackTheme {
        TipsScreen()
    }
}
