package ai.ciel.assistant.ui.models

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ciel.assistant.R
import ai.ciel.assistant.domain.model.AiModelInfo
import ai.ciel.assistant.ui.CielUiState
import ai.ciel.assistant.ui.theme.*

@Composable
fun AiModelsScreen(
    uiState: CielUiState,
    availableModels: List<AiModelInfo>,
    onSelectModel: (String) -> Unit,
    onNavigateToChat: () -> Unit,
    onOpenSlideBar: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                            text = "AI MODELS",
                            color = CielTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Universal Intelligence Grid",
                            color = CielAccentCyan,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    color = CielChipBg,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CielCardBorderDim)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(CielStatusGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "6 Online",
                            color = CielTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(color = CielCardBorderDim, thickness = 1.dp)

            // Content List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    // Hero Intro Banner
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CielCardGlass,
                        border = BorderStroke(
                            1.2.dp,
                            Brush.horizontalGradient(
                                listOf(CielNeonGlow.copy(alpha = 0.5f), CielNeonPurple.copy(alpha = 0.4f))
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CielNeonGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SELECT YOUR ENGINE",
                                    color = CielAccentCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "CIEL seamlessly routes queries between top frontier cloud models and high-performance offline on-device neural weights.",
                                color = CielTextSecondary,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                items(availableModels) { model ->
                    val isSelected = uiState.activeModelId == model.id
                    AiModelCard(
                        model = model,
                        isSelected = isSelected,
                        onSelect = { onSelectModel(model.id) },
                        onTestInChat = {
                            onSelectModel(model.id)
                            onNavigateToChat()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AiModelCard(
    model: AiModelInfo,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onTestInChat: () -> Unit
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) Color(0xD90E1A30) else CielCardGlassSubtle,
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) Brush.horizontalGradient(listOf(CielNeonGlow, CielNeonPurple)) else Brush.linearGradient(listOf(CielCardBorderDim, CielCardBorderDim))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) CielNeonGlow else Color.Transparent)
                            .border(1.5.dp, if (isSelected) CielNeonGlow else CielTextDim, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = model.name,
                            color = if (isSelected) CielTextPrimary else CielTextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = model.provider,
                            color = CielTextDim,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    color = if (isSelected) CielAccentCyan.copy(alpha = 0.2f) else CielChipBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, if (isSelected) CielAccentCyan else CielCardBorderDim)
                ) {
                    Text(
                        text = model.badge,
                        color = if (isSelected) CielNeonGlow else CielTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = model.description,
                color = CielTextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Specs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ModelStatPill("Context", model.contextWindow)
                ModelStatPill("Latency", model.latency)
                ModelStatPill("Benchmark", model.benchmarkScore)
            }

            if (isSelected) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onTestInChat,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CielAccentBlue.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CielNeonGlow.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Test In Chat",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModelStatPill(label: String, value: String) {
    Column {
        Text(text = label, color = CielTextDim, fontSize = 9.5.sp)
        Text(text = value, color = CielTextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
    }
}
