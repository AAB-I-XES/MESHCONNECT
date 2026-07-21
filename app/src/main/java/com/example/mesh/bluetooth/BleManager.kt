package com.example.mesh.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
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
import java.util.UUID

data class DiscoveredBlePeer(
    val device: BluetoothDevice,
    val address: String,
    val name: String,
    val rssi: Int,
    val lastSeen: Long = System.currentTimeMillis()
)

@SuppressLint("MissingPermission")
class BleManager(
    private val context: Context,
    private val onPacketReceived: (MeshPacket) -> Unit
) {
    companion object {
        private const val TAG = "BleManager"
        val MESH_SERVICE_UUID: UUID = UUID.fromString("0000FE33-0000-1000-8000-00805F9B34FB")
        val RX_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FE34-0000-1000-8000-00805F9B34FB")
        val TX_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FE35-0000-1000-8000-00805F9B34FB")
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var leAdvertiser: BluetoothLeAdvertiser? = null
    private var leScanner: BluetoothLeScanner? = null
    private var gattServer: BluetoothGattServer? = null

    private val activeGattClients = mutableMapOf<String, BluetoothGatt>()
    private val connectedServerDevices = java.util.concurrent.ConcurrentHashMap.newKeySet<BluetoothDevice>()

    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<Map<String, DiscoveredBlePeer>>(emptyMap())
    val discoveredPeers: StateFlow<Map<String, DiscoveredBlePeer>> = _discoveredPeers.asStateFlow()

    private val _connectedPeersCount = MutableStateFlow(0)
    val connectedPeersCount: StateFlow<Int> = _connectedPeersCount.asStateFlow()

    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    fun startBleEngine(myMeshId: String) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.w(TAG, "Bluetooth adapter is disabled or not supported")
            return
        }

        try {
            setupGattServer()
            startAdvertising(myMeshId)
            startScanning()
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException starting BLE engine", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting BLE engine", e)
        }
    }

    fun stopBleEngine() {
        try {
            stopAdvertising()
            stopScanning()
            gattServer?.close()
            gattServer = null
            activeGattClients.values.forEach { it.close() }
            activeGattClients.clear()
            _connectedPeersCount.value = 0
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping BLE engine", e)
        }
    }

    private fun setupGattServer() {
        if (bluetoothManager == null) return
        val gattServerCallback = object : BluetoothGattServerCallback() {
            override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "GATT Server connected to device ${device.address}")
                    connectedServerDevices.add(device)
                    _connectedPeersCount.value = activeGattClients.size + connectedServerDevices.size
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "GATT Server disconnected from device ${device.address}")
                    connectedServerDevices.remove(device)
                    _connectedPeersCount.value = (activeGattClients.size + connectedServerDevices.size).coerceAtLeast(0)
                }
            }

            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray
            ) {
                if (characteristic.uuid == RX_CHARACTERISTIC_UUID) {
                    val packet = BinaryPacketSerializer.deserialize(value)
                    if (packet != null) {
                        Log.d(TAG, "Received BLE Packet ${packet.packetId} from ${packet.sourceMeshId}")
                        scope.launch { onPacketReceived(packet) }
                    }
                    if (responseNeeded) {
                        gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
                    }
                }
            }
        }

        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        val service = BluetoothGattService(MESH_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val rxChar = BluetoothGattCharacteristic(
            RX_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val txChar = BluetoothGattCharacteristic(
            TX_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        service.addCharacteristic(rxChar)
        service.addCharacteristic(txChar)
        gattServer?.addService(service)
    }

    private fun startAdvertising(myMeshId: String) {
        leAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        if (leAdvertiser == null) return

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(MESH_SERVICE_UUID))
            .addManufacturerData(0x00E0, myMeshId.take(8).toByteArray(Charsets.UTF_8))
            .build()

        leAdvertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    private fun stopAdvertising() {
        leAdvertiser?.stopAdvertising(advertiseCallback)
        _isAdvertising.value = false
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(TAG, "BLE Advertising started successfully")
            _isAdvertising.value = true
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "BLE Advertising failed with code $errorCode")
            _isAdvertising.value = false
        }
    }

    private fun startScanning() {
        leScanner = bluetoothAdapter?.bluetoothLeScanner
        if (leScanner == null) return

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(MESH_SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        leScanner?.startScan(listOf(filter), settings, scanCallback)
        _isScanning.value = true
    }

    private fun stopScanning() {
        leScanner?.stopScan(scanCallback)
        _isScanning.value = false
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val address = device.address
            val rssi = result.rssi
            val name = device.name ?: "Peer Device ($address)"

            val discovered = DiscoveredBlePeer(
                device = device,
                address = address,
                name = name,
                rssi = rssi
            )

            val current = _discoveredPeers.value.toMutableMap()
            current[address] = discovered
            _discoveredPeers.value = current

            // Auto-connect GATT client if not connected
            if (!activeGattClients.containsKey(address)) {
                connectGattClient(device)
            }
        }
    }

    private fun connectGattClient(device: BluetoothDevice) {
        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "Connected to GATT Client ${device.address}")
                    activeGattClients[device.address] = gatt
                    _connectedPeersCount.value = activeGattClients.size + connectedServerDevices.size
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "Disconnected GATT Client ${device.address}")
                    activeGattClients.remove(device.address)
                    _connectedPeersCount.value = (activeGattClients.size + connectedServerDevices.size).coerceAtLeast(0)
                    gatt.close()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "GATT Services discovered for ${device.address}")
                    val service = gatt.getService(MESH_SERVICE_UUID)
                    val txChar = service?.getCharacteristic(TX_CHARACTERISTIC_UUID)
                    if (txChar != null) {
                        gatt.setCharacteristicNotification(txChar, true)
                        val descriptor = txChar.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                        if (descriptor != null) {
                            descriptor.value = android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                }
            }

            @Deprecated("Deprecated in Java/Android")
            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                if (characteristic.uuid == TX_CHARACTERISTIC_UUID) {
                    val packet = BinaryPacketSerializer.deserialize(characteristic.value)
                    if (packet != null) {
                        Log.d(TAG, "Received GATT notification Packet ${packet.packetId}")
                        scope.launch { onPacketReceived(packet) }
                    }
                }
            }
        }

        device.connectGatt(context, false, gattCallback)
    }

    fun sendPacketToPeer(targetAddress: String, packet: MeshPacket): Boolean {
        val gatt = activeGattClients[targetAddress] ?: return false
        val service = gatt.getService(MESH_SERVICE_UUID) ?: return false
        val rxChar = service.getCharacteristic(RX_CHARACTERISTIC_UUID) ?: return false

        val bytes = BinaryPacketSerializer.serialize(packet)
        rxChar.value = bytes
        rxChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        return gatt.writeCharacteristic(rxChar)
    }

    fun broadcastPacket(packet: MeshPacket) {
        val bytes = BinaryPacketSerializer.serialize(packet)
        // 1. Send to remote GATT Servers via our GATT Clients
        activeGattClients.values.forEach { gatt ->
            val service = gatt.getService(MESH_SERVICE_UUID) ?: return@forEach
            val rxChar = service.getCharacteristic(RX_CHARACTERISTIC_UUID) ?: return@forEach
            rxChar.value = bytes
            rxChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            gatt.writeCharacteristic(rxChar)
        }

        // 2. Notify remote GATT Clients via our local GATT Server
        val server = gattServer
        if (server != null && connectedServerDevices.isNotEmpty()) {
            val service = server.getService(MESH_SERVICE_UUID)
            val txChar = service?.getCharacteristic(TX_CHARACTERISTIC_UUID)
            if (txChar != null) {
                txChar.value = bytes
                connectedServerDevices.forEach { dev ->
                    server.notifyCharacteristicChanged(dev, txChar, false)
                }
            }
        }
    }
}
