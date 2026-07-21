package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val conversationId: String,
    val title: String,
    val lastMessage: String,
    val lastTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isGroup: Boolean = false,
    val isPinned: Boolean = false,
    val participantMeshIds: String = ""
)
