package com.example.mesh.wifidirect

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import com.example.mesh.BinaryPacketSerializer
import com.example.mesh.MeshPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

data class DiscoveredWifiP2pPeer(
    val deviceName: String,
    val deviceAddress: String,
    val status: Int
)

@SuppressLint("MissingPermission")
class WifiDirectManager(
    private val context: Context,
    private val onPacketReceived: (MeshPacket) -> Unit
) {
    companion object {
        private const val TAG = "WifiDirectManager"
        private const val P2P_PORT = 8888
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    private var channel: WifiP2pManager.Channel? = null

    private var serverSocket: ServerSocket? = null
    private var activeSocket: Socket? = null

    private val _isWifiP2pEnabled = MutableStateFlow(false)
    val isWifiP2pEnabled: StateFlow<Boolean> = _isWifiP2pEnabled.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<DiscoveredWifiP2pPeer>>(emptyList())
    val discoveredPeers: StateFlow<List<DiscoveredWifiP2pPeer>> = _discoveredPeers.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _groupOwnerAddress = MutableStateFlow<InetAddress?>(null)

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    fun startWifiDirect() {
        if (wifiP2pManager == null) return
        try {
            channel = wifiP2pManager.initialize(context, context.mainLooper, null)
            context.registerReceiver(receiver, intentFilter)
            discoverPeers()
            startSocketServer()
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException starting Wi-Fi Direct (missing NEARBY_WIFI_DEVICES or FINE_LOCATION)", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Wi-Fi Direct", e)
        }
    }

    fun stopWifiDirect() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
        serverSocket?.close()
        serverSocket = null
        activeSocket?.close()
        activeSocket = null
        _isConnected.value = false
    }

    fun discoverPeers() {
        channel?.let { ch ->
            try {
                wifiP2pManager?.discoverPeers(ch, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d(TAG, "Wi-Fi Direct discoverPeers initiated")
                    }

                    override fun onFailure(reasonCode: Int) {
                        Log.e(TAG, "Wi-Fi Direct discoverPeers failed: $reasonCode")
                    }
                })
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException in discoverPeers", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error in discoverPeers", e)
            }
        }
    }

    fun connectToPeer(deviceAddress: String) {
        val config = WifiP2pConfig().apply {
            this.deviceAddress = deviceAddress
        }
        channel?.let { ch ->
            try {
                wifiP2pManager?.connect(ch, config, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d(TAG, "Wi-Fi Direct connect requested to $deviceAddress")
                    }

                    override fun onFailure(reason: Int) {
                        Log.e(TAG, "Wi-Fi Direct connect failed: $reason")
                    }
                })
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException in connectToPeer", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error in connectToPeer", e)
            }
        }
    }

    private fun startSocketServer() {
        scope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(P2P_PORT)
                Log.d(TAG, "Wi-Fi Direct ServerSocket listening on port $P2P_PORT")
                while (serverSocket?.isClosed == false) {
                    val socket = serverSocket?.accept() ?: break
                    activeSocket = socket
                    Log.d(TAG, "Accepted incoming Wi-Fi Direct socket connection from ${socket.inetAddress}")
                    handleIncomingSocketStream(socket)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wi-Fi Direct ServerSocket exception", e)
            }
        }
    }

    private fun handleIncomingSocketStream(socket: Socket) {
        scope.launch(Dispatchers.IO) {
            try {
                val dis = DataInputStream(socket.getInputStream())
                while (socket.isConnected && !socket.isClosed) {
                    val length = dis.readInt()
                    if (length <= 0 || length > 10 * 1024 * 1024) break
                    val bytes = ByteArray(length)
                    dis.readFully(bytes)
                    val packet = BinaryPacketSerializer.deserialize(bytes)
                    if (packet != null) {
                        Log.d(TAG, "Received Wi-Fi Direct packet ${packet.packetId}")
                        onPacketReceived(packet)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling Wi-Fi Direct socket stream", e)
            }
        }
    }

    fun sendPacketOverSocket(targetIp: InetAddress?, packet: MeshPacket): Boolean {
        val bytes = BinaryPacketSerializer.serialize(packet)
        val ip = targetIp ?: _groupOwnerAddress.value ?: return false

        scope.launch(Dispatchers.IO) {
            try {
                val socket = Socket(ip, P2P_PORT)
                val dos = DataOutputStream(socket.getOutputStream())
                dos.writeInt(bytes.size)
                dos.write(bytes)
                dos.flush()
                socket.close()
                Log.d(TAG, "Sent packet ${packet.packetId} over Wi-Fi Direct socket to $ip")
            } catch (e: Exception) {
                Log.e(TAG, "Failed sending packet over Wi-Fi Direct socket", e)
            }
        }
        return true
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    _isWifiP2pEnabled.value = (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    channel?.let { ch ->
                        try {
                            wifiP2pManager?.requestPeers(ch) { peersList: WifiP2pDeviceList ->
                                val peers = peersList.deviceList.map { dev: WifiP2pDevice ->
                                    DiscoveredWifiP2pPeer(
                                        deviceName = dev.deviceName ?: "Wi-Fi Peer (${dev.deviceAddress})",
                                        deviceAddress = dev.deviceAddress,
                                        status = dev.status
                                    )
                                }
                                _discoveredPeers.value = peers
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error requesting peers in receiver", e)
                        }
                    }
                }

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                    if (networkInfo?.isConnected == true) {
                        channel?.let { ch ->
                            try {
                                wifiP2pManager?.requestConnectionInfo(ch) { info: WifiP2pInfo ->
                                    _isConnected.value = true
                                    _groupOwnerAddress.value = info.groupOwnerAddress
                                    Log.d(TAG, "Wi-Fi Direct connected! GO IP: ${info.groupOwnerAddress}")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error requesting connection info in receiver", e)
                            }
                        }
                    } else {
                        _isConnected.value = false
                        _groupOwnerAddress.value = null
                    }
                }
            }
        }
    }
}
