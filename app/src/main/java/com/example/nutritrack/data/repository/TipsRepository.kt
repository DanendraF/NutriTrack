package com.example.nutritrack.data.repository

import com.example.nutritrack.domain.model.Tip
import com.example.nutritrack.domain.model.DailyRecommend
import com.example.nutritrack.domain.model.Article
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

interface TipsRepository {
    fun getTips(): Flow<List<Tip>>
    fun getDailyRecommends(): Flow<List<DailyRecommend>>
    fun getArticles(): Flow<List<Article>>
}

@Singleton
class TipsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TipsRepository {

    override fun getTips(): Flow<List<Tip>> = callbackFlow {
        val listener = firestore.collection("tips")
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("TipsRepository", "Error fetching tips", error)
                    close(error)
                    return@addSnapshotListener
                }

                val tips = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Tip(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            category = doc.getString("category") ?: "",
                            icon = doc.getString("icon") ?: "",
                            order = doc.getLong("order")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("TipsRepository", "Error parsing tip: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                android.util.Log.d("TipsRepository", "Fetched ${tips.size} tips")
                trySend(tips)
            }

        awaitClose { listener.remove() }
    }

    override fun getDailyRecommends(): Flow<List<DailyRecommend>> = callbackFlow {
        val listener = firestore.collection("daily_recommends")
            .orderBy("order", Query.Direction.ASCENDING)
            .limit(6) // Get top 6 recommends
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("TipsRepository", "Error fetching daily recommends", error)
                    close(error)
                    return@addSnapshotListener
                }

                val recommends = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        DailyRecommend(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            category = doc.getString("category") ?: "",
                            icon = doc.getString("icon") ?: "",
                            order = doc.getLong("order")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("TipsRepository", "Error parsing recommend: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                android.util.Log.d("TipsRepository", "Fetched ${recommends.size} daily recommends")
                trySend(recommends)
            }

        awaitClose { listener.remove() }
    }

    override fun getArticles(): Flow<List<Article>> = callbackFlow {
        val listener = firestore.collection("articles")
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("TipsRepository", "Error fetching articles", error)
                    close(error)
                    return@addSnapshotListener
                }

                val articles = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Article(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            category = doc.getString("category") ?: "",
                            readTime = doc.getString("readTime") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            content = doc.getString("content") ?: "",
                            author = doc.getString("author") ?: "",
                            publishedAt = doc.getString("publishedAt") ?: "",
                            order = doc.getLong("order")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("TipsRepository", "Error parsing article: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                android.util.Log.d("TipsRepository", "Fetched ${articles.size} articles")
                trySend(articles)
            }

        awaitClose { listener.remove() }
    }
}
