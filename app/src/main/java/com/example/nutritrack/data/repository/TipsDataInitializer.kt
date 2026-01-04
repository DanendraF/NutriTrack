package com.example.nutritrack.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TipsDataInitializer @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun initializeDataIfNeeded() {
        try {
            // Check if tips collection is empty
            val tipsSnapshot = firestore.collection("tips").limit(1).get().await()
            if (tipsSnapshot.isEmpty) {
                android.util.Log.d("TipsDataInitializer", "Initializing tips...")
                initializeTips()
            }

            // Check if daily_recommends collection is empty
            val recommendsSnapshot = firestore.collection("daily_recommends").limit(1).get().await()
            if (recommendsSnapshot.isEmpty) {
                android.util.Log.d("TipsDataInitializer", "Initializing daily recommends...")
                initializeDailyRecommends()
            }

            // Check if articles collection is empty
            val articlesSnapshot = firestore.collection("articles").limit(1).get().await()
            if (articlesSnapshot.isEmpty) {
                android.util.Log.d("TipsDataInitializer", "Initializing articles...")
                initializeArticles()
            }

            android.util.Log.d("TipsDataInitializer", "✅ Tips data initialization complete!")
        } catch (e: Exception) {
            android.util.Log.e("TipsDataInitializer", "Error initializing tips data", e)
        }
    }

    private suspend fun initializeTips() {
        val tips = listOf(
            mapOf(
                "id" to "tip_1",
                "title" to "Minum Air Putih",
                "description" to "Minum minimal 8 gelas air putih setiap hari untuk menjaga hidrasi tubuh",
                "category" to "hydration",
                "icon" to "💧"
            ),
            mapOf(
                "id" to "tip_2",
                "title" to "Olahraga Teratur",
                "description" to "Lakukan olahraga minimal 30 menit setiap hari untuk kesehatan jantung",
                "category" to "exercise",
                "icon" to "🏃"
            ),
            mapOf(
                "id" to "tip_3",
                "title" to "Konsumsi Sayur & Buah",
                "description" to "Makan 5 porsi sayur dan buah setiap hari untuk nutrisi seimbang",
                "category" to "nutrition",
                "icon" to "🥗"
            ),
            mapOf(
                "id" to "tip_4",
                "title" to "Tidur Cukup",
                "description" to "Tidur 7-8 jam setiap malam untuk pemulihan tubuh optimal",
                "category" to "sleep",
                "icon" to "😴"
            ),
            mapOf(
                "id" to "tip_5",
                "title" to "Kurangi Gula",
                "description" to "Batasi konsumsi gula tambahan maksimal 50 gram per hari",
                "category" to "nutrition",
                "icon" to "🍬"
            ),
            mapOf(
                "id" to "tip_6",
                "title" to "Sarapan Sehat",
                "description" to "Jangan skip sarapan! Mulai hari dengan makanan bernutrisi",
                "category" to "nutrition",
                "icon" to "🍳"
            ),
            mapOf(
                "id" to "tip_7",
                "title" to "Protein Cukup",
                "description" to "Konsumsi protein 0.8-1g per kg berat badan untuk massa otot",
                "category" to "nutrition",
                "icon" to "🥩"
            ),
            mapOf(
                "id" to "tip_8",
                "title" to "Kelola Stress",
                "description" to "Lakukan meditasi atau yoga untuk mengurangi stress",
                "category" to "mental_health",
                "icon" to "🧘"
            )
        )

        tips.forEach { tip ->
            val id = tip["id"] as String
            firestore.collection("tips").document(id).set(tip).await()
            android.util.Log.d("TipsDataInitializer", "✓ Added tip: ${tip["title"]}")
        }
    }

    private suspend fun initializeDailyRecommends() {
        val recommends = listOf(
            mapOf(
                "id" to "rec_1",
                "title" to "Ganti soda dengan air putih",
                "description" to "Hindari minuman bersoda dan ganti dengan air putih untuk mengurangi asupan gula",
                "category" to "hydration",
                "icon" to "water",
                "order" to 1
            ),
            mapOf(
                "id" to "rec_2",
                "title" to "1 buah sebelum makan",
                "description" to "Konsumsi buah segar sebelum makan utama untuk asupan vitamin dan serat",
                "category" to "nutrition",
                "icon" to "fruit",
                "order" to 2
            ),
            mapOf(
                "id" to "rec_3",
                "title" to "Sarapan protein tinggi",
                "description" to "Mulai hari dengan sarapan berprotein tinggi seperti telur atau yogurt",
                "category" to "breakfast",
                "icon" to "egg",
                "order" to 3
            ),
            mapOf(
                "id" to "rec_4",
                "title" to "Kurangi gula tambahan",
                "description" to "Batasi konsumsi makanan dan minuman dengan gula tambahan",
                "category" to "nutrition",
                "icon" to "sugar",
                "order" to 4
            ),
            mapOf(
                "id" to "rec_5",
                "title" to "Makan sayur setiap makan",
                "description" to "Tambahkan sayuran di setiap waktu makan untuk nutrisi seimbang",
                "category" to "vegetables",
                "icon" to "vegetables",
                "order" to 5
            ),
            mapOf(
                "id" to "rec_6",
                "title" to "Batasi makanan olahan",
                "description" to "Kurangi konsumsi makanan olahan dan pilih makanan segar",
                "category" to "healthy_eating",
                "icon" to "natural",
                "order" to 6
            )
        )

        recommends.forEach { rec ->
            val id = rec["id"] as String
            firestore.collection("daily_recommends").document(id).set(rec).await()
            android.util.Log.d("TipsDataInitializer", "✓ Added recommend: ${rec["title"]}")
        }
    }

    private suspend fun initializeArticles() {
        val articles = listOf(
            mapOf(
                "id" to "article_1",
                "title" to "Manfaat Diet Seimbang untuk Kesehatan",
                "category" to "Nutrition",
                "readTime" to "5 min read",
                "imageUrl" to "",
                "content" to "Diet seimbang adalah kunci untuk hidup sehat. Dengan mengonsumsi berbagai jenis makanan dalam proporsi yang tepat, tubuh mendapatkan semua nutrisi yang dibutuhkan.",
                "author" to "Dr. Nutrisi",
                "publishedAt" to "2026-01-01",
                "order" to 1
            ),
            mapOf(
                "id" to "article_2",
                "title" to "Olahraga Sederhana untuk Jantung Sehat",
                "category" to "Fitness",
                "readTime" to "7 min read",
                "imageUrl" to "",
                "content" to "Jantung yang sehat adalah fondasi kehidupan yang berkualitas. Olahraga teratur seperti berjalan kaki, jogging ringan, dan berenang dapat meningkatkan kesehatan jantung.",
                "author" to "Dr. Kardio",
                "publishedAt" to "2026-01-02",
                "order" to 2
            ),
            mapOf(
                "id" to "article_3",
                "title" to "Pentingnya Sarapan untuk Metabolisme",
                "category" to "Nutrition",
                "readTime" to "4 min read",
                "imageUrl" to "",
                "content" to "Sarapan adalah waktu makan terpenting yang memberikan energi untuk memulai hari. Sarapan yang sehat dapat meningkatkan metabolisme dan konsentrasi.",
                "author" to "Nutritionist Sarah",
                "publishedAt" to "2026-01-03",
                "order" to 3
            ),
            mapOf(
                "id" to "article_4",
                "title" to "Mengelola Stress dengan Pola Makan Sehat",
                "category" to "Mental Health",
                "readTime" to "6 min read",
                "imageUrl" to "",
                "content" to "Pola makan yang sehat tidak hanya baik untuk tubuh, tetapi juga untuk kesehatan mental. Makanan kaya omega-3 dan vitamin B dapat membantu mengurangi stress.",
                "author" to "Dr. Mental Wellness",
                "publishedAt" to "2025-12-30",
                "order" to 4
            ),
            mapOf(
                "id" to "article_5",
                "title" to "Hidrasi yang Cukup untuk Performa Optimal",
                "category" to "Hydration",
                "readTime" to "3 min read",
                "imageUrl" to "",
                "content" to "Air adalah komponen vital tubuh. Minum cukup air setiap hari membantu fungsi organ, meningkatkan energi, dan menjaga kesehatan kulit.",
                "author" to "Health Coach Maya",
                "publishedAt" to "2025-12-29",
                "order" to 5
            ),
            mapOf(
                "id" to "article_6",
                "title" to "Protein untuk Pembentukan Otot",
                "category" to "Fitness",
                "readTime" to "8 min read",
                "imageUrl" to "",
                "content" to "Protein adalah building block untuk otot. Konsumsi protein yang cukup dari sumber hewani dan nabati penting untuk pertumbuhan dan pemulihan otot.",
                "author" to "Fitness Trainer Budi",
                "publishedAt" to "2025-12-28",
                "order" to 6
            )
        )

        articles.forEach { article ->
            val id = article["id"] as String
            firestore.collection("articles").document(id).set(article).await()
            android.util.Log.d("TipsDataInitializer", "✓ Added article: ${article["title"]}")
        }
    }
}
