package com.example.nutritrack.data.repository

import com.example.nutritrack.domain.model.Tip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

interface TipsRepository {
    fun getTips(): Flow<List<Tip>>
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
}
