package ai.ciel.assistant

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import ai.ciel.assistant.service.FloatingAssistantService
import ai.ciel.assistant.service.WakeWordDetectionService
import ai.ciel.assistant.ui.MainViewModel
import ai.ciel.assistant.ui.chat.ChatScreen
import ai.ciel.assistant.ui.components.CielSlideBar
import ai.ciel.assistant.ui.customize.CustomizeScreen
import ai.ciel.assistant.ui.dashboard.DashboardScreen
import ai.ciel.assistant.ui.history.HistoryScreen
import ai.ciel.assistant.ui.memory.MemoryScreen
import ai.ciel.assistant.ui.models.AiModelsScreen
import ai.ciel.assistant.ui.settings.SettingsScreen
import ai.ciel.assistant.ui.theme.*
import ai.ciel.assistant.ui.tools.ToolsScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.RECORD_AUDIO] == true) {
            startWakeWordService()
        }
    }

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val captureIntent = Intent(this, ai.ciel.assistant.service.ScreenCaptureService::class.java).apply {
                action = ai.ciel.assistant.service.ScreenCaptureService.ACTION_START
                putExtra(ai.ciel.assistant.service.ScreenCaptureService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ai.ciel.assistant.service.ScreenCaptureService.EXTRA_RESULT_DATA, result.data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(captureIntent)
            } else {
                startService(captureIntent)
            }
            viewModel.addActivity("Live Screen Sharing Started", "Vision")
        }
    }

    fun startScreenCaptureConsent() {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? android.media.projection.MediaProjectionManager
        projectionManager?.let {
            mediaProjectionLauncher.launch(it.createScreenCaptureIntent())
        }
    }

    fun startFloatingAssistantOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            val intent = Intent(this, FloatingAssistantService::class.java).apply {
                action = FloatingAssistantService.ACTION_SHOW_OVERLAY
            }
            startService(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupLockScreenAndDarkWindow()
        checkAndRequestAllPermissions()
        handleIncomingIntent(intent)

        setContent {
            CielTheme {
                val uiState by viewModel.uiState.collectAsState()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                // Intercept Android hardware Back button
                BackHandler(enabled = true) {
                    if (drawerState.isOpen) {
                        scope.launch { drawerState.close() }
                    } else if (uiState.currentTab != "home") {
                        viewModel.setTab("home")
                    } else {
                        finish()
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    scrimColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.70f),
                    drawerContent = {
                        CielSlideBar(
                            uiState = uiState,
                            onNavigate = { tab ->
                                viewModel.setTab(tab)
                            },
                            onClose = {
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                ) {
                    Scaffold(
                        containerColor = CielBgDark
                    ) { innerPadding ->
                        when (uiState.currentTab) {
                            "home" -> DashboardScreen(
                                uiState = uiState,
                                onInputChanged = { viewModel.onInputTextChanged(it) },
                                onSubmitPrompt = { viewModel.submitUserPrompt(it) },
                                onMicClicked = { viewModel.triggerPushToTalk() },
                                onToggleWebSearch = { viewModel.toggleWebSearch() },
                                onNavigateToChat = { viewModel.setTab("chat") },
                                onNavigateToMemory = { viewModel.setTab("memory") },
                                onNavigateToTab = { viewModel.setTab(it) },
                                onOpenSlideBar = { scope.launch { drawerState.open() } },
                                modifier = Modifier.padding(innerPadding)
                            )
                            "chat" -> ChatScreen(
                                uiState = uiState,
                                onInputChanged = { viewModel.onInputTextChanged(it) },
                                onSubmitPrompt = { viewModel.submitUserPrompt(it) },
                                onMicClicked = { viewModel.triggerPushToTalk() },
                                onToggleWebSearch = { viewModel.toggleWebSearch() },
                                onStopSpeaking = { viewModel.stopSpeaking() },
                                onClearChat = { viewModel.clearChat() },
                                onOpenSlideBar = { scope.launch { drawerState.open() } },
                                onNavigateBack = { viewModel.setTab("home") },
                                modifier = Modifier.padding(innerPadding)
                            )
                            "history" -> HistoryScreen(
                                uiState = uiState,
                                onNavigateToChat = { viewModel.setTab("chat") },
                                onClearHistory = { viewModel.clearChat() },
                                onOpenSlideBar = { scope.launch { drawerState.open() } },
                                modifier = Modifier.padding(innerPadding)
                            )
                            "models" -> AiModelsScreen(
                                uiState = uiState,
                                availableModels = viewModel.availableModels,
                                onSelectModel = { viewModel.selectModel(it) },
                                onNavigateToChat = { viewModel.setTab("chat") },
                                onOpenSlideBar = { scope.launch { drawerState.open() } },
                                modifier = Modifier.padding(innerPadding)
                            )
                            "tools" -> ToolsScreen(
                                uiState = uiState,
                                onExecuteTool = { viewModel.executeAutonomousToolDirectly(it) },
                                onOpenSlideBar = { scope.launch { drawerState.open() } },
                                modifier = Modifier.padding(innerPadding)
                            )
                            "customize" -> CustomizeScreen(
                                uiState = uiState,
                                onSaveAccent = { viewModel.setAccentTheme(it) },
                                onSaveCharacterStyle = { viewModel.setCharacterStyle(it) },
                                onSaveMotionIntensity = { viewModel.setMotionIntensity(it) },
                                onSaveCommandBarAlpha = { viewModel.setCommandBarAlpha(it) },
                                onSaveChatDensity = { viewModel.setChatDensity(it) },
                                onSaveVoicePitch = { viewModel.setVoicePitch(it) },
                                onSaveSpeechRate = { viewModel.setSpeechRate(it) },
                                onSaveVoiceProfile = { viewModel.setVoiceProfile(it) },
                                onPlayVoicePreview = { viewModel.playVoicePreview() },
                                onToggleStreamingSpeech = { viewModel.toggleStreamingSpeech() },
                                onToggleContinuousVoice = { viewModel.toggleContinuousVoice() },
                                onSaveWakeWordPhrase = { viewModel.setWakeWordPhrase(it) },
                                onSaveUserName = { viewModel.setUserName(it) },
                                onOpenSlideBar = { scope.launch { drawerState.open() } },
                                modifier = Modifier.padding(innerPadding)
                            )
                            "settings" -> SettingsScreen(
                                uiState = uiState,
                                onNavigateToTab = { viewModel.setTab(it) },
                                onOpenSlideBar = { scope.launch { drawerState.open() } },
                                onToggleWebSearch = { viewModel.toggleWebSearch() },
                                onToggleContinuousVoice = { viewModel.toggleContinuousVoice() },
                                onToggleStreamingSpeech = { viewModel.toggleStreamingSpeech() },
                                onToggleWakeWord = { viewModel.toggleWakeWord(it) },
                                onPlayVoicePreview = { viewModel.playVoicePreview() },
                                onClearChat = { viewModel.clearChat() },
                                modifier = Modifier.padding(innerPadding)
                            )
                            "memory" -> MemoryScreen(
                                uiState = uiState,
                                onAddMemory = { key, content -> viewModel.addMemory(key, content) },
                                onDeleteMemory = { id -> viewModel.deleteMemory(id) },
                                onOpenSlideBar = { scope.launch { drawerState.open() } },
                                modifier = Modifier.padding(innerPadding)
                            )
                            else -> DashboardScreen(
                                uiState = uiState,
                                onInputChanged = { viewModel.onInputTextChanged(it) },
                                onSubmitPrompt = { viewModel.submitUserPrompt(it) },
                                onMicClicked = { viewModel.triggerPushToTalk() },
                                onToggleWebSearch = { viewModel.toggleWebSearch() },
                                onNavigateToChat = { viewModel.setTab("chat") },
                                onNavigateToMemory = { viewModel.setTab("memory") },
                                onNavigateToTab = { viewModel.setTab(it) },
                                onOpenSlideBar = { scope.launch { drawerState.open() } },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }


    private fun setupLockScreenAndDarkWindow() {
        // Guarantee dark background from opening frame
        window.decorView.setBackgroundColor(android.graphics.Color.parseColor("#070B14"))
        window.statusBarColor = android.graphics.Color.parseColor("#070B14")
        window.navigationBarColor = android.graphics.Color.parseColor("#070B14")

        // Display over lock screen and wake screen up on voice activation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent) {
        if (intent.getBooleanExtra("ACTIVATE_VOICE", false)) {
            viewModel.triggerPushToTalk()
        }
        val prompt = intent.getStringExtra("USER_PROMPT")
        if (!prompt.isNullOrBlank()) {
            val byVoice = intent.getBooleanExtra("VOICE_RESPONSE", false)
            val clean = prompt.replace("%s", " ").trim()
            viewModel.setTab("chat")
            viewModel.submitUserPrompt(clean, triggeredByVoice = byVoice)
        }
    }

    private fun checkAndRequestAllPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val ungranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (ungranted.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(ungranted.toTypedArray())
        } else {
            startWakeWordService()
        }
    }

    private fun startWakeWordService() {
        val serviceIntent = Intent(this, WakeWordDetectionService::class.java).apply {
            action = WakeWordDetectionService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}

@Composable
fun CielBottomNavigationBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp, top = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating 3D Cyber Glass Dock
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .graphicsLayer {
                    shadowElevation = 24f
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                    clip = true
                },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
            color = CielDockSurface,
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(
                        CielNeonGlow.copy(alpha = 0.5f),
                        CielAccentBlue.copy(alpha = 0.35f),
                        CielNeonPurple.copy(alpha = 0.4f),
                        CielNeonGlow.copy(alpha = 0.5f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockNavItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    isSelected = currentTab == "home",
                    onClick = { onTabSelected("home") }
                )
                DockNavItem(
                    label = "Chat",
                    icon = Icons.Default.Chat,
                    isSelected = currentTab == "chat",
                    onClick = { onTabSelected("chat") }
                )
                DockNavItem(
                    label = "Memory",
                    icon = Icons.Default.Memory,
                    isSelected = currentTab == "memory",
                    onClick = { onTabSelected("memory") }
                )
            }
        }
    }
}

@Composable
fun DockNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "nav_scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(
                                    CielAccentCyan.copy(alpha = 0.22f),
                                    CielAccentBlue.copy(alpha = 0.18f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(CielNeonGlow.copy(alpha = 0.7f), CielAccentCyan.copy(alpha = 0.4f))
                            ),
                            androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                        )
                } else Modifier
            )
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) CielNeonGlow else CielTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = CielTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
