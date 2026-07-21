package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Hub
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextSecondary

@Composable
fun MeshStatusBadge(
    transport: String,
    rssi: Int,
    hopsAway: Int,
    modifier: Modifier = Modifier
) {
    val transportIcon = if (transport.contains("WIFI", ignoreCase = true)) Icons.Default.Wifi else Icons.Default.Bluetooth
    val statusColor = when {
        hopsAway == 1 && rssi > -65 -> EmeraldGreen
        hopsAway <= 2 -> CyberCyan
        else -> NeonAmber
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Pulsing green dot
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "●",
                color = statusColor,
                fontSize = 10.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
            Icon(
                imageVector = transportIcon,
                contentDescription = transport,
                tint = statusColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$transport • $hopsAway ${if (hopsAway == 1) "hop" else "hops"} ($rssi dBm)",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}
