package com.luna.chat.data.remote.dto

import com.google.gson.Gson
import org.junit.Test
import org.junit.Assert.*

class GroqChatResponseTest {

    private val gson = Gson()

    @Test
    fun `create valid response should succeed`() {
        val message = GroqMessage.createAssistantMessage("Hello, human!")
        val choice = GroqChoice(message, GroqChatResponse.FINISH_REASON_STOP)
        val usage = GroqUsage(10, 20, 30)
        val response = GroqChatResponse(listOf(choice), usage)
        
        assertTrue(response.isValid())
        assertEquals(1, response.choices.size)
        assertEquals(choice, response.choices[0])
        assertEquals(usage, response.usage)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty choices should throw exception`() {
        val usage = GroqUsage(10, 20, 30)
        GroqChatResponse(emptyList(), usage)
    }

    @Test
    fun `getFirstChoice should return first choice`() {
        val message1 = GroqMessage.createAssistantMessage("First response")
        val message2 = GroqMessage.createAssistantMessage("Second response")
        val choice1 = GroqChoice(message1, GroqChatResponse.FINISH_REASON_STOP)
        val choice2 = GroqChoice(message2, GroqChatResponse.FINISH_REASON_STOP)
        val usage = GroqUsage(10, 20, 30)
        val response = GroqChatResponse(listOf(choice1, choice2), usage)
        
        assertEquals(choice1, response.getFirstChoice())
    }

    @Test
    fun `getAssistantMessage should return first choice message content`() {
        val message = GroqMessage.createAssistantMessage("Hello, human!")
        val choice = GroqChoice(message, GroqChatResponse.FINISH_REASON_STOP)
        val usage = GroqUsage(10, 20, 30)
        val response = GroqChatResponse(listOf(choice), usage)
        
        assertEquals("Hello, human!", response.getAssistantMessage())
    }

    @Test
    fun `isComplete should return true for stop finish reason`() {
        val message = GroqMessage.createAssistantMessage("Complete response")
        val choice = GroqChoice(message, GroqChatResponse.FINISH_REASON_STOP)
        val usage = GroqUsage(10, 20, 30)
        val response = GroqChatResponse(listOf(choice), usage)
        
        assertTrue(response.isComplete())
    }

    @Test
    fun `isComplete should return false for non-stop finish reason`() {
        val message = GroqMessage.createAssistantMessage("Incomplete response")
        val choice = GroqChoice(message, GroqChatResponse.FINISH_REASON_LENGTH)
        val usage = GroqUsage(10, 20, 30)
        val response = GroqChatResponse(listOf(choice), usage)
        
        assertFalse(response.isComplete())
    }

    @Test
    fun `deserialize from JSON should create valid object`() {
        val json = """
            {
                "choices": [
                    {
                        "message": {
                            "role": "assistant",
                            "content": "Hello, how can I help you today?"
                        },
                        "finish_reason": "stop",
                        "index": 0
                    }
                ],
                "usage": {
                    "prompt_tokens": 15,
                    "completion_tokens": 25,
                    "total_tokens": 40
                },
                "id": "chatcmpl-123",
                "object": "chat.completion",
                "created": 1677652288,
                "model": "mixtral-8x7b-32768"
            }
        """.trimIndent()
        
        val response = gson.fromJson(json, GroqChatResponse::class.java)
        
        assertTrue(response.isValid())
        assertEquals(1, response.choices.size)
        assertEquals("Hello, how can I help you today?", response.getAssistantMessage())
        assertEquals("chatcmpl-123", response.id)
        assertEquals("chat.completion", response.objectType)
        assertEquals(1677652288L, response.created)
        assertEquals("mixtral-8x7b-32768", response.model)
    }
}

class GroqChoiceTest {

    @Test
    fun `create valid choice should succeed`() {
        val message = GroqMessage.createAssistantMessage("Hello!")
        val choice = GroqChoice(message, GroqChatResponse.FINISH_REASON_STOP)
        
        assertTrue(choice.isValid())
        assertEquals(message, choice.message)
        assertEquals(GroqChatResponse.FINISH_REASON_STOP, choice.finishReason)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty finish reason should throw exception`() {
        val message = GroqMessage.createAssistantMessage("Hello!")
        GroqChoice(message, "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid finish reason should throw exception`() {
        val message = GroqMessage.createAssistantMessage("Hello!")
        GroqChoice(message, "invalid_reason")
    }

    @Test
    fun `isComplete should return true for stop finish reason`() {
        val message = GroqMessage.createAssistantMessage("Complete response")
        val choice = GroqChoice(message, GroqChatResponse.FINISH_REASON_STOP)
        
        assertTrue(choice.isComplete())
    }

    @Test
    fun `isComplete should return false for non-stop finish reason`() {
        val message = GroqMessage.createAssistantMessage("Incomplete response")
        val choice = GroqChoice(message, GroqChatResponse.FINISH_REASON_LENGTH)
        
        assertFalse(choice.isComplete())
    }

    @Test
    fun `wasContentFiltered should return true for content filter finish reason`() {
        val message = GroqMessage.createAssistantMessage("Filtered response")
        val choice = GroqChoice(message, GroqChatResponse.FINISH_REASON_CONTENT_FILTER)
        
        assertTrue(choice.wasContentFiltered())
    }

    @Test
    fun `wasContentFiltered should return false for other finish reasons`() {
        val message = GroqMessage.createAssistantMessage("Normal response")
        val choice = GroqChoice(message, GroqChatResponse.FINISH_REASON_STOP)
        
        assertFalse(choice.wasContentFiltered())
    }

    @Test
    fun `all valid finish reasons should be accepted`() {
        val message = GroqMessage.createAssistantMessage("Test response")
        
        val stopChoice = GroqChoice(message, GroqChatResponse.FINISH_REASON_STOP)
        val lengthChoice = GroqChoice(message, GroqChatResponse.FINISH_REASON_LENGTH)
        val filterChoice = GroqChoice(message, GroqChatResponse.FINISH_REASON_CONTENT_FILTER)
        
        assertTrue(stopChoice.isValid())
        assertTrue(lengthChoice.isValid())
        assertTrue(filterChoice.isValid())
    }
}

class GroqUsageTest {

    @Test
    fun `create valid usage should succeed`() {
        val usage = GroqUsage(10, 20, 30)
        
        assertTrue(usage.isValid())
        assertEquals(10, usage.promptTokens)
        assertEquals(20, usage.completionTokens)
        assertEquals(30, usage.totalTokens)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative prompt tokens should throw exception`() {
        GroqUsage(-1, 20, 19)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative completion tokens should throw exception`() {
        GroqUsage(10, -1, 9)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative total tokens should throw exception`() {
        GroqUsage(10, 20, -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `incorrect total tokens should throw exception`() {
        GroqUsage(10, 20, 25) // Should be 30
    }

    @Test
    fun `zero tokens should be valid`() {
        val usage = GroqUsage(0, 0, 0)
        assertTrue(usage.isValid())
    }

    @Test
    fun `getCostEstimate should calculate correctly`() {
        val usage = GroqUsage(1000, 2000, 3000)
        val inputCost = 0.001 // $0.001 per 1K tokens
        val outputCost = 0.002 // $0.002 per 1K tokens
        
        val expectedCost = (1000 * inputCost / 1000) + (2000 * outputCost / 1000)
        val actualCost = usage.getCostEstimate(inputCost, outputCost)
        
        assertEquals(expectedCost, actualCost, 0.0001)
    }

    @Test
    fun `getCostEstimate with default rates should work`() {
        val usage = GroqUsage(1000, 1000, 2000)
        val cost = usage.getCostEstimate()
        
        // Default rate is 0.0002 per 1K tokens for both input and output
        val expectedCost = (1000 * 0.0002 / 1000) + (1000 * 0.0002 / 1000)
        assertEquals(expectedCost, cost, 0.0001)
    }
}