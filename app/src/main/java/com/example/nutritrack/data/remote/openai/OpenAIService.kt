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
    private val openAI = OpenAI(BuildConfig.OPENAI_API_KEY)
    private val gson = Gson()

    /**
     * Analyze food image using GPT-4 Vision
     * @param base64Image Base64 encoded image string
     * @return Result with food nutrition data
     */
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

    /**
     * Calculate personalized nutrition targets based on user profile
     * @param request User profile data
     * @return Result with calculated nutrition targets
     */
    suspend fun calculateNutrition(request: NutritionCalculationRequest): Result<NutritionCalculationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val promptText = buildPrompt(request)

                val chatRequest = ChatCompletionRequest(
                    model = ModelId("gpt-3.5-turbo"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = "You are a professional nutritionist. Always respond with valid JSON only, no additional text."
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
Calculate daily nutritional needs based on the following profile:
- Age: ${request.age} years
- Gender: ${request.gender}
- Height: ${request.height} cm
- Weight: ${request.weight} kg
- Activity Level: ${request.activityLevel}
- Goal: ${request.goal}

Return a JSON object with this exact format:
{
  "targetCalories": <number>,
  "targetProtein": <number in grams>,
  "targetCarbs": <number in grams>,
  "targetFat": <number in grams>,
  "analysis": "<brief explanation in Indonesian, max 100 words>",
  "tips": ["<tip 1 in Indonesian>", "<tip 2 in Indonesian>", "<tip 3 in Indonesian>"]
}

Use scientific methods like Harris-Benedict or Mifflin-St Jeor equation as base. Adjust for activity level and goals. Be precise and evidence-based.
        """.trimIndent()
    }

    private fun parseGPTResponse(responseText: String): NutritionCalculationResponse {
        // Clean up response (remove markdown code blocks if present)
        val cleanedResponse = responseText
            .replace("```json", "")
            .replace("```", "")
            .trim()

        return try {
            gson.fromJson(cleanedResponse, NutritionCalculationResponse::class.java)
        } catch (e: Exception) {
            android.util.Log.e("OpenAIService", "Failed to parse GPT response: $cleanedResponse", e)
            // Fallback to default values
            NutritionCalculationResponse(
                targetCalories = 2000,
                targetProtein = 150,
                targetCarbs = 250,
                targetFat = 67,
                analysis = "Terjadi kesalahan dalam perhitungan. Menggunakan nilai default.",
                tips = listOf(
                    "Konsultasikan dengan ahli gizi untuk perhitungan yang lebih akurat",
                    "Perhatikan asupan protein harian Anda",
                    "Minum air putih minimal 8 gelas per hari"
                )
            )
        }
    }

    fun close() {
        // OpenAI client cleanup if needed
    }
}
