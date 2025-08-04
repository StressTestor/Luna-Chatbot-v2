package com.luna.chat.domain.usecase

import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SendMessageUseCaseTest {
    
    private lateinit var sendMessageUseCase: SendMessageUseCase
    private lateinit var chatRepository: ChatRepository
    private lateinit var contentFilterUseCase: ContentFilterUseCase
    
    @Before
    fun setUp() {
        chatRepository = mockk()
        contentFilterUseCase = mockk()
        sendMessageUseCase = SendMessageUseCase(chatRepository, contentFilterUseCase)
    }
    
    @Test
    fun `invoke with appropriate message should return success`() = runTest {
        // Given
        val userMessage = "What is 2 + 2?"
        val aiResponse = "2 + 2 equals 4!"
        val filterResult = FilterResult(userMessage, false, null)
        val aiFilterResult = FilterResult(aiResponse, false, null)
        
        every { contentFilterUseCase.filterUserInput(userMessage) } returns filterResult
        every { contentFilterUseCase.filterAiResponse(aiResponse) } returns aiFilterResult
        coEvery { chatRepository.sendMessage(userMessage) } returns flowOf(Result.success(aiResponse))
        
        // When
        val results = sendMessageUseCase(userMessage).toList()
        
        // Then
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
        
        verify { contentFilterUseCase.filterUserInput(userMessage) }
        verify { contentFilterUseCase.filterAiResponse(aiResponse) }
    }
    
    @Test
    fun `invoke with inappropriate user input should return filtered error`() = runTest {
        // Given
        val inappropriateMessage = "I want to hurt someone"
        val filterResult = FilterResult(inappropriateMessage, true, "Inappropriate content detected")
        
        every { contentFilterUseCase.filterUserInput(inappropriateMessage) } returns filterResult
        
        // When
        val results = sendMessageUseCase(inappropriateMessage).toList()
        
        // Then
        assertEquals(1, results.size)
        val result = results[0]
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ContentFilterException)
        assertEquals("Let's talk about something else! What would you like to learn today? 📚", 
                    result.exceptionOrNull()?.message)
        
        verify { contentFilterUseCase.filterUserInput(inappropriateMessage) }
    }
    
    @Test
    fun `invoke with inappropriate AI response should return safe alternative`() = runTest {
        // Given
        val userMessage = "Tell me a story"
        val inappropriateAiResponse = "Here's a scary story about death..."
        val filterResult = FilterResult(userMessage, false, null)
        val aiFilterResult = FilterResult(inappropriateAiResponse, true, "Inappropriate AI response")
        
        every { contentFilterUseCase.filterUserInput(userMessage) } returns filterResult
        every { contentFilterUseCase.filterAiResponse(inappropriateAiResponse) } returns aiFilterResult
        coEvery { chatRepository.sendMessage(userMessage) } returns flowOf(Result.success(inappropriateAiResponse))
        
        // When
        val results = sendMessageUseCase(userMessage).toList()
        
        // Then
        assertEquals(2, results.size)
        
        // Second result should be safe alternative message
        val aiResult = results[1]
        assertTrue(aiResult.isSuccess)
        val aiChatMessage = aiResult.getOrNull()!!
        assertEquals("I'd rather talk about something else! What's your favorite subject in school? 🎓", 
                    aiChatMessage.content)
        assertFalse(aiChatMessage.isFromUser)
        assertEquals(MessageStatus.DELIVERED, aiChatMessage.status)
    }
    
    @Test
    fun `invoke with API failure should return error`() = runTest {
        // Given
        val userMessage = "What is the weather?"
        val filterResult = FilterResult(userMessage, false, null)
        val apiException = RuntimeException("Network error")
        
        every { contentFilterUseCase.filterUserInput(userMessage) } returns filterResult
        coEvery { chatRepository.sendMessage(userMessage) } returns flowOf(Result.failure(apiException))
        
        // When
        val results = sendMessageUseCase(userMessage).toList()
        
        // Then
        assertEquals(2, results.size)
        
        // First result should be user message
        val userResult = results[0]
        assertTrue(userResult.isSuccess)
        
        // Second result should be API error
        val errorResult = results[1]
        assertTrue(errorResult.isFailure)
        assertEquals(apiException, errorResult.exceptionOrNull())
    }
}