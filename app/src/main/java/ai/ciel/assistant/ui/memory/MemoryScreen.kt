package ai.ciel.assistant.ui.memory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ciel.assistant.R
import ai.ciel.assistant.domain.model.MemoryItem
import ai.ciel.assistant.ui.CielUiState
import ai.ciel.assistant.ui.theme.*

@Composable
fun MemoryScreen(
    uiState: CielUiState,
    onAddMemory: (String, String) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onOpenSlideBar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var keyText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CielBgDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onOpenSlideBar)
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
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Memory Bank",
                            color = CielTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Long-Term Neural Context",
                            color = CielAccentCyan,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(
                    onClick = { showDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = CielCardGlass)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Memory",
                        tint = CielAccentCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                if (uiState.memories.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CielCardGlass),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No saved memories. CIEL learns facts you share or that you add explicitly.",
                                color = CielTextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(uiState.memories) { mem ->
                        MemoryCard(mem = mem, onDelete = { onDeleteMemory(mem.id) })
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Add Custom Memory", color = CielTextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = keyText,
                            onValueChange = { keyText = it },
                            label = { Text("Topic / Key (e.g. Favorite Language)") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = contentText,
                            onValueChange = { contentText = it },
                            label = { Text("Details / Fact (e.g. Kotlin & Rust)") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (keyText.isNotBlank() && contentText.isNotBlank()) {
                            onAddMemory(keyText, contentText)
                            keyText = ""
                            contentText = ""
                            showDialog = false
                        }
                    }) {
                        Text("Save", color = CielAccentCyan)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel", color = CielTextSecondary)
                    }
                },
                containerColor = CielSurfaceDark
            )
        }
    }
}

@Composable
fun MemoryCard(mem: MemoryItem, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CielCardGlass),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mem.key,
                    color = CielAccentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = mem.content,
                    color = CielTextPrimary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Memory",
                    tint = CielTextSecondary.copy(alpha = 0.6f)
                )
            }
        }
    }
}
