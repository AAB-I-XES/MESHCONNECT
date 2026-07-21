package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ContactEntity
import com.example.security.CryptoManager
import com.example.ui.MeshLinkViewModel
import com.example.ui.components.MeshStatusBadge
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.HighDensityPurple
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: MeshLinkViewModel,
    onBackClick: () -> Unit,
    onStartChat: (String) -> Unit
) {
    val contacts by viewModel.contacts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var manualMeshIdInput by remember { mutableStateOf("") }

    val user = currentUser ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                title = {
                    Text(
                        text = "Peer Discovery & Identity",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(padding)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp)
            ) {
            // Identity QR Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Your Decentralized Identity",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Scan QR to exchange encryption keys offline",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // QR Code Canvas Matrix Visualizer
                        QrCodeVisualizer(
                            payload = CryptoManager.generateQrPayload(user.meshId, user.publicKey, user.displayName),
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Mesh ID: ${user.meshId}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityPurple,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Manual Pairing Input
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = manualMeshIdInput,
                        onValueChange = { manualMeshIdInput = it },
                        placeholder = { Text("Enter Peer Mesh ID...", color = TextSecondary, fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = HighDensityPurple,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_mesh_id_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (manualMeshIdInput.isNotBlank()) {
                                val dummyQr = CryptoManager.generateQrPayload(manualMeshIdInput, "pub_manual", manualMeshIdInput)
                                viewModel.addContactFromQr(dummyQr)
                                manualMeshIdInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPurple, contentColor = Color.White),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("add_mesh_peer_button"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Peer", modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Discovered Contacts Header
            item {
                Text(
                    text = "Discovered Mesh Peers (${contacts.size})",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
            }

            // Peer Item Cards
            items(contacts, key = { it.meshId }) { contact ->
                HumanContactCardItem(
                    contact = contact,
                    onMessageClick = { onStartChat("conv_${contact.meshId}") }
                )
            }
        }
    }
}
}

@Composable
fun HumanContactCardItem(
    contact: ContactEntity,
    onMessageClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(HighDensityPurple, ElectricBlue))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.displayName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = contact.displayName,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                    Text(
                        text = contact.meshId,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    MeshStatusBadge(
                        transport = contact.transportType,
                        rssi = contact.rssi,
                        hopsAway = contact.hopsAway
                    )
                }
            }

            IconButton(
                onClick = onMessageClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(HighDensityPurple)
            ) {
                Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color.White)
            }
        }
    }
}

@Composable
fun QrCodeVisualizer(
    payload: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cellSize = size.width / 16f
        val hash = payload.hashCode()
        for (i in 0 until 16) {
            for (j in 0 until 16) {
                val isDark = ((i * 31 + j * 17 + hash) % 3) == 0 || (i in 0..3 && j in 0..3) || (i in 12..15 && j in 0..3) || (i in 0..3 && j in 12..15)
                if (isDark) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(i * cellSize, j * cellSize),
                        size = Size(cellSize - 1f, cellSize - 1f)
                    )
                }
            }
        }
    }
}
