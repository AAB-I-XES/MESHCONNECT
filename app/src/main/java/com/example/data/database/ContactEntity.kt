package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val meshId: String,
    val displayName: String,
    val publicKey: String,
    val status: String = "ONLINE", // ONLINE, OFFLINE, BLOCKED, FAVORITE
    val lastSeen: Long = System.currentTimeMillis(),
    val hopsAway: Int = 1,
    val rssi: Int = -60,
    val transportType: String = "BLE", // BLE, WIFI_DIRECT
    val batteryPercent: Int = 85,
    val latencyMs: Long = 42,
    val avatarId: Int = 0
)
