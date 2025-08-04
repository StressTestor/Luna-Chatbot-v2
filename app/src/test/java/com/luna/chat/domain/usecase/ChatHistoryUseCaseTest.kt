package com.luna.chat.domain.usecase

import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ChatHistoryUseCaseTest {
    
    private lateinit var chatHistoryUseCase: ChatHistoryUseCase
    private lateinit var chatRepository: ChatRepository
    
    @Before
    fun setUp() {
        chatRepository = mockk(relaxed = true)
        chatHistoryUseCase = ChatHistoryUseCase(chatRepository)
    }
    
    @Test
    fun `getChatHistory should return recent messages within age limit`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val recentMessage = ChatMessage.create(
            content = "What is 2 + 2?",
            isFromUser = true,
            timestamp = currentTime - (2 * 24 * 60 * 60 * 1000) // 2 days ago
        )
        val oldMessage = ChatMessage.create(
            content = "Old message",
            isFromUser = true,
            timestamp = currentTime - (10 * 24 * 60 * 60 * 1000) // 10 days ago
        )
        
        coEvery { chatRepository.getChatHistory() } returns flowOf(listOf(recentMessage, oldMessage))
        
        // When
        val result = chatHistoryUseCase.getChatHistory().toList()
        
        // Then
        assertEquals(1, result.size)
        assertEquals(1, result[0].size)
        assertEquals(recentMessage.content, result[0][0].content)
    }
    
    @Test
    fun `getChatHistory should limit messages to maximum count`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val messages = (1..150).map { index ->
            ChatMessage.create(
                content = "Message $index",
                isFromUser = true,
                timestamp = currentTime - (index * 1000) // Recent messages
            )
        }
        
        coEvery { chatRepository.getChatHistory() } returns flowOf(messages)
        
        // When
        val result = chatHistoryUseCase.getChatHistory().toList()
        
        // Then
        assertEquals(1, result.size)
        assertEquals(100, result[0].size) // Should be limited to MAX_MESSAGES_TO_KEEP
    }
    
    @Test
    fun `saveMessage should save safe messages`() = runTest {
        // Given
        val safeMessage = ChatMessage.create(
            content = "What is the capital of France?",
            isFromUser = true
        )
        
        // When
        chatHistoryUseCase.saveMessage(safeMessage)
        
        // Then
        coVerify { chatRepository.saveChatHistory(listOf(safeMessage)) }
    }
    
    @Test
    fun `saveMessage should not save messages with personal information`() = runTest {
        // Given
        val personalMessage = ChatMessage.create(
            content = "My name is John and I live at 123 Main Street",
            isFromUser = true
        )
        
        // When
        chatHistoryUseCase.saveMessage(personalMessage)
        
        // Then
        coVerify(exactly = 0) { chatRepository.saveChatHistory(any()) }
    }
    
    @Test
    fun `saveMessages should filter out unsafe messages`() = runTest {
        // Given
        val safeMessage = ChatMessage.create(
            content = "What is 2 + 2?",
            isFromUser = true
        )
        val personalMessage = ChatMessage.create(
            content = "My name is John",
            isFromUser = true
        )
        val messages = listOf(safeMessage, personalMessage)
        
        // When
        chatHistoryUseCase.saveMessages(messages)
        
        // Then
        coVerify { chatRepository.saveChatHistory(listOf(safeMessage)) }
    }
    
    @Test
    fun `saveMessages should not save anything if all messages are unsafe`() = runTest {
        // Given
        val personalMessage1 = ChatMessage.create(
            content = "My name is John",
            isFromUser = true
        )
        val personalMessage2 = ChatMessage.create(
            content = "I live at 123 Main Street",
            isFromUser = true
        )
        val messages = listOf(personalMessage1, personalMessage2)
        
        // When
        chatHistoryUseCase.saveMessages(messages)
        
        // Then
        coVerify(exactly = 0) { chatRepository.saveChatHistory(any()) }
    }
    
    @Test
    fun `clearChatHistory should call repository clearChatHistory`() = runTest {
        // When
        chatHistoryUseCase.clearChatHistory()
        
        // Then
        coVerify { chatRepository.clearChatHistory() }
    }
    
    @Test
    fun `performAutomaticCleanup should remove old messages`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val recentMessage = ChatMessage.create(
            content = "Recent message",
            isFromUser = true,
            timestamp = currentTime - (2 * 24 * 60 * 60 * 1000) // 2 days ago
        )
        val oldMessage = ChatMessage.create(
            content = "Old message",
            isFromUser = true,
            timestamp = currentTime - (10 * 24 * 60 * 60 * 1000) // 10 days ago
        )
        
        coEvery { chatRepository.getChatHistory() } returns flowOf(listOf(recentMessage, oldMessage))
        
        // When
        chatHistoryUseCase.performAutomaticCleanup()
        
        // Then
        coVerify { chatRepository.clearChatHistory() }
        coVerify { chatRepository.saveChatHistory(listOf(recentMessage)) }
    }
    
    @Test
    fun `performAutomaticCleanup should not modify storage if no cleanup needed`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val recentMessage = ChatMessage.create(
            content = "Recent message",
            isFromUser = true,
            timestamp = currentTime - (2 * 24 * 60 * 60 * 1000) // 2 days ago
        )
        
        coEvery { chatRepository.getChatHistory() } returns flowOf(listOf(recentMessage))
        
        // When
        chatHistoryUseCase.performAutomaticCleanup()
        
        // Then
        coVerify(exactly = 0) { chatRepository.clearChatHistory() }
        coVerify(exactly = 0) { chatRepository.saveChatHistory(any()) }
    }
    
    @Test
    fun `getMessageCount should return correct count`() = runTest {
        // Given
        val messages = listOf(
            ChatMessage.create("Message 1", true),
            ChatMessage.create("Message 2", false)
        )
        coEvery { chatRepository.getChatHistory() } returns flowOf(messages)
        
        // When
        val result = chatHistoryUseCase.getMessageCount().toList()
        
        // Then
        assertEquals(1, result.size)
        assertEquals(2, result[0])
    }
    
    @Test
    fun `should not save very long messages`() = runTest {
        // Given
        val longContent = "A".repeat(600) // Exceeds MAX_MESSAGE_LENGTH_TO_STORE
        val longMessage = ChatMessage.create(
            content = longContent,
            isFromUser = true
        )
        
        // When
        chatHistoryUseCase.saveMessage(longMessage)
        
        // Then
        coVerify(exactly = 0) { chatRepository.saveChatHistory(any()) }
    }
    
    @Test
    fun `should not save messages with family information`() = runTest {
        // Given
        val familyMessages = listOf(
            "My mom works at the hospital",
            "My dad is a teacher",
            "My brother goes to high school",
            "My sister is 8 years old"
        )
        
        // When & Then
        familyMessages.forEach { content ->
            val message = ChatMessage.create(content, true)
            chatHistoryUseCase.saveMessage(message)
            coVerify(exactly = 0) { chatRepository.saveChatHistory(listOf(message)) }
        }
    }
    
    @Test
    fun `should not save messages with location information`() = runTest {
        // Given
        val locationMessages = listOf(
            "I live in New York",
            "My address is 123 Main Street",
            "My school is Roosevelt Elementary",
            "I go to school at Lincoln Middle School"
        )
        
        // When & Then
        locationMessages.forEach { content ->
            val message = ChatMessage.create(content, true)
            chatHistoryUseCase.saveMessage(message)
            coVerify(exactly = 0) { chatRepository.saveChatHistory(listOf(message)) }
        }
    }
}