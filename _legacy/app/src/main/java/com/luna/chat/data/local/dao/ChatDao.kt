package com.luna.chat.data.local.dao

import androidx.room.*
import com.luna.chat.data.local.entity.ChatMessageEntity
import com.luna.chat.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    
    // Chat Message Operations
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE session_id = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: String): Flow<List<ChatMessageEntity>>
    
    @Query("SELECT * FROM chat_messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): ChatMessageEntity?
    
    @Query("SELECT * FROM chat_messages WHERE is_from_user = :isFromUser ORDER BY timestamp DESC")
    fun getMessagesByType(isFromUser: Boolean): Flow<List<ChatMessageEntity>>
    
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMessages(limit: Int): Flow<List<ChatMessageEntity>>
    
    @Query("SELECT DISTINCT session_id FROM chat_messages ORDER BY timestamp DESC")
    suspend fun getAllSessionIds(): List<String>
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE session_id = :sessionId")
    suspend fun getMessageCountBySession(sessionId: String): Int
    
    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getTotalMessageCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)
    
    @Update
    suspend fun updateMessage(message: ChatMessageEntity)
    
    @Query("UPDATE chat_messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()

    @Query("DELETE FROM chat_messages WHERE session_id = :sessionId")
    suspend fun clearSessionMessages(sessionId: String)
    
    @Query("DELETE FROM chat_messages WHERE timestamp < :timestamp")
    suspend fun deleteMessagesOlderThan(timestamp: Long)
    
    @Query("DELETE FROM chat_messages WHERE session_id IN (SELECT session_id FROM chat_messages GROUP BY session_id HAVING COUNT(*) > :maxMessages)")
    suspend fun deleteExcessiveMessages(maxMessages: Int)
    
    // User Preferences Operations
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getUserPreferences(): Flow<UserPreferencesEntity?>
    
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getUserPreferencesSync(): UserPreferencesEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferencesEntity)
    
    @Update
    suspend fun updateUserPreferences(preferences: UserPreferencesEntity)
    
    @Query("UPDATE user_preferences SET selected_theme = :theme, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateTheme(theme: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET api_key_configured = :configured, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateApiKeyStatus(configured: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET first_time_user = 0, updated_at = :updatedAt WHERE id = 1")
    suspend fun markNotFirstTimeUser(updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET parental_controls_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateParentalControls(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    // Combined Operations
    @Transaction
    suspend fun insertMessageAndUpdatePreferences(
        message: ChatMessageEntity,
        preferences: UserPreferencesEntity
    ) {
        insertMessage(message)
        updateUserPreferences(preferences)
    }
    
    @Transaction
    suspend fun clearAllDataAndResetPreferences() {
        clearAllMessages()
        val defaultPrefs = UserPreferencesEntity.createDefault()
        insertUserPreferences(defaultPrefs)
    }
    
    // Analytics/Statistics Operations
    @Query("""
        SELECT COUNT(*) as total_messages,
               SUM(CASE WHEN is_from_user = 1 THEN 1 ELSE 0 END) as user_messages,
               SUM(CASE WHEN is_from_user = 0 THEN 1 ELSE 0 END) as ai_messages,
               MIN(timestamp) as first_message_time,
               MAX(timestamp) as last_message_time
        FROM chat_messages
    """)
    suspend fun getChatStatistics(): ChatStatistics?
    
    @Query("SELECT session_id, COUNT(*) as message_count FROM chat_messages GROUP BY session_id ORDER BY message_count DESC")
    suspend fun getSessionStatistics(): List<SessionStatistics>
}

// Data classes for statistics
data class ChatStatistics(
    @ColumnInfo(name = "total_messages") val totalMessages: Int,
    @ColumnInfo(name = "user_messages") val userMessages: Int,
    @ColumnInfo(name = "ai_messages") val aiMessages: Int,
    @ColumnInfo(name = "first_message_time") val firstMessageTime: Long?,
    @ColumnInfo(name = "last_message_time") val lastMessageTime: Long?
)

data class SessionStatistics(
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "message_count") val messageCount: Int
)