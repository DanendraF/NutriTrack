package com.example.nutritrack.data.remote.openai

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.TextPart
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.nutritrack.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class NutritionCalculationRequest(
    val age: Int,
    val gender: String,
    val height: Int,
    val weight: Int,
    val activityLevel: String,
    val goal: String
)

data class NutritionCalculationResponse(
    val targetCalories: Int,
    val targetProtein: Int,
    val targetCarbs: Int,
    val targetFat: Int,
    val analysis: String,
    val tips: List<String>
)

data class FoodAnalysisResponse(
    val name: String,
    val nameIndonesian: String,
    val category: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float,
    val sugar: Float,
    val sodium: Float
)

class OpenAIService {

    private val openAI = OpenAI("OPENAI_API")

    private val gson = Gson()

    suspend fun analyzeFoodImage(base64Image: String): Result<FoodAnalysisResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val promptText = """
                Identifikasi makanan dalam gambar. Berikan data estimasi nutrisi per 100 gram.
                Output HARUS dalam format JSON mentah dengan struktur berikut:
                {
                  "name": "nama makanan dalam bahasa Inggris",
                  "nameIndonesian": "nama makanan dalam bahasa Indonesia",
                  "category": "kategori",
                  "calories": 0.0,
                  "protein": 0.0,
                  "carbs": 0.0,
                  "fat": 0.0,
                  "fiber": 0.0,
                  "sugar": 0.0,
                  "sodium": 0.0
                }
                """.trimIndent()

                // PERBAIKAN UTAMA: Langsung mengirimkan listOf(TextPart, ImagePart)
                // ke parameter content karena ChatMessage di versi Anda meminta List<ContentPart>
                val chatRequest = ChatCompletionRequest(
                    model = ModelId("gpt-4o"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.User,
                            content = listOf(
                                TextPart(promptText),
                                ImagePart("data:image/jpeg;base64,$base64Image")
                            )
                        )
                    ),
                    responseFormat = ChatResponseFormat.JsonObject
                )

                val completion: ChatCompletion = openAI.chatCompletion(chatRequest)
                val responseText = completion.choices.first().message.content ?: ""

                val response = gson.fromJson(responseText, FoodAnalysisResponse::class.java)
                Result.success(response)
            } catch (e: Exception) {
                android.util.Log.e("OpenAIService", "Error analyzing food image", e)
                Result.failure(e)
            }
        }
    }

    suspend fun calculateNutrition(request: NutritionCalculationRequest): Result<NutritionCalculationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val promptText = buildPrompt(request)

                val chatRequest = ChatCompletionRequest(
                    model = ModelId("gpt-4o"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = "You are a professional nutritionist. Always respond with valid JSON only."
                        ),
                        ChatMessage(
                            role = ChatRole.User,
                            content = promptText
                        )
                    ),
                    temperature = 0.7,
                    maxTokens = 500,
                    responseFormat = ChatResponseFormat.JsonObject
                )

                val completion: ChatCompletion = openAI.chatCompletion(chatRequest)
                val responseText = completion.choices.first().message.content ?: ""

                val response = parseGPTResponse(responseText)
                Result.success(response)
            } catch (e: Exception) {
                android.util.Log.e("OpenAIService", "Error calling GPT API", e)
                Result.failure(e)
            }
        }
    }

    private fun buildPrompt(request: NutritionCalculationRequest): String {
        return """
        Calculate daily nutritional needs based on:
        - Age: ${request.age}, Gender: ${request.gender}, Height: ${request.height}, Weight: ${request.weight}
        Return JSON with: targetCalories, targetProtein, targetCarbs, targetFat, analysis, tips.
        """.trimIndent()
    }

    private fun parseGPTResponse(responseText: String): NutritionCalculationResponse {
        return try {
            gson.fromJson(responseText, NutritionCalculationResponse::class.java)
        } catch (e: Exception) {
            NutritionCalculationResponse(2000, 150, 250, 67, "Error", emptyList())
        }
    }
}
