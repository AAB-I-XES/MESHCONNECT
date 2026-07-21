package com.example.mesh

import android.content.Context
import com.example.mesh.bluetooth.BleManager
import com.example.mesh.wifidirect.WifiDirectManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DiscoveryPowerMode {
    ACTIVE_HIGH_THROUGHPUT,
    BALANCED_DUTY_CYCLE,
    BATTERY_SAVER_SLEEP
}

data class DiscoveredPeer(
    val meshId: String,
    val displayName: String,
    val rssi: Int,
    val transport: TransportType,
    val batteryPercent: Int,
    val publicKey: String,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

class MeshDiscoveryManager(
    private val context: Context,
    private val bleManager: BleManager,
    private val wifiDirectManager: WifiDirectManager
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val isAdvertising: StateFlow<Boolean> = bleManager.isAdvertising
    val isScanning: StateFlow<Boolean> = bleManager.isScanning

    private val _powerMode = MutableStateFlow(DiscoveryPowerMode.BALANCED_DUTY_CYCLE)
    val powerMode: StateFlow<DiscoveryPowerMode> = _powerMode.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<DiscoveredPeer>>(emptyList())
    val discoveredPeers: StateFlow<List<DiscoveredPeer>> = _discoveredPeers.asStateFlow()

    init {
        observeHardwareDiscoveredPeers()
    }

    fun setPowerMode(mode: DiscoveryPowerMode) {
        _powerMode.value = mode
    }

    fun startDiscovery(myMeshId: String) {
        bleManager.startBleEngine(myMeshId)
        wifiDirectManager.startWifiDirect()
    }

    fun stopDiscovery() {
        bleManager.stopBleEngine()
        wifiDirectManager.stopWifiDirect()
    }

    private fun observeHardwareDiscoveredPeers() {
        scope.launch {
            bleManager.discoveredPeers.collect { blePeersMap ->
                val bleList = blePeersMap.values.map { blePeer ->
                    DiscoveredPeer(
                        meshId = "mesh_" + blePeer.address.replace(":", "").lowercase().take(8),
                        displayName = blePeer.name,
                        rssi = blePeer.rssi,
                        transport = TransportType.BLE,
                        batteryPercent = 90,
                        publicKey = "pub_ble_${blePeer.address}",
                        lastSeenTimestamp = blePeer.lastSeen
                    )
                }

                val wifiList = wifiDirectManager.discoveredPeers.value.map { wifiPeer ->
                    DiscoveredPeer(
                        meshId = "mesh_wifi_" + wifiPeer.deviceAddress.replace(":", "").lowercase().take(6),
                        displayName = wifiPeer.deviceName,
                        rssi = -55,
                        transport = TransportType.WIFI_DIRECT,
                        batteryPercent = 95,
                        publicKey = "pub_wifi_${wifiPeer.deviceAddress}",
                        lastSeenTimestamp = System.currentTimeMillis()
                    )
                }

                _discoveredPeers.value = (bleList + wifiList).sortedByDescending { it.rssi }
            }
        }
    }
}
