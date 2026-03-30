package com.luna.chat.domain.repository

import com.luna.chat.domain.entity.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(message: String, conversationId: String): Flow<Result<ChatMessage>>
    fun getMessagesForConversation(conversationId: String): List<ChatMessage>
    suspend fun persistMessage(message: ChatMessage, conversationId: String)
    suspend fun clearConversationMessages(conversationId: String)
    suspend fun clearAllMessages()
}
