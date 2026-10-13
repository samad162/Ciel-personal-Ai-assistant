package ai.ciel.assistant.ui.customize

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ciel.assistant.R
import ai.ciel.assistant.ui.CielUiState
import ai.ciel.assistant.ui.theme.*

@Composable
fun CustomizeScreen(
    uiState: CielUiState,
    onSaveAccent: (String) -> Unit,
    onSaveCharacterStyle: (String) -> Unit,
    onSaveMotionIntensity: (String) -> Unit,
    onSaveCommandBarAlpha: (Float) -> Unit,
    onSaveChatDensity: (String) -> Unit,
    onSaveVoicePitch: (Float) -> Unit = {},
    onSaveSpeechRate: (Float) -> Unit = {},
    onSaveVoiceProfile: (String) -> Unit = {},
    onPlayVoicePreview: () -> Unit = {},
    onToggleStreamingSpeech: () -> Unit = {},
    onToggleContinuousVoice: () -> Unit = {},
    onSaveWakeWordPhrase: (String) -> Unit = {},
    onSaveUserName: (String) -> Unit = {},
    onOpenSlideBar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAccent by remember { mutableStateOf(uiState.accentTheme) }
    var selectedStyle by remember { mutableStateOf(uiState.characterStyle) }
    var selectedMotion by remember { mutableStateOf(uiState.motionIntensity) }
    var commandAlpha by remember { mutableFloatStateOf(uiState.commandBarAlpha) }
    var selectedDensity by remember { mutableStateOf(uiState.chatDensity) }
    var voicePitch by remember { mutableFloatStateOf(uiState.voicePitch) }
    var speechRate by remember { mutableFloatStateOf(uiState.speechRate) }
    var selectedProfile by remember { mutableStateOf(uiState.voiceProfile) }
    var wakePhrase by remember { mutableStateOf(uiState.wakeWordPhrase) }
    var inputUserName by remember(uiState.userName) { mutableStateOf(uiState.userName) }
    var nameSavedFeedback by remember { mutableStateOf<String?>(null) }
    var hasSavedFeedback by remember { mutableStateOf(false) }
    var previewFeedback by remember { mutableStateOf<String?>(null) }

    val accentColors = listOf(
        "cyan" to ("Electric Cyan" to CielNeonGlow),
        "violet" to ("Cyber Violet" to CielNeonPurple),
        "ice_blue" to ("Arctic Ice" to CielAccentIceBlue),
        "emerald" to ("Aurora Emerald" to CielStatusGreen)
    )

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
            // Header Bar with Minimal Transparent Grain-Sized Menu Icon
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
                            text = "CUSTOMIZE CIEL",
                            color = CielTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Aesthetic & Identity Studio",
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
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 0. User Identity & Companion Call Sign Card
                item {
                    Text(
                        text = "USER IDENTITY & PROFILE",
                        color = CielAccentCyan,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CielCardGlassSubtle,
                        border = BorderStroke(1.dp, CielCardBorderDim),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.user_samad_avatar),
                                    contentDescription = "User Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, CielAccentCyan, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Your Companion Call Sign",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "CIEL greets you by this name across all voice & dashboard interfaces",
                                        color = CielTextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = inputUserName,
                                onValueChange = {
                                    inputUserName = it
                                    nameSavedFeedback = null
                                },
                                label = { Text("Display Name", fontSize = 12.sp) },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = CielAccentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CielNeonGlow,
                                    unfocusedBorderColor = CielCardBorderDim,
                                    focusedLabelColor = CielNeonGlow,
                                    unfocusedLabelColor = CielTextSecondary,
                                    cursorColor = CielNeonGlow
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Preview: \"Good Evening, ${inputUserName.ifBlank { "User" }}\"",
                                    color = CielAccentCyan.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic
                                )

                                Button(
                                    onClick = {
                                        val trimmed = inputUserName.trim().ifEmpty { "User" }
                                        inputUserName = trimmed
                                        onSaveUserName(trimmed)
                                        nameSavedFeedback = "Saved as \"$trimmed\""
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CielAccentBlue
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Save Name",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (nameSavedFeedback != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "✓ ${nameSavedFeedback}",
                                    color = CielStatusGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // 1. Live Interactive Preview Card
                item {
                    Text(
                        text = "LIVE AESTHETIC PREVIEW",
                        color = CielAccentCyan,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val activePreviewColor = when (selectedAccent) {
                        "violet" -> CielNeonPurple
                        "ice_blue" -> CielAccentIceBlue
                        "emerald" -> CielStatusGreen
                        else -> CielNeonGlow
                    }

                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xF00A1222),
                        border = BorderStroke(
                            1.5.dp,
                            Brush.horizontalGradient(
                                listOf(activePreviewColor, activePreviewColor.copy(alpha = 0.3f), activePreviewColor)
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.ciel_hero_character),
                                contentDescription = "Preview Character",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        alpha = if (selectedStyle == "minimal") 0.25f else 0.85f
                                    }
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                CielBgDark.copy(alpha = 0.65f),
                                                CielBgDark.copy(alpha = 0.95f)
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        color = activePreviewColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, activePreviewColor.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "CIEL 2.0 CINEMATIC",
                                            color = activePreviewColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF091222).copy(alpha = commandAlpha),
                                    border = BorderStroke(1.dp, activePreviewColor.copy(alpha = 0.7f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_ciel_sparkle),
                                            contentDescription = null,
                                            tint = activePreviewColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Ask Ciel anything...",
                                            color = CielTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Neon Accent Color Palette
                item {
                    Text(
                        text = "CYBERNETIC ACCENT COLOR",
                        color = CielAccentCyan,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        accentColors.forEach { (key, pair) ->
                            val (label, col) = pair
                            val isSel = selectedAccent == key
                            Surface(
                                onClick = { selectedAccent = key },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSel) col.copy(alpha = 0.18f) else CielCardGlassSubtle,
                                border = BorderStroke(
                                    if (isSel) 1.5.dp else 1.dp,
                                    if (isSel) col else CielCardBorderDim
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = label.split(" ")[1],
                                        color = if (isSel) CielTextPrimary else CielTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Character Presentation Style
                item {
                    Text(
                        text = "CHARACTER PRESENTATION STYLE",
                        color = CielAccentCyan,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val styles = listOf(
                        "cinematic" to "Cinematic Hero (Full Artwork & Depth)",
                        "ambient" to "Ambient Horizon (Dimmed Backdrop)",
                        "minimal" to "Minimalist Focus (OLED Black Contrast)"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        styles.forEach { (styleKey, styleLabel) ->
                            val isSel = selectedStyle == styleKey
                            Surface(
                                onClick = { selectedStyle = styleKey },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) CielAccentBlue.copy(alpha = 0.25f) else CielCardGlassSubtle,
                                border = BorderStroke(1.dp, if (isSel) CielNeonGlow.copy(alpha = 0.6f) else CielCardBorderDim),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isSel) CielNeonGlow else CielTextDim)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = styleLabel,
                                        color = if (isSel) CielTextPrimary else CielTextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Command Bar Transparency Slider
                item {
                    Text(
                        text = "COMMAND BAR GLASS OPACITY",
                        color = CielAccentCyan,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adjust the command capsule glass blur density (${(commandAlpha * 100).toInt()}%)",
                        color = CielTextSecondary,
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Slider(
                        value = commandAlpha,
                        onValueChange = { commandAlpha = it },
                        valueRange = 0.60f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = CielNeonGlow,
                            activeTrackColor = CielAccentCyan,
                            inactiveTrackColor = Color(0xFF132338)
                        )
                    )
                }

                // 5. Motion Intensity
                item {
                    Text(
                        text = "MOTION DYNAMICS",
                        color = CielAccentCyan,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = { selectedMotion = "smooth" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedMotion == "smooth") CielAccentBlue.copy(alpha = 0.25f) else CielCardGlassSubtle,
                            border = BorderStroke(1.dp, if (selectedMotion == "smooth") CielNeonGlow else CielCardBorderDim),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Cinematic 60fps",
                                    color = CielTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Full spring dynamics",
                                    color = CielTextSecondary,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Surface(
                            onClick = { selectedMotion = "reduced" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedMotion == "reduced") CielAccentBlue.copy(alpha = 0.25f) else CielCardGlassSubtle,
                            border = BorderStroke(1.dp, if (selectedMotion == "reduced") CielNeonGlow else CielCardBorderDim),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Reduced Motion",
                                    color = CielTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Optimized battery mode",
                                    color = CielTextSecondary,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }

                // 6. VOICE & PERSONALITY STUDIO
                item {
                    Text(
                        text = "VOICE & PERSONALITY STUDIO",
                        color = CielAccentCyan,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Warm, gentle anime heroine acoustic design inspired by Kaoruko Waguri with neural sentence streaming",
                        color = CielTextSecondary,
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Voice Profiles
                    val profiles = listOf(
                        Triple(
                            "kaoruko_gentle",
                            "🌸 Kaoruko Gentle Heroine",
                            "Soft, warm, youthful-adult, reassuring, and subtly expressive. Natural feminine warmth without artificial squeakiness."
                        ),
                        Triple(
                            "ethereal_whisper",
                            "✨ Ethereal Whisper",
                            "Intimate, calm, airy, and soothing vocal presence with velvet smoothness."
                        ),
                        Triple(
                            "articulate_companion",
                            "💎 Articulate Companion",
                            "Poised, crystal-clear, intelligent, and focused conversational cadence."
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        profiles.forEach { (profileKey, profileName, profileDesc) ->
                            val isSel = selectedProfile == profileKey
                            Surface(
                                onClick = {
                                    selectedProfile = profileKey
                                    onSaveVoiceProfile(profileKey)
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSel) CielAccentBlue.copy(alpha = 0.28f) else CielCardGlassSubtle,
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isSel) CielNeonGlow else CielCardBorderDim
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isSel) CielNeonGlow else CielTextDim)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = profileName,
                                                color = if (isSel) CielTextPrimary else CielTextSecondary,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (profileKey == "kaoruko_gentle") {
                                                Surface(
                                                    color = CielChipBg,
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(0.8.dp, CielAccentCyan.copy(alpha = 0.5f))
                                                ) {
                                                    Text(
                                                        text = "Recommended",
                                                        color = CielNeonGlow,
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = profileDesc,
                                            color = CielTextSecondary.copy(alpha = 0.85f),
                                            fontSize = 11.5.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pitch Slider
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CielCardGlassSubtle,
                        border = BorderStroke(1.dp, CielCardBorderDim),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Vocal Pitch (Warmth & Resonance)",
                                        color = CielTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Comfortable mature register (~1.08x recommended)",
                                        color = CielTextSecondary,
                                        fontSize = 10.5.sp
                                    )
                                }
                                Surface(
                                    color = CielChipBg,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.8.dp, CielNeonGlow.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%.2f", voicePitch)}x",
                                        color = CielNeonGlow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Slider(
                                value = voicePitch,
                                onValueChange = {
                                    voicePitch = it
                                    onSaveVoicePitch(it)
                                },
                                valueRange = 0.85f..1.30f,
                                colors = SliderDefaults.colors(
                                    thumbColor = CielNeonGlow,
                                    activeTrackColor = CielAccentCyan,
                                    inactiveTrackColor = Color(0xFF132338)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Speech Rate Slider
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CielCardGlassSubtle,
                        border = BorderStroke(1.dp, CielCardBorderDim),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Conversational Cadence & Pacing",
                                        color = CielTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Natural pauses and punctuation pacing",
                                        color = CielTextSecondary,
                                        fontSize = 10.5.sp
                                    )
                                }
                                Surface(
                                    color = CielChipBg,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.8.dp, CielNeonGlow.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%.2f", speechRate)}x",
                                        color = CielNeonGlow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Slider(
                                value = speechRate,
                                onValueChange = {
                                    speechRate = it
                                    onSaveSpeechRate(it)
                                },
                                valueRange = 0.80f..1.30f,
                                colors = SliderDefaults.colors(
                                    thumbColor = CielNeonGlow,
                                    activeTrackColor = CielAccentCyan,
                                    inactiveTrackColor = Color(0xFF132338)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pipelined Sentence Streaming & Continuous Mode Switches
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CielCardGlassSubtle,
                        border = BorderStroke(1.dp, CielCardBorderDim),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Sentence-Level Speech Streaming",
                                        color = CielTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Speaks as text generates for zero perceived lag",
                                        color = CielTextSecondary,
                                        fontSize = 10.5.sp
                                    )
                                }
                                Switch(
                                    checked = uiState.isStreamingSpeech,
                                    onCheckedChange = { onToggleStreamingSpeech() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = CielAccentCyan,
                                        uncheckedThumbColor = CielTextDim,
                                        uncheckedTrackColor = Color(0xFF132338)
                                    )
                                )
                            }
                            HorizontalDivider(color = CielCardBorderDim.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Hands-Free Continuous Conversation",
                                        color = CielTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Keeps listening after speaking for back-and-forth flow",
                                        color = CielTextSecondary,
                                        fontSize = 10.5.sp
                                    )
                                }
                                Switch(
                                    checked = uiState.isContinuousVoice,
                                    onCheckedChange = { onToggleContinuousVoice() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = CielAccentCyan,
                                        uncheckedThumbColor = CielTextDim,
                                        uncheckedTrackColor = Color(0xFF132338)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Wake-Word Phrase Input
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CielCardGlassSubtle,
                        border = BorderStroke(1.dp, CielCardBorderDim),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Configurable Wake-Word Phrase",
                                color = CielTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Keyword triggering voice activation (e.g., 'Hey CIEL')",
                                color = CielTextSecondary,
                                fontSize = 10.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF091222),
                                border = BorderStroke(1.dp, CielAccentCyan.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                androidx.compose.foundation.text.BasicTextField(
                                    value = wakePhrase,
                                    onValueChange = {
                                        wakePhrase = it
                                        onSaveWakeWordPhrase(it)
                                    },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = CielNeonGlow,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Test Voice Preview Button
                    Surface(
                        onClick = {
                            onPlayVoicePreview()
                            previewFeedback = "Playing original gentle heroine voice preview..."
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x3300F0FF),
                        border = BorderStroke(1.4.dp, CielNeonGlow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Test Voice",
                                tint = CielNeonGlow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Test Voice Preview (Kaoruko Acoustic Sample)",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (previewFeedback != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = previewFeedback ?: "",
                            color = CielAccentIceBlue,
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // 7. Action Buttons: Save & Reset
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedAccent = "cyan"
                                selectedStyle = "cinematic"
                                selectedMotion = "smooth"
                                commandAlpha = 0.90f
                                selectedDensity = "comfortable"
                                voicePitch = 1.08f
                                speechRate = 1.00f
                                selectedProfile = "kaoruko_gentle"
                                wakePhrase = "Hey CIEL"
                                onSaveAccent("cyan")
                                onSaveCharacterStyle("cinematic")
                                onSaveMotionIntensity("smooth")
                                onSaveCommandBarAlpha(0.90f)
                                onSaveChatDensity("comfortable")
                                onSaveVoicePitch(1.08f)
                                onSaveSpeechRate(1.00f)
                                onSaveVoiceProfile("kaoruko_gentle")
                                onSaveWakeWordPhrase("Hey CIEL")
                                hasSavedFeedback = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CielCardBorderDim),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Reset Default", color = CielTextSecondary)
                        }

                        Button(
                            onClick = {
                                onSaveAccent(selectedAccent)
                                onSaveCharacterStyle(selectedStyle)
                                onSaveMotionIntensity(selectedMotion)
                                onSaveCommandBarAlpha(commandAlpha)
                                onSaveChatDensity(selectedDensity)
                                onSaveVoicePitch(voicePitch)
                                onSaveSpeechRate(speechRate)
                                onSaveVoiceProfile(selectedProfile)
                                onSaveWakeWordPhrase(wakePhrase)
                                hasSavedFeedback = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CielAccentBlue),
                            border = BorderStroke(1.dp, CielNeonGlow),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Apply Changes", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (hasSavedFeedback) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Preferences saved and applied successfully!",
                            color = CielStatusGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
