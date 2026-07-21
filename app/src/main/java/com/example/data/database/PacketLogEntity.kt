package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "packet_logs")
data class PacketLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packetId: String,
    val sourceMeshId: String,
    val targetMeshId: String,
    val payloadType: String, // CHAT, VOICE, HANDSHAKE, ROUTE_ANNOUNCE, FILE, ACK
    val hopCount: Int,
    val routePath: String,
    val sizeBytes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "DELIVERED"
)
