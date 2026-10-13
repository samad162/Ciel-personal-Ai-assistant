package ai.ciel.assistant.domain.model

enum class AssistantState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR,
    OFFLINE
}

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val executedTool: String? = null,
    val isOfflineResponse: Boolean = false,
    val generatedFileUri: String? = null,
    val generatedFileName: String? = null,
    val generatedFileType: String? = null,
    val generatedFileSize: String? = null,
    val generatedImageUrl: String? = null
)

data class MemoryItem(
    val id: Long = 0,
    val key: String,
    val content: String,
    val category: String = "general",
    val importance: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

data class SystemTelemetry(
    val coreAiStatus: String = "Online",
    val memoryStatus: String = "Active",
    val toolsStatus: String = "Ready",
    val internetStatus: String = "Connected",
    val ramUsageMb: Long = 64,
    val maxHeapMb: Long = 256,
    val isLowMemory: Boolean = false,
    val batteryPercent: Int = 85
)

data class RecentActivityItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis(),
    val timeAgo: String = "Just now"
)

data class AiModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val badge: String,
    val description: String,
    val contextWindow: String,
    val latency: String,
    val benchmarkScore: String,
    val isRecommended: Boolean = false
)

sealed class AssistantAction {
    data class LaunchApp(val packageName: String, val appName: String) : AssistantAction()
    data class WebSearch(val query: String) : AssistantAction()
    data class SaveMemory(val key: String, val fact: String) : AssistantAction()
    data class QueryDevice(val metric: String) : AssistantAction()
    data class DeepResearch(val topic: String) : AssistantAction()
    data class CodeExecution(val snippet: String) : AssistantAction()
}

