package com.example.nutritrack.data.remote.openai

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
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

class OpenAIService {
    private val openAI = OpenAI(BuildConfig.OPENAI_API_KEY)
    private val gson = Gson()

    suspend fun calculateNutrition(request: NutritionCalculationRequest): Result<NutritionCalculationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(request)

                val chatRequest = ChatCompletionRequest(
                    model = ModelId("gpt-3.5-turbo"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = "You are a professional nutritionist. Always respond with valid JSON only, no additional text."
                        ),
                        ChatMessage(
                            role = ChatRole.User,
                            content = prompt
                        )
                    ),
                    temperature = 0.7,
                    maxTokens = 500
                )

                val completion: ChatCompletion = openAI.chatCompletion(chatRequest)
                val responseText = completion.choices.first().message.content ?: ""

                // Parse JSON response
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
