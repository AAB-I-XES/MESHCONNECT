package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val destinationMeshId: String,
    val nextHopMeshId: String,
    val hopCount: Int,
    val rssi: Int,
    val transportType: String, // BLE, WIFI_DIRECT
    val latencyMs: Long,
    val batteryLevel: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)
