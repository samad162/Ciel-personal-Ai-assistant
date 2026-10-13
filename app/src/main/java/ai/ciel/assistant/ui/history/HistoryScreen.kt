package ai.ciel.assistant.ui.history

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ciel.assistant.R
import ai.ciel.assistant.domain.model.ChatMessage
import ai.ciel.assistant.domain.model.MessageSender
import ai.ciel.assistant.ui.CielUiState
import ai.ciel.assistant.ui.theme.*

data class HistoryItemView(
    val id: String,
    val title: String,
    val preview: String,
    val category: String,
    val icon: ImageVector,
    val timeAgo: String,
    val modelName: String,
    val userMessage: ChatMessage
)

@Composable
fun HistoryScreen(
    uiState: CielUiState,
    onNavigateToChat: () -> Unit,
    onClearHistory: () -> Unit,
    onOpenSlideBar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showClearDialog by remember { mutableStateOf(false) }

    // Transform chat messages into concise high-level conversation topics
    val conversationEntries = remember(uiState.messages) {
        val list = mutableListOf<HistoryItemView>()
        val msgs = uiState.messages
        for (i in msgs.indices) {
            if (msgs[i].sender == MessageSender.USER) {
                val nextAssistant = msgs.drop(i + 1).firstOrNull { it.sender == MessageSender.ASSISTANT }
                val prompt = msgs[i].content

                val category = when {
                    prompt.contains("code", ignoreCase = true) || prompt.contains("kotlin", ignoreCase = true) || prompt.contains("function", ignoreCase = true) -> "Coding"
                    prompt.contains("research", ignoreCase = true) || prompt.contains("search", ignoreCase = true) || prompt.contains("neural", ignoreCase = true) -> "Research"
                    prompt.contains("torch", ignoreCase = true) || prompt.contains("battery", ignoreCase = true) || prompt.contains("volume", ignoreCase = true) || prompt.contains("location", ignoreCase = true) -> "Tools"
                    prompt.contains("remember", ignoreCase = true) -> "Memory"
                    else -> "General"
                }

                val icon = when (category) {
                    "Coding" -> Icons.Default.Code
                    "Research" -> Icons.Default.Psychology
                    "Tools" -> Icons.Default.Build
                    "Memory" -> Icons.Default.Bookmark
                    else -> Icons.AutoMirrored.Filled.Chat
                }

                val preview = nextAssistant?.content?.take(90)?.replace("\n", " ") ?: "Conversation session with CIEL"

                list.add(
                    HistoryItemView(
                        id = msgs[i].id,
                        title = prompt.take(45),
                        preview = if (preview.length >= 90) "$preview..." else preview,
                        category = category,
                        icon = icon,
                        timeAgo = formatTimeAgo(msgs[i].timestamp),
                        modelName = if (nextAssistant?.isOfflineResponse == true) "On-Device" else "GPT-4o",
                        userMessage = msgs[i]
                    )
                )
            }
        }
        list.reversed()
    }

    // Filter by search query and category
    val filteredEntries = conversationEntries.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.preview.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

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
                            text = "HISTORY",
                            color = CielTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Past Conversations & Insights",
                            color = CielAccentCyan,
                            fontSize = 10.sp
                        )
                    }
                }

                if (conversationEntries.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CielChipBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear History",
                            tint = CielTextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = CielCardBorderDim, thickness = 1.dp)

            // Search Bar & Filter Chips
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                // Search Input Box
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CielCardGlassSubtle,
                    border = BorderStroke(1.dp, CielCardBorderDim),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = CielTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(color = CielTextPrimary, fontSize = 13.5.sp),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search conversations...",
                                        color = CielTextDim,
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = CielTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                val categories = listOf("All", "Research", "Coding", "Tools", "General")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            onClick = { selectedCategory = cat },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CielAccentBlue.copy(alpha = 0.3f) else CielCardGlassSubtle,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) CielNeonGlow.copy(alpha = 0.6f) else CielCardBorderDim
                            )
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) CielNeonGlow else CielTextSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // List of Concise Conversation Cards
            if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(CielCardGlassSubtle)
                            .border(1.dp, CielCardBorderDim, RoundedCornerShape(20.dp))
                            .padding(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = CielNeonGlow.copy(alpha = 0.6f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No Matching Records" else "No Conversation History",
                            color = CielTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Try refining your search terms." else "Your conversations, deep thinking queries, and autonomous actions will appear here.",
                            color = CielTextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToChat,
                                colors = ButtonDefaults.buttonColors(containerColor = CielAccentBlue),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CielNeonGlow.copy(alpha = 0.6f))
                            ) {
                                Text(text = "Start New Conversation", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredEntries, key = { it.id }) { item ->
                        HistoryTopicCard(
                            item = item,
                            onClick = onNavigateToChat
                        )
                    }
                }
            }
        }

        // Confirmation Dialog for Cleared History
        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text(text = "Clear History?", color = CielTextPrimary) },
                text = {
                    Text(
                        text = "Are you sure you want to clear your conversation history? This cannot be undone.",
                        color = CielTextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onClearHistory()
                            showClearDialog = false
                            Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
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
fun HistoryTopicCard(
    item: HistoryItemView,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CielChipBg)
                        .border(1.dp, CielCardBorderDim, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.category,
                        tint = CielAccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            color = CielTextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = item.preview,
                        color = CielTextSecondary,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.timeAgo,
                            color = CielTextDim,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${item.modelName}",
                            color = CielAccentCyan.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open in Chat",
                tint = CielTextDim,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> "Earlier"
    }
}
