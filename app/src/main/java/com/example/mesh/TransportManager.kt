package com.example.mesh

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransportChannelStatus(
    val transport: TransportType,
    val isConnected: Boolean,
    val bandwidthKbps: Int,
    val latencyMs: Long,
    val packetLossPercent: Float,
    val score: Int
)

class TransportManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _activeTransport = MutableStateFlow(TransportType.BLE)
    val activeTransport: StateFlow<TransportType> = _activeTransport.asStateFlow()

    private val _bleStatus = MutableStateFlow(
        TransportChannelStatus(
            transport = TransportType.BLE,
            isConnected = true,
            bandwidthKbps = 128,
            latencyMs = 35L,
            packetLossPercent = 0.5f,
            score = 85
        )
    )
    val bleStatus: StateFlow<TransportChannelStatus> = _bleStatus.asStateFlow()

    private val _wifiDirectStatus = MutableStateFlow(
        TransportChannelStatus(
            transport = TransportType.WIFI_DIRECT,
            isConnected = true,
            bandwidthKbps = 15400,
            latencyMs = 12L,
            packetLossPercent = 0.1f,
            score = 98
        )
    )
    val wifiDirectStatus: StateFlow<TransportChannelStatus> = _wifiDirectStatus.asStateFlow()

    init {
        startTransportMonitor()
    }

    fun selectTransportForPayload(payloadType: PacketPayloadType): TransportType {
        return when (payloadType) {
            PacketPayloadType.CHAT_VOICE_CHUNK,
            PacketPayloadType.CHAT_FILE_CHUNK,
            PacketPayloadType.VOICE_STREAM_DATA -> {
                _activeTransport.value = TransportType.WIFI_DIRECT
                TransportType.WIFI_DIRECT
            }
            else -> {
                // Low overhead packets remain on BLE unless Wi-Fi Direct is already open
                _activeTransport.value
            }
        }
    }

    private fun startTransportMonitor() {
        scope.launch {
            while (true) {
                delay(2500)
                // Periodically update channel metrics
                val bleLatency = (30L..45L).random()
                val wifiLatency = (8L..15L).random()

                _bleStatus.value = _bleStatus.value.copy(latencyMs = bleLatency)
                _wifiDirectStatus.value = _wifiDirectStatus.value.copy(latencyMs = wifiLatency)
            }
        }
    }
}
