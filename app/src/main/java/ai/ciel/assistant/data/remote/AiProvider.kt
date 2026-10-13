package ai.ciel.assistant.data.remote

import ai.ciel.assistant.domain.model.ChatMessage
import ai.ciel.assistant.domain.model.MemoryItem
import kotlinx.coroutines.flow.Flow

interface AiProvider {
    val providerName: String
    val isAvailable: Boolean

    /**
     * Streams real-time tokens from the conversational model.
     * Incorporates conversational history and retrieved long-term memory.
     */
    fun streamCompletion(
        userPrompt: String,
        history: List<ChatMessage>,
        memories: List<MemoryItem>,
        enableWebSearch: Boolean = false
    ): Flow<String>

    suspend fun testConnection(): Boolean
}
