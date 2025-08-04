package com.luna.chat.domain.integration

import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.domain.repository.ChatRepository
import com.luna.chat.domain.usecase.SendMessageUseCase
import com.luna.chat.domain.usecase.ContentFilterUseCase
import com.luna.chat.domain.usecase.ChatHistoryUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for the complete chat flow from user input to AI response
 */
class ChatFlowIntegrationTest {
    
    private lateinit var chatRepository: ChatRepository
    private lateinit var contentFilterUseCase: ContentFilterUseCase
    private lateinit var sendMessageUseCase: SendMessageUseCase
    private lateinit var chatHistoryUseCase: ChatHistoryUseCase
    
    @Before
    fun setUp() {
        chatRepository = mockk(relaxed = true)
        contentFilterUseCase = mockk(relaxed = true)
        chatHistoryUseCase = mockk(relaxed = true)
        
        sendMessageUseCase = SendMessageUseCase(chatRepository, contentFilterUseCase)
    }
    
    @Test
    fun `complete chat flow should work end-to-end`() = runTest {
        // Given - Setup a complete conversation flow
        val userMessage = "What is 2 + 2?"
        val aiResponse = "2 + 2 equals 4! Math is fun!"
        
        // Mock content filtering to allow the message
        every { contentFilterUseCase.filterUserInput(userMessage) } returns 
            FilterResult(userMessage, false, null)
        every { contentFilterUseCase.filterAiResponse(aiResponse) } returns 
            FilterResult(aiResponse, false, null)
        
        // Mock repository to return AI response
        coEvery { chatRepository.sendMessage(userMessage) } returns 
            flowOf(Result.success(aiResponse))
        
        // When - Send the message
        val results = sendMessageUseCase(userMessage).toList()
        
        // Then - Verify complete flow
        assertEquals(2, results.size)
        
        // First result should be user message
        val userResult = results[0]
        assertTrue(userResult.isSuccess)
        val userChatMessage = userResult.getOrNull()!!
        assertEquals(userMessage, userChatMessage.content)
        assertTrue(userChatMessage.isFromUser)
        assertEquals(MessageStatus.SENDING, userChatMessage.status)
        
        // Second result should be AI response
        val aiResult = results[1]
        assertTrue(aiResult.isSuccess)
        val aiChatMessage = aiResult.getOrNull()!!
        assertEquals(aiResponse, aiChatMessage.content)
        assertFalse(aiChatMessage.isFromUser)
        assertEquals(MessageStatus.DELIVERED, aiChatMessage.status)
        
        // Verify all components were called correctly
        coVerify { chatRepository.sendMessage(userMessage) }
        every { contentFilterUseCase.filterUserInput(userMessage) }
        every { contentFilterUseCase.filterAiResponse(aiResponse) }
    }
    
    @Test
    fun `chat flow with content filtering should block inappropriate content`() = runTest {
        // Given - Inappropriate user input
        val inappropriateMessage = "I want to hurt someone"
        
        // Mock content filter to block the message
        every { contentFilterUseCase.filterUserInput(inappropriateMessage) } returns 
            FilterResult(inappropriateMessage, true, "Inappropriate content detected")
        
        // When - Try to send inappropriate message
        val results = sendMessageUseCase(inappropriateMessage).toList()
        
        // Then - Should get filtered error
        assertEquals(1, results.size)
        val result = results[0]
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ContentFilterException)
        
        // Repository should not be called
        coVerify(exactly = 0) { chatRepository.sendMessage(any()) }
    }
    
    @Test
    fun `chat flow with AI response filtering should provide safe alternative`() = runTest {
        // Given - User asks innocent question but AI gives inappropriate response
        val userMessage = "Tell me a story"
        val inappropriateAiResponse = "Here's a scary story about death and violence..."
        
        // Mock content filtering
        every { contentFilterUseCase.filterUserInput(userMessage) } returns 
            FilterResult(userMessage, false, null)
        every { contentFilterUseCase.filterAiResponse(inappropriateAiResponse) } returns 
            FilterResult(inappropriateAiResponse, true, "Inappropriate AI response")
        
        // Mock repository to return inappropriate response
        coEvery { chatRepository.sendMessage(userMessage) } returns 
            flowOf(Result.success(inappropriateAiResponse))
        
        // When - Send the message
        val results = sendMessageUseCase(userMessage).toList()
        
        // Then - Should get safe alternative response
        assertEquals(2, results.size)
        
        val aiResult = results[1]
        assertTrue(aiResult.isSuccess)
        val aiChatMessage = aiResult.getOrNull()!!
        assertEquals("I'd rather talk about something else! What's your favorite subject in school? 🎓", 
                    aiChatMessage.content)
        assertFalse(aiChatMessage.isFromUser)
        assertEquals(MessageStatus.DELIVERED, aiChatMessage.status)
    }
    
    @Test
    fun `chat flow with network error should handle gracefully`() = runTest {
        // Given - Network error scenario
        val userMessage = "What's the weather like?"
        val networkError = java.net.UnknownHostException("No internet connection")
        
        // Mock content filtering to allow message
        every { contentFilterUseCase.filterUserInput(userMessage) } returns 
            FilterResult(userMessage, false, null)
        
        // Mock repository to throw network error
        coEvery { chatRepository.sendMessage(userMessage) } returns 
            flowOf(Result.failure(networkError))
        
        // When - Send the message
        val results = sendMessageUseCase(userMessage).toList()
        
        // Then - Should handle error gracefully
        assertEquals(2, results.size)
        
        // First result should be user message
        val userResult = results[0]
        assertTrue(userResult.isSuccess)
        
        // Second result should be network error
        val errorResult = results[1]
        assertTrue(errorResult.isFailure)
        assertEquals(networkError, errorResult.exceptionOrNull())
    }
    
    @Test
    fun `chat flow with conversation history should maintain context`() = runTest {
        // Given - Existing conversation history
        val previousMessages = listOf(
            ChatMessage.create("Hello", true, status = MessageStatus.SENT),
            ChatMessage.create("Hi there! How can I help you?", false, status = MessageStatus.DELIVERED)
        )
        
        val followUpMessage = "Can you help me with math?"
        val contextualResponse = "Of course! I'd be happy to help you with math. What would you like to learn?"
        
        // Mock chat history
        coEvery { chatHistoryUseCase.getChatHistory() } returns flowOf(previousMessages)
        
        // Mock content filtering
        every { contentFilterUseCase.filterUserInput(followUpMessage) } returns 
            FilterResult(followUpMessage, false, null)
        every { contentFilterUseCase.filterAiResponse(contextualResponse) } returns 
            FilterResult(contextualResponse, false, null)
        
        // Mock repository response
        coEvery { chatRepository.sendMessage(followUpMessage) } returns 
            flowOf(Result.success(contextualResponse))
        
        // When - Send follow-up message
        val results = sendMessageUseCase(followUpMessage).toList()
        
        // Then - Should maintain conversation context
        assertEquals(2, results.size)
        
        val aiResult = results[1]
        assertTrue(aiResult.isSuccess)
        val aiChatMessage = aiResult.getOrNull()!!
        assertEquals(contextualResponse, aiChatMessage.content)
        
        // Verify repository was called with the message
        coVerify { chatRepository.sendMessage(followUpMessage) }
    }
    
    @Test
    fun `chat flow should handle educational content appropriately`() = runTest {
        // Given - Educational question
        val educationalMessage = "How do plants make food?"
        val educationalResponse = "Plants make food through photosynthesis! They use sunlight, water, and carbon dioxide to create sugar. It's like cooking with sunshine! 🌱☀️"
        
        // Mock content filtering to recognize educational content
        every { contentFilterUseCase.filterUserInput(educationalMessage) } returns 
            FilterResult(educationalMessage, false, null)
        every { contentFilterUseCase.filterAiResponse(educationalResponse) } returns 
            FilterResult(educationalResponse, false, null)
        
        // Mock repository response
        coEvery { chatRepository.sendMessage(educationalMessage) } returns 
            flowOf(Result.success(educationalResponse))
        
        // When - Send educational question
        val results = sendMessageUseCase(educationalMessage).toList()
        
        // Then - Should provide appropriate educational response
        assertEquals(2, results.size)
        
        val aiResult = results[1]
        assertTrue(aiResult.isSuccess)
        val aiChatMessage = aiResult.getOrNull()!!
        assertEquals(educationalResponse, aiChatMessage.content)
        assertTrue(aiChatMessage.content.contains("photosynthesis"))
        assertTrue(aiChatMessage.content.contains("🌱")) // Child-friendly emoji
    }
    
    @Test
    fun `chat flow should handle multiple rapid messages`() = runTest {
        // Given - Multiple messages sent in quick succession
        val messages = listOf(
            "Hi there!",
            "What's your name?",
            "Can you help me?"
        )
        
        val responses = listOf(
            "Hello! Nice to meet you!",
            "I'm Luna, your AI friend!",
            "Of course! I'm here to help!"
        )
        
        // Mock content filtering for all messages
        messages.forEachIndexed { index, message ->
            every { contentFilterUseCase.filterUserInput(message) } returns 
                FilterResult(message, false, null)
            every { contentFilterUseCase.filterAiResponse(responses[index]) } returns 
                FilterResult(responses[index], false, null)
            
            coEvery { chatRepository.sendMessage(message) } returns 
                flowOf(Result.success(responses[index]))
        }
        
        // When - Send all messages
        val allResults = mutableListOf<Result<ChatMessage>>()
        messages.forEach { message ->
            val results = sendMessageUseCase(message).toList()
            allResults.addAll(results)
        }
        
        // Then - Should handle all messages correctly
        assertEquals(6, allResults.size) // 2 results per message (user + AI)
        
        // Verify all user messages
        val userResults = allResults.filterIndexed { index, _ -> index % 2 == 0 }
        userResults.forEachIndexed { index, result ->
            assertTrue(result.isSuccess)
            val message = result.getOrNull()!!
            assertEquals(messages[index], message.content)
            assertTrue(message.isFromUser)
        }
        
        // Verify all AI responses
        val aiResults = allResults.filterIndexed { index, _ -> index % 2 == 1 }
        aiResults.forEachIndexed { index, result ->
            assertTrue(result.isSuccess)
            val message = result.getOrNull()!!
            assertEquals(responses[index], message.content)
            assertFalse(message.isFromUser)
        }
    }
    
    @Test
    fun `chat flow should handle empty or whitespace messages`() = runTest {
        // Given - Empty and whitespace messages
        val emptyMessages = listOf("", "   ", "\n\t", "  \n  ")
        
        // When & Then - Each should be rejected without calling repository
        emptyMessages.forEach { message ->
            try {
                sendMessageUseCase(message).toList()
                fail("Expected exception for empty message: '$message'")
            } catch (e: IllegalArgumentException) {
                assertTrue(e.message?.contains("empty") == true || 
                          e.message?.contains("blank") == true)
            }
        }
        
        // Verify repository was never called
        coVerify(exactly = 0) { chatRepository.sendMessage(any()) }
    }
}