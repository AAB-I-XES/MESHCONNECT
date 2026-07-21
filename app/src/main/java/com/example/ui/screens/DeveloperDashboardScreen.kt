package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.PacketLogEntity
import com.example.mesh.PacketPayloadType
import com.example.ui.MeshLinkViewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperDashboardScreen(
    viewModel: MeshLinkViewModel,
    onBackClick: () -> Unit
) {
    val packetLogs by viewModel.packetLogs.collectAsState()
    val metrics by viewModel.meshRoutingEngine.networkMetrics.collectAsState()
    val routes by viewModel.routes.collectAsState()

    var testTargetMeshId by remember { mutableStateOf("mesh_gamma") }

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
                        Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = NeonAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mesh Diagnostic Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBackground)
            )
        },
        containerColor = NavyBackground
    ) { padding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Live Radio & System Metrics
            item {
                GlassCard(
                    hasAccentGlow = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Radio & System State",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatusItem("BLE Scanner", "ACTIVE", EmeraldGreen)
                            StatusItem("Wi-Fi Direct", "BOUND", EmeraldGreen)
                            StatusItem("RAM Usage", "42 MB", CyberCyan)
                            StatusItem("Battery Drain", "1.2 %/hr", NeonAmber)
                        }
                    }
                }
            }

            // Packet Injector Test Bench
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Packet Injector (Multi-hop Route Test)",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = testTargetMeshId,
                                onValueChange = { testTargetMeshId = it },
                                label = { Text("Target Mesh ID", color = TextSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("packet_target_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.injectDiagnosticPacket(testTargetMeshId, PacketPayloadType.PING_HEARTBEAT)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = NavyBackground),
                                modifier = Modifier.testTag("inject_packet_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Inject")
                            }
                        }
                    }
                }
            }

            // Active Routing Table Inspector
            item {
                Text(
                    text = "AODV Routing Table (${routes.size} routes)",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
            }

            items(routes, key = { it.destinationMeshId }) { route ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Dest: ${route.destinationMeshId}",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = CyberCyan,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Next Hop: ${route.nextHopMeshId} • ${route.transportType}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${route.hopCount} Hops",
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${route.latencyMs} ms",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Live Packet Log Stream
            item {
                Text(
                    text = "Live Packet Audit Stream (${packetLogs.size} logged)",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
            }

            items(packetLogs, key = { it.id }) { log ->
                PacketLogRow(log)
            }
        }
    }
}

@Composable
fun StatusItem(title: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column {
        Text(title, fontSize = 10.sp, color = TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun PacketLogRow(log: PacketLogEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "${log.packetId} • ${log.payloadType}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    fontSize = 12.sp
                )
                Text(
                    text = "Path: ${log.routePath}",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = log.status,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (log.status == "DELIVERED") EmeraldGreen else NeonAmber
            )
        }
    }
}
