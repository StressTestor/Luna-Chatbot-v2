package com.luna.chat.data.local.entity

import com.luna.chat.domain.entity.MessageStatus
import org.junit.Test
import org.junit.Assert.*

class ChatMessageEntityTest {

    @Test
    fun `constructor should create valid entity`() {
        val entity = ChatMessageEntity(
            id = "test-id",
            content = "Test message",
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            sessionId = "session-1",
            status = MessageStatus.SENT.name
        )

        assertEquals("test-id", entity.id)
        assertEquals("Test message", entity.content)
        assertTrue(entity.isFromUser)
        assertTrue(entity.timestamp > 0)
        assertEquals("session-1", entity.sessionId)
        assertEquals(MessageStatus.SENT.name, entity.status)
    }

    @Test
    fun `create should generate entity from domain model`() {
        val domainMessage = com.luna.chat.domain.entity.ChatMessage.create(
            content = "Domain message",
            isFromUser = false,
            status = MessageStatus.DELIVERED
        )

        val entity = ChatMessageEntity.create(domainMessage, "session-123")

        assertEquals(domainMessage.id, entity.id)
        assertEquals(domainMessage.content, entity.content)
        assertEquals(domainMessage.isFromUser, entity.isFromUser)
        assertEquals(domainMessage.timestamp, entity.timestamp)
        assertEquals("session-123", entity.sessionId)
        assertEquals(domainMessage.status.name, entity.status)
    }

    @Test
    fun `toDomainModel should convert to domain entity`() {
        val entity = ChatMessageEntity(
            id = "entity-id",
            content = "Entity message",
            isFromUser = true,
            timestamp = 1234567890L,
            sessionId = "session-456",
            status = MessageStatus.FAILED.name
        )

        val domainMessage = entity.toDomainModel()

        assertEquals(entity.id, domainMessage.id)
        assertEquals(entity.content, domainMessage.content)
        assertEquals(entity.isFromUser, domainMessage.isFromUser)
        assertEquals(entity.timestamp, domainMessage.timestamp)
        assertEquals(MessageStatus.FAILED, domainMessage.status)
    }

    @Test
    fun `toDomainModel should handle invalid status gracefully`() {
        val entity = ChatMessageEntity(
            id = "entity-id",
            content = "Entity message",
            isFromUser = true,
            timestamp = 1234567890L,
            sessionId = "session-456",
            status = "INVALID_STATUS"
        )

        val domainMessage = entity.toDomainModel()

        // Should default to SENT for invalid status
        assertEquals(MessageStatus.SENT, domainMessage.status)
    }

    @Test
    fun `isUserMessage should return correct boolean`() {
        val userEntity = ChatMessageEntity(
            id = "user-id",
            content = "User message",
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            sessionId = "session",
            status = MessageStatus.SENT.name
        )

        val aiEntity = ChatMessageEntity(
            id = "ai-id", 
            content = "AI message",
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            sessionId = "session",
            status = MessageStatus.DELIVERED.name
        )

        assertTrue(userEntity.isUserMessage())
        assertFalse(aiEntity.isUserMessage())
    }

    @Test
    fun `isAiMessage should return correct boolean`() {
        val userEntity = ChatMessageEntity(
            id = "user-id",
            content = "User message", 
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            sessionId = "session",
            status = MessageStatus.SENT.name
        )

        val aiEntity = ChatMessageEntity(
            id = "ai-id",
            content = "AI message",
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            sessionId = "session", 
            status = MessageStatus.DELIVERED.name
        )

        assertFalse(userEntity.isAiMessage())
        assertTrue(aiEntity.isAiMessage())
    }

    @Test
    fun `getMessageStatus should return correct enum`() {
        val sentEntity = ChatMessageEntity(
            id = "id",
            content = "Message",
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            sessionId = "session",
            status = MessageStatus.SENT.name
        )

        val deliveredEntity = sentEntity.copy(status = MessageStatus.DELIVERED.name)
        val failedEntity = sentEntity.copy(status = MessageStatus.FAILED.name)

        assertEquals(MessageStatus.SENT, sentEntity.getMessageStatus())
        assertEquals(MessageStatus.DELIVERED, deliveredEntity.getMessageStatus())
        assertEquals(MessageStatus.FAILED, failedEntity.getMessageStatus())
    }

    @Test
    fun `getMessageStatus should handle invalid status`() {
        val invalidEntity = ChatMessageEntity(
            id = "id",
            content = "Message",
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            sessionId = "session",
            status = "INVALID"
        )

        assertEquals(MessageStatus.SENT, invalidEntity.getMessageStatus())
    }

    @Test
    fun `isRecent should check timestamp correctly`() {
        val now = System.currentTimeMillis()
        val recentEntity = ChatMessageEntity(
            id = "recent",
            content = "Recent message",
            isFromUser = true,
            timestamp = now - 1000, // 1 second ago
            sessionId = "session",
            status = MessageStatus.SENT.name
        )

        val oldEntity = ChatMessageEntity(
            id = "old",
            content = "Old message", 
            isFromUser = true,
            timestamp = now - (2 * 60 * 60 * 1000), // 2 hours ago
            sessionId = "session",
            status = MessageStatus.SENT.name
        )

        assertTrue(recentEntity.isRecent(60 * 60 * 1000)) // Within 1 hour
        assertFalse(oldEntity.isRecent(60 * 60 * 1000)) // Older than 1 hour
    }

    @Test
    fun `copy should create independent copy`() {
        val original = ChatMessageEntity(
            id = "original",
            content = "Original message",
            isFromUser = true,
            timestamp = 1234567890L,
            sessionId = "session-1",
            status = MessageStatus.SENT.name
        )

        val copy = original.copy(
            content = "Modified message",
            status = MessageStatus.DELIVERED.name
        )

        assertEquals("original", copy.id) // Unchanged
        assertEquals("Modified message", copy.content) // Changed
        assertTrue(copy.isFromUser) // Unchanged
        assertEquals(1234567890L, copy.timestamp) // Unchanged
        assertEquals("session-1", copy.sessionId) // Unchanged
        assertEquals(MessageStatus.DELIVERED.name, copy.status) // Changed

        // Original should be unchanged
        assertEquals("Original message", original.content)
        assertEquals(MessageStatus.SENT.name, original.status)
    }

    @Test
    fun `equals and hashCode should work correctly`() {
        val entity1 = ChatMessageEntity(
            id = "same-id",
            content = "Message",
            isFromUser = true,
            timestamp = 1234567890L,
            sessionId = "session",
            status = MessageStatus.SENT.name
        )

        val entity2 = ChatMessageEntity(
            id = "same-id",
            content = "Different message", // Different content
            isFromUser = false, // Different user flag
            timestamp = 9876543210L, // Different timestamp
            sessionId = "different-session", // Different session
            status = MessageStatus.DELIVERED.name // Different status
        )

        val entity3 = ChatMessageEntity(
            id = "different-id",
            content = "Message",
            isFromUser = true,
            timestamp = 1234567890L,
            sessionId = "session",
            status = MessageStatus.SENT.name
        )

        // Entities with same ID should be equal (based on Room's primary key)
        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())

        // Entities with different IDs should not be equal
        assertNotEquals(entity1, entity3)
    }

    @Test
    fun `toString should include key information`() {
        val entity = ChatMessageEntity(
            id = "test-id",
            content = "Test message",
            isFromUser = true,
            timestamp = 1234567890L,
            sessionId = "session-1",
            status = MessageStatus.SENT.name
        )

        val toString = entity.toString()

        assertTrue(toString.contains("test-id"))
        assertTrue(toString.contains("Test message"))
        assertTrue(toString.contains("true"))
        assertTrue(toString.contains("session-1"))
        assertTrue(toString.contains("SENT"))
    }

    @Test
    fun `validation should handle edge cases`() {
        // Test with empty content (should be allowed at entity level)
        val emptyContentEntity = ChatMessageEntity(
            id = "empty-content",
            content = "",
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            sessionId = "session",
            status = MessageStatus.SENT.name
        )

        assertEquals("", emptyContentEntity.content)

        // Test with very long content
        val longContent = "a".repeat(1000)
        val longContentEntity = ChatMessageEntity(
            id = "long-content",
            content = longContent,
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            sessionId = "session",
            status = MessageStatus.SENT.name
        )

        assertEquals(longContent, longContentEntity.content)
    }
}