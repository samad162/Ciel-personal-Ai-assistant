package ai.ciel.assistant.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ciel.assistant.R
import ai.ciel.assistant.ui.CielUiState
import ai.ciel.assistant.ui.theme.*

@Composable
fun SettingsScreen(
    uiState: CielUiState,
    onNavigateToTab: (String) -> Unit,
    onOpenSlideBar: () -> Unit,
    onToggleWebSearch: () -> Unit,
    onToggleContinuousVoice: () -> Unit,
    onToggleStreamingSpeech: () -> Unit = {},
    onToggleWakeWord: (Boolean) -> Unit = {},
    onPlayVoicePreview: () -> Unit = {},
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var statusFeedback by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CielBgDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onOpenSlideBar),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Slide Bar",
                            tint = CielAccentCyan.copy(alpha = 0.85f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ciel_logo),
                        contentDescription = "CIEL Logo",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SETTINGS",
                            color = CielTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "System Architecture & Preferences",
                            color = CielAccentCyan,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = CielCardBorderDim, thickness = 1.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Account Section
                item {
                    SettingsSectionHeader("ACCOUNT")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CielCardGlassSubtle,
                        border = BorderStroke(1.dp, CielCardBorderDim),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.user_samad_avatar),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, CielAccentCyan, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.userName,
                                    color = CielTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${uiState.userName.lowercase().replace(" ", "")}@ciel.ai • Free Tier Plan",
                                    color = CielTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Surface(
                                color = CielChipBg,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.8.dp, CielAccentCyan.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "Active",
                                    color = CielNeonGlow,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // AI Models Section
                item {
                    SettingsSectionHeader("AI & REASONING CORE")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CielCardGlassSubtle,
                        border = BorderStroke(1.dp, CielCardBorderDim),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            SettingsRow(
                                title = "Active AI Model",
                                subtitle = "Currently selected: ${uiState.activeModelId.uppercase()}",
                                icon = Icons.Default.Hub,
                                onClick = { onNavigateToTab("models") }
                            )
                            HorizontalDivider(color = CielCardBorderDim.copy(alpha = 0.5f))
                            SettingsToggleRow(
                                title = "Live Web Search Synthesis",
                                subtitle = "Augment neural responses with real-time web exploration",
                                icon = Icons.Default.Public,
                                isChecked = uiState.isWebSearchEnabled,
                                onToggle = onToggleWebSearch
                            )
                        }
                    }
                }

                // Appearance Section
                item {
                    SettingsSectionHeader("APPEARANCE & VISUALS")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CielCardGlassSubtle,
                        border = BorderStroke(1.dp, CielCardBorderDim),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            SettingsRow(
                                title = "Customize Ciel Studio",
                                subtitle = "Accent colors, character presentation, and motion dynamics",
                                icon = Icons.Default.Palette,
                                onClick = { onNavigateToTab("customize") }
                            )
                            HorizontalDivider(color = CielCardBorderDim.copy(alpha = 0.5f))
                            SettingsRow(
                                title = "Dark Cinematic Theme",
                                subtitle = "OLED true-black background with electric cyber glow",
                                icon = Icons.Default.DarkMode,
                                showChevron = false
                            )
                        }
                    }
                }

                // Voice & Language Section
                item {
                    SettingsSectionHeader("VOICE & CONVERSATION")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CielCardGlassSubtle,
                        border = BorderStroke(1.dp, CielCardBorderDim),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            SettingsRow(
                                title = "Voice & Personality Studio",
                                subtitle = "Tone: ${if (uiState.voiceProfile == "kaoruko_gentle") "Kaoruko Gentle Heroine" else uiState.voiceProfile} • ${String.format(java.util.Locale.US, "%.2f", uiState.voicePitch)}x pitch",
                                icon = Icons.Default.SpatialAudio,
                                onClick = { onNavigateToTab("customize") }
                            )
                            HorizontalDivider(color = CielCardBorderDim.copy(alpha = 0.5f))
                            SettingsToggleRow(
                                title = "Sentence-Level Speech Streaming",
                                subtitle = "Synthesize and speak sentences in real-time as tokens generate",
                                icon = Icons.Default.GraphicEq,
                                isChecked = uiState.isStreamingSpeech,
                                onToggle = onToggleStreamingSpeech
                            )
                            HorizontalDivider(color = CielCardBorderDim.copy(alpha = 0.5f))
                            SettingsToggleRow(
                                title = "Continuous Voice Mode",
                                subtitle = "Keep microphone open for hands-free natural back-and-forth",
                                icon = Icons.Default.RecordVoiceOver,
                                isChecked = uiState.isContinuousVoice,
                                onToggle = onToggleContinuousVoice
                            )
                            HorizontalDivider(color = CielCardBorderDim.copy(alpha = 0.5f))
                            SettingsToggleRow(
                                title = "Background Wake-Word Activation",
                                subtitle = "Listen in background for '${uiState.wakeWordPhrase}'",
                                icon = Icons.Default.MicNone,
                                isChecked = uiState.isWakeWordEnabled,
                                onToggle = { onToggleWakeWord(!uiState.isWakeWordEnabled) }
                            )
                            HorizontalDivider(color = CielCardBorderDim.copy(alpha = 0.5f))
                            SettingsRow(
                                title = "Test Voice Sample",
                                subtitle = "Play live preview of CIEL's gentle heroine voice profile",
                                icon = Icons.Default.VolumeUp,
                                onClick = onPlayVoicePreview
                            )
                            HorizontalDivider(color = CielCardBorderDim.copy(alpha = 0.5f))
                            SettingsRow(
                                title = "Language Recognition",
                                subtitle = "Auto-detect (Supports English, Tamil, Hindi, Japanese & 40+ languages)",
                                icon = Icons.Default.Translate,
                                showChevron = false
                            )
                        }
                    }
                }

                // Storage & Privacy
                item {
                    SettingsSectionHeader("STORAGE & PRIVACY")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CielCardGlassSubtle,
                        border = BorderStroke(1.dp, CielCardBorderDim),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            SettingsRow(
                                title = "Memory Vault Management",
                                subtitle = "${uiState.memories.size} facts currently stored in long-term memory",
                                icon = Icons.Default.Psychology,
                                onClick = { onNavigateToTab("memory") }
                            )
                            HorizontalDivider(color = CielCardBorderDim.copy(alpha = 0.5f))
                            SettingsRow(
                                title = "Clear Conversation History",
                                subtitle = "Permanently purge stored message transcripts from database",
                                icon = Icons.Default.DeleteOutline,
                                onClick = { showClearDialog = true }
                            )
                        }
                    }
                }

                // About CIEL Section
                item {
                    SettingsSectionHeader("ABOUT CIEL")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CielCardGlassSubtle,
                        border = BorderStroke(1.dp, CielCardBorderDim),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.ciel_logo),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "CIEL — Cinematic Edition",
                                        color = CielTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Version 2.0.0 (Build 2026.10)",
                                        color = CielTextDim,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Engineered with native Kotlin, Jetpack Compose, Room SQLite, and multi-provider neural fallback architectures.",
                                color = CielTextSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text(text = "Clear History?", color = CielTextPrimary) },
                text = {
                    Text(
                        text = "Are you sure you want to permanently delete all stored chat transcripts? This action cannot be undone.",
                        color = CielTextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onClearChat()
                            showClearDialog = false
                            statusFeedback = "Chat history cleared."
                        }
                    ) {
                        Text("Clear All", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel", color = CielTextSecondary)
                    }
                },
                containerColor = CielSurfaceDark
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = CielAccentCyan,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = CielAccentCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = CielTextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = CielTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = CielTextDim,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = CielAccentCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = CielTextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = CielTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CielAccentBlue,
                uncheckedThumbColor = CielTextSecondary,
                uncheckedTrackColor = Color(0xFF142033)
            )
        )
    }
}
