package ai.ciel.assistant.ui.tools

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ciel.assistant.R
import ai.ciel.assistant.ui.CielUiState
import ai.ciel.assistant.ui.theme.*

data class ToolItemData(
    val title: String,
    val description: String,
    val command: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun ToolsScreen(
    uiState: CielUiState,
    onExecuteTool: (String) -> Unit,
    onOpenSlideBar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var lastExecutedTool by remember { mutableStateOf<String?>(null) }
    var executionStatusText by remember { mutableStateOf("Select a tool above to test real hardware and system dispatch.") }

    val toolsList = listOf(
        ToolItemData(
            title = "Device Diagnostics",
            description = "Check real available storage, RAM usage, and battery health",
            command = "device diagnostics and status",
            icon = Icons.Default.Analytics,
            accentColor = CielAccentCyan
        ),
        ToolItemData(
            title = "Current Location",
            description = "Retrieve live GPS coordinates and local address with Geocoder",
            command = "where am i right now",
            icon = Icons.Default.LocationOn,
            accentColor = CielStatusGreen
        ),
        ToolItemData(
            title = "Flashlight Toggle",
            description = "Toggle hardware camera torch on or off instantly",
            command = "turn on torch",
            icon = Icons.Default.FlashlightOn,
            accentColor = CielWarningYellow
        ),
        ToolItemData(
            title = "Volume & Sound",
            description = "Adjust Android media stream volume or mute/unmute",
            command = "volume up",
            icon = Icons.Default.VolumeUp,
            accentColor = CielAccentIceBlue
        ),
        ToolItemData(
            title = "Open Camera",
            description = "Launch camera app for visual capture and photography",
            command = "open camera",
            icon = Icons.Default.CameraAlt,
            accentColor = CielNeonPurple
        ),
        ToolItemData(
            title = "Set 5m Timer",
            description = "Configure device countdown timer with alarm manager",
            command = "set timer 5 minutes",
            icon = Icons.Default.Timer,
            accentColor = CielNeonPink
        ),
        ToolItemData(
            title = "Web Intelligence",
            description = "Conduct live web search across current online sources",
            command = "search latest breakthroughs in AI",
            icon = Icons.Default.Public,
            accentColor = CielNeonGlow
        ),
        ToolItemData(
            title = "Launch Application",
            description = "Dynamic scanner searches and opens installed Android apps",
            command = "open youtube",
            icon = Icons.Default.Apps,
            accentColor = CielElectricViolet
        )
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
                            text = "POWERFUL TOOLS",
                            color = CielTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Autonomous System Skills",
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
                            text = "Dispatcher Ready",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live Execution Result Card
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xCC0D1B2D),
                        border = BorderStroke(
                            1.2.dp,
                            Brush.horizontalGradient(
                                listOf(CielNeonGlow.copy(alpha = 0.6f), CielAccentCyan.copy(alpha = 0.4f))
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = CielNeonGlow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIVE DISPATCH MONITOR",
                                        color = CielAccentCyan,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    )
                                }
                                if (lastExecutedTool != null) {
                                    Text(
                                        text = "Triggered: $lastExecutedTool",
                                        color = CielTextDim,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = executionStatusText,
                                color = CielTextPrimary,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Grid of Tools
                items(toolsList) { tool ->
                    Surface(
                        onClick = {
                            lastExecutedTool = tool.title
                            executionStatusText = "Executing: '${tool.command}'..."
                            onExecuteTool(tool.command)
                            executionStatusText = "Action dispatched: '${tool.command}'. Check chat history or device status for full feedback."
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = CielCardGlassSubtle,
                        border = BorderStroke(1.dp, CielCardBorderDim),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(tool.accentColor.copy(alpha = 0.15f))
                                        .border(1.dp, tool.accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tool.icon,
                                        contentDescription = tool.title,
                                        tint = tool.accentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = tool.title,
                                        color = CielTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = tool.description,
                                        color = CielTextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                color = CielChipBg,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.8.dp, tool.accentColor.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "Run",
                                    color = tool.accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
