package ai.ciel.assistant.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ciel.assistant.R
import ai.ciel.assistant.ui.CielUiState
import ai.ciel.assistant.ui.theme.*

data class DrawerGroup(
    val title: String,
    val items: List<DrawerItem>
)

data class DrawerItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val badge: String? = null
)

@Composable
fun CielSlideBar(
    uiState: CielUiState,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val menuGroups = listOf(
        DrawerGroup(
            title = "PERSONAL SPACE",
            items = listOf(
                DrawerItem("home", "Home", Icons.Default.Home),
                DrawerItem("chat", "Chat", Icons.AutoMirrored.Filled.Chat),
                DrawerItem("history", "History", Icons.Default.History)
            )
        ),
        DrawerGroup(
            title = "INTELLIGENCE",
            items = listOf(
                DrawerItem("models", "AI Models", Icons.Default.Hub, "GPT-4o"),
                DrawerItem("tools", "Tools", Icons.Default.Widgets, "Ready"),
                DrawerItem("memory", "Memory Vault", Icons.Default.Psychology)
            )
        ),
        DrawerGroup(
            title = "PREFERENCES",
            items = listOf(
                DrawerItem("customize", "Customize Ciel", Icons.Default.Palette),
                DrawerItem("settings", "Settings", Icons.Default.Settings)
            )
        )
    )

    Surface(
        color = Color(0xF7060B16),
        modifier = modifier
            .fillMaxHeight()
            .width(285.dp)
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        CielNeonGlow.copy(alpha = 0.5f),
                        CielAccentCyan.copy(alpha = 0.3f),
                        CielNeonPurple.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            )
            .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper Section: Header Brand + Menu Groups
            Column {
                // Header: CIEL Identity Mark + Dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ciel_star_logo),
                            contentDescription = "CIEL Logo",
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CIEL",
                                color = CielTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "Your AI Companion",
                                color = CielAccentCyan,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onClose),
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

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = CielCardBorderDim, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Organized Menu Groups
                menuGroups.forEach { group ->
                    Text(
                        text = group.title,
                        color = CielTextDim,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        modifier = Modifier.padding(start = 6.dp, bottom = 6.dp, top = 8.dp)
                    )

                    group.items.forEach { item ->
                        val isSelected = uiState.currentTab == item.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onNavigate(item.id)
                                    onClose()
                                },
                            color = if (isSelected) CielAccentBlue.copy(alpha = 0.28f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) CielNeonGlow.copy(alpha = 0.55f) else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) CielNeonGlow else CielTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.title,
                                        color = if (isSelected) CielTextPrimary else CielTextSecondary,
                                        fontSize = 13.5.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }

                                if (item.badge != null) {
                                    Surface(
                                        color = CielChipBg,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.8.dp, CielCardBorderDim)
                                    ) {
                                        Text(
                                            text = item.badge,
                                            color = CielAccentCyan,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Lower Section: User Profile & AI Credits (matching reference design)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                HorizontalDivider(color = CielCardBorderDim, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Profile Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CielCardGlassSubtle,
                    border = BorderStroke(1.dp, CielCardBorderDim),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.user_samad_avatar),
                                contentDescription = "User Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, CielAccentCyan.copy(alpha = 0.6f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.userName,
                                    color = CielTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Free Plan",
                                    color = CielAccentCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = CielTextSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // AI Credits Gauge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "AI Credits",
                                color = CielTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "2,487 / 5,000 (49%)",
                                color = CielNeonGlow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        // Gradient Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF132035))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.49f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(CielNeonPurple, CielAccentCyan, CielNeonGlow)
                                        )
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Brand quote
                Text(
                    text = "More than just an AI...\nCiel thinks with you.",
                    color = CielTextSecondary.copy(alpha = 0.7f),
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Designed for dreamers, built for doers.",
                    color = CielTextDim,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
