package com.example.nutritrack.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.example.nutritrack.data.remote.openai.OpenAIService
import com.example.nutritrack.domain.model.Food
import com.example.nutritrack.domain.model.NutritionInfo
import com.example.nutritrack.domain.model.ServingSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class ScanRepository(private val openAIService: OpenAIService) {

    suspend fun analyzeFoodImage(bitmap: Bitmap): Result<Food> {
        return withContext(Dispatchers.IO) {
            try {
                val base64Image = encodeImageToBase64(bitmap)
                val response = openAIService.analyzeFoodImage(base64Image)
                
                response.map { foodResponse ->
                    Food(
                        foodId = UUID.randomUUID().toString(),
                        name = foodResponse.name,
                        nameIndonesian = foodResponse.nameIndonesian,
                        category = foodResponse.category,
                        nutrition = NutritionInfo(
                            calories = foodResponse.calories,
                            protein = foodResponse.protein,
                            carbs = foodResponse.carbs,
                            fat = foodResponse.fat,
                            fiber = foodResponse.fiber,
                            sugar = foodResponse.sugar,
                            sodium = foodResponse.sodium
                        ),
                        servingSize = ServingSize(
                            amount = 100f,
                            unit = "gram"
                        ),
                        barcode = null,
                        imageUrl = null, // Diisi nanti jika diupload ke storage
                        isVerified = false,
                        source = "ai_scan"
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
