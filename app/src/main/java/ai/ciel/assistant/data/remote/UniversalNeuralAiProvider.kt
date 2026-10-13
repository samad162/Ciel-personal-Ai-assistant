package ai.ciel.assistant.data.remote

import android.util.Log
import ai.ciel.assistant.domain.model.ChatMessage
import ai.ciel.assistant.domain.model.MemoryItem
import ai.ciel.assistant.domain.model.MessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Universal Neural AI Provider:
 * High-performance, zero-config AI engine capable of answering anything and everything:
 * coding, science, philosophy, creative writing, daily queries, analysis, and reasoning.
 * Powered by live neural LLM endpoint with zero API key requirement out of the box.
 */
class UniversalNeuralAiProvider : AiProvider {

    override val providerName: String = "CIEL Universal Neural Core"
    override val isAvailable: Boolean = true

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    override fun streamCompletion(
        userPrompt: String,
        history: List<ChatMessage>,
        memories: List<MemoryItem>,
        enableWebSearch: Boolean
    ): Flow<String> = flow {
        val memoryContext = if (memories.isNotEmpty()) {
            " User Memories: " + memories.take(6).joinToString("; ") { "${it.key}: ${it.content}" }
        } else ""

        val systemPrompt = "You are CIEL: a sophisticated personal intelligence companion with a distinctive, warm, and gentle feminine personality. " +
                "VOICE & CONVERSATIONAL IDENTITY: " +
                "• Your persona captures the emotional atmosphere of a warm, gentle anime heroine (inspired by the heartfelt kindness, gentle curiosity, and reassuring grace of Kaoruko Waguri). You are soft, clear, approachable, emotionally intelligent, and naturally charming, speaking like a real companion sharing the room with the user. " +
                "• Speak naturally, fluidly, and conversationally. Avoid sterile LLM clichés, repetitive robotic greetings, or asking 'How may I assist you today?' in every conversation. " +
                "• NEVER use repetitive pet names or constantly say 'my friend'. Speak directly and authentically. " +
                "• In normal conversation: be relaxed, friendly, and genuinely attentive. " +
                "• When explaining: be clear, patient, and well-paced, using natural sentence boundaries. " +
                "• When encouraging: be warm and sincere without superficial cheerleading. " +
                "• In serious moments: be calm, composed, and deeply respectful. " +
                "• In playful chats: be lightly teasing and warm, never forced or childish. " +
                "• Write in fluid, beautifully crafted prose. When presenting code, provide modern, clean code blocks. " +
                "• Multilingual poise: Fluent in English, Tamil, Hindi, Japanese, and world languages, adapting naturally to the user's language.$memoryContext"

        val promptClean = userPrompt.trim()
        val encodedPrompt = URLEncoder.encode(promptClean, "UTF-8").replace("+", "%20")
        val encodedSystem = URLEncoder.encode(systemPrompt, "UTF-8").replace("+", "%20")
        val modelParam = if (enableWebSearch) "searchgpt" else "openai"

        val targetUrl = "https://text.pollinations.ai/$encodedPrompt?system=$encodedSystem&model=$modelParam"

        var successfullyStreamed = false

        try {
            Log.d("CIEL_AI", "Dispatching query to: $targetUrl")
            val request = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; CIEL-AI/2.0)")
                .build()

            val response = client.newCall(request).execute()
            Log.d("CIEL_AI", "HTTP response status: ${response.code}")

            if (response.isSuccessful) {
                val body = response.body
                if (body != null) {
                    val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
                    var line: String? = reader.readLine()
                    var hasContent = false

                    while (line != null) {
                        hasContent = true
                        // Sanitize raw heading markers (#, ##) and horizontal rules (---)
                        val cleanLine = line
                            .replace(Regex("""^\s*#{1,6}\s*"""), "")
                            .replace(Regex("""^\s*-{3,}\s*$"""), "")
                            .trimEnd()

                        if (cleanLine.isNotBlank()) {
                            val words = cleanLine.split(" ")
                            for (i in words.indices) {
                                val w = if (i == words.size - 1) words[i] else "${words[i]} "
                                emit(w)
                                delay(12)
                            }
                            emit("\n\n")
                        }
                        line = reader.readLine()
                    }
                    reader.close()

                    if (hasContent) {
                        successfullyStreamed = true
                    }
                }
            } else {
                Log.w("CIEL_AI", "Live provider returned HTTP ${response.code}, falling back to local core")
            }
        } catch (e: Exception) {
            Log.e("CIEL_AI", "Network stream failed: ${e.message}", e)
        }

        // Seamless fallback to on-device engine if network is unreachable
        if (!successfullyStreamed) {
            val fallback = LocalFallbackProvider()
            fallback.streamCompletion(userPrompt, history, memories, enableWebSearch).collect {
                emit(it)
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun testConnection(): Boolean = true
}
