package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PacketLogDao {
    @Query("SELECT * FROM packet_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<PacketLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PacketLogEntity)

    @Query("DELETE FROM packet_logs")
    suspend fun clearLogs()
}
