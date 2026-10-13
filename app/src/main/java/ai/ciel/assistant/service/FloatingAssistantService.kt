package ai.ciel.assistant.service

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ai.ciel.assistant.MainActivity
import ai.ciel.assistant.R
import ai.ciel.assistant.tools.AssistantToolDispatcher
import ai.ciel.assistant.voice.CielVoiceEngine
import kotlinx.coroutines.*
import java.util.Locale

/**
 * CIEL System-Wide Floating Companion — Ultra-Compact "Little Circle" Edition:
 * - Replaces intrusive screen-covering rectangular cards with a sleek, non-intrusive floating circular orb (54dp).
 * - 100x redesigned futuristic starting animation with elastic spring entrance & concentric holographic sonic shockwaves.
 * - Continuous organic breathing idle loop.
 * - Interactive voice-reactive sonic ripple rings during active listening.
 * - Dynamic Island-style auto-collapsing mini speech pill for transcripts & responses that never covers the user's screen.
 * - Edge-snapping physics (smooth spring snap to left or right screen edge).
 * - Complete integration with CielVoiceEngine, AssistantToolDispatcher, DocumentEngine, and ImageEngine.
 */
class FloatingAssistantService : Service() {

    companion object {
        const val ACTION_SHOW_OVERLAY = "ai.ciel.action.SHOW_OVERLAY"
        const val ACTION_START_VOICE = "ai.ciel.action.START_FLOATING_VOICE"
        private const val TAG = "CielFloatingOrb"
    }

    private var windowManager: WindowManager? = null
    private var rootView: View? = null
    private var windowParams: WindowManager.LayoutParams? = null

    // UI Elements inside layout_floating_ciel_orb
    private var orbContainer: FrameLayout? = null
    private var orbCoreFrame: View? = null
    private var rippleRing1: View? = null
    private var rippleRing2: View? = null
    private var orbGlow: View? = null
    private var imgOrbAvatar: ImageView? = null
    private var statusDot: View? = null

    // Dynamic Island Mini Speech Pill
    private var miniPillContainer: LinearLayout? = null
    private var tvPillText: TextView? = null
    private var btnPillDismiss: ImageView? = null

    // Animators
    private var idleBreathingAnimator: ValueAnimator? = null
    private var listeningRippleAnimator1: AnimatorSet? = null
    private var listeningRippleAnimator2: AnimatorSet? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var toolDispatcher: AssistantToolDispatcher
    private var voiceEngine: CielVoiceEngine? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var screenWidth = 1080

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        toolDispatcher = AssistantToolDispatcher(this)
        calculateScreenDimensions()

        initVoiceEngine()
        setupFloatingOrbView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        playStartingAnimation()
        when (intent?.action) {
            ACTION_START_VOICE -> {
                mainHandler.postDelayed({ startVoiceCapture() }, 450)
            }
            ACTION_SHOW_OVERLAY -> {
                showMiniPill("Ready for command", autoCollapseMs = 2800)
            }
        }
        return START_NOT_STICKY
    }

    private fun calculateScreenDimensions() {
        val display = windowManager?.defaultDisplay
        val size = Point()
        display?.getSize(size)
        screenWidth = if (size.x > 0) size.x else 1080
    }

    private fun getOverlayLayoutFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun initVoiceEngine() {
        voiceEngine = CielVoiceEngine(
            context = this,
            onSpeakingStateChanged = { speaking ->
                mainHandler.post {
                    if (speaking) {
                        startSpeakingAnimation()
                    } else {
                        stopSpeakingAnimation()
                    }
                }
            },
            onPlaybackCompleted = {
                mainHandler.post {
                    stopSpeakingAnimation()
                    showMiniPill("Done", autoCollapseMs = 1800)
                }
            }
        ).apply {
            val prefs = ai.ciel.assistant.util.PreferencesManager(this@FloatingAssistantService)
            userPitch = prefs.voicePitch
            userSpeechRate = prefs.speechRate
            voiceProfile = prefs.voiceProfile
        }
    }

    private fun setupFloatingOrbView() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = inflater.inflate(R.layout.layout_floating_ciel_orb, null)

        orbContainer = rootView?.findViewById(R.id.orbContainer)
        orbCoreFrame = rootView?.findViewById(R.id.orbCoreFrame)
        rippleRing1 = rootView?.findViewById(R.id.rippleRing1)
        rippleRing2 = rootView?.findViewById(R.id.rippleRing2)
        orbGlow = rootView?.findViewById(R.id.orbGlow)
        imgOrbAvatar = rootView?.findViewById(R.id.imgOrbAvatar)
        statusDot = rootView?.findViewById(R.id.statusDot)
        orbCoreFrame?.clipToOutline = true

        miniPillContainer = rootView?.findViewById(R.id.miniPillContainer)
        tvPillText = rootView?.findViewById(R.id.tvPillText)
        btnPillDismiss = rootView?.findViewById(R.id.btnPillDismiss)

        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            getOverlayLayoutFlag(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 360
        }

        setupGestures()

        btnPillDismiss?.setOnClickListener {
            collapseMiniPill()
        }

        try {
            if (Settings.canDrawOverlays(this)) {
                windowManager?.addView(rootView, windowParams)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding floating orb: ${e.message}")
        }
    }

    private fun setupGestures() {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = true
        var lastClickTime = 0L

        orbContainer?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = windowParams?.x ?: 0
                    initialY = windowParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true

                    // Scale feedback
                    orbCoreFrame?.animate()?.scaleX(0.92f)?.scaleY(0.92f)?.setDuration(100)?.start()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()

                    if (Math.abs(dx) > 14 || Math.abs(dy) > 14) {
                        isClick = false
                    }

                    windowParams?.x = initialX + dx
                    windowParams?.y = initialY + dy
                    windowManager?.updateViewLayout(rootView, windowParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    orbCoreFrame?.animate()?.scaleX(1.0f)?.scaleY(1.0f)?.setDuration(120)?.start()

                    if (isClick) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastClickTime < 350) {
                            // Double tap: Launch main full app
                            openFullApp()
                        } else {
                            // Single tap: Toggle voice listening
                            lastClickTime = currentTime
                            mainHandler.postDelayed({
                                if (System.currentTimeMillis() - lastClickTime >= 340) {
                                    toggleVoiceListening()
                                }
                            }, 350)
                        }
                    } else {
                        // Snap smoothly to closest edge
                        snapToClosestEdge()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun snapToClosestEdge() {
        val currentX = windowParams?.x ?: 0
        val targetX = if (currentX + 34 > screenWidth / 2) {
            screenWidth - (rootView?.width ?: 180) - 24
        } else {
            24
        }

        val animator = ValueAnimator.ofInt(currentX, targetX).apply {
            duration = 260
            interpolator = DecelerateInterpolator(1.8f)
            addUpdateListener { va ->
                windowParams?.x = va.animatedValue as Int
                windowManager?.updateViewLayout(rootView, windowParams)
            }
        }
        animator.start()
    }

    private fun openFullApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("FROM_FLOATING_ORB", true)
        }
        startActivity(intent)
    }

    /**
     * 100x Redesigned Futuristic Starting Animation:
     * - Elastic spring entrance with OvershootInterpolator (scale 0 -> 1.0f).
     * - Dual concentric sonic holographic shockwave ripples blossoming outward.
     * - Transitions into organic ambient breathing idle loop.
     */
    private fun playStartingAnimation() {
        orbContainer?.scaleX = 0f
        orbContainer?.scaleY = 0f
        orbContainer?.alpha = 0f

        // Spring entrance
        orbContainer?.animate()
            ?.scaleX(1.0f)
            ?.scaleY(1.0f)
            ?.alpha(1.0f)
            ?.setDuration(460)
            ?.setInterpolator(OvershootInterpolator(2.4f))
            ?.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    startIdleBreathingAnimation()
                }
            })
            ?.start()

        // Concentric Holographic Sonic Shockwave 1
        rippleRing1?.apply {
            visibility = View.VISIBLE
            scaleX = 0.5f
            scaleY = 0.5f
            alpha = 0.9f
            animate()
                .scaleX(2.3f)
                .scaleY(2.3f)
                .alpha(0f)
                .setDuration(720)
                .setInterpolator(DecelerateInterpolator(1.6f))
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        alpha = 0f
                    }
                })
                .start()
        }

        // Concentric Holographic Sonic Shockwave 2
        rippleRing2?.apply {
            visibility = View.VISIBLE
            scaleX = 0.5f
            scaleY = 0.5f
            alpha = 0.75f
            animate()
                .scaleX(2.7f)
                .scaleY(2.7f)
                .alpha(0f)
                .setStartDelay(140)
                .setDuration(880)
                .setInterpolator(DecelerateInterpolator(1.8f))
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        alpha = 0f
                    }
                })
                .start()
        }
    }

    private fun startIdleBreathingAnimation() {
        idleBreathingAnimator?.cancel()
        idleBreathingAnimator = ValueAnimator.ofFloat(1.0f, 1.06f).apply {
            duration = 2200
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { va ->
                val s = va.animatedValue as Float
                orbGlow?.scaleX = s
                orbGlow?.scaleY = s
                orbGlow?.alpha = 0.65f + (s - 1.0f) * 4f
            }
        }
        idleBreathingAnimator?.start()
    }

    private fun startListeningAnimation() {
        idleBreathingAnimator?.cancel()
        stopListeningAnimation()

        // Continuous ripple wave 1
        val scaleX1 = ObjectAnimator.ofFloat(rippleRing1, View.SCALE_X, 1.0f, 2.4f)
        val scaleY1 = ObjectAnimator.ofFloat(rippleRing1, View.SCALE_Y, 1.0f, 2.4f)
        val alpha1 = ObjectAnimator.ofFloat(rippleRing1, View.ALPHA, 0.85f, 0f)

        listeningRippleAnimator1 = AnimatorSet().apply {
            playTogether(scaleX1, scaleY1, alpha1)
            duration = 1100
            interpolator = DecelerateInterpolator(1.5f)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (isListening) start()
                }
            })
            start()
        }

        // Continuous ripple wave 2 (offset)
        mainHandler.postDelayed({
            if (isListening) {
                val scaleX2 = ObjectAnimator.ofFloat(rippleRing2, View.SCALE_X, 1.0f, 2.7f)
                val scaleY2 = ObjectAnimator.ofFloat(rippleRing2, View.SCALE_Y, 1.0f, 2.7f)
                val alpha2 = ObjectAnimator.ofFloat(rippleRing2, View.ALPHA, 0.70f, 0f)

                listeningRippleAnimator2 = AnimatorSet().apply {
                    playTogether(scaleX2, scaleY2, alpha2)
                    duration = 1200
                    interpolator = DecelerateInterpolator(1.5f)
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            if (isListening) start()
                        }
                    })
                    start()
                }
            }
        }, 400)

        // Glow pulse
        statusDot?.setBackgroundResource(R.drawable.bg_mic_circle)
        showMiniPill("Listening...", autoCollapseMs = 0)
    }

    private fun stopListeningAnimation() {
        listeningRippleAnimator1?.cancel()
        listeningRippleAnimator2?.cancel()
        rippleRing1?.animate()?.alpha(0f)?.setDuration(200)?.start()
        rippleRing2?.animate()?.alpha(0f)?.setDuration(200)?.start()
        startIdleBreathingAnimation()
    }

    private fun startSpeakingAnimation() {
        stopListeningAnimation()
        orbCoreFrame?.animate()?.scaleX(1.08f)?.scaleY(1.08f)?.setDuration(300)?.start()
        orbGlow?.animate()?.scaleX(1.2f)?.scaleY(1.2f)?.alpha(1.0f)?.setDuration(300)?.start()
    }

    private fun stopSpeakingAnimation() {
        orbCoreFrame?.animate()?.scaleX(1.0f)?.scaleY(1.0f)?.setDuration(250)?.start()
        orbGlow?.animate()?.scaleX(1.0f)?.scaleY(1.0f)?.alpha(0.7f)?.setDuration(250)?.start()
        startIdleBreathingAnimation()
    }

    private fun showMiniPill(text: String, autoCollapseMs: Long = 3200) {
        tvPillText?.text = text
        miniPillContainer?.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationX = -20f
            animate()
                .alpha(1.0f)
                .translationX(0f)
                .setDuration(220)
                .setInterpolator(DecelerateInterpolator(1.6f))
                .start()
        }

        if (autoCollapseMs > 0) {
            mainHandler.removeCallbacks(collapsePillRunnable)
            mainHandler.postDelayed(collapsePillRunnable, autoCollapseMs)
        }
    }

    private val collapsePillRunnable = Runnable {
        collapseMiniPill()
    }

    private fun collapseMiniPill() {
        miniPillContainer?.animate()
            ?.alpha(0f)
            ?.translationX(-16f)
            ?.setDuration(200)
            ?.withEndAction {
                miniPillContainer?.visibility = View.GONE
            }
            ?.start()
    }

    private fun toggleVoiceListening() {
        if (isListening) {
            stopVoiceCapture()
        } else {
            startVoiceCapture()
        }
    }

    private fun startVoiceCapture() {
        if (isListening) return
        isListening = true
        startListeningAnimation()

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: android.os.Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {
                        // Dynamic micro-pulse to voice volume
                        val boost = (rmsdB / 18f).coerceIn(0f, 0.12f)
                        orbCoreFrame?.scaleX = 1.0f + boost
                        orbCoreFrame?.scaleY = 1.0f + boost
                    }
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        stopVoiceCapture()
                    }

                    override fun onError(error: Int) {
                        stopVoiceCapture()
                        showMiniPill("Ready", autoCollapseMs = 1500)
                    }

                    override fun onResults(results: android.os.Bundle?) {
                        stopVoiceCapture()
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognized = matches[0]
                            processAssistantPrompt(recognized)
                        }
                    }

                    override fun onPartialResults(partialResults: android.os.Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            tvPillText?.text = matches[0]
                        }
                    }

                    override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)

        } catch (e: Exception) {
            Log.e(TAG, "SpeechRecognizer error: ${e.message}")
            stopVoiceCapture()
        }
    }

    private fun stopVoiceCapture() {
        isListening = false
        stopListeningAnimation()
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.w(TAG, "Stop recognizer: ${e.message}")
        }
    }

    private fun processAssistantPrompt(prompt: String) {
        showMiniPill("Thinking...", autoCollapseMs = 0)

        serviceScope.launch {
            if (toolDispatcher.isAutonomousToolCommand(prompt)) {
                val result = toolDispatcher.executeToolAsync(prompt)
                showMiniPill(result.message, autoCollapseMs = 4500)
                voiceEngine?.speakFullText(result.message)

                if (result.generatedFile != null) {
                    mainHandler.postDelayed({
                        toolDispatcher.documentEngine.openFile(result.generatedFile)
                    }, 1100)
                }
            } else {
                // Conversational query
                val provider = ai.ciel.assistant.data.remote.UniversalNeuralAiProvider()
                val sb = StringBuilder()
                provider.streamCompletion(prompt, emptyList(), emptyList(), false).collect { chunk ->
                    sb.append(chunk)
                    val currentText = sb.toString()
                    if (currentText.length < 50) {
                        tvPillText?.text = currentText
                    }
                }
                val finalOutput = sb.toString()
                showMiniPill(finalOutput.take(45) + if (finalOutput.length > 45) "..." else "", autoCollapseMs = 4500)
                voiceEngine?.speakFullText(finalOutput)
            }
        }
    }

    override fun onDestroy() {
        stopVoiceCapture()
        idleBreathingAnimator?.cancel()
        listeningRippleAnimator1?.cancel()
        listeningRippleAnimator2?.cancel()
        voiceEngine?.destroy()
        serviceScope.cancel()

        try {
            rootView?.let { windowManager?.removeView(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Error removing root view: ${e.message}")
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
