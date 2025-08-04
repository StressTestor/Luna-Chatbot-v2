package com.luna.chat.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.luna.chat.data.local.dao.ChatDao
import com.luna.chat.data.local.entity.ChatMessageEntity
import com.luna.chat.data.local.entity.UserPreferencesEntity

@Database(
    entities = [ChatMessageEntity::class, UserPreferencesEntity::class],
    version = 2,
    exportSchema = true
)
abstract class LunaDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        const val DATABASE_NAME = "luna_database"
        
        @Volatile
        private var INSTANCE: LunaDatabase? = null
        
        fun getDatabase(context: Context, encryptionHelper: DatabaseEncryptionHelper? = null): LunaDatabase {
            return INSTANCE ?: synchronized(this) {
                val databaseBuilder = Room.databaseBuilder(
                    context.applicationContext,
                    LunaDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(DatabaseCallback())
                
                // Apply encryption if helper is provided
                encryptionHelper?.let { helper ->
                    databaseBuilder.openHelperFactory(helper.createSupportFactory())
                }
                
                val instance = databaseBuilder.build()
                INSTANCE = instance
                instance
            }
        }
        
        // Migration from version 1 to 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to user_preferences table
                database.execSQL("""
                    ALTER TABLE user_preferences 
                    ADD COLUMN first_time_user INTEGER NOT NULL DEFAULT 1
                """)
                database.execSQL("""
                    ALTER TABLE user_preferences 
                    ADD COLUMN auto_clear_history_days INTEGER NOT NULL DEFAULT 30
                """)
                database.execSQL("""
                    ALTER TABLE user_preferences 
                    ADD COLUMN content_filter_enabled INTEGER NOT NULL DEFAULT 1
                """)
                database.execSQL("""
                    ALTER TABLE user_preferences 
                    ADD COLUMN voice_input_enabled INTEGER NOT NULL DEFAULT 1
                """)
                database.execSQL("""
                    ALTER TABLE user_preferences 
                    ADD COLUMN created_at INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                """)
                database.execSQL("""
                    ALTER TABLE user_preferences 
                    ADD COLUMN updated_at INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                """)
                
                // Update column names in chat_messages table to use snake_case
                database.execSQL("""
                    CREATE TABLE chat_messages_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        content TEXT NOT NULL,
                        is_from_user INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        session_id TEXT NOT NULL,
                        status TEXT NOT NULL
                    )
                """)
                
                // Copy data from old table to new table
                database.execSQL("""
                    INSERT INTO chat_messages_new (id, content, is_from_user, timestamp, session_id, status)
                    SELECT id, content, isFromUser, timestamp, sessionId, status
                    FROM chat_messages
                """)
                
                // Drop old table and rename new table
                database.execSQL("DROP TABLE chat_messages")
                database.execSQL("ALTER TABLE chat_messages_new RENAME TO chat_messages")
                
                // Create indices for better performance
                database.execSQL("CREATE INDEX index_chat_messages_session_id ON chat_messages(session_id)")
                database.execSQL("CREATE INDEX index_chat_messages_timestamp ON chat_messages(timestamp)")
                database.execSQL("CREATE INDEX index_chat_messages_is_from_user ON chat_messages(is_from_user)")
            }
        }
        
        // Database callback for initialization
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Insert default user preferences when database is created
                db.execSQL("""
                    INSERT INTO user_preferences (
                        id, selected_theme, parental_controls_enabled, api_key_configured,
                        first_time_user, auto_clear_history_days, content_filter_enabled,
                        voice_input_enabled, created_at, updated_at
                    ) VALUES (
                        1, 'rainbow', 1, 0, 1, 30, 1, 1, 
                        ${System.currentTimeMillis()}, ${System.currentTimeMillis()}
                    )
                """)
            }
            
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Enable foreign key constraints
                db.execSQL("PRAGMA foreign_keys=ON")
            }
        }
        
        // Helper method to clear database instance (useful for testing)
        fun clearInstance() {
            INSTANCE = null
        }
    }
}