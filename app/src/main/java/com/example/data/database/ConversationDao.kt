package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, lastTimestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE conversationId = :id LIMIT 1")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE conversationId = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE conversationId = :id")
    suspend fun updatePinned(id: String, isPinned: Boolean)

    @Query("DELETE FROM conversations WHERE conversationId = :id")
    suspend fun deleteConversation(id: String)
}
