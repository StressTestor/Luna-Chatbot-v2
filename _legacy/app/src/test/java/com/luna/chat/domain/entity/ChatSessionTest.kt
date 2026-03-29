package com.luna.chat.domain.entity

import org.junit.Test
import org.junit.Assert.*
import java.util.UUID

class ChatSessionTest {

    @Test
    fun `create should generate valid session`() {
        val session = ChatSession.create()
        
        assertNotNull(session.id)
        assertTrue(session.messages.isEmpty())
        assertTrue(session.createdAt > 0)
        assertTrue(session.lastUpdated > 0)
        assertEquals(session.createdAt, session.lastUpdated)
    }

    @Test
    fun `create with custom id should use provided id`() {
        val customId = "custom-session-id"
        val session = ChatSession.create(customId)
        
        assertEquals(customId, session.id)
        assertTrue(session.messages.isEmpty())
    }

    @Test
    fun `addMessage should add message and update timestamp`() {
        val session = ChatSession.create()
        val originalTimestamp = session.lastUpdated
        
        // Small delay to ensure timestamp difference
        Thread.sleep(1)
        
        val message = ChatMessage.create("Hello", true)
        session.addMessage(message)
        
        assertEquals(1, session.messages.size)
        assertEquals(message, session.messages[0])
        assertTrue(session.lastUpdated > originalTimestamp)
    }

    @Test
    fun `addMessage should maintain message order`() {
        val session = ChatSession.create()
        
        val message1 = ChatMessage.create("First message", true)
        val message2 = ChatMessage.create("Second message", false)
        val message3 = ChatMessage.create("Third message", true)
        
        session.addMessage(message1)
        session.addMessage(message2)
        session.addMessage(message3)
        
        assertEquals(3, session.messages.size)
        assertEquals(message1, session.messages[0])
        assertEquals(message2, session.messages[1])
        assertEquals(message3, session.messages[2])
    }

    @Test
    fun `addMessages should add multiple messages`() {
        val session = ChatSession.create()
        
        val messages = listOf(
            ChatMessage.create("Message 1", true),
            ChatMessage.create("Message 2", false),
            ChatMessage.create("Message 3", true)
        )
        
        session.addMessages(messages)
        
        assertEquals(3, session.messages.size)
        assertEquals(messages, session.messages)
    }

    @Test
    fun `removeMessage should remove message by id`() {
        val session = ChatSession.create()
        
        val message1 = ChatMessage.create("Keep this", true)
        val message2 = ChatMessage.create("Remove this", false)
        val message3 = ChatMessage.create("Keep this too", true)
        
        session.addMessages(listOf(message1, message2, message3))
        
        val removed = session.removeMessage(message2.id)
        
        assertTrue(removed)
        assertEquals(2, session.messages.size)
        assertFalse(session.messages.contains(message2))
        assertTrue(session.messages.contains(message1))
        assertTrue(session.messages.contains(message3))
    }

    @Test
    fun `removeMessage should return false for non-existent message`() {
        val session = ChatSession.create()
        val message = ChatMessage.create("Test", true)
        session.addMessage(message)
        
        val removed = session.removeMessage("non-existent-id")
        
        assertFalse(removed)
        assertEquals(1, session.messages.size)
    }

    @Test
    fun `clearMessages should remove all messages`() {
        val session = ChatSession.create()
        
        val messages = listOf(
            ChatMessage.create("Message 1", true),
            ChatMessage.create("Message 2", false)
        )
        
        session.addMessages(messages)
        assertEquals(2, session.messages.size)
        
        session.clearMessages()
        
        assertTrue(session.messages.isEmpty())
    }

    @Test
    fun `getLastMessage should return most recent message`() {
        val session = ChatSession.create()
        
        assertNull(session.getLastMessage())
        
        val message1 = ChatMessage.create("First", true)
        val message2 = ChatMessage.create("Last", false)
        
        session.addMessage(message1)
        assertEquals(message1, session.getLastMessage())
        
        session.addMessage(message2)
        assertEquals(message2, session.getLastMessage())
    }

    @Test
    fun `getLastUserMessage should return most recent user message`() {
        val session = ChatSession.create()
        
        assertNull(session.getLastUserMessage())
        
        val userMessage1 = ChatMessage.create("User message 1", true)
        val aiMessage = ChatMessage.create("AI response", false)
        val userMessage2 = ChatMessage.create("User message 2", true)
        
        session.addMessages(listOf(userMessage1, aiMessage, userMessage2))
        
        assertEquals(userMessage2, session.getLastUserMessage())
    }

    @Test
    fun `getLastAiMessage should return most recent AI message`() {
        val session = ChatSession.create()
        
        assertNull(session.getLastAiMessage())
        
        val userMessage = ChatMessage.create("User message", true)
        val aiMessage1 = ChatMessage.create("AI response 1", false)
        val aiMessage2 = ChatMessage.create("AI response 2", false)
        
        session.addMessages(listOf(userMessage, aiMessage1, aiMessage2))
        
        assertEquals(aiMessage2, session.getLastAiMessage())
    }

    @Test
    fun `getUserMessages should return only user messages`() {
        val session = ChatSession.create()
        
        val userMessage1 = ChatMessage.create("User 1", true)
        val aiMessage = ChatMessage.create("AI", false)
        val userMessage2 = ChatMessage.create("User 2", true)
        
        session.addMessages(listOf(userMessage1, aiMessage, userMessage2))
        
        val userMessages = session.getUserMessages()
        
        assertEquals(2, userMessages.size)
        assertTrue(userMessages.contains(userMessage1))
        assertTrue(userMessages.contains(userMessage2))
        assertFalse(userMessages.contains(aiMessage))
    }

    @Test
    fun `getAiMessages should return only AI messages`() {
        val session = ChatSession.create()
        
        val userMessage = ChatMessage.create("User", true)
        val aiMessage1 = ChatMessage.create("AI 1", false)
        val aiMessage2 = ChatMessage.create("AI 2", false)
        
        session.addMessages(listOf(userMessage, aiMessage1, aiMessage2))
        
        val aiMessages = session.getAiMessages()
        
        assertEquals(2, aiMessages.size)
        assertTrue(aiMessages.contains(aiMessage1))
        assertTrue(aiMessages.contains(aiMessage2))
        assertFalse(aiMessages.contains(userMessage))
    }

    @Test
    fun `getMessageCount should return correct count`() {
        val session = ChatSession.create()
        
        assertEquals(0, session.getMessageCount())
        
        session.addMessage(ChatMessage.create("Message 1", true))
        assertEquals(1, session.getMessageCount())
        
        session.addMessage(ChatMessage.create("Message 2", false))
        assertEquals(2, session.getMessageCount())
    }

    @Test
    fun `hasMessages should return correct boolean`() {
        val session = ChatSession.create()
        
        assertFalse(session.hasMessages())
        
        session.addMessage(ChatMessage.create("Message", true))
        
        assertTrue(session.hasMessages())
    }

    @Test
    fun `isEmpty should return correct boolean`() {
        val session = ChatSession.create()
        
        assertTrue(session.isEmpty())
        
        session.addMessage(ChatMessage.create("Message", true))
        
        assertFalse(session.isEmpty())
    }

    @Test
    fun `getDurationMinutes should calculate session duration`() {
        val session = ChatSession.create()
        
        // For a new session, duration should be very small (less than 1 minute)
        assertTrue(session.getDurationMinutes() < 1)
        
        // Test with custom timestamps
        val oldTimestamp = System.currentTimeMillis() - (5 * 60 * 1000) // 5 minutes ago
        val sessionWithHistory = ChatSession(
            id = "test",
            messages = mutableListOf(),
            createdAt = oldTimestamp,
            lastUpdated = System.currentTimeMillis()
        )
        
        assertTrue(sessionWithHistory.getDurationMinutes() >= 4) // Should be around 5 minutes
    }

    @Test
    fun `getRecentMessages should return last N messages`() {
        val session = ChatSession.create()
        
        val messages = (1..10).map { 
            ChatMessage.create("Message $it", it % 2 == 1)
        }
        
        session.addMessages(messages)
        
        val recent3 = session.getRecentMessages(3)
        assertEquals(3, recent3.size)
        assertEquals("Message 8", recent3[0].content)
        assertEquals("Message 9", recent3[1].content)
        assertEquals("Message 10", recent3[2].content)
        
        val recent15 = session.getRecentMessages(15)
        assertEquals(10, recent15.size) // Should return all messages when limit exceeds count
    }

    @Test
    fun `copy should create independent copy`() {
        val original = ChatSession.create()
        val message = ChatMessage.create("Original message", true)
        original.addMessage(message)
        
        val copy = original.copy()
        
        assertEquals(original.id, copy.id)
        assertEquals(original.createdAt, copy.createdAt)
        assertEquals(original.messages.size, copy.messages.size)
        
        // Verify it's a deep copy by modifying the copy
        val newMessage = ChatMessage.create("New message", false)
        copy.addMessage(newMessage)
        
        assertEquals(1, original.messages.size)
        assertEquals(2, copy.messages.size)
    }

    @Test
    fun `updateLastUpdated should update timestamp`() {
        val session = ChatSession.create()
        val originalTimestamp = session.lastUpdated
        
        Thread.sleep(1)
        session.updateLastUpdated()
        
        assertTrue(session.lastUpdated > originalTimestamp)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor should reject empty id`() {
        ChatSession(
            id = "",
            messages = mutableListOf(),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor should reject negative createdAt`() {
        ChatSession(
            id = "test",
            messages = mutableListOf(),
            createdAt = -1,
            lastUpdated = System.currentTimeMillis()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor should reject negative lastUpdated`() {
        ChatSession(
            id = "test",
            messages = mutableListOf(),
            createdAt = System.currentTimeMillis(),
            lastUpdated = -1
        )
    }
}