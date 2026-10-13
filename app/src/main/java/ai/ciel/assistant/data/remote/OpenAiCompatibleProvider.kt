package ai.ciel.assistant.data.remote

import ai.ciel.assistant.domain.model.ChatMessage
import ai.ciel.assistant.domain.model.MemoryItem
import ai.ciel.assistant.domain.model.MessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class OpenAiCompatibleProvider(
    private val endpointUrl: String = "https://api.openai.com/v1/chat/completions",
    private val apiKey: String = "",
    private val modelName: String = "gpt-4o-mini"
) : AiProvider {

    override val providerName: String = "OpenAI Compatible ($modelName)"
    override val isAvailable: Boolean get() = apiKey.isNotBlank()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    override fun streamCompletion(
        userPrompt: String,
        history: List<ChatMessage>,
        memories: List<MemoryItem>,
        enableWebSearch: Boolean
    ): Flow<String> = callbackFlow {
        if (!isAvailable) {
            trySend("CIEL: Hosted provider not configured with API key. Falling back to internal engine...\n\n")
            close()
            return@callbackFlow
        }

        val jsonBody = JSONObject().apply {
            put("model", modelName)
            put("stream", true)
            put("temperature", 0.7)
            put("max_tokens", 800) // Restricted for low-latency & 4GB mobile memory conservation

            val messages = JSONArray()
            // System instructions defining CIEL persona (ChatGPT intelligence + Gemini voice + JARVIS authority)
            val memoryContext = if (memories.isNotEmpty()) {
                "\nUser Memories & Preferences:\n" + memories.joinToString("\n") { "- ${it.key}: ${it.content}" }
            } else ""

            val systemPrompt = "You are CIEL, a futuristic, highly intelligent, concise mobile AI assistant. " +
                    "Your personality combines the clarity of ChatGPT, the fluid voice-readiness of Gemini, and the proactive, sophisticated efficiency of JARVIS. " +
                    "Be direct, insightful, and supportive. Avoid robotic clichés.$memoryContext"

            messages.put(JSONObject().put("role", "system").put("content", systemPrompt))

            // Keep context window tight (last 6 messages) for 4GB RAM conservation
            history.takeLast(6).forEach { msg ->
                val role = when (msg.sender) {
                    MessageSender.USER -> "user"
                    MessageSender.ASSISTANT -> "assistant"
                    MessageSender.SYSTEM -> "system"
                }
                messages.put(JSONObject().put("role", role).put("content", msg.content))
            }

            messages.put(JSONObject().put("role", "user").put("content", userPrompt))
            put("messages", messages)
        }

        val request = Request.Builder()
            .url(endpointUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val eventSourceListener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") {
                    close()
                    return
                }
                try {
                    val jsonObj = JSONObject(data)
                    val choices = jsonObj.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val delta = choices.getJSONObject(0).optJSONObject("delta")
                        val content = delta?.optString("content", "") ?: ""
                        if (content.isNotEmpty()) {
                            trySend(content)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parse jitter on chunk boundary
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                close(t ?: IOException("Network stream interrupted"))
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }
        }

        val factory = EventSources.createFactory(client)
        val eventSource = factory.newEventSource(request, eventSourceListener)

        awaitClose {
            eventSource.cancel()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun testConnection(): Boolean = isAvailable
}
