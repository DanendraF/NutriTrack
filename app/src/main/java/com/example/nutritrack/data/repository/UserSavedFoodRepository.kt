package com.example.nutritrack.data.repository

import android.util.Log
import com.example.nutritrack.domain.model.UserSavedFood
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface UserSavedFoodRepository {
    suspend fun saveFood(userSavedFood: UserSavedFood): Result<String>
    fun getUserSavedFoods(userId: String): Flow<List<UserSavedFood>>
    fun getFrequentlyUsedFoods(userId: String, limit: Int = 10): Flow<List<UserSavedFood>>
    suspend fun updateFood(userSavedFood: UserSavedFood): Result<Unit>
    suspend fun updateLastUsed(foodId: String, userId: String): Result<Unit>
    suspend fun deleteFood(foodId: String, userId: String): Result<Unit>
    suspend fun getFoodById(foodId: String, userId: String): UserSavedFood?
}

@Singleton
class FirestoreUserSavedFoodRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserSavedFoodRepository {

    private val collection = firestore.collection("user_saved_foods")

    override suspend fun saveFood(userSavedFood: UserSavedFood): Result<String> {
        return try {
            val docRef = if (userSavedFood.id.isEmpty()) {
                collection.document()
            } else {
                collection.document(userSavedFood.id)
            }

            val foodWithId = userSavedFood.copy(id = docRef.id)
            docRef.set(foodWithId).await()

            Log.d("UserSavedFoodRepo", "✅ Saved food: ${foodWithId.foodName} for user ${foodWithId.userId}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("UserSavedFoodRepo", "❌ Error saving food", e)
            Result.failure(e)
        }
    }

    override fun getUserSavedFoods(userId: String): Flow<List<UserSavedFood>> = callbackFlow {
        val listener = collection
            .whereEqualTo("userId", userId)
            .orderBy("lastUsedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("UserSavedFoodRepo", "Error listening to saved foods", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val foods = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(UserSavedFood::class.java)
                    } catch (e: Exception) {
                        Log.e("UserSavedFoodRepo", "Error parsing food: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                Log.d("UserSavedFoodRepo", "📦 Loaded ${foods.size} saved foods for user $userId")
                trySend(foods)
            }

        awaitClose { listener.remove() }
    }

    override fun getFrequentlyUsedFoods(userId: String, limit: Int): Flow<List<UserSavedFood>> = callbackFlow {
        val listener = collection
            .whereEqualTo("userId", userId)
            .orderBy("useCount", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("UserSavedFoodRepo", "Error getting frequent foods", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val foods = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(UserSavedFood::class.java)
                } ?: emptyList()

                trySend(foods)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun updateFood(userSavedFood: UserSavedFood): Result<Unit> {
        return try {
            val docRef = collection.document(userSavedFood.id)
            val existingFood = docRef.get().await().toObject(UserSavedFood::class.java)

            if (existingFood?.userId == userSavedFood.userId) {
                docRef.set(userSavedFood).await()
                Log.d("UserSavedFoodRepo", "✅ Updated food: ${userSavedFood.foodName}")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Food not found or unauthorized"))
            }
        } catch (e: Exception) {
            Log.e("UserSavedFoodRepo", "❌ Error updating food", e)
            Result.failure(e)
        }
    }

    override suspend fun updateLastUsed(foodId: String, userId: String): Result<Unit> {
        return try {
            val docRef = collection.document(foodId)
            val food = docRef.get().await().toObject(UserSavedFood::class.java)

            if (food?.userId == userId) {
                docRef.update(
                    mapOf(
                        "lastUsedAt" to System.currentTimeMillis(),
                        "useCount" to (food.useCount + 1)
                    )
                ).await()

                Log.d("UserSavedFoodRepo", "✅ Updated last used for food: $foodId")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Food not found or unauthorized"))
            }
        } catch (e: Exception) {
            Log.e("UserSavedFoodRepo", "❌ Error updating last used", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteFood(foodId: String, userId: String): Result<Unit> {
        return try {
            val docRef = collection.document(foodId)
            val food = docRef.get().await().toObject(UserSavedFood::class.java)

            if (food?.userId == userId) {
                docRef.delete().await()
                Log.d("UserSavedFoodRepo", "✅ Deleted food: $foodId")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Food not found or unauthorized"))
            }
        } catch (e: Exception) {
            Log.e("UserSavedFoodRepo", "❌ Error deleting food", e)
            Result.failure(e)
        }
    }

    override suspend fun getFoodById(foodId: String, userId: String): UserSavedFood? {
        return try {
            val doc = collection.document(foodId).get().await()
            val food = doc.toObject(UserSavedFood::class.java)

            if (food?.userId == userId) {
                food
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("UserSavedFoodRepo", "❌ Error getting food by id", e)
            null
        }
    }
}
