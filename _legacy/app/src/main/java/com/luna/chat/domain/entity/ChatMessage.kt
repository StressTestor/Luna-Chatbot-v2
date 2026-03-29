package com.luna.chat.domain.entity

import java.util.UUID

data class ChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val status: MessageStatus
) {
    init {
        validateMessage()
    }
    
    private fun validateMessage() {
        require(id.isNotBlank()) { "Message ID cannot be blank" }
        require(content.isNotBlank()) { "Message content cannot be blank" }
        require(content.length <= MAX_MESSAGE_LENGTH) { 
            "Message content cannot exceed $MAX_MESSAGE_LENGTH characters" 
        }
        require(timestamp > 0) { "Timestamp must be positive" }
    }
    
    fun isValid(): Boolean {
        return try {
            validateMessage()
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    
    companion object {
        const val MAX_MESSAGE_LENGTH = 4000
        
        fun create(
            content: String,
            isFromUser: Boolean,
            timestamp: Long = System.currentTimeMillis(),
            status: MessageStatus = if (isFromUser) MessageStatus.SENDING else MessageStatus.DELIVERED
        ): ChatMessage {
            return ChatMessage(
                id = UUID.randomUUID().toString(),
                content = content.trim(),
                isFromUser = isFromUser,
                timestamp = timestamp,
                status = status
            )
        }
    }
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    FAILED
}