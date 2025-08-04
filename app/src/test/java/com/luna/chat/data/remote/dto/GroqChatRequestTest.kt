package com.luna.chat.data.remote.dto

import com.google.gson.Gson
import org.junit.Test
import org.junit.Assert.*

class GroqChatRequestTest {

    private val gson = Gson()

    @Test
    fun `create valid request should succeed`() {
        val messages = listOf(
            GroqMessage.createUserMessage("Hello, AI!")
        )
        val request = GroqChatRequest.create(messages)
        
        assertTrue(request.isValid())
        assertEquals(GroqChatRequest.DEFAULT_MODEL, request.model)
        assertEquals(messages, request.messages)
        assertEquals(GroqChatRequest.DEFAULT_TEMPERATURE, request.temperature, 0.001)
        assertEquals(GroqChatRequest.DEFAULT_MAX_TOKENS, request.maxTokens)
        assertFalse(request.stream)
    }

    @Test
    fun `create request with custom parameters should succeed`() {
        val messages = listOf(
            GroqMessage.createUserMessage("Hello, AI!")
        )
        val customModel = "deepseek/deepseek-chat-v3-0324:free"
        val customTemp = 0.5
        val customTokens = 2000
        
        val request = GroqChatRequest.create(
            messages = messages,
            model = customModel,
            temperature = customTemp,
            maxTokens = customTokens
        )
        
        assertTrue(request.isValid())
        assertEquals(customModel, request.model)
        assertEquals(customTemp, request.temperature, 0.001)
        assertEquals(customTokens, request.maxTokens)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty model should throw exception`() {
        val messages = listOf(GroqMessage.createUserMessage("Hello"))
        GroqChatRequest(
            model = "",
            messages = messages
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty messages should throw exception`() {
        GroqChatRequest(
            model = "test-model",
            messages = emptyList()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `temperature below range should throw exception`() {
        val messages = listOf(GroqMessage.createUserMessage("Hello"))
        GroqChatRequest(
            model = "test-model",
            messages = messages,
            temperature = -0.1
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `temperature above range should throw exception`() {
        val messages = listOf(GroqMessage.createUserMessage("Hello"))
        GroqChatRequest(
            model = "test-model",
            messages = messages,
            temperature = 2.1
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative max tokens should throw exception`() {
        val messages = listOf(GroqMessage.createUserMessage("Hello"))
        GroqChatRequest(
            model = "test-model",
            messages = messages,
            maxTokens = -1
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `max tokens exceeding limit should throw exception`() {
        val messages = listOf(GroqMessage.createUserMessage("Hello"))
        GroqChatRequest(
            model = "test-model",
            messages = messages,
            maxTokens = GroqChatRequest.MAX_ALLOWED_TOKENS + 1
        )
    }

    @Test
    fun `max tokens at limit should be valid`() {
        val messages = listOf(GroqMessage.createUserMessage("Hello"))
        val request = GroqChatRequest(
            model = "test-model",
            messages = messages,
            maxTokens = GroqChatRequest.MAX_ALLOWED_TOKENS
        )
        
        assertTrue(request.isValid())
    }

    @Test
    fun `temperature at boundaries should be valid`() {
        val messages = listOf(GroqMessage.createUserMessage("Hello"))
        
        val request1 = GroqChatRequest(
            model = "test-model",
            messages = messages,
            temperature = 0.0
        )
        assertTrue(request1.isValid())
        
        val request2 = GroqChatRequest(
            model = "test-model",
            messages = messages,
            temperature = 2.0
        )
        assertTrue(request2.isValid())
    }

    @Test
    fun `serialize to JSON should contain correct fields`() {
        val messages = listOf(
            GroqMessage.createUserMessage("Hello, AI!")
        )
        val request = GroqChatRequest.create(messages)
        
        val json = gson.toJson(request)
        
        assertTrue(json.contains("\"model\":\"${GroqChatRequest.DEFAULT_MODEL}\""))
        assertTrue(json.contains("\"temperature\":${GroqChatRequest.DEFAULT_TEMPERATURE}"))
        assertTrue(json.contains("\"max_tokens\":${GroqChatRequest.DEFAULT_MAX_TOKENS}"))
        assertTrue(json.contains("\"stream\":false"))
        assertTrue(json.contains("\"messages\""))
    }

    @Test
    fun `deserialize from JSON should create valid object`() {
        val json = """
            {
                "model": "deepseek/deepseek-chat-v3-0324:free",
                "messages": [
                    {
                        "role": "user",
                        "content": "Hello, AI!"
                    }
                ],
                "temperature": 0.7,
                "max_tokens": 1000,
                "stream": false
            }
        """.trimIndent()
        
        val request = gson.fromJson(json, GroqChatRequest::class.java)
        
        assertTrue(request.isValid())
        assertEquals("deepseek/deepseek-chat-v3-0324:free", request.model)
        assertEquals(1, request.messages.size)
        assertEquals("Hello, AI!", request.messages[0].content)
        assertEquals(0.7, request.temperature, 0.001)
        assertEquals(1000, request.maxTokens)
        assertFalse(request.stream)
    }
}

class GroqMessageTest {

    @Test
    fun `create user message should succeed`() {
        val message = GroqMessage.createUserMessage("Hello, AI!")
        
        assertTrue(message.isValid())
        assertEquals(GroqMessage.ROLE_USER, message.role)
        assertEquals("Hello, AI!", message.content)
        assertTrue(message.isUserMessage())
        assertFalse(message.isAssistantMessage())
    }

    @Test
    fun `create assistant message should succeed`() {
        val message = GroqMessage.createAssistantMessage("Hello, human!")
        
        assertTrue(message.isValid())
        assertEquals(GroqMessage.ROLE_ASSISTANT, message.role)
        assertEquals("Hello, human!", message.content)
        assertFalse(message.isUserMessage())
        assertTrue(message.isAssistantMessage())
    }

    @Test
    fun `create system message should succeed`() {
        val message = GroqMessage.createSystemMessage("You are a helpful assistant.")
        
        assertTrue(message.isValid())
        assertEquals(GroqMessage.ROLE_SYSTEM, message.role)
        assertEquals("You are a helpful assistant.", message.content)
        assertFalse(message.isUserMessage())
        assertFalse(message.isAssistantMessage())
    }

    @Test
    fun `create message should trim whitespace`() {
        val message = GroqMessage.createUserMessage("  Hello, AI!  ")
        
        assertEquals("Hello, AI!", message.content)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty role should throw exception`() {
        GroqMessage(
            role = "",
            content = "Valid content"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid role should throw exception`() {
        GroqMessage(
            role = "invalid_role",
            content = "Valid content"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty content should throw exception`() {
        GroqMessage(
            role = GroqMessage.ROLE_USER,
            content = ""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank content should throw exception`() {
        GroqMessage(
            role = GroqMessage.ROLE_USER,
            content = "   "
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `content exceeding max length should throw exception`() {
        val longContent = "a".repeat(GroqMessage.MAX_CONTENT_LENGTH + 1)
        GroqMessage(
            role = GroqMessage.ROLE_USER,
            content = longContent
        )
    }

    @Test
    fun `content at max length should be valid`() {
        val maxContent = "a".repeat(GroqMessage.MAX_CONTENT_LENGTH)
        val message = GroqMessage(
            role = GroqMessage.ROLE_USER,
            content = maxContent
        )
        
        assertTrue(message.isValid())
    }

    @Test
    fun `all valid roles should be accepted`() {
        val userMessage = GroqMessage(GroqMessage.ROLE_USER, "User content")
        val assistantMessage = GroqMessage(GroqMessage.ROLE_ASSISTANT, "Assistant content")
        val systemMessage = GroqMessage(GroqMessage.ROLE_SYSTEM, "System content")
        
        assertTrue(userMessage.isValid())
        assertTrue(assistantMessage.isValid())
        assertTrue(systemMessage.isValid())
    }
}