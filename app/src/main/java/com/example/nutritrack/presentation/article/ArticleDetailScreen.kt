package com.example.nutritrack.presentation.article

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.nutritrack.domain.model.Article
import com.example.nutritrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    article: Article,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article", color = TextBlack, fontWeight = FontWeight.SemiBold) },
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
        ) {
            // Header Image
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            // Content Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightGreen.copy(alpha = 0.7f),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            text = article.category,
                            fontSize = 12.sp,
                            color = DarkGreen,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Title
                    Text(
                        text = article.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack,
                        lineHeight = 30.sp
                    )

                    // Metadata Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "By ${article.author}",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                        Text(
                            text = article.readTime,
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )

                    // Content - Parse markdown-like formatting
                    val paragraphs = article.content.split("\n\n")
                    paragraphs.forEach { paragraph ->
                        when {
                            paragraph.startsWith("**") && paragraph.endsWith("**") -> {
                                // Bold heading
                                Text(
                                    text = paragraph.removeSurrounding("**"),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextBlack,
                                    lineHeight = 26.sp,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                            }
                            paragraph.contains("**") -> {
                                // Mixed text with bold sections
                                FormattedText(paragraph)
                            }
                            else -> {
                                // Normal paragraph with justify alignment
                                Text(
                                    text = paragraph,
                                    fontSize = 16.sp,
                                    color = TextBlack.copy(alpha = 0.8f),
                                    lineHeight = 24.sp,
                                    textAlign = TextAlign.Justify,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormattedText(text: String) {
    // Simple parser for **bold** text
    val parts = text.split("**")
    val annotatedText = buildString {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 0) {
                append(part)
            } else {
                // This would be bold, but we'll handle it differently
                append(part)
            }
        }
    }

    Text(
        text = annotatedText,
        fontSize = 16.sp,
        color = TextBlack.copy(alpha = 0.8f),
        lineHeight = 24.sp,
        textAlign = TextAlign.Justify,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
