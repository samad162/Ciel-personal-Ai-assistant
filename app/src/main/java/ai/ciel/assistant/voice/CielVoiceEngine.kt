package ai.ciel.assistant.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * CIEL Neural Voice & Speech Engine:
 * - High-fidelity feminine voice synthesis inspired by the warm, gentle emotional atmosphere of Waguri Kaoruko.
 * - Sentence-level pipelined streaming playback for ultra-low latency conversational responses.
 * - Neural voice selection prioritizing natural Google TTS models.
 * - Immediate interruption, audio focus ducking, and real-time audio state tracking.
 */
class CielVoiceEngine(
    private val context: Context,
    private val onSpeakingStateChanged: (Boolean) -> Unit,
    private val onPlaybackCompleted: () -> Unit
) : TextToSpeech.OnInitListener {

    private val tag = "CielVoiceEngine"

    private var textToSpeech: TextToSpeech? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var isTtsReady = false
    private val sentenceQueue = ConcurrentLinkedQueue<String>()
    private var activeUtteranceCount = 0
    private var isAwaitingMoreStreamChunks = false

    // Configurable voice parameters (default: gentle anime heroine warmth)
    var userPitch: Float = 1.08f
    var userSpeechRate: Float = 1.00f
    var voiceProfile: String = "kaoruko_gentle"

    init {
        initializeTts()
    }

    private fun initializeTts() {
        try {
            // Attempt explicitly with Google TTS engine for neural voice models
            textToSpeech = TextToSpeech(context, this, "com.google.android.tts")
        } catch (e: Exception) {
            Log.w(tag, "Google TTS unavailable, falling back to default engine: ${e.message}")
            textToSpeech = TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            setupAudioAttributes()
            configureFeminineVoice(Locale.US)
            attachUtteranceListener()
            Log.i(tag, "CIEL Voice Engine initialized successfully.")
        } else {
            Log.e(tag, "TextToSpeech init failed with status: $status")
        }
    }

    private fun setupAudioAttributes() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        textToSpeech?.setAudioAttributes(audioAttributes)
    }

    /**
     * Attaches progress listener to track sentence-by-sentence playback
     * and notify UI when speaking finishes or is interrupted.
     */
    private fun attachUtteranceListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                onSpeakingStateChanged(true)
            }

            override fun onDone(utteranceId: String?) {
                activeUtteranceCount = (activeUtteranceCount - 1).coerceAtLeast(0)
                if (activeUtteranceCount == 0 && !isAwaitingMoreStreamChunks) {
                    _isSpeaking.value = false
                    onSpeakingStateChanged(false)
                    abandonAudioFocus()
                    onPlaybackCompleted()
                }
            }

            override fun onError(utteranceId: String?) {
                activeUtteranceCount = (activeUtteranceCount - 1).coerceAtLeast(0)
                if (activeUtteranceCount == 0) {
                    _isSpeaking.value = false
                    onSpeakingStateChanged(false)
                    abandonAudioFocus()
                }
            }
        })
    }

    /**
     * Intelligently selects the highest-quality, natural feminine voice available.
     * Prioritizes Google Neural female models (e.g. en-us-x-sfg, ja-jp-x-htm).
     */
    fun configureFeminineVoice(locale: Locale) {
        val tts = textToSpeech ?: return
        try {
            val available = tts.isLanguageAvailable(locale)
            if (available >= TextToSpeech.LANG_AVAILABLE) {
                tts.language = locale
            }

            val voices = tts.voices
            if (!voices.isNullOrEmpty()) {
                val candidate = findBestFemaleVoice(voices, locale)
                if (candidate != null) {
                    tts.voice = candidate
                    Log.d(tag, "Selected neural voice: ${candidate.name} for locale: $locale")
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Voice configuration exception: ${e.message}")
        }

        // Apply pitch and rate based on selected voice profile
        val effectivePitch = when (voiceProfile) {
            "ethereal_whisper" -> userPitch * 1.05f
            "articulate_companion" -> userPitch * 0.98f
            else -> userPitch // "kaoruko_gentle": 1.08f
        }.coerceIn(0.80f, 1.35f)

        val effectiveRate = when (voiceProfile) {
            "ethereal_whisper" -> userSpeechRate * 0.96f
            "articulate_companion" -> userSpeechRate * 1.02f
            else -> userSpeechRate // 1.00f
        }.coerceIn(0.80f, 1.35f)

        tts.setPitch(effectivePitch)
        tts.setSpeechRate(effectiveRate)
    }

    private fun findBestFemaleVoice(voices: Set<Voice>, locale: Locale): Voice? {
        val lang = locale.language

        // Rank 1: High-quality Google Neural female voices
        val neuralFemale = voices.firstOrNull { v ->
            v.locale.language.equals(lang, ignoreCase = true) &&
            (v.name.contains("female", ignoreCase = true) ||
             v.name.contains("#female") ||
             v.name.contains("sfg") ||
             v.name.contains("tpd") ||
             v.name.contains("iol") ||
             v.name.contains("htm")) &&
            !v.name.contains("male", ignoreCase = true) &&
            v.quality >= Voice.QUALITY_NORMAL
        }
        if (neuralFemale != null) return neuralFemale

        // Rank 2: Any matching language female voice
        val anyFemale = voices.firstOrNull { v ->
            v.locale.language.equals(lang, ignoreCase = true) &&
            (v.name.contains("female", ignoreCase = true) || v.name.contains("#female"))
        }
        if (anyFemale != null) return anyFemale

        // Rank 3: Default matching language voice
        return voices.firstOrNull { it.locale.language.equals(lang, ignoreCase = true) }
    }

    /**
     * Speaks a complete text response immediately (clears previous queue).
     */
    fun speakFullText(text: String, onFinished: (() -> Unit)? = null) {
        if (!isTtsReady || text.isBlank()) return
        stopSpeaking()

        val clean = sanitizeForSpeech(text)
        if (clean.isBlank()) return

        requestAudioFocus()
        val locale = detectLocale(clean)
        configureFeminineVoice(locale)

        val utteranceId = "ciel_utterance_${UUID.randomUUID()}"
        activeUtteranceCount = 1
        isAwaitingMoreStreamChunks = false

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }

        textToSpeech?.speak(clean, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    /**
     * Streaming sentence-by-sentence playback:
     * Enqueues incoming sentences without waiting for full response generation,
     * delivering conversational responses with near-zero initial latency.
     */
    fun enqueueStreamingSentence(sentence: String, isFinalChunk: Boolean = false) {
        if (!isTtsReady || sentence.isBlank()) return

        val clean = sanitizeForSpeech(sentence)
        if (clean.isBlank()) return

        if (!_isSpeaking.value) {
            requestAudioFocus()
            val locale = detectLocale(clean)
            configureFeminineVoice(locale)
        }

        val utteranceId = "ciel_stream_${UUID.randomUUID()}"
        activeUtteranceCount++
        isAwaitingMoreStreamChunks = !isFinalChunk

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }

        val queueMode = if (activeUtteranceCount == 1) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        textToSpeech?.speak(clean, queueMode, params, utteranceId)
    }

    /**
     * Plays a preview of CIEL's gentle heroine voice for user testing in settings.
     */
    fun playVoicePreview(pitch: Float, rate: Float, profile: String, userName: String = "Samad") {
        userPitch = pitch
        userSpeechRate = rate
        voiceProfile = profile

        val name = userName.trim().ifEmpty { "there" }
        val previewSample = when (profile) {
            "ethereal_whisper" -> "Hello $name. I am always right here beside you, listening in the quiet."
            "articulate_companion" -> "Good evening, $name. All systems are operational. What shall we achieve today?"
            else -> "Hello $name! It's so lovely to speak with you today. What's on your mind?"
        }

        speakFullText(previewSample)
    }

    /**
     * Immediately interrupts speech synthesis and releases audio focus.
     */
    fun stopSpeaking() {
        if (_isSpeaking.value || activeUtteranceCount > 0) {
            textToSpeech?.stop()
            activeUtteranceCount = 0
            isAwaitingMoreStreamChunks = false
            sentenceQueue.clear()
            _isSpeaking.value = false
            onSpeakingStateChanged(false)
            abandonAudioFocus()
        }
    }

    /**
     * Strips Markdown syntax, raw code blocks, URLs, and table formats
     * so that CIEL speaks naturally and conversationally like a human.
     */
    fun sanitizeForSpeech(rawText: String): String {
        return rawText
            // Replace code blocks with a natural spoken cue
            .replace(Regex("""```[\w]*\n[\s\S]*?```"""), " Here is the code shown on your screen. ")
            // Remove inline code backticks
            .replace(Regex("""`([^`]+)`"""), "$1")
            // Remove markdown images and links
            .replace(Regex("""!\[.*?\]\(.*?\)"""), "")
            .replace(Regex("""\[(.*?)\]\(.*?\)"""), "$1")
            // Remove headers (#, ##, ###)
            .replace(Regex("""^\s*#{1,6}\s*""", RegexOption.MULTILINE), "")
            // Remove blockquotes and horizontal dividers
            .replace(Regex("""^\s*>\s*""", RegexOption.MULTILINE), "")
            .replace(Regex("""^\s*[-*_]{3,}\s*$""", RegexOption.MULTILINE), "")
            // Remove bold/italic asterisks and underscores
            .replace(Regex("""[*_~]"""), "")
            // Strip URLs
            .replace(Regex("""https?://\S+"""), " link ")
            // Strip emojis
            .replace(Regex("""[\uD83C-\uDBFF\uDC00-\uDFFF]+"""), "")
            // Clean excess whitespace
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    /**
     * Automatically detects script to route to appropriate language model.
     */
    fun detectLocale(text: String): Locale {
        for (char in text) {
            when (char.code) {
                in 0x0B80..0x0BFF -> return Locale("ta", "IN") // Tamil
                in 0x0900..0x097F -> return Locale("hi", "IN") // Hindi
                in 0x0C00..0x0C7F -> return Locale("te", "IN") // Telugu
                in 0x0C80..0x0CFF -> return Locale("kn", "IN") // Kannada
                in 0x0D00..0x0D7F -> return Locale("ml", "IN") // Malayalam
                in 0x0600..0x06FF -> return Locale("ar", "SA") // Arabic/Urdu
                in 0x3040..0x30FF, in 0x4E00..0x9FFF -> return Locale.JAPANESE
            }
        }
        return Locale.US
    }

    private fun requestAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
            audioFocusRequest = req
            am.requestAudioFocus(req)
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(null)
        }
    }

    fun destroy() {
        stopSpeaking()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
