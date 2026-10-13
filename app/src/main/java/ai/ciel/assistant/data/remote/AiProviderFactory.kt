package ai.ciel.assistant.data.remote

import ai.ciel.assistant.domain.model.ChatMessage
import ai.ciel.assistant.domain.model.MemoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * Resilient AI Provider Orchestrator:
 * Tries hosted primary model -> falls back to secondary provider -> falls back to local on-device engine.
 */
class AiProviderFactory(
    private var primaryProvider: AiProvider = UniversalNeuralAiProvider(),
    private val localFallbackProvider: LocalFallbackProvider = LocalFallbackProvider()
) {

    var isOnline: Boolean = true

    fun setPrimaryApiKey(key: String, endpoint: String = "https://api.openai.com/v1/chat/completions", model: String = "gpt-4o-mini") {
        if (key.isNotBlank()) {
            primaryProvider = OpenAiCompatibleProvider(endpoint, key, model)
        }
    }

    fun streamAssistantResponse(
        prompt: String,
        history: List<ChatMessage>,
        memories: List<MemoryItem>,
        forceOffline: Boolean = false,
        enableWebSearch: Boolean = false
    ): Flow<String> {
        if (forceOffline || !isOnline || !primaryProvider.isAvailable) {
            return localFallbackProvider.streamCompletion(prompt, history, memories, enableWebSearch)
        }

        return primaryProvider.streamCompletion(prompt, history, memories, enableWebSearch)
            .catch { throwable ->
                // Automatic graceful fallback to offline model
                emit("\n[Notice: Cloud connection interrupted. Switching to local fallback]\n\n")
                localFallbackProvider.streamCompletion(prompt, history, memories, enableWebSearch).collect {
                    emit(it)
                }
            }
    }
}
