package com.luna.chat.domain.entity

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ChatSession(
    val id: String,
    val messages: List<ChatMessage>,
    val createdAt: Long
) {
    init {
        validateSession()
    }

    private fun validateSession() {
        require(id.isNotBlank()) { "Session ID cannot be blank" }
        require(createdAt > 0) { "Creation timestamp must be positive" }
        require(messages.size <= MAX_MESSAGES_PER_SESSION) {
            "Session cannot exceed $MAX_MESSAGES_PER_SESSION messages"
        }
        messages.forEach { message ->
            require(message.isValid()) { "All messages in session must be valid" }
        }
    }

    fun isValid(): Boolean {
        return try {
            validateSession()
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun addMessage(message: ChatMessage): ChatSession {
        require(messages.size < MAX_MESSAGES_PER_SESSION) {
            "Cannot add more messages, session is at maximum capacity"
        }
        return copy(messages = messages + message)
    }

    fun getLastMessage(): ChatMessage? = messages.lastOrNull()
    fun getUserMessages(): List<ChatMessage> = messages.filter { it.isFromUser }
    fun getAiMessages(): List<ChatMessage> = messages.filter { !it.isFromUser }

    companion object {
        const val MAX_MESSAGES_PER_SESSION = 1000

        @OptIn(ExperimentalUuidApi::class)
        fun create(
            createdAt: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        ): ChatSession {
            return ChatSession(
                id = Uuid.random().toString(),
                messages = emptyList(),
                createdAt = createdAt
            )
        }
    }
}
