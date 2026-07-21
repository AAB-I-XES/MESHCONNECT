package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        ContactEntity::class,
        MessageEntity::class,
        ConversationEntity::class,
        RouteEntity::class,
        PacketLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MeshDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactDao(): ContactDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun routeDao(): RouteDao
    abstract fun packetLogDao(): PacketLogDao

    companion object {
        @Volatile
        private var INSTANCE: MeshDatabase? = null

        fun getDatabase(context: Context): MeshDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeshDatabase::class.java,
                    "meshlink_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
