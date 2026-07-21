package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val meshId: String,
    val displayName: String,
    val publicKey: String,
    val privateKey: String,
    val bio: String,
    val avatarId: Int,
    val createdAt: Long = System.currentTimeMillis()
)
