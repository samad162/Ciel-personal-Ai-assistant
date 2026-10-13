package ai.ciel.assistant.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ai.ciel.assistant.data.local.CielDatabase
import ai.ciel.assistant.data.local.MemoryEntity
import ai.ciel.assistant.data.remote.AiProviderFactory
import ai.ciel.assistant.domain.model.*
import ai.ciel.assistant.tools.AssistantToolDispatcher
import ai.ciel.assistant.util.MemoryBudgetManager
import ai.ciel.assistant.voice.VoiceInteractionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CielUiState(
    val currentTab: String = "home", // home, chat, history, models, tools, customize, settings, memory
    val assistantState: AssistantState = AssistantState.IDLE,
    val currentTaskDescription: String = "Waiting for your command...",
    val inputText: String = "",
    val isWebSearchEnabled: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val memories: List<MemoryItem> = emptyList(),
    val telemetry: SystemTelemetry = SystemTelemetry(),
    val isContinuousVoice: Boolean = false,
    val isOfflineMode: Boolean = false,
    val selectedLanguage: String = "auto", // auto, en, ta, hi, te, kn, ml, ur, ar, ja
    val activeModelId: String = "gpt-4o",
    val accentTheme: String = "cyan", // cyan, violet, ice_blue, emerald
    val characterStyle: String = "cinematic", // cinematic, ambient, minimal
    val motionIntensity: String = "smooth", // smooth, reduced
    val commandBarAlpha: Float = 0.90f,
    val chatDensity: String = "comfortable", // comfortable, compact
    val voicePitch: Float = 1.08f,
    val speechRate: Float = 1.00f,
    val voiceProfile: String = "kaoruko_gentle", // kaoruko_gentle, ethereal_whisper, articulate_companion
    val audioRmsLevel: Float = 0f,
    val isStreamingSpeech: Boolean = true,
    val isWakeWordEnabled: Boolean = false,
    val wakeWordPhrase: String = "Hey CIEL",
    val userName: String = "Samad",
    val recentActivities: List<RecentActivityItem> = listOf(
        RecentActivityItem(title = "Image analysis completed", category = "Vision", timeAgo = "2m ago"),
        RecentActivityItem(title = "Research on AI models", category = "Deep Think", timeAgo = "12m ago"),
        RecentActivityItem(title = "Code generation completed", category = "Coding", timeAgo = "18m ago")
    ),
    val errorMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CielDatabase.getInstance(application)
    private val memoryDao = db.memoryDao()
    private val chatDao = db.chatDao()

    private val toolDispatcher = AssistantToolDispatcher(application)
    private val aiProviderFactory = AiProviderFactory()
    private val memoryBudgetManager = MemoryBudgetManager(application)
    val prefs = ai.ciel.assistant.util.PreferencesManager(application)

    private val _uiState = MutableStateFlow(
        CielUiState(
            activeModelId = prefs.activeModel,
            accentTheme = prefs.accentColor,
            characterStyle = prefs.characterStyle,
            motionIntensity = prefs.motionIntensity,
            commandBarAlpha = prefs.commandBarAlpha,
            chatDensity = prefs.chatDensity,
            isWebSearchEnabled = prefs.isWebSearchEnabled,
            isContinuousVoice = prefs.isContinuousVoice,
            selectedLanguage = prefs.selectedLanguage,
            voicePitch = prefs.voicePitch,
            speechRate = prefs.speechRate,
            voiceProfile = prefs.voiceProfile,
            isStreamingSpeech = prefs.isStreamingSpeech,
            isWakeWordEnabled = prefs.isWakeWordEnabled,
            wakeWordPhrase = prefs.wakeWordPhrase,
            userName = prefs.userName
        )
    )
    val uiState: StateFlow<CielUiState> = _uiState.asStateFlow()

    val availableModels: List<AiModelInfo> = listOf(
        AiModelInfo(
            id = "gpt-4o",
            name = "GPT-4o Omniscient",
            provider = "OpenAI",
            badge = "Flagship",
            description = "High-speed multimodal intelligence with deep situational awareness and fluid precision.",
            contextWindow = "128k tokens",
            latency = "~320 ms",
            benchmarkScore = "92.4%",
            isRecommended = true
        ),
        AiModelInfo(
            id = "claude-3-5",
            name = "Claude 3.5 Sonnet",
            provider = "Anthropic",
            badge = "Reasoning Pro",
            description = "Unrivaled logical rigor, coding elegance, and nuanced editorial understanding.",
            contextWindow = "200k tokens",
            latency = "~410 ms",
            benchmarkScore = "93.7%"
        ),
        AiModelInfo(
            id = "deepseek-r1",
            name = "DeepSeek R1 Core",
            provider = "DeepSeek AI",
            badge = "Math & Logic",
            description = "Open reasoning architecture specialized in chain-of-thought analysis and algorithms.",
            contextWindow = "64k tokens",
            latency = "~450 ms",
            benchmarkScore = "91.8%"
        ),
        AiModelInfo(
            id = "llama-3-3",
            name = "Llama 3.3 70B",
            provider = "Meta AI",
            badge = "Open Weights",
            description = "Ultra-fast open ecosystem powerhouse with highly refined conversational nuance.",
            contextWindow = "128k tokens",
            latency = "~280 ms",
            benchmarkScore = "88.5%"
        ),
        AiModelInfo(
            id = "qwen-2-5",
            name = "Qwen 2.5 72B",
            provider = "Alibaba Cloud",
            badge = "Polyglot",
            description = "Superior polyglot mastery across Asian and European languages, math, and code.",
            contextWindow = "128k tokens",
            latency = "~310 ms",
            benchmarkScore = "89.2%"
        ),
        AiModelInfo(
            id = "on-device",
            name = "CIEL Neural Core",
            provider = "On-Device Offline",
            badge = "Private & Instant",
            description = "Zero-latency edge fallback operating completely on hardware with 100% data privacy.",
            contextWindow = "32k tokens",
            latency = "~45 ms",
            benchmarkScore = "82.0%"
        )
    )

    lateinit var voiceManager: VoiceInteractionManager
        private set

    init {
        initVoiceManager()
        observeMemories()
        observePersistedChat()
        updateTelemetry()
    }


    private fun initVoiceManager() {
        voiceManager = VoiceInteractionManager(
            context = getApplication(),
            onSpeechRecognized = { spokenText ->
                submitUserPrompt(spokenText, triggeredByVoice = true)
            },
            onError = { error ->
                _uiState.update { it.copy(
                    assistantState = AssistantState.ERROR,
                    errorMessage = error,
                    currentTaskDescription = "Voice error: $error"
                ) }
            }
        ).apply {
            applyVoicePreferences(prefs.voicePitch, prefs.speechRate, prefs.voiceProfile)
            isContinuousConversation = prefs.isContinuousVoice
            currentLanguageCode = prefs.selectedLanguage
        }

        viewModelScope.launch {
            voiceManager.audioRmsLevel.collect { rms ->
                _uiState.update { it.copy(audioRmsLevel = rms) }
            }
        }

        viewModelScope.launch {
            voiceManager.isSpeaking.collect { speaking ->
                if (!speaking && _uiState.value.assistantState == AssistantState.SPEAKING) {
                    _uiState.update { it.copy(
                        assistantState = AssistantState.IDLE,
                        currentTaskDescription = "Waiting for your command..."
                    ) }
                }
            }
        }
    }

    private fun observeMemories() {
        viewModelScope.launch {
            memoryDao.getAllMemories().collect { entities ->
                val items = entities.map {
                    MemoryItem(it.id, it.key, it.content, it.category, it.importance, it.createdAt)
                }
                _uiState.update { it.copy(memories = items) }
            }
        }
    }

    private fun observePersistedChat() {
        viewModelScope.launch {
            chatDao.getRecentMessages(100).collect { entities ->
                if (entities.isNotEmpty() && _uiState.value.messages.isEmpty()) {
                    val loaded = entities.map {
                        ChatMessage(
                            id = it.id,
                            content = it.content,
                            sender = if (it.sender == "USER") MessageSender.USER else MessageSender.ASSISTANT,
                            timestamp = it.timestamp,
                            executedTool = it.executedTool,
                            isOfflineResponse = it.isOfflineResponse
                        )
                    }
                    _uiState.update { it.copy(messages = loaded) }
                }
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun toggleWebSearch() {
        val next = !_uiState.value.isWebSearchEnabled
        prefs.isWebSearchEnabled = next
        _uiState.update { it.copy(isWebSearchEnabled = next) }
    }

    fun setTab(tab: String) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectModel(modelId: String) {
        prefs.activeModel = modelId
        _uiState.update { it.copy(activeModelId = modelId) }
        addActivity("Switched AI model to $modelId", "Model")
    }

    fun setAccentTheme(theme: String) {
        prefs.accentColor = theme
        _uiState.update { it.copy(accentTheme = theme) }
    }

    fun setCharacterStyle(style: String) {
        prefs.characterStyle = style
        _uiState.update { it.copy(characterStyle = style) }
    }

    fun setMotionIntensity(intensity: String) {
        prefs.motionIntensity = intensity
        _uiState.update { it.copy(motionIntensity = intensity) }
    }

    fun setCommandBarAlpha(alpha: Float) {
        prefs.commandBarAlpha = alpha
        _uiState.update { it.copy(commandBarAlpha = alpha) }
    }

    fun setChatDensity(density: String) {
        prefs.chatDensity = density
        _uiState.update { it.copy(chatDensity = density) }
    }

    fun setUserName(name: String) {
        val trimmed = name.trim().ifEmpty { "User" }
        prefs.userName = trimmed
        _uiState.update { it.copy(userName = trimmed) }
        addActivity("Updated identity profile for $trimmed", "Profile")
    }

    fun addActivity(title: String, category: String) {
        val newActivity = RecentActivityItem(title = title, category = category, timeAgo = "Just now")
        _uiState.update {
            it.copy(recentActivities = listOf(newActivity) + it.recentActivities.take(9))
        }
    }

    fun executeAutonomousToolDirectly(command: String) {
        viewModelScope.launch {
            val result = toolDispatcher.executeToolAsync(command)
            val toolMessage = ChatMessage(
                content = result.message,
                sender = MessageSender.ASSISTANT,
                executedTool = command,
                generatedFileUri = result.generatedFile?.uri?.toString(),
                generatedFileName = result.generatedFile?.fileName,
                generatedFileType = result.generatedFile?.fileType,
                generatedFileSize = result.generatedFile?.fileSizeFormatted,
                generatedImageUrl = result.generatedImage?.file?.absolutePath ?: result.generatedImage?.imageUrl
            )
            _uiState.update {
                it.copy(
                    messages = it.messages + toolMessage,
                    currentTaskDescription = "Executed: $command"
                )
            }
            chatDao.insertMessage(
                ai.ciel.assistant.data.local.ChatMessageEntity(
                    id = toolMessage.id,
                    content = toolMessage.content,
                    sender = "ASSISTANT",
                    timestamp = toolMessage.timestamp,
                    executedTool = command,
                    isOfflineResponse = false
                )
            )
            addActivity("Executed tool: $command", "Tool")
        }
    }

    private fun pauseWakeMicrophone() {
        try {
            val intent = android.content.Intent(getApplication(), ai.ciel.assistant.service.WakeWordDetectionService::class.java).apply {
                action = ai.ciel.assistant.service.WakeWordDetectionService.ACTION_PAUSE_MIC
            }
            getApplication<Application>().startService(intent)
        } catch (ignored: Exception) {}
    }

    private fun resumeWakeMicrophone() {
        try {
            if (_uiState.value.isWakeWordEnabled) {
                val intent = android.content.Intent(getApplication(), ai.ciel.assistant.service.WakeWordDetectionService::class.java).apply {
                    action = ai.ciel.assistant.service.WakeWordDetectionService.ACTION_RESUME_MIC
                }
                getApplication<Application>().startService(intent)
            }
        } catch (ignored: Exception) {}
    }

    fun openConversationInChat(message: ChatMessage) {
        _uiState.update { it.copy(currentTab = "chat") }
    }

    fun setLanguage(langCode: String) {
        voiceManager.currentLanguageCode = langCode
        prefs.selectedLanguage = langCode
        _uiState.update { it.copy(selectedLanguage = langCode) }
    }

    fun toggleContinuousVoice() {
        val current = _uiState.value.isContinuousVoice
        voiceManager.isContinuousConversation = !current
        prefs.isContinuousVoice = !current
        _uiState.update { it.copy(isContinuousVoice = !current) }
    }

    fun triggerPushToTalk() {
        if (_uiState.value.assistantState == AssistantState.SPEAKING) {
            voiceManager.stopSpeaking()
            _uiState.update { it.copy(assistantState = AssistantState.IDLE, currentTaskDescription = "Waiting for your command...") }
            return
        }

        if (_uiState.value.assistantState == AssistantState.LISTENING) {
            voiceManager.stopListening()
            _uiState.update { it.copy(assistantState = AssistantState.IDLE, currentTaskDescription = "Waiting for your command...") }
        } else {
            _uiState.update { it.copy(assistantState = AssistantState.LISTENING, currentTaskDescription = "Listening to your voice...") }
            voiceManager.startListening()
        }
    }

    fun submitUserPrompt(promptText: String, triggeredByVoice: Boolean = false) {
        val prompt = promptText.trim()
        if (prompt.isEmpty()) return

        _uiState.update { it.copy(inputText = "") }

        val userMessage = ChatMessage(
            content = prompt,
            sender = MessageSender.USER
        )

        val updatedMessages = _uiState.value.messages + userMessage
        _uiState.update {
            it.copy(
                messages = updatedMessages,
                assistantState = AssistantState.PROCESSING,
                currentTaskDescription = "Reasoning & generating answer..."
            )
        }

        viewModelScope.launch {
            chatDao.insertMessage(
                ai.ciel.assistant.data.local.ChatMessageEntity(
                    id = userMessage.id,
                    content = userMessage.content,
                    sender = "USER",
                    timestamp = userMessage.timestamp,
                    executedTool = null,
                    isOfflineResponse = false
                )
            )
        }
        addActivity("Prompt: ${prompt.take(30)}...", "Chat")

        // Check for autonomous memory command (e.g., "remember that meeting is at 3pm")
        if (prompt.lowercase().startsWith("remember that ") || prompt.lowercase().startsWith("remember ")) {
            val toRemember = prompt.replace(Regex("(?i)^remember( that)? "), "").trim()
            addMemory(key = "Quick Note", content = toRemember)
            val msg = "I've safely saved that in our memory vault for you: '$toRemember'. You can always count on me to remember!"
            val assistantMessage = ChatMessage(content = msg, sender = MessageSender.ASSISTANT)
            _uiState.update {
                it.copy(
                    messages = it.messages + assistantMessage,
                    assistantState = AssistantState.IDLE,
                    currentTaskDescription = "Memory saved"
                )
            }
            viewModelScope.launch {
                chatDao.insertMessage(
                    ai.ciel.assistant.data.local.ChatMessageEntity(
                        id = assistantMessage.id,
                        content = assistantMessage.content,
                        sender = "ASSISTANT",
                        timestamp = assistantMessage.timestamp,
                        executedTool = null,
                        isOfflineResponse = false
                    )
                )
            }
            if (triggeredByVoice) {
                voiceManager.speak(msg)
            }
            return
        }

        // Check for autonomous hardware & app action
        if (toolDispatcher.isAutonomousToolCommand(prompt)) {
            pauseWakeMicrophone()
            viewModelScope.launch {
                val result = toolDispatcher.executeToolAsync(prompt)
                val toolMessage = ChatMessage(
                    content = result.message,
                    sender = MessageSender.ASSISTANT,
                    executedTool = prompt,
                    generatedFileUri = result.generatedFile?.uri?.toString(),
                    generatedFileName = result.generatedFile?.fileName,
                    generatedFileType = result.generatedFile?.fileType,
                    generatedFileSize = result.generatedFile?.fileSizeFormatted,
                    generatedImageUrl = result.generatedImage?.file?.absolutePath ?: result.generatedImage?.imageUrl
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + toolMessage,
                        assistantState = AssistantState.IDLE,
                        currentTaskDescription = "Autonomous action: ${result.message}"
                    )
                }
                chatDao.insertMessage(
                    ai.ciel.assistant.data.local.ChatMessageEntity(
                        id = toolMessage.id,
                        content = toolMessage.content,
                        sender = "ASSISTANT",
                        timestamp = toolMessage.timestamp,
                        executedTool = prompt,
                        isOfflineResponse = false
                    )
                )
                if (triggeredByVoice) {
                    voiceManager.speak(result.message) {
                        resumeWakeMicrophone()
                    }
                } else {
                    resumeWakeMicrophone()
                }
            }
            return
        }

        // Stream LLM response
        val assistantMessageId = java.util.UUID.randomUUID().toString()
        val initialAssistantMessage = ChatMessage(
            id = assistantMessageId,
            content = "",
            sender = MessageSender.ASSISTANT,
            isStreaming = true,
            isOfflineResponse = _uiState.value.isOfflineMode
        )

        _uiState.update { it.copy(messages = it.messages + initialAssistantMessage) }

        viewModelScope.launch {
            val stringBuilder = StringBuilder()
            val sentenceBuilder = StringBuilder()
            val isVoiceActive = triggeredByVoice || _uiState.value.isContinuousVoice
            val streamSpeech = prefs.isStreamingSpeech && isVoiceActive

            if (isVoiceActive) {
                _uiState.update { it.copy(
                    assistantState = AssistantState.SPEAKING,
                    currentTaskDescription = "Speaking response..."
                ) }
            }

            aiProviderFactory.streamAssistantResponse(
                prompt = prompt,
                history = updatedMessages,
                memories = _uiState.value.memories,
                forceOffline = _uiState.value.isOfflineMode,
                enableWebSearch = _uiState.value.isWebSearchEnabled
            ).collect { token ->
                stringBuilder.append(token)
                val currentText = stringBuilder.toString()
                _uiState.update { state ->
                    val msgs = state.messages.map { msg ->
                        if (msg.id == assistantMessageId) msg.copy(content = currentText) else msg
                    }
                    state.copy(messages = msgs)
                }

                if (streamSpeech) {
                    sentenceBuilder.append(token)
                    val buffer = sentenceBuilder.toString()
                    val match = Regex("""^(.*?[.!?])(?:\s+|\n+)""").find(buffer)
                    if (match != null) {
                        val sentence = match.groups[1]?.value?.trim() ?: ""
                        val remainder = buffer.substring(match.range.last + 1)
                        if (sentence.isNotBlank()) {
                            voiceManager.enqueueStreamingSentence(sentence, isFinal = false)
                        }
                        sentenceBuilder.clear()
                        sentenceBuilder.append(remainder)
                    }
                }
            }

            val finalOutput = stringBuilder.toString()
            _uiState.update { state ->
                val msgs = state.messages.map { msg ->
                    if (msg.id == assistantMessageId) msg.copy(isStreaming = false) else msg
                }
                state.copy(
                    messages = msgs,
                    assistantState = if (isVoiceActive) AssistantState.SPEAKING else AssistantState.IDLE,
                    currentTaskDescription = if (isVoiceActive) "Speaking response..." else "Task completed"
                )
            }

            chatDao.insertMessage(
                ai.ciel.assistant.data.local.ChatMessageEntity(
                    id = assistantMessageId,
                    content = finalOutput,
                    sender = "ASSISTANT",
                    timestamp = System.currentTimeMillis(),
                    executedTool = null,
                    isOfflineResponse = _uiState.value.isOfflineMode
                )
            )

            if (isVoiceActive) {
                if (streamSpeech) {
                    val remaining = sentenceBuilder.toString().trim()
                    if (remaining.isNotBlank()) {
                        voiceManager.enqueueStreamingSentence(remaining, isFinal = true)
                    }
                } else {
                    voiceManager.speak(finalOutput) {
                        _uiState.update { it.copy(assistantState = AssistantState.IDLE, currentTaskDescription = "Waiting for your command...") }
                        resumeWakeMicrophone()
                    }
                }
            } else {
                resumeWakeMicrophone()
            }
        }
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
        _uiState.update { it.copy(assistantState = AssistantState.IDLE, currentTaskDescription = "Speech stopped") }
        resumeWakeMicrophone()
    }

    fun setVoicePitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.80f, 1.35f)
        prefs.voicePitch = clamped
        _uiState.update { it.copy(voicePitch = clamped) }
        voiceManager.applyVoicePreferences(clamped, _uiState.value.speechRate, _uiState.value.voiceProfile)
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.80f, 1.35f)
        prefs.speechRate = clamped
        _uiState.update { it.copy(speechRate = clamped) }
        voiceManager.applyVoicePreferences(_uiState.value.voicePitch, clamped, _uiState.value.voiceProfile)
    }

    fun setVoiceProfile(profile: String) {
        prefs.voiceProfile = profile
        _uiState.update { it.copy(voiceProfile = profile) }
        voiceManager.applyVoicePreferences(_uiState.value.voicePitch, _uiState.value.speechRate, profile)
    }

    fun playVoicePreview() {
        voiceManager.playPreview(
            pitch = _uiState.value.voicePitch,
            rate = _uiState.value.speechRate,
            profile = _uiState.value.voiceProfile,
            userName = _uiState.value.userName
        )
    }

    fun toggleStreamingSpeech() {
        val current = _uiState.value.isStreamingSpeech
        prefs.isStreamingSpeech = !current
        _uiState.update { it.copy(isStreamingSpeech = !current) }
    }

    fun setWakeWordPhrase(phrase: String) {
        prefs.wakeWordPhrase = phrase
        _uiState.update { it.copy(wakeWordPhrase = phrase) }
    }

    fun toggleWakeWord(enabled: Boolean) {
        prefs.isWakeWordEnabled = enabled
        _uiState.update { it.copy(isWakeWordEnabled = enabled) }
        val intent = android.content.Intent(getApplication(), ai.ciel.assistant.service.WakeWordDetectionService::class.java).apply {
            action = if (enabled) ai.ciel.assistant.service.WakeWordDetectionService.ACTION_START else ai.ciel.assistant.service.WakeWordDetectionService.ACTION_STOP
        }
        try {
            if (enabled) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    getApplication<Application>().startForegroundService(intent)
                } else {
                    getApplication<Application>().startService(intent)
                }
            } else {
                getApplication<Application>().stopService(intent)
            }
        } catch (e: Exception) {
            android.util.Log.w("MainViewModel", "Wake word toggle service error: ${e.message}")
        }
    }

    fun addMemory(key: String, content: String) {
        viewModelScope.launch {
            memoryDao.insertMemory(
                MemoryEntity(key = key, content = content, category = "preference", importance = 1, createdAt = System.currentTimeMillis())
            )
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            memoryDao.deleteById(id)
        }
    }

    fun clearChat() {
        _uiState.update { it.copy(messages = emptyList()) }
        viewModelScope.launch { chatDao.clearHistory() }
    }

    private fun updateTelemetry() {
        val stats = memoryBudgetManager.checkCurrentMemory()
        _uiState.update {
            it.copy(
                telemetry = it.telemetry.copy(
                    ramUsageMb = stats.usedMb,
                    maxHeapMb = stats.maxMb,
                    isLowMemory = stats.isCritical
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}

