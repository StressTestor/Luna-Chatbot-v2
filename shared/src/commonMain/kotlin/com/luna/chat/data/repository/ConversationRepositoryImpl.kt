package com.luna.chat.data.repository

import com.luna.chat.db.LunaDatabase
import com.luna.chat.domain.entity.Conversation
import com.luna.chat.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ConversationRepositoryImpl(
    private val database: LunaDatabase,
) : ConversationRepository {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    override val conversationsFlow: Flow<List<Conversation>> = _conversations.asStateFlow()

    init {
        refresh()
    }

    private fun now() = Clock.System.now().toEpochMilliseconds()

    private fun refresh() {
        _conversations.value = database.conversationQueries.getAllConversations()
            .executeAsList()
            .map { row ->
                Conversation(
                    id = row.id,
                    title = row.title,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
    }

    override suspend fun getConversation(id: String): Conversation? {
        return database.conversationQueries.getConversationById(id)
            .executeAsOneOrNull()
            ?.let { Conversation(id = it.id, title = it.title, createdAt = it.created_at, updatedAt = it.updated_at) }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createConversation(title: String): Conversation {
        val id = Uuid.random().toString()
        val ts = now()
        database.conversationQueries.insertConversation(id, title, ts, ts)
        refresh()
        return Conversation(id = id, title = title, createdAt = ts, updatedAt = ts)
    }

    override suspend fun updateTitle(id: String, title: String) {
        database.conversationQueries.updateConversationTitle(title, now(), id)
        refresh()
    }

    override suspend fun touch(id: String) {
        database.conversationQueries.updateConversationTimestamp(now(), id)
        refresh()
    }

    override suspend fun deleteConversation(id: String) {
        database.conversationQueries.deleteConversation(id)
        database.chatMessageQueries.clearSessionMessages(id)
        refresh()
    }

    override suspend fun deleteAll() {
        database.conversationQueries.deleteAllConversations()
        database.chatMessageQueries.clearAllMessages()
        refresh()
    }
}
