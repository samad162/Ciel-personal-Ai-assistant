package ai.ciel.assistant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MemoryEntity::class, ChatMessageEntity::class], version = 1, exportSchema = false)
abstract class CielDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: CielDatabase? = null

        fun getInstance(context: Context): CielDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CielDatabase::class.java,
                    "ciel_assistant.db"
                )
                // In-memory cache limit for low RAM footprint
                .setJournalMode(JournalMode.TRUNCATE)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
