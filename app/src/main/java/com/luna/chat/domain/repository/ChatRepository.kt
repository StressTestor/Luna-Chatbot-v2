package com.luna.chat.domain.repository

import com.luna.chat.domain.entity.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(message: String): Flow<Result<String>>
    suspend fun getChatHistory(): Flow<List<ChatMessage>>
    suspend fun saveChatHistory(messages: List<ChatMessage>)
    suspend fun clearChatHistory()
}