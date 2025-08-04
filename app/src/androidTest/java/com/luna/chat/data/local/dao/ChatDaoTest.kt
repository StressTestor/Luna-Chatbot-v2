package com.luna.chat.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.luna.chat.data.local.database.LunaDatabase
import com.luna.chat.data.local.entity.ChatMessageEntity
import com.luna.chat.data.local.entity.UserPreferencesEntity
import com.luna.chat.domain.entity.MessageStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ChatDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: LunaDatabase
    private lateinit var chatDao: ChatDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LunaDatabase::class.java
        ).allowMainThreadQueries().build()
        
        chatDao = database.chatDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetMessage() = runTest {
        val message = ChatMessageEntity(
            id = "test-id-1",
            content = "Hello, AI!",
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            sessionId = "session-1",
            status = MessageStatus.SENT.name
        )

        chatDao.insertMessage(message)
        val retrievedMessage = chatDao.getMessageById("test-id-1")

        assertNotNull(retrievedMessage)
        assertEquals(message.id, retrievedMessage?.id)
        assertEquals(message.content, retrievedMessage?.content)
        assertEquals(message.isFromUser, retrievedMessage?.isFromUser)
        assertEquals(message.sessionId, retrievedMessage?.sessionId)
    }

    @Test
    fun insertMultipleMessagesAndGetAll() = runTest {
        val messages = listOf(
            ChatMessageEntity(
                id = "msg-1",
                content = "First message",
                isFromUser = true,
                timestamp = 1000L,
                sessionId = "session-1",
                status = MessageStatus.SENT.name
            ),
            ChatMessageEntity(
                id = "msg-2",
                content = "Second message",
                isFromUser = false,
                timestamp = 2000L,
                sessionId = "session-1",
                status = MessageStatus.DELIVERED.name
            )
        )

        chatDao.insertMessages(messages)
        val allMessages = chatDao.getAllMessages().first()

        assertEquals(2, allMessages.size)
        assertEquals("First message", allMessages[0].content)
        assertEquals("Second message", allMessages[1].content)
    }

    @Test
    fun getMessagesBySession() = runTest {
        val session1Messages = listOf(
            ChatMessageEntity("msg-1", "Session 1 Message 1", true, 1000L, "session-1", MessageStatus.SENT.name),
            ChatMessageEntity("msg-2", "Session 1 Message 2", false, 2000L, "session-1", MessageStatus.DELIVERED.name)
        )
        val session2Messages = listOf(
            ChatMessageEntity("msg-3", "Session 2 Message 1", true, 3000L, "session-2", MessageStatus.SENT.name)
        )

        chatDao.insertMessages(session1Messages + session2Messages)
        
        val session1Retrieved = chatDao.getMessagesBySession("session-1").first()
        val session2Retrieved = chatDao.getMessagesBySession("session-2").first()

        assertEquals(2, session1Retrieved.size)
        assertEquals(1, session2Retrieved.size)
        assertEquals("Session 1 Message 1", session1Retrieved[0].content)
        assertEquals("Session 2 Message 1", session2Retrieved[0].content)
    }

    @Test
    fun getMessagesByType() = runTest {
        val messages = listOf(
            ChatMessageEntity("msg-1", "User message 1", true, 1000L, "session-1", MessageStatus.SENT.name),
            ChatMessageEntity("msg-2", "AI message 1", false, 2000L, "session-1", MessageStatus.DELIVERED.name),
            ChatMessageEntity("msg-3", "User message 2", true, 3000L, "session-1", MessageStatus.SENT.name)
        )

        chatDao.insertMessages(messages)
        
        val userMessages = chatDao.getMessagesByType(true).first()
        val aiMessages = chatDao.getMessagesByType(false).first()

        assertEquals(2, userMessages.size)
        assertEquals(1, aiMessages.size)
        assertTrue(userMessages.all { it.isFromUser })
        assertTrue(aiMessages.all { !it.isFromUser })
    }

    @Test
    fun updateMessageStatus() = runTest {
        val message = ChatMessageEntity(
            id = "test-id",
            content = "Test message",
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            sessionId = "session-1",
            status = MessageStatus.SENDING.name
        )

        chatDao.insertMessage(message)
        chatDao.updateMessageStatus("test-id", MessageStatus.SENT.name)
        
        val updatedMessage = chatDao.getMessageById("test-id")
        assertEquals(MessageStatus.SENT.name, updatedMessage?.status)
    }

    @Test
    fun deleteMessage() = runTest {
        val message = ChatMessageEntity(
            id = "test-id",
            content = "Test message",
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            sessionId = "session-1",
            status = MessageStatus.SENT.name
        )

        chatDao.insertMessage(message)
        chatDao.deleteMessage("test-id")
        
        val deletedMessage = chatDao.getMessageById("test-id")
        assertNull(deletedMessage)
    }

    @Test
    fun clearSessionMessages() = runTest {
        val messages = listOf(
            ChatMessageEntity("msg-1", "Session 1 Message", true, 1000L, "session-1", MessageStatus.SENT.name),
            ChatMessageEntity("msg-2", "Session 2 Message", true, 2000L, "session-2", MessageStatus.SENT.name)
        )

        chatDao.insertMessages(messages)
        chatDao.clearSessionMessages("session-1")
        
        val session1Messages = chatDao.getMessagesBySession("session-1").first()
        val session2Messages = chatDao.getMessagesBySession("session-2").first()
        
        assertEquals(0, session1Messages.size)
        assertEquals(1, session2Messages.size)
    }

    @Test
    fun getMessageCounts() = runTest {
        val messages = listOf(
            ChatMessageEntity("msg-1", "Message 1", true, 1000L, "session-1", MessageStatus.SENT.name),
            ChatMessageEntity("msg-2", "Message 2", false, 2000L, "session-1", MessageStatus.DELIVERED.name),
            ChatMessageEntity("msg-3", "Message 3", true, 3000L, "session-2", MessageStatus.SENT.name)
        )

        chatDao.insertMessages(messages)
        
        val totalCount = chatDao.getTotalMessageCount()
        val session1Count = chatDao.getMessageCountBySession("session-1")
        val session2Count = chatDao.getMessageCountBySession("session-2")
        
        assertEquals(3, totalCount)
        assertEquals(2, session1Count)
        assertEquals(1, session2Count)
    }

    @Test
    fun deleteMessagesOlderThan() = runTest {
        val currentTime = System.currentTimeMillis()
        val messages = listOf(
            ChatMessageEntity("msg-1", "Old message", true, currentTime - 10000, "session-1", MessageStatus.SENT.name),
            ChatMessageEntity("msg-2", "Recent message", true, currentTime, "session-1", MessageStatus.SENT.name)
        )

        chatDao.insertMessages(messages)
        chatDao.deleteMessagesOlderThan(currentTime - 5000)
        
        val remainingMessages = chatDao.getAllMessages().first()
        assertEquals(1, remainingMessages.size)
        assertEquals("Recent message", remainingMessages[0].content)
    }

    @Test
    fun userPreferencesOperations() = runTest {
        val preferences = UserPreferencesEntity.createDefault()
        
        chatDao.insertUserPreferences(preferences)
        val retrieved = chatDao.getUserPreferencesSync()
        
        assertNotNull(retrieved)
        assertEquals(preferences.selectedTheme, retrieved?.selectedTheme)
        assertEquals(preferences.parentalControlsEnabled, retrieved?.parentalControlsEnabled)
    }

    @Test
    fun updateUserPreferencesFields() = runTest {
        val preferences = UserPreferencesEntity.createDefault()
        chatDao.insertUserPreferences(preferences)
        
        chatDao.updateTheme("ocean")
        chatDao.updateApiKeyStatus(true)
        chatDao.markNotFirstTimeUser()
        
        val updated = chatDao.getUserPreferencesSync()
        assertEquals("ocean", updated?.selectedTheme)
        assertTrue(updated?.apiKeyConfigured == true)
        assertFalse(updated?.firstTimeUser == true)
    }

    @Test
    fun getChatStatistics() = runTest {
        val messages = listOf(
            ChatMessageEntity("msg-1", "User message 1", true, 1000L, "session-1", MessageStatus.SENT.name),
            ChatMessageEntity("msg-2", "AI message 1", false, 2000L, "session-1", MessageStatus.DELIVERED.name),
            ChatMessageEntity("msg-3", "User message 2", true, 3000L, "session-1", MessageStatus.SENT.name)
        )

        chatDao.insertMessages(messages)
        val stats = chatDao.getChatStatistics()
        
        assertNotNull(stats)
        assertEquals(3, stats?.totalMessages)
        assertEquals(2, stats?.userMessages)
        assertEquals(1, stats?.aiMessages)
        assertEquals(1000L, stats?.firstMessageTime)
        assertEquals(3000L, stats?.lastMessageTime)
    }

    @Test
    fun getSessionStatistics() = runTest {
        val messages = listOf(
            ChatMessageEntity("msg-1", "Message 1", true, 1000L, "session-1", MessageStatus.SENT.name),
            ChatMessageEntity("msg-2", "Message 2", false, 2000L, "session-1", MessageStatus.DELIVERED.name),
            ChatMessageEntity("msg-3", "Message 3", true, 3000L, "session-2", MessageStatus.SENT.name)
        )

        chatDao.insertMessages(messages)
        val sessionStats = chatDao.getSessionStatistics()
        
        assertEquals(2, sessionStats.size)
        val session1Stats = sessionStats.find { it.sessionId == "session-1" }
        val session2Stats = sessionStats.find { it.sessionId == "session-2" }
        
        assertEquals(2, session1Stats?.messageCount)
        assertEquals(1, session2Stats?.messageCount)
    }

    @Test
    fun transactionOperations() = runTest {
        val message = ChatMessageEntity(
            id = "test-id",
            content = "Test message",
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            sessionId = "session-1",
            status = MessageStatus.SENT.name
        )
        val preferences = UserPreferencesEntity.createDefault().updateTheme("ocean")
        
        chatDao.insertMessageAndUpdatePreferences(message, preferences)
        
        val retrievedMessage = chatDao.getMessageById("test-id")
        val retrievedPrefs = chatDao.getUserPreferencesSync()
        
        assertNotNull(retrievedMessage)
        assertEquals("ocean", retrievedPrefs?.selectedTheme)
    }

    @Test
    fun clearAllDataAndResetPreferences() = runTest {
        // Insert some data
        val message = ChatMessageEntity(
            id = "test-id",
            content = "Test message",
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            sessionId = "session-1",
            status = MessageStatus.SENT.name
        )
        val preferences = UserPreferencesEntity.createDefault().updateTheme("ocean")
        
        chatDao.insertMessage(message)
        chatDao.insertUserPreferences(preferences)
        
        // Clear all data
        chatDao.clearAllDataAndResetPreferences()
        
        val messages = chatDao.getAllMessages().first()
        val resetPrefs = chatDao.getUserPreferencesSync()
        
        assertEquals(0, messages.size)
        assertEquals(UserPreferencesEntity.DEFAULT_THEME, resetPrefs?.selectedTheme)
    }
}