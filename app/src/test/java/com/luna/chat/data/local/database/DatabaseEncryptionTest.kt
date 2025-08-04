package com.luna.chat.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.luna.chat.data.local.dao.ChatDao
import com.luna.chat.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class DatabaseEncryptionTest {

    private lateinit var database: LunaDatabase
    private lateinit var chatDao: ChatDao
    
    @Mock
    private lateinit var encryptionHelper: DatabaseEncryptionHelper
    
    private val context: Context = ApplicationProvider.getApplicationContext()
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Use in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            LunaDatabase::class.java
        ).build()
        
        chatDao = database.chatDao()
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun testDatabaseOperations() = runBlocking {
        // Create a test message
        val message = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            content = "Test message",
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            sessionId = "test-session",
            status = "sent"
        )
        
        // Insert message
        chatDao.insertMessage(message)
        
        // Retrieve message
        val retrievedMessage = chatDao.getMessageById(message.id)
        
        // Verify message was stored and retrieved correctly
        assertNotNull(retrievedMessage)
        assertEquals(message.id, retrievedMessage?.id)
        assertEquals(message.content, retrievedMessage?.content)
        assertEquals(message.isFromUser, retrievedMessage?.isFromUser)
    }
    
    @Test
    fun testDatabaseEncryptionHelper() {
        // Create a real encryption helper
        val realEncryptionHelper = DatabaseEncryptionHelper(context)
        
        // Verify support factory creation doesn't throw exceptions
        val supportFactory = realEncryptionHelper.createSupportFactory()
        assertNotNull(supportFactory)
        
        // Verify encrypted file was created
        val passphraseFile = File(context.filesDir, "db_passphrase.bin")
        assert(passphraseFile.exists())
    }
}