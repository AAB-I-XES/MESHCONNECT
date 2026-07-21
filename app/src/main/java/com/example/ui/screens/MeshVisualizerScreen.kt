package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mesh.NodeInfo
import com.example.mesh.RouteStrategy
import com.example.mesh.TransportType
import com.example.ui.MeshLinkViewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeshVisualizerScreen(
    viewModel: MeshLinkViewModel,
    onBackClick: () -> Unit
) {
    val activeNodes by viewModel.meshRoutingEngine.activeNodes.collectAsState()
    val metrics by viewModel.meshRoutingEngine.networkMetrics.collectAsState()
    val strategy by viewModel.meshRoutingEngine.routeStrategy.collectAsState()

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "Mesh Network Graph",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Live Peer Topology & Dynamic Routing",
                            fontSize = 11.sp,
                            color = CyberCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBackground)
            )
        },
        containerColor = NavyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Strategy Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                RouteStrategy.entries.forEach { s ->
                    val isSelected = strategy == s
                    val label = when (s) {
                        RouteStrategy.SHORTEST_PATH -> "Shortest Path"
                        RouteStrategy.MIN_LATENCY -> "Min Latency"
                        RouteStrategy.BATTERY_SAVER -> "Battery Saver"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateRouteStrategy(s) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan,
                            selectedLabelColor = NavyBackground,
                            containerColor = DarkSurface,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("strategy_chip_${s.name}")
                    )
                }
            }

            // Interactive Node Graph Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(NavyBackground, RoundedCornerShape(16.dp))
                    .testTag("mesh_canvas_view")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Concentric Range Rings
                    listOf(0.25f, 0.50f, 0.75f).forEach { scale ->
                        drawCircle(
                            color = CyberCyan.copy(alpha = 0.12f),
                            radius = (size.width / 2f) * scale,
                            center = center,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Pulsing Search Wave
                    drawCircle(
                        color = CyberCyan.copy(alpha = (1f - pulseRadius) * 0.3f),
                        radius = (size.width / 2f) * pulseRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw Center Node (Self)
                    drawCircle(
                        color = CyberCyan,
                        radius = 12.dp.toPx(),
                        center = center
                    )

                    // Draw Peer Nodes and Link Vectors
                    activeNodes.forEach { node ->
                        val nodeOffset = Offset(
                            x = size.width * node.xRatio,
                            y = size.height * node.yRatio
                        )

                        val lineColor = if (node.transport == TransportType.WIFI_DIRECT) EmeraldGreen else CyberCyan

                        // Draw Link Line
                        drawLine(
                            color = lineColor.copy(alpha = 0.6f),
                            start = center,
                            end = nodeOffset,
                            strokeWidth = if (node.isDirectPeer) 2.dp.toPx() else 1.dp.toPx()
                        )

                        // Traveling Packet Pulse along line
                        val packetOffset = Offset(
                            x = center.x + (nodeOffset.x - center.x) * pulseRadius,
                            y = center.y + (nodeOffset.y - center.y) * pulseRadius
                        )
                        drawCircle(
                            color = NeonAmber,
                            radius = 4.dp.toPx(),
                            center = packetOffset
                        )

                        // Node Circle
                        val nodeColor = if (node.isDirectPeer) ElectricBlue else NeonAmber
                        drawCircle(
                            color = nodeColor,
                            radius = 9.dp.toPx(),
                            center = nodeOffset
                        )
                    }
                }

                // Legend Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text("● Self (Hub)", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("● Direct Peer (1 Hop)", color = ElectricBlue, fontSize = 11.sp)
                    Text("● Relay Node (2+ Hops)", color = NeonAmber, fontSize = 11.sp)
                }
            }

            // Real-time Telemetry Dashboard Card
            GlassCard(
                hasAccentGlow = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = "Live Mesh Telemetry",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MetricItem(title = "Peers Connected", value = "${metrics.connectedPeersCount} / ${metrics.totalNodesCount}")
                        MetricItem(title = "Avg Latency", value = "${metrics.avgLatencyMs} ms")
                        MetricItem(title = "Traffic Rate", value = "${metrics.packetsPerSec} pkts/s")
                        MetricItem(title = "Health Score", value = "${metrics.networkHealthPercent}%")
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItem(title: String, value: String) {
    Column {
        Text(text = title, fontSize = 10.sp, color = TextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
    }
}
