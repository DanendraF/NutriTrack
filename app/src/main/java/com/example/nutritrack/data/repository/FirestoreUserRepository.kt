package com.example.nutritrack.data.repository

import com.example.nutritrack.domain.model.User
import com.example.nutritrack.domain.model.ActivityLevel
import com.example.nutritrack.domain.model.Macros
import com.example.nutritrack.domain.model.NutritionGoal
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val localUserRepository: UserRepository
) {

    private val usersCollection = firestore.collection("users")

    suspend fun saveUserToFirestore(user: User): Result<Unit> {
        return try {
            val userData = hashMapOf(
                "name" to user.name,
                "email" to user.email,
                "gender" to user.gender,
                "age" to user.age,
                "height" to user.height,
                "weight" to user.weight,
                "activityLevel" to user.activityLevel.name,
                "nutritionGoal" to user.nutritionGoal.name,
                "targetCalories" to user.targetCalories,
                "targetProtein" to user.targetMacros.protein,
                "targetCarbs" to user.targetMacros.carbs,
                "targetFat" to user.targetMacros.fat,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            usersCollection.document(user.userId).set(userData).await()

            // Also save to local Room database
            localUserRepository.saveUser(user)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserFromFirestore(userId: String): Result<User?> {
        return try {
            val document = usersCollection.document(userId).get().await()

            if (document.exists()) {
                val user = User(
                    userId = userId,
                    name = document.getString("name") ?: "",
                    email = document.getString("email") ?: "",
                    gender = document.getString("gender") ?: "Male",
                    age = document.getLong("age")?.toInt() ?: 0,
                    height = document.getDouble("height")?.toFloat() ?: 0f,
                    weight = document.getDouble("weight")?.toFloat() ?: 0f,
                    activityLevel = ActivityLevel.fromString(
                        document.getString("activityLevel") ?: "SEDENTARY"
                    ),
                    nutritionGoal = NutritionGoal.fromString(
                        document.getString("nutritionGoal") ?: "MAINTAIN_WEIGHT"
                    ),
                    targetCalories = document.getLong("targetCalories")?.toInt() ?: 2000,
                    targetMacros = Macros(
                        protein = document.getDouble("targetProtein")?.toFloat() ?: 0f,
                        carbs = document.getDouble("targetCarbs")?.toFloat() ?: 0f,
                        fat = document.getDouble("targetFat")?.toFloat() ?: 0f
                    )
                )

                // Save to local database for offline access
                localUserRepository.saveUser(user)

                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUser(userId: String): Flow<User?> = callbackFlow {
        val listener = usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = User(
                        userId = userId,
                        name = snapshot.getString("name") ?: "",
                        email = snapshot.getString("email") ?: "",
                        gender = snapshot.getString("gender") ?: "Male",
                        age = snapshot.getLong("age")?.toInt() ?: 0,
                        height = snapshot.getDouble("height")?.toFloat() ?: 0f,
                        weight = snapshot.getDouble("weight")?.toFloat() ?: 0f,
                        activityLevel = ActivityLevel.fromString(
                            snapshot.getString("activityLevel") ?: "SEDENTARY"
                        ),
                        nutritionGoal = NutritionGoal.fromString(
                            snapshot.getString("nutritionGoal") ?: "MAINTAIN_WEIGHT"
                        ),
                        targetCalories = snapshot.getLong("targetCalories")?.toInt() ?: 2000,
                        targetMacros = Macros(
                            protein = snapshot.getDouble("targetProtein")?.toFloat() ?: 0f,
                            carbs = snapshot.getDouble("targetCarbs")?.toFloat() ?: 0f,
                            fat = snapshot.getDouble("targetFat")?.toFloat() ?: 0f
                        )
                    )
                    trySend(user)
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun updateUserInFirestore(user: User): Result<Unit> {
        return try {
            val updates = hashMapOf(
                "name" to user.name,
                "gender" to user.gender,
                "age" to user.age,
                "height" to user.height,
                "weight" to user.weight,
                "activityLevel" to user.activityLevel.name,
                "nutritionGoal" to user.nutritionGoal.name,
                "targetCalories" to user.targetCalories,
                "targetProtein" to user.targetMacros.protein,
                "targetCarbs" to user.targetMacros.carbs,
                "targetFat" to user.targetMacros.fat,
                "updatedAt" to System.currentTimeMillis()
            )

            usersCollection.document(user.userId).update(updates as Map<String, Any>).await()

            // Update local database
            localUserRepository.updateUser(user)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUserFromFirestore(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId).delete().await()

            // Delete from local database
            localUserRepository.deleteUser(userId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
