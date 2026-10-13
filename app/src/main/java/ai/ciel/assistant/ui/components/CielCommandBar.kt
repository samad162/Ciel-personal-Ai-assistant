package ai.ciel.assistant.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ciel.assistant.R
import ai.ciel.assistant.domain.model.AssistantState
import ai.ciel.assistant.ui.theme.*

data class ExpandableActionItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val promptPrefix: String,
    val isToggleWebSearch: Boolean = false,
    val isAttachment: Boolean = false
)

@Composable
fun CielCommandBar(
    inputText: String,
    isWebSearchEnabled: Boolean,
    assistantState: AssistantState,
    rmsLevel: Float = 0f,
    surfaceAlpha: Float = 0.90f,
    showActionChips: Boolean = false,
    onInputChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onMicClicked: () -> Unit,
    onToggleWebSearch: () -> Unit,
    onChipSelected: (String) -> Unit = {},
    onAttachmentClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isListening = assistantState == AssistantState.LISTENING
    val isSpeaking = assistantState == AssistantState.SPEAKING
    val isProcessing = assistantState == AssistantState.PROCESSING

    var isExpanded by remember { mutableStateOf(false) }

    // Breathing glow animation for active state
    val infiniteTransition = rememberInfiniteTransition(label = "command_bar_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    // Smooth rotation of + icon when expanded
    val plusRotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "plus_rotate"
    )

    val actionOptions = listOf(
        ExpandableActionItem(
            id = "live_screen",
            title = "Live Screen",
            description = "Gemini Live-style screen content analysis",
            icon = Icons.Default.SmartDisplay,
            iconColor = Color(0xFF00F0FF),
            promptPrefix = "What is on my screen? Explain the current content: "
        ),
        ExpandableActionItem(
            id = "live_camera",
            title = "Live Camera",
            description = "Show CIEL what you see in real-time",
            icon = Icons.Default.CameraAlt,
            iconColor = Color(0xFF38BDF8),
            promptPrefix = "What is this object in front of me? "
        ),
        ExpandableActionItem(
            id = "doc_gen",
            title = "Create Document",
            description = "Generate genuine PDF, CSV, Word & Markdown files",
            icon = Icons.Default.Description,
            iconColor = Color(0xFF10B981),
            promptPrefix = "Create a PDF report about: "
        ),
        ExpandableActionItem(
            id = "image_gen",
            title = "Image Generation",
            description = "Synthesize illustrations & concept art",
            icon = Icons.Default.AutoAwesome,
            iconColor = Color(0xFFA855F7),
            promptPrefix = "Create a cinematic anime illustration of: "
        ),
        ExpandableActionItem(
            id = "web_search",
            title = "Deep Web Search",
            description = "Live real-time web research & citations",
            icon = Icons.Default.Public,
            iconColor = Color(0xFF06B6D4),
            promptPrefix = "Search the web and synthesize: ",
            isToggleWebSearch = true
        ),
        ExpandableActionItem(
            id = "system_tools",
            title = "Device Actions",
            description = "Launch apps, settings, volume & diagnostics",
            icon = Icons.Default.Bolt,
            iconColor = Color(0xFFF59E0B),
            promptPrefix = "Execute device action: "
        )
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating Glassmorphism Intelligence Action Menu (Expands above Command Bar)
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(220)) + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut(tween(180)) + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xF207101E),
                border = BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(
                        listOf(
                            CielNeonGlow.copy(alpha = 0.7f),
                            CielAccentCyan.copy(alpha = 0.4f),
                            CielNeonPurple.copy(alpha = 0.6f)
                        )
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .graphicsLayer {
                        shadowElevation = 24f
                        shape = RoundedCornerShape(22.dp)
                        clip = true
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CielNeonGlow,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = "CIEL INTELLIGENCE CAPABILITIES",
                                color = CielTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable { isExpanded = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Menu",
                                tint = CielTextSecondary.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = CielCardBorderDim, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Compact 2-Column Grid of Capabilities
                    val actionPairs = actionOptions.chunked(2)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        actionPairs.forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { action ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0x660E182A),
                                        border = BorderStroke(0.8.dp, CielCardBorderDim.copy(alpha = 0.6f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                isExpanded = false
                                                if (action.isToggleWebSearch && !isWebSearchEnabled) {
                                                    onToggleWebSearch()
                                                }
                                                if (action.isAttachment) {
                                                    onAttachmentClicked()
                                                }
                                                if (action.promptPrefix.isNotBlank()) {
                                                    onInputChanged(action.promptPrefix)
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 9.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(7.dp))
                                                    .background(action.iconColor.copy(alpha = 0.16f))
                                                    .border(0.8.dp, action.iconColor.copy(alpha = 0.4f), RoundedCornerShape(7.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = action.icon,
                                                    contentDescription = action.title,
                                                    tint = action.iconColor,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = action.title,
                                                    color = Color.White,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = action.description,
                                                    color = CielTextSecondary,
                                                    fontSize = 9.sp,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Main Glassmorphism Command Capsule
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .graphicsLayer {
                    shadowElevation = if (isListening || isSpeaking) 24f else 16f
                    shape = RoundedCornerShape(28.dp)
                    clip = true
                },
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF091222).copy(alpha = surfaceAlpha),
            border = BorderStroke(
                1.4.dp,
                when {
                    isListening -> Brush.horizontalGradient(
                        listOf(Color(0xFFEF4444).copy(alpha = pulseGlow), Color(0xFFF97316), Color(0xFFEF4444).copy(alpha = pulseGlow))
                    )
                    isSpeaking -> Brush.horizontalGradient(
                        listOf(CielNeonGlow, CielAccentIceBlue, CielAccentCyan, CielNeonGlow)
                    )
                    else -> Brush.horizontalGradient(
                        listOf(
                            CielNeonGlow.copy(alpha = 0.75f * pulseGlow),
                            CielAccentCyan.copy(alpha = 0.6f),
                            CielNeonPurple.copy(alpha = 0.65f),
                            CielNeonGlow.copy(alpha = 0.75f * pulseGlow)
                        )
                    )
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Expandable (+) Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isExpanded -> CielNeonGlow.copy(alpha = 0.28f)
                                isListening -> Color(0x33EF4444)
                                isSpeaking -> CielAccentCyan.copy(alpha = 0.25f)
                                else -> CielAccentCyan.copy(alpha = 0.14f)
                            }
                        )
                        .border(
                            1.dp,
                            if (isExpanded) CielNeonGlow else CielCardBorderDim,
                            CircleShape
                        )
                        .clickable {
                            isExpanded = !isExpanded
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Intelligence Actions",
                        tint = when {
                            isExpanded -> CielNeonGlow
                            isListening -> Color(0xFFEF4444)
                            isSpeaking -> CielNeonGlow
                            else -> CielAccentCyan
                        },
                        modifier = Modifier
                            .size(19.dp)
                            .graphicsLayer { rotationZ = plusRotation }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Input Field
                BasicTextField(
                    value = inputText,
                    onValueChange = onInputChanged,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = { if (inputText.isNotBlank()) onSubmit() }
                    ),
                    textStyle = TextStyle(
                        color = CielTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (inputText.isEmpty()) {
                            Text(
                                text = when (assistantState) {
                                    AssistantState.LISTENING -> "Listening to your voice..."
                                    AssistantState.SPEAKING -> "CIEL is speaking... (tap Stop to interrupt)"
                                    AssistantState.PROCESSING -> "CIEL is reasoning..."
                                    else -> "Ask CIEL anything..."
                                },
                                color = when (assistantState) {
                                    AssistantState.LISTENING -> CielNeonGlow
                                    AssistantState.SPEAKING -> CielAccentIceBlue
                                    AssistantState.PROCESSING -> CielAccentCyan
                                    else -> CielTextSecondary.copy(alpha = 0.75f)
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        innerTextField()
                    }
                )

                // Reactive Waveform during Listening or Speaking
                if (isListening || isSpeaking) {
                    AudioWaveform(
                        state = assistantState,
                        rmsLevel = rmsLevel,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }

                // Attachment Action Button
                IconButton(
                    onClick = onAttachmentClicked,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach Document",
                        tint = CielTextSecondary.copy(alpha = 0.8f),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Web Search Toggle
                IconButton(
                    onClick = onToggleWebSearch,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Web Search",
                        tint = if (isWebSearchEnabled) CielNeonGlow else CielTextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Voice Mic, Stop Speaking, or Send Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSpeaking -> Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF06B6D4)))
                                isListening -> Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFF97316)))
                                inputText.isNotBlank() -> Brush.linearGradient(listOf(CielAccentCyan, CielAccentBlue))
                                else -> Brush.linearGradient(listOf(CielAccentBlue.copy(alpha = 0.85f), CielNeonPurple.copy(alpha = 0.85f)))
                            }
                        )
                        .border(
                            1.2.dp,
                            when {
                                isSpeaking -> CielNeonGlow
                                isListening -> Color(0xFFFF8888)
                                else -> CielNeonGlow.copy(alpha = 0.8f)
                            },
                            CircleShape
                        )
                        .clickable {
                            if (inputText.isNotBlank()) onSubmit() else onMicClicked()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isSpeaking -> {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Speaking",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        inputText.isNotBlank() -> {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        else -> {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_mic),
                                contentDescription = "Voice Input",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }

        // Optional Quick Action Chips Row (only rendered if explicitly requested)
        if (showActionChips) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CommandQuickChip(
                    icon = Icons.Default.Psychology,
                    label = "Deep Thinking",
                    onClick = { onChipSelected("Conduct a deep analytical thinking breakdown on: ") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                CommandQuickChip(
                    icon = Icons.Default.Public,
                    label = "Search Web",
                    onClick = { onChipSelected("Search the web and synthesize latest developments regarding: ") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                CommandQuickChip(
                    icon = Icons.Default.Image,
                    label = "Generate Image",
                    onClick = { onChipSelected("Describe a high-concept visual scene for: ") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                CommandQuickChip(
                    icon = Icons.Default.Code,
                    label = "Code Assistant",
                    onClick = { onChipSelected("Write optimized, modern code for: ") }
                )
            }
        }
    }
}

@Composable
fun CommandQuickChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xB30A1424),
        border = BorderStroke(1.dp, CielCardBorderDim),
        modifier = Modifier.height(30.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CielAccentCyan,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                color = CielTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
