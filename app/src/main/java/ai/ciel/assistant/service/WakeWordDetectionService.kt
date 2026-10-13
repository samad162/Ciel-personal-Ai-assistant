package ai.ciel.assistant.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import ai.ciel.assistant.CielApplication
import ai.ciel.assistant.MainActivity
import ai.ciel.assistant.R
import ai.ciel.assistant.util.PreferencesManager
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Optimized Voice Activation & Wake-Word Service:
 * - Low-power acoustic voice activity detection (VAD) loop.
 * - Releases microphone immediately during AI processing and speech output to prevent
 *   the permanent green microphone privacy indicator.
 * - Minimal, non-intrusive notification without clunky "Push to Talk" buttons.
 * - Automatic resource cleanup on destroy and error handling.
 */
class WakeWordDetectionService : Service() {

    companion object {
        const val ACTION_START = "ai.ciel.action.START_LISTENING"
        const val ACTION_STOP = "ai.ciel.action.STOP_LISTENING"
        const val ACTION_PAUSE_MIC = "ai.ciel.action.PAUSE_MIC"
        const val ACTION_RESUME_MIC = "ai.ciel.action.RESUME_MIC"
        private const val NOTIFICATION_ID = 2026
        private const val TAG = "CielWakeService"

        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isServiceRunning = false
    private val isMicPaused = AtomicBoolean(false)
    private var audioRecordThread: Thread? = null
    private val isRecordingAudio = AtomicBoolean(false)
    private var isRecognizing = false

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ai.ciel.assistant:WakeServiceLock"
        ).apply {
            setReferenceCounted(false)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopWakeWordListener()
            ACTION_PAUSE_MIC -> pauseMicrophone()
            ACTION_RESUME_MIC -> resumeMicrophone()
            ACTION_START -> startWakeWordListener()
            else -> startWakeWordListener()
        }
        return START_STICKY
    }

    private fun startWakeWordListener() {
        if (isServiceRunning) return
        isServiceRunning = true
        isMicPaused.set(false)

        try {
            // Acquire wake lock with a 30-minute safety limit, renewing as necessary
            wakeLock?.acquire(30 * 60 * 1000L)
        } catch (e: Exception) {
            Log.w(TAG, "WakeLock error: ${e.message}")
        }

        val notification = buildForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)

        startAcousticMonitor()
    }

    private fun stopWakeWordListener() {
        isServiceRunning = false
        isMicPaused.set(true)
        stopAcousticMonitor()
        destroyRecognizer()

        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "WakeLock release error: ${e.message}")
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun pauseMicrophone() {
        Log.d(TAG, "Pausing wake-word microphone (AI processing/speaking)...")
        isMicPaused.set(true)
        stopAcousticMonitor()
        destroyRecognizer()
    }

    private fun resumeMicrophone() {
        if (!isServiceRunning) return
        Log.d(TAG, "Resuming wake-word acoustic monitor...")
        isMicPaused.set(false)
        mainHandler.postDelayed({
            if (isServiceRunning && !isMicPaused.get()) {
                startAcousticMonitor()
            }
        }, 600)
    }

    /**
     * Lightweight acoustic monitor using AudioRecord to detect voice activity
     * before activating full SpeechRecognizer. This drastically saves battery
     * and minimizes unnecessary heavy speech recognizer IPC sessions.
     */
    private fun startAcousticMonitor() {
        if (isRecordingAudio.get() || isMicPaused.get()) return

        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (minBufferSize <= 0) {
            // Fallback to standard recognizer loop if AudioRecord is unavailable
            startSpeechRecognizerSession()
            return
        }

        isRecordingAudio.set(true)
        audioRecordThread = Thread({
            var audioRecord: AudioRecord? = null
            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    minBufferSize * 2
                )

                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    Log.w(TAG, "AudioRecord failed to initialize, falling back to Recognizer")
                    isRecordingAudio.set(false)
                    mainHandler.post { startSpeechRecognizerSession() }
                    return@Thread
                }

                audioRecord.startRecording()
                val buffer = ShortArray(minBufferSize)
                var speechFrames = 0

                while (isRecordingAudio.get() && !isMicPaused.get()) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        // Calculate RMS energy
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                        }
                        val rms = Math.sqrt(sum / read)
                        val db = if (rms > 0) 20 * Math.log10(rms) else 0.0

                        // Threshold for human speech voice activity (~48 dB)
                        if (db > 48.0) {
                            speechFrames++
                            if (speechFrames >= 3) { // Consistent human speech detected
                                Log.d(TAG, "Voice activity detected (RMS: ${db.toInt()} dB), triggering keyword recognition...")
                                isRecordingAudio.set(false)
                                break
                            }
                        } else {
                            if (speechFrames > 0) speechFrames--
                        }
                    }
                    Thread.sleep(60)
                }

                try {
                    audioRecord.stop()
                    audioRecord.release()
                } catch (e: Exception) {
                    // Ignore release errors
                }

                // If voice activity was detected and not paused, trigger recognizer check
                if (!isMicPaused.get() && isServiceRunning) {
                    mainHandler.post { startSpeechRecognizerSession() }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Acoustic monitor error: ${e.message}")
                isRecordingAudio.set(false)
                try {
                    audioRecord?.release()
                } catch (ignored: Exception) {}
            }
        }, "CielAcousticMonitor").apply {
            priority = Thread.NORM_PRIORITY
            start()
        }
    }

    private fun stopAcousticMonitor() {
        isRecordingAudio.set(false)
        audioRecordThread?.interrupt()
        audioRecordThread = null
    }

    private fun startSpeechRecognizerSession() {
        if (!isServiceRunning || isMicPaused.get() || isRecognizing) return
        isRecognizing = true

        mainHandler.post {
            try {
                destroyRecognizer()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {}
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {}

                        override fun onError(error: Int) {
                            isRecognizing = false
                            destroyRecognizer()
                            // Return to low-power acoustic monitor after brief cooldown
                            if (isServiceRunning && !isMicPaused.get()) {
                                mainHandler.postDelayed({ startAcousticMonitor() }, 1000)
                            }
                        }

                        override fun onResults(results: Bundle?) {
                            isRecognizing = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val detectedText = matches[0].lowercase().trim()
                                val targetPhrase = PreferencesManager(this@WakeWordDetectionService).wakeWordPhrase.lowercase().trim()
                                Log.d(TAG, "Wake check: '$detectedText' against '$targetPhrase'")

                                if (detectedText.contains("ciel") ||
                                    (targetPhrase.isNotBlank() && detectedText.contains(targetPhrase)) ||
                                    detectedText.contains("hey ciel") ||
                                    detectedText.contains("ok ciel") ||
                                    detectedText.contains("hi ciel") ||
                                    detectedText.contains("hello ciel")) {
                                    onWakeWordDetected(detectedText)
                                    return
                                }
                            }
                            destroyRecognizer()
                            if (isServiceRunning && !isMicPaused.get()) {
                                mainHandler.postDelayed({ startAcousticMonitor() }, 800)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val detected = matches[0].lowercase().trim()
                                val targetPhrase = PreferencesManager(this@WakeWordDetectionService).wakeWordPhrase.lowercase().trim()
                                if (detected.contains("ciel") || (targetPhrase.isNotBlank() && detected.contains(targetPhrase))) {
                                    onWakeWordDetected(detected)
                                }
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }
                speechRecognizer?.startListening(intent)

            } catch (e: Exception) {
                Log.e(TAG, "Recognizer session error: ${e.message}")
                isRecognizing = false
                destroyRecognizer()
                if (isServiceRunning && !isMicPaused.get()) {
                    mainHandler.postDelayed({ startAcousticMonitor() }, 1500)
                }
            }
        }
    }

    private fun destroyRecognizer() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.w(TAG, "Destroy recognizer: ${e.message}")
        }
        isRecognizing = false
    }

    private fun onWakeWordDetected(detectedQuery: String) {
        Log.i(TAG, "Wake phrase detected: $detectedQuery! Activating CIEL...")
        destroyRecognizer()
        stopAcousticMonitor()

        // Haptic feedback
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibrator feedback error: ${e.message}")
        }

        // Launch Floating Assistant or Activity
        val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("ACTIVATE_VOICE", true)
            putExtra("WAKE_WORD_DETECTED", true)
            putExtra("WAKE_QUERY", detectedQuery)
        }
        startActivity(launchIntent)
    }

    /**
     * Unobtrusive, minimal foreground notification.
     * Android requires a foreground service notification when RECORD_AUDIO is used,
     * but it is styled quietly without repeated alert sounds or intrusive action buttons.
     */
    private fun buildForegroundNotification(): Notification {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingTapIntent = PendingIntent.getActivity(
            this, 0, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CielApplication.VOICE_CHANNEL_ID)
            .setContentTitle("CIEL Companion")
            .setContentText("Voice activation ready")
            .setSmallIcon(R.drawable.ic_ciel_sparkle)
            .setContentIntent(pendingTapIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setShowWhen(false)
            .build()
    }

    override fun onDestroy() {
        stopWakeWordListener()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
