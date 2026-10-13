package ai.ciel.assistant

import ai.ciel.assistant.data.remote.LocalFallbackProvider
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class LocalFallbackProviderTest {

    @Test
    fun testFallbackStreamingOfflineResponsiveness() = runBlocking {
        val provider = LocalFallbackProvider()
        assertTrue(provider.isAvailable)

        val tokens = provider.streamCompletion(
            userPrompt = "who are you",
            history = emptyList(),
            memories = emptyList()
        ).toList()

        val fullResponse = tokens.joinToString("")
        assertTrue(fullResponse.contains("CIEL"))
        assertTrue(tokens.isNotEmpty())
    }

    @Test
    fun testStatusPromptRecognition() = runBlocking {
        val provider = LocalFallbackProvider()
        val tokens = provider.streamCompletion(
            userPrompt = "system status",
            history = emptyList(),
            memories = emptyList()
        ).toList()

        val fullResponse = tokens.joinToString("")
        assertTrue(fullResponse.contains("telemetry") || fullResponse.contains("Core AI online"))
    }
}
