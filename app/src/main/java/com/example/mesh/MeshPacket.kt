package com.example.mesh

data class MeshPacket(
    val packetId: String,
    val sourceMeshId: String,
    val destinationMeshId: String,
    val payloadType: PacketPayloadType,
    val encryptedData: String,
    val ttl: Int = 7,
    val hopCount: Int = 0,
    val routePath: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val signature: String = "",
    val transport: TransportType = TransportType.BLE,
    val isStoreAndForward: Boolean = false
)
