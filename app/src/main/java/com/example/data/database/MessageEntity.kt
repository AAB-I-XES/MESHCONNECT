package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val conversationId: String,
    val senderMeshId: String,
    val recipientMeshId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "DELIVERED", // SENDING, DELIVERED, READ, FAILED
    val isEncrypted: Boolean = true,
    val routeHops: Int = 1,
    val reaction: String = "",
    val isPinned: Boolean = false,
    val isEdited: Boolean = false,
    val expiryTimestamp: Long = 0L,
    val messageType: String = "TEXT", // TEXT, VOICE, FILE, SYSTEM, CALL
    val mediaUri: String? = null,
    val mediaName: String? = null,
    val mediaSize: Long = 0L,
    val voiceDurationSec: Int = 0
)
