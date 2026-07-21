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

data class MeshDiagnosticTelemetry(
    val connectedPeersCount: Int,
    val totalNodesCount: Int,
    val packetsPerSec: Int,
    val avgLatencyMs: Long,
    val packetLossPercentage: Float,
    val networkHealthPercent: Int,
    val cpuUsagePercent: Int,
    val ramUsageMb: Int,
    val batteryDrainPercentPerHour: Float,
    val storeAndForwardQueueSize: Int
)

class NetworkMonitor(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _telemetry = MutableStateFlow(
        MeshDiagnosticTelemetry(
            connectedPeersCount = 3,
            totalNodesCount = 5,
            packetsPerSec = 14,
            avgLatencyMs = 38L,
            packetLossPercentage = 0.2f,
            networkHealthPercent = 98,
            cpuUsagePercent = 4,
            ramUsageMb = 42,
            batteryDrainPercentPerHour = 1.2f,
            storeAndForwardQueueSize = 0
        )
    )
    val telemetry: StateFlow<MeshDiagnosticTelemetry> = _telemetry.asStateFlow()

    init {
        startMonitoringLoop()
    }

    private fun startMonitoringLoop() {
        scope.launch {
            while (true) {
                delay(3000)
                val current = _telemetry.value
                val pps = (10..22).random()
                val lat = (32L..44L).random()
                val cpu = (2..7).random()

                _telemetry.value = current.copy(
                    packetsPerSec = pps,
                    avgLatencyMs = lat,
                    cpuUsagePercent = cpu
                )
            }
        }
    }
}
