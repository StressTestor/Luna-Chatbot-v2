package com.luna.chat.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GroqChatRequest(
    @SerializedName("model")
    val model: String = DEFAULT_MODEL,
    @SerializedName("messages")
    val messages: List<GroqMessage>,
    @SerializedName("temperature")
    val temperature: Double = DEFAULT_TEMPERATURE,
    @SerializedName("max_tokens")
    val maxTokens: Int = DEFAULT_MAX_TOKENS,
    @SerializedName("stream")
    val stream: Boolean = false
) {
    init {
        validateRequest()
    }
    
    private fun validateRequest() {
        require(model.isNotBlank()) { "Model cannot be blank" }
        require(messages.isNotEmpty()) { "Messages cannot be empty" }
        require(temperature in 0.0..2.0) { "Temperature must be between 0.0 and 2.0" }
        require(maxTokens > 0) { "Max tokens must be positive" }
        require(maxTokens <= MAX_ALLOWED_TOKENS) { "Max tokens cannot exceed $MAX_ALLOWED_TOKENS" }
        
        // Validate all messages
        messages.forEach { message ->
            require(message.isValid()) { "All messages must be valid" }
        }
    }
    
    fun isValid(): Boolean {
        return try {
            validateRequest()
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    
    companion object {
        const val DEFAULT_MODEL = "meta-llama/llama-4-maverick-17b-128e-instruct"
        const val DEFAULT_TEMPERATURE = 0.7
        const val DEFAULT_MAX_TOKENS = 1000
        const val MAX_ALLOWED_TOKENS = 4000
        
        fun create(
            messages: List<GroqMessage>,
            model: String = DEFAULT_MODEL,
            temperature: Double = DEFAULT_TEMPERATURE,
            maxTokens: Int = DEFAULT_MAX_TOKENS
        ): GroqChatRequest {
            return GroqChatRequest(
                model = model,
                messages = messages,
                temperature = temperature,
                maxTokens = maxTokens,
                stream = false
            )
        }
    }
}

data class GroqMessage(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
) {
    init {
        validateMessage()
    }
    
    private fun validateMessage() {
        require(role.isNotBlank()) { "Role cannot be blank" }
        require(role in VALID_ROLES) { "Role must be one of: ${VALID_ROLES.joinToString()}" }
        require(content.isNotBlank()) { "Content cannot be blank" }
        require(content.length <= MAX_CONTENT_LENGTH) { 
            "Content cannot exceed $MAX_CONTENT_LENGTH characters" 
        }
    }
    
    fun isValid(): Boolean {
        return try {
            validateMessage()
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    
    fun isUserMessage(): Boolean = role == ROLE_USER
    fun isAssistantMessage(): Boolean = role == ROLE_ASSISTANT
    
    companion object {
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"
        const val ROLE_SYSTEM = "system"
        const val MAX_CONTENT_LENGTH = 8000
        
        val VALID_ROLES = setOf(ROLE_USER, ROLE_ASSISTANT, ROLE_SYSTEM)
        
        fun createUserMessage(content: String): GroqMessage {
            return GroqMessage(
                role = ROLE_USER,
                content = content.trim()
            )
        }
        
        fun createAssistantMessage(content: String): GroqMessage {
            return GroqMessage(
                role = ROLE_ASSISTANT,
                content = content.trim()
            )
        }
        
        fun createSystemMessage(content: String): GroqMessage {
            return GroqMessage(
                role = ROLE_SYSTEM,
                content = content.trim()
            )
        }
    }
}