package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY status ASC, displayName ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE meshId = :meshId LIMIT 1")
    suspend fun getContactById(meshId: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE status != 'BLOCKED' ORDER BY displayName ASC")
    fun getActiveContacts(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Query("DELETE FROM contacts WHERE meshId = :meshId")
    suspend fun deleteContact(meshId: String)
}
