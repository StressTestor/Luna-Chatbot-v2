package com.luna.chat.domain.repository

import com.luna.chat.domain.entity.Conversation
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    val conversationsFlow: Flow<List<Conversation>>
    suspend fun getConversation(id: String): Conversation?
    suspend fun createConversation(title: String = ""): Conversation
    suspend fun updateTitle(id: String, title: String)
    suspend fun touch(id: String)
    suspend fun deleteConversation(id: String)
    suspend fun deleteAll()
}
