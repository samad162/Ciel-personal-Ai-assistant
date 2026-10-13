package ai.ciel.assistant.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import java.io.File
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ciel.assistant.R
import ai.ciel.assistant.domain.model.AssistantState
import ai.ciel.assistant.domain.model.ChatMessage
import ai.ciel.assistant.domain.model.MessageSender
import ai.ciel.assistant.ui.CielUiState
import ai.ciel.assistant.ui.components.CielCommandBar
import ai.ciel.assistant.ui.theme.*

@Composable
fun ChatScreen(
    uiState: CielUiState,
    onInputChanged: (String) -> Unit,
    onSubmitPrompt: (String) -> Unit,
    onMicClicked: () -> Unit,
    onToggleWebSearch: () -> Unit,
    onStopSpeaking: () -> Unit,
    onClearChat: () -> Unit,
    onOpenSlideBar: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CielBgDark)
    ) {
        // Atmospheric Anime Companion Background
        Image(
            painter = painterResource(id = R.drawable.ciel_bg_cinematic),
            contentDescription = "CIEL Cyber Backdrop",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = when (uiState.characterStyle) {
                        "minimal" -> 0.10f
                        "ambient" -> 0.22f
                        else -> 0.32f
                    }
                }
        )

        // Layered Sci-Fi Dark Gradient for Reading Legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CielBgDark.copy(alpha = 0.90f),
                            CielBgDark.copy(alpha = 0.70f),
                            CielBgDark.copy(alpha = 0.88f),
                            CielBgDark.copy(alpha = 0.98f)
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Sleek Chat Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
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
                            contentDescription = "Menu",
                            tint = CielAccentCyan.copy(alpha = 0.85f),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Image(
                        painter = painterResource(id = R.drawable.ciel_avatar),
                        contentDescription = "CIEL Companion",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.2.dp, CielNeonGlow.copy(alpha = 0.8f), CircleShape)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CIEL",
                                color = CielTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(CielStatusGreen)
                            )
                        }
                        Text(
                            text = when (uiState.assistantState) {
                                AssistantState.LISTENING -> "Listening to your voice..."
                                AssistantState.PROCESSING -> "Synthesizing answer..."
                                AssistantState.SPEAKING -> "Speaking response..."
                                else -> "AI Companion Active"
                            },
                            color = CielAccentCyan,
                            fontSize = 10.5.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.assistantState == AssistantState.SPEAKING) {
                        IconButton(
                            onClick = onStopSpeaking,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x33EF4444))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Speaking",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Surface(
                        color = CielChipBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.8.dp, CielCardBorderDim)
                    ) {
                        Text(
                            text = uiState.activeModelId.uppercase(),
                            color = CielTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = CielCardBorderDim, thickness = 1.dp)

            // Chat Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
            ) {
                if (uiState.messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = Color(0xD90A1324),
                                border = BorderStroke(
                                    1.2.dp,
                                    Brush.horizontalGradient(
                                        listOf(CielNeonGlow.copy(alpha = 0.5f), CielNeonPurple.copy(alpha = 0.4f))
                                    )
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ciel_avatar),
                                        contentDescription = "CIEL",
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, CielNeonGlow, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "CIEL INTELLIGENCE",
                                        color = CielTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Your world, amplified. Ask complex questions, write code, run autonomous device actions, or brainstorm ideas.",
                                        color = CielTextSecondary,
                                        fontSize = 12.5.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(uiState.messages, key = { it.id }) { message ->
                        EditorialMessageBubble(
                            message = message,
                            density = uiState.chatDensity,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(message.content))
                            }
                        )
                    }
                }
            }
        }

        // Floating Bottom Command Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            CielCommandBar(
                inputText = uiState.inputText,
                isWebSearchEnabled = uiState.isWebSearchEnabled,
                assistantState = uiState.assistantState,
                rmsLevel = uiState.audioRmsLevel,
                surfaceAlpha = uiState.commandBarAlpha,
                showActionChips = false,
                onInputChanged = onInputChanged,
                onSubmit = { onSubmitPrompt(uiState.inputText) },
                onMicClicked = onMicClicked,
                onToggleWebSearch = onToggleWebSearch,
                onAttachmentClicked = {
                    onInputChanged("Analyze attached document contents: ")
                }
            )
        }
    }
}

@Composable
fun EditorialMessageBubble(
    message: ChatMessage,
    density: String,
    onCopy: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Author Label & Metadata
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            if (!isUser) {
                Image(
                    painter = painterResource(id = R.drawable.ciel_avatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = if (isUser) "YOU" else "CIEL",
                color = if (isUser) CielAccentCyan else CielNeonGlow,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            if (message.isOfflineResponse) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "[Offline Core]",
                    color = CielWarningYellow,
                    fontSize = 9.5.sp
                )
            }
            if (message.isStreaming) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• Generating...",
                    color = CielAccentIceBlue,
                    fontSize = 9.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Message Surface
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) Color(0xD910243E) else Color(0xEB0A1322),
            border = BorderStroke(
                1.dp,
                if (isUser) CielAccentCyan.copy(alpha = 0.5f) else CielCardBorderDim
            ),
            modifier = Modifier.widthIn(max = 330.dp)
        ) {
            Column(
                modifier = Modifier.padding(
                    if (density == "compact") 10.dp else 14.dp
                )
            ) {
                // If this message executed an autonomous tool
                if (message.executedTool != null) {
                    Surface(
                        color = Color(0x3300F0FF),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CielAccentCyan.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = CielNeonGlow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Action: ${message.executedTool}",
                                color = CielAccentCyan,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Render structured message content (Code blocks or paragraphs)
                StructuredMessageContent(
                    content = message.content,
                    onCopyCode = { codeSnippet ->
                        clipboardManager.setText(AnnotatedString(codeSnippet))
                    }
                )

                // Generated File Card (PDF, CSV, MD, TXT, DOCX)
                if (message.generatedFileUri != null) {
                    GeneratedFileAttachmentView(message = message)
                }

                // Generated Image Card
                if (message.generatedImageUrl != null) {
                    GeneratedImageView(message = message)
                }

                // Footer Actions (Copy Button)
                if (!isUser && message.content.isNotBlank() && !message.isStreaming) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = CielTextSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StructuredMessageContent(
    content: String,
    onCopyCode: (String) -> Unit
) {
    val blocks = remember(content) { parseContentIntoBlocks(content) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is ContentBlock.Code -> {
                    CodeBlockCard(
                        code = block.code,
                        language = block.language,
                        onCopy = { onCopyCode(block.code) }
                    )
                }
                is ContentBlock.Text -> {
                    Text(
                        text = formatMarkdownAnnotated(block.text),
                        color = CielTextPrimary,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

sealed class ContentBlock {
    data class Text(val text: String) : ContentBlock()
    data class Code(val code: String, val language: String) : ContentBlock()
}

fun parseContentIntoBlocks(text: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val parts = text.split("```")

    for (i in parts.indices) {
        val segment = parts[i]
        if (segment.isBlank()) continue

        if (i % 2 == 1) {
            // Code block
            val lines = segment.trim().lines()
            val firstLine = lines.firstOrNull()?.trim() ?: ""
            val hasLang = firstLine.matches(Regex("^[a-zA-Z0-9#+_-]+$"))
            val lang = if (hasLang) firstLine else "CODE"
            val codeBody = if (hasLang) lines.drop(1).joinToString("\n") else segment.trim()
            blocks.add(ContentBlock.Code(codeBody, lang.uppercase()))
        } else {
            blocks.add(ContentBlock.Text(segment.trim()))
        }
    }

    if (blocks.isEmpty() && text.isNotBlank()) {
        blocks.add(ContentBlock.Text(text))
    }
    return blocks
}

@Composable
fun CodeBlockCard(
    code: String,
    language: String,
    onCopy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF030712),
        border = BorderStroke(1.dp, CielAccentCyan.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Code Block Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF081224))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language,
                    color = CielAccentCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier.clickable(onClick = onCopy),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = CielTextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Copy",
                        color = CielTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // Code Content
            Text(
                text = code,
                color = Color(0xFF7DD3FC),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .horizontalScroll(rememberScrollState())
            )
        }
    }
}

fun formatMarkdownAnnotated(raw: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val regex = Regex("""\*\*(.*?)\*\*|`([^`]+)`""")
        val matches = regex.findAll(raw)

        for (match in matches) {
            if (match.range.first > cursor) {
                append(raw.substring(cursor, match.range.first))
            }
            val boldText = match.groups[1]?.value
            val codeText = match.groups[2]?.value

            if (boldText != null) {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White))
                append(boldText)
                pop()
            } else if (codeText != null) {
                pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, color = CielAccentCyan))
                append(codeText)
                pop()
            }
            cursor = match.range.last + 1
        }
        if (cursor < raw.length) {
            append(raw.substring(cursor))
        }
    }
}

@Composable
fun GeneratedFileAttachmentView(message: ChatMessage) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0x3300F0FF),
        border = BorderStroke(1.dp, CielAccentCyan.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (message.generatedFileType) {
                        "PDF" -> Color(0xFFEF4444)
                        "CSV" -> Color(0xFF10B981)
                        "MARKDOWN" -> Color(0xFF38BDF8)
                        else -> CielAccentCyan
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = message.generatedFileType ?: "FILE",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = message.generatedFileName ?: "Generated Document",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    if (message.generatedFileSize != null) {
                        Text(
                            text = message.generatedFileSize,
                            color = CielTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        try {
                            val uri = Uri.parse(message.generatedFileUri)
                            val mime = when (message.generatedFileType) {
                                "PDF" -> "application/pdf"
                                "CSV" -> "text/csv"
                                "MARKDOWN" -> "text/markdown"
                                "DOCX" -> "application/msword"
                                else -> "text/plain"
                            }
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, mime)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CielAccentCyan),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Open", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = {
                        try {
                            val uri = Uri.parse(message.generatedFileUri)
                            val mime = when (message.generatedFileType) {
                                "PDF" -> "application/pdf"
                                "CSV" -> "text/csv"
                                "MARKDOWN" -> "text/markdown"
                                "DOCX" -> "application/msword"
                                else -> "text/plain"
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = mime
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            val chooser = Intent.createChooser(intent, "Share Document via").apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(chooser)
                        } catch (e: Exception) {}
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CielAccentCyan.copy(alpha = 0.7f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Share", color = CielAccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GeneratedImageView(message: ChatMessage) {
    val context = LocalContext.current
    val imagePath = message.generatedImageUrl ?: return
    val bitmap = remember(imagePath) {
        try {
            val file = File(imagePath)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        } catch (e: Exception) { null }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0x3300F0FF),
        border = BorderStroke(1.dp, CielAccentCyan.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Generated Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Text(
                    text = "Image saved: $imagePath",
                    color = CielAccentCyan,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        try {
                            val file = File(imagePath)
                            if (file.exists()) {
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context, "ai.ciel.assistant.fileprovider", file
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "image/*")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        } catch (e: Exception) {}
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CielAccentCyan),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("View", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = {
                        try {
                            val file = File(imagePath)
                            if (file.exists()) {
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context, "ai.ciel.assistant.fileprovider", file
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/jpeg"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                val chooser = Intent.createChooser(intent, "Share Image via").apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(chooser)
                            }
                        } catch (e: Exception) {}
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CielAccentCyan.copy(alpha = 0.7f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Share", color = CielAccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
