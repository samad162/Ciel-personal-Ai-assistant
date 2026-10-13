package ai.ciel.assistant.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ciel.assistant.R
import ai.ciel.assistant.ui.CielUiState
import ai.ciel.assistant.ui.components.CielCommandBar
import ai.ciel.assistant.ui.theme.*
import java.util.Calendar

@Composable
fun DashboardScreen(
    uiState: CielUiState,
    onInputChanged: (String) -> Unit,
    onSubmitPrompt: (String) -> Unit,
    onMicClicked: () -> Unit,
    onToggleWebSearch: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToTab: (String) -> Unit,
    onOpenSlideBar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Dynamic greeting based on current local hour
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning,"
            in 12..17 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CielBgDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // 1. Cinematic Edge-to-Edge Companion Hero & Floating Command Hub
            item {
                CinematicCompanionHeroSection(
                    uiState = uiState,
                    greeting = greeting,
                    onOpenSlideBar = onOpenSlideBar,
                    onOpenSettings = { onNavigateToTab("settings") },
                    onNavigateToChat = onNavigateToChat,
                    onInputChanged = onInputChanged,
                    onSubmitPrompt = onSubmitPrompt,
                    onMicClicked = onMicClicked,
                    onToggleWebSearch = onToggleWebSearch
                )
            }

            // 2. Three 3D Holographic Core Feature Cards (Horizontally Scrollable)
            item {
                Spacer(modifier = Modifier.height(14.dp))
                CoreHolographicCardsRow(
                    onExploreModels = { onNavigateToTab("models") },
                    onPowerfulTools = { onNavigateToTab("tools") },
                    onCustomize = { onNavigateToTab("customize") }
                )
            }

            // 3. Showcase Card: "Latest from Ciel" with Holographic Galaxy
            item {
                Spacer(modifier = Modifier.height(14.dp))
                LatestFromCielCard(
                    onExploreNew = { onNavigateToTab("models") }
                )
            }

            // 4. Live System Telemetry Card
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SystemTelemetryCard(uiState = uiState)
            }

            // 5. Quick Actions Grid
            item {
                Spacer(modifier = Modifier.height(14.dp))
                QuickActionsSection(
                    onSubmitPrompt = { prompt ->
                        onSubmitPrompt(prompt)
                        onNavigateToChat()
                    }
                )
            }

            // 6. Recent Activity & Conversations
            item {
                Spacer(modifier = Modifier.height(14.dp))
                RecentActivityCard(
                    uiState = uiState,
                    onOpenHistory = { onNavigateToTab("history") },
                    onOpenChat = onNavigateToChat
                )
            }

            // 7. Bottom Brand Signature
            item {
                Text(
                    text = "CIEL • YOUR INTELLIGENCE. YOUR WORLD.",
                    color = CielTextDim,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp, bottom = 12.dp)
                )
            }
        }
    }
}

/**
 * 1. Cinematic Companion Hero Section:
 * Edge-to-edge seamless artwork with natural dark gradient fade,
 * floating greeting, quote card, and signature command capsule.
 */
@Composable
fun CinematicCompanionHeroSection(
    uiState: CielUiState,
    greeting: String,
    onOpenSlideBar: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateToChat: () -> Unit,
    onInputChanged: (String) -> Unit,
    onSubmitPrompt: (String) -> Unit,
    onMicClicked: () -> Unit,
    onToggleWebSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(510.dp)
    ) {
        // Edge-to-edge Cinematic Companion Artwork
        Image(
            painter = painterResource(id = R.drawable.ciel_hero_character),
            contentDescription = "CIEL Anime Companion",
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onNavigateToChat)
        )

        // Dark Atmospheric Scrim & Smooth Bottom Gradient Blend
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.70f),
                            Color.Transparent,
                            Color.Transparent,
                            CielBgDark.copy(alpha = 0.60f),
                            CielBgDark
                        )
                    )
                )
        )

        // Top Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            HomeTopBar(
                onOpenSlideBar = onOpenSlideBar,
                onOpenSettings = onOpenSettings
            )
        }

        // Floating Quote Card (Top Right)
        Surface(
            color = Color(0xB3081224),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.8.dp, CielNeonGlow.copy(alpha = 0.45f)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 74.dp, end = 16.dp)
                .widthIn(max = 195.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ciel_star_logo),
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Ciel",
                        color = CielAccentCyan,
                        fontSize = 11.5.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "“Every question opens a new possibility.”",
                    color = Color.White.copy(alpha = 0.90f),
                    fontSize = 10.sp,
                    lineHeight = 13.5.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }

        // Floating Greeting & Command Bar at the bottom of the hero
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Personalized Greeting
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .height(36.dp)
                        .background(CielAccentCyan, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = greeting,
                        color = CielTextSecondary,
                        fontSize = 12.5.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = uiState.userName,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Your ideas, my intelligence. Let's create something amazing today.",
                        color = CielAccentCyan.copy(alpha = 0.95f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Signature Translucent Command Bar with Workable Expandable (+) Button
            CielCommandBar(
                inputText = uiState.inputText,
                isWebSearchEnabled = uiState.isWebSearchEnabled,
                assistantState = uiState.assistantState,
                rmsLevel = uiState.audioRmsLevel,
                surfaceAlpha = uiState.commandBarAlpha,
                showActionChips = false,
                onInputChanged = onInputChanged,
                onSubmit = {
                    if (uiState.inputText.isNotBlank()) {
                        onSubmitPrompt(uiState.inputText)
                        onNavigateToChat()
                    }
                },
                onMicClicked = onMicClicked,
                onToggleWebSearch = onToggleWebSearch,
                onChipSelected = { chipPrompt ->
                    onInputChanged(chipPrompt)
                },
                onAttachmentClicked = {
                    onInputChanged("Analyze attached file: ")
                }
            )
        }
    }
}

/**
 * Top Header Bar matching reference mockup
 */
@Composable
fun HomeTopBar(
    onOpenSlideBar: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Minimal Transparent Slide Bar Grain Icon + CIEL Diamond Star Logo
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOpenSlideBar),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = CielAccentCyan.copy(alpha = 0.85f),
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onOpenSlideBar)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ciel_star_logo),
                    contentDescription = "CIEL Star Logo",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "CIEL",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Your AI Companion",
                        color = CielAccentCyan,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Right: Online Chip + Notification + Samad Profile Avatar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = CielChipBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, CielCardBorderDim)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(CielStatusGreen)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Online",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = CielTextSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Image(
                painter = painterResource(id = R.drawable.user_samad_avatar),
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.2.dp, CielAccentCyan, CircleShape)
                    .clickable(onClick = onOpenSettings)
            )
        }
    }
}

/**
 * 2. Three 3D Holographic Feature Cards matching reference mockup:
 * - Explore AI Models (with 3D Neural Globe)
 * - Powerful Tools (with 3D Isometric Cube)
 * - Customize Ciel (with 3D Particle Orb)
 */
@Composable
fun CoreHolographicCardsRow(
    onExploreModels: () -> Unit,
    onPowerfulTools: () -> Unit,
    onCustomize: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CORE CAPABILITIES",
                color = CielTextDim,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )
            Text(
                text = "Swipe to explore",
                color = CielAccentCyan,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HolographicFeatureCard(
                    title = "Explore AI Models",
                    subtitle = "Access the best models for your needs",
                    imageRes = R.drawable.graphic_ai_globe,
                    chips = listOf("GPT-4o", "Claude", "Llama", "Qwen"),
                    onClick = onExploreModels
                )
            }
            item {
                HolographicFeatureCard(
                    title = "Powerful Tools",
                    subtitle = "Boost your productivity",
                    imageRes = R.drawable.graphic_tools_cube,
                    chips = listOf("Web Search", "Image Gen", "Code", "OCR"),
                    onClick = onPowerfulTools
                )
            }
            item {
                HolographicFeatureCard(
                    title = "Customize Ciel",
                    subtitle = "Make it truly yours",
                    imageRes = R.drawable.graphic_customize_orb,
                    chips = listOf("Themes", "Voice", "Shortcuts", "Plugins"),
                    onClick = onCustomize
                )
            }
        }
    }
}

@Composable
fun HolographicFeatureCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    chips: List<String>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xCC090F1C),
        border = BorderStroke(
            1.dp,
            if (isPressed) CielAccentCyan else CielCardBorderDim
        ),
        modifier = Modifier
            .width(260.dp)
            .height(185.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 3D Holographic Graphic Top Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0x80090F1C),
                                    Color(0xCC090F1C)
                                )
                            )
                        )
                )
            }

            // Title, Subtitle, and Pill Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = CielAccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = subtitle,
                    color = CielTextSecondary,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Chips Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    chips.forEach { chip ->
                        Surface(
                            color = Color(0x99111C30),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.6.dp, CielNeonGlow.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = chip,
                                color = CielAccentCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Latest from Ciel Showcase Card with 3D Galaxy Graphic
 */
@Composable
fun LatestFromCielCard(onExploreNew: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xCC090F1C),
        border = BorderStroke(1.dp, CielCardBorderDim),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Galaxy Artwork Top Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.graphic_galaxy),
                    contentDescription = "Galaxy",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0x99090F1C),
                                    Color(0xCC090F1C)
                                )
                            )
                        )
                )

                // Top Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xB36366F1),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "AI",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Latest from Ciel",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Description and CTA Button
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(
                    text = "Smarter. Faster. More Human.",
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ciel now supports advanced reasoning, real-time web search and multi-modal understanding. Experience the next generation of AI companion.",
                    color = CielTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    onClick = onExploreNew,
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x666366F1),
                    border = BorderStroke(1.dp, CielNeonPurple.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "Explore What's New →",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * 4. System Telemetry Card
 */
@Composable
fun SystemTelemetryCard(uiState: CielUiState) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xCC090F1C),
        border = BorderStroke(1.dp, CielCardBorderDim),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SYSTEM STATUS",
                    color = CielAccentCyan,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(CielStatusGreen)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "All Systems Operational",
                        color = CielStatusGreen,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryPillItem("Core AI", "Online", Icons.Default.Hub)
                TelemetryPillItem("Memory", "${uiState.memories.size} Active", Icons.Default.Psychology)
                TelemetryPillItem("Tools", "Ready", Icons.Default.Build)
                TelemetryPillItem("RAM", "${uiState.telemetry.ramUsageMb} MB", Icons.Default.Memory)
            }
        }
    }
}

@Composable
fun TelemetryPillItem(label: String, value: String, icon: ImageVector) {
    Surface(
        color = Color(0xB30B1527),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.8.dp, CielCardBorderDim)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CielAccentCyan,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = label, color = CielTextSecondary, fontSize = 9.5.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 5. Quick Actions Section
 */
@Composable
fun QuickActionsSection(onSubmitPrompt: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "QUICK ACTIONS",
            color = CielTextDim,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionItem(
                title = "Image Analysis",
                icon = Icons.Default.Image,
                onClick = { onSubmitPrompt("Analyze this image and explain key details:") },
                modifier = Modifier.weight(1f)
            )
            QuickActionItem(
                title = "Document Reader",
                icon = Icons.Default.Description,
                onClick = { onSubmitPrompt("Summarize this document with key insights:") },
                modifier = Modifier.weight(1f)
            )
            QuickActionItem(
                title = "Code Help",
                icon = Icons.Default.Code,
                onClick = { onSubmitPrompt("Review and optimize this code snippet:") },
                modifier = Modifier.weight(1f)
            )
            QuickActionItem(
                title = "Web Search",
                icon = Icons.Default.Language,
                onClick = { onSubmitPrompt("Search the web for the latest updates on:") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xCC090F1C),
        border = BorderStroke(0.8.dp, CielCardBorderDim),
        modifier = modifier.height(72.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = CielAccentCyan,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = CielTextSecondary,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 6. Recent Activity Section matching reference mockup
 */
@Composable
fun RecentActivityCard(
    uiState: CielUiState,
    onOpenHistory: () -> Unit,
    onOpenChat: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xCC090F1C),
        border = BorderStroke(1.dp, CielCardBorderDim),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ACTIVITY",
                    color = CielAccentCyan,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )
                Text(
                    text = "View All →",
                    color = CielTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onOpenHistory)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Activity Item 1
            ActivityRowItem(
                title = "Image analysis completed",
                timestamp = "2m ago",
                avatarRes = R.drawable.ciel_avatar,
                onClick = onOpenChat
            )
            Divider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 6.dp))

            // Activity Item 2
            ActivityRowItem(
                title = "Research on AI models",
                timestamp = "12m ago",
                icon = Icons.Default.Search,
                onClick = onOpenChat
            )
            Divider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 6.dp))

            // Activity Item 3
            ActivityRowItem(
                title = "Code generation completed",
                timestamp = "18m ago",
                icon = Icons.Default.Code,
                onClick = onOpenChat
            )
        }
    }
}

@Composable
fun ActivityRowItem(
    title: String,
    timestamp: String,
    avatarRes: Int? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (avatarRes != null) {
                Image(
                    painter = painterResource(id = avatarRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.dp, CielAccentCyan, CircleShape)
                )
            } else if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0x336366F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = CielAccentCyan,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.90f),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = timestamp,
            color = CielTextDim,
            fontSize = 10.sp
        )
    }
}
