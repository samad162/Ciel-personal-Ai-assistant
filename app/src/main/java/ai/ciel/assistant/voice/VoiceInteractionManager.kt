package ai.ciel.assistant.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceInteractionManager(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit,
    private val onError: (String) -> Unit
) {

    private val tag = "VoiceInteraction"

    private var speechRecognizer: SpeechRecognizer? = null
    val voiceEngine: CielVoiceEngine

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioRmsLevel = MutableStateFlow(0f)
    val audioRmsLevel: StateFlow<Float> = _audioRmsLevel.asStateFlow()

    var isContinuousConversation: Boolean = false
    var currentLanguageCode: String = "auto"

    val isSpeaking: StateFlow<Boolean>
        get() = voiceEngine.isSpeaking

    init {
        voiceEngine = CielVoiceEngine(
            context = context,
            onSpeakingStateChanged = { /* handled via StateFlow */ },
            onPlaybackCompleted = {
                if (isContinuousConversation) {
                    startListening()
                }
            }
        )
        initSpeechRecognizer()
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        _audioRmsLevel.value = (rmsdB.coerceIn(-2f, 10f) + 2f) / 12f
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _audioRmsLevel.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _audioRmsLevel.value = 0f
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                            SpeechRecognizer.ERROR_NETWORK -> "Network issue"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                            else -> "Recognition code $error"
                        }
                        onError(errorMsg)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioRmsLevel.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onSpeechRecognized(matches[0])
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        // Immediate interruption: stop assistant speech before listening
        stopSpeaking()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            val reqLocale = when (currentLanguageCode) {
                "ta" -> Locale("ta", "IN")
                "hi" -> Locale("hi", "IN")
                "te" -> Locale("te", "IN")
                "kn" -> Locale("kn", "IN")
                "ml" -> Locale("ml", "IN")
                "ur" -> Locale("ur", "PK")
                "ar" -> Locale("ar", "SA")
                "ja" -> Locale.JAPANESE
                "en" -> Locale.US
                else -> Locale.getDefault()
            }
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, reqLocale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, reqLocale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e(tag, "Failed to start speech recognizer: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _isListening.value = false
            _audioRmsLevel.value = 0f
        } catch (e: Exception) {
            Log.e(tag, "Error stopping recognizer: ${e.message}")
        }
    }

    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        voiceEngine.speakFullText(text, onFinished)
    }

    fun enqueueStreamingSentence(sentence: String, isFinal: Boolean = false) {
        voiceEngine.enqueueStreamingSentence(sentence, isFinal)
    }

    fun stopSpeaking() {
        voiceEngine.stopSpeaking()
    }

    fun playPreview(pitch: Float, rate: Float, profile: String, userName: String = "Samad") {
        voiceEngine.playVoicePreview(pitch, rate, profile, userName)
    }

    fun applyVoicePreferences(pitch: Float, rate: Float, profile: String) {
        voiceEngine.userPitch = pitch
        voiceEngine.userSpeechRate = rate
        voiceEngine.voiceProfile = profile
    }

    fun destroy() {
        speechRecognizer?.destroy()
        voiceEngine.destroy()
    }
}
