package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.MessageEntity
import com.example.ui.MeshLinkViewModel
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.HighDensityContainer
import com.example.ui.theme.HighDensityPurple
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    viewModel: MeshLinkViewModel,
    onBackClick: () -> Unit,
    onStartCall: (String, String, Boolean) -> Unit
) {
    val conversations by viewModel.conversations.collectAsState()
    val messages by viewModel.currentMessages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val conversation = conversations.find { it.conversationId == conversationId }
    val peerMeshId = conversation?.participantMeshIds?.split(",")?.firstOrNull() ?: "mesh_peer"
    val peerTitle = conversation?.title ?: "Mesh Node"

    var textInput by remember { mutableStateOf("") }
    var selectedDisappearingSec by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var selectedMessageForReaction by remember { mutableStateOf<MessageEntity?>(null) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(HighDensityPurple, ElectricBlue))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = peerTitle.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 17.sp
                                )
                            }
                            // Online Indicator Dot
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(NavyBackground)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(EmeraldGreen)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = peerTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Encrypted",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = "Direct Mesh • Active",
                                fontSize = 11.sp,
                                color = EmeraldGreen
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onStartCall(peerMeshId, peerTitle, false) },
                        modifier = Modifier.testTag("voice_call_button")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = TextPrimary)
                    }
                    IconButton(
                        onClick = { onStartCall(peerMeshId, peerTitle, true) },
                        modifier = Modifier.testTag("walkie_talkie_button")
                    ) {
                        Icon(Icons.Default.Radio, contentDescription = "Push To Talk", tint = HighDensityContainer)
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = TextPrimary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Disappearing Messages (30s)") },
                            onClick = {
                                selectedDisappearingSec = if (selectedDisappearingSec == 0) 30 else 0
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Messages") },
                            onClick = {
                                viewModel.clearConversation(conversationId)
                                showMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBackground)
            )
        },
        containerColor = NavyBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(padding)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
            // Disappearing Messages Banner
            if (selectedDisappearingSec > 0) {
                Surface(
                    color = NeonAmber.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Disappearing Messages Active: Expire in ${selectedDisappearingSec}s",
                            color = NeonAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Message History List
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(messages, key = { it.messageId }) { msg ->
                    val isMe = msg.senderMeshId == currentUser?.meshId
                    HumanMessageBubble(
                        message = msg,
                        isMe = isMe,
                        onLongClick = { selectedMessageForReaction = msg },
                        onTogglePin = { viewModel.togglePinMessage(msg.messageId, msg.isPinned) },
                        onDelete = { viewModel.deleteMessage(msg.messageId) }
                    )
                }
            }

            // Emoji Reaction Bar overlay
            AnimatedVisibility(visible = selectedMessageForReaction != null) {
                val activeMsg = selectedMessageForReaction
                if (activeMsg != null) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated)
                            .padding(vertical = 10.dp)
                    ) {
                        listOf("❤️", "👍", "🔥", "😊", "🛡️").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 26.sp,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.addReaction(activeMsg.messageId, emoji)
                                        selectedMessageForReaction = null
                                    }
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            // Attachment Options Drawer
            AnimatedVisibility(visible = showAttachmentMenu) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(vertical = 14.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            viewModel.sendFile(conversationId, peerMeshId, "Shared_Document.pdf", 2.4)
                            showAttachmentMenu = false
                        }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HighDensityPurple.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = HighDensityPurple)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Document", fontSize = 12.sp, color = TextPrimary)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            viewModel.sendVoiceNote(conversationId, peerMeshId, 12)
                            showAttachmentMenu = false
                        }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ElectricBlue.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = ElectricBlue)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Voice Note", fontSize = 12.sp, color = TextPrimary)
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                color = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    IconButton(onClick = { showAttachmentMenu = !showAttachmentMenu }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attach",
                            tint = TextSecondary
                        )
                    }

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Message...", color = TextSecondary, fontSize = 14.sp) },
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedBorderColor = HighDensityPurple,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .testTag("chat_input_field")
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendTextMessage(
                                    conversationId = conversationId,
                                    text = textInput,
                                    recipientMeshId = peerMeshId,
                                    expirySec = selectedDisappearingSec
                                )
                                textInput = ""
                            } else {
                                viewModel.sendVoiceNote(conversationId, peerMeshId, 5)
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(HighDensityPurple)
                            .testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = if (textInput.isBlank()) Icons.Default.Mic else Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun HumanMessageBubble(
    message: MessageEntity,
    isMe: Boolean,
    onLongClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    val bubbleColor = if (isMe) HighDensityPurple else DarkSurfaceElevated

    Column(
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = bubbleShape,
            color = bubbleColor,
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongClick() }
                    )
                }
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (message.isPinned) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Pin,
                            contentDescription = "Pinned",
                            tint = if (isMe) Color.White.copy(alpha = 0.8f) else HighDensityContainer,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pinned",
                            fontSize = 11.sp,
                            color = if (isMe) Color.White.copy(alpha = 0.8f) else HighDensityContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                when (message.messageType) {
                    "VOICE" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (isMe) Color.White else HighDensityContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Voice Message (${message.voiceDurationSec}s)",
                                color = if (isMe) Color.White else TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                    "FILE" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = if (isMe) Color.White else HighDensityContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = message.mediaName ?: "Attachment",
                                    color = if (isMe) Color.White else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${message.mediaSize / (1024 * 1024)} MB",
                                    color = if (isMe) Color.White.copy(alpha = 0.7f) else TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = message.content,
                            color = if (isMe) Color.White else TextPrimary,
                            fontSize = 15.sp
                        )
                    }
                }

                if (message.reaction.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = message.reaction, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = formatClockTime(message.timestamp),
                        fontSize = 10.sp,
                        color = if (isMe) Color.White.copy(alpha = 0.7f) else TextSecondary
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        val statusIcon = when (message.status) {
                            "SENDING" -> Icons.Default.Schedule
                            "DELIVERED" -> Icons.Default.Check
                            else -> Icons.Default.DoneAll
                        }
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = message.status,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

fun formatClockTime(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}
