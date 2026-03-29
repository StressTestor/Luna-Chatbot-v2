package com.luna.chat.domain.usecase

import com.luna.chat.db.LunaDatabase
import kotlinx.datetime.Clock

/**
 * Handles automatic cleanup of old message content.
 * Conversation metadata persists indefinitely; message content is pruned after 30 days.
 */
class ChatHistoryUseCase constructor(
    private val database: LunaDatabase,
) {
    /**
     * Remove message content older than 30 days.
     * Conversation rows are kept (for the drawer) — only message bodies are deleted.
     */
    suspend fun clearChatHistory() {
        database.chatMessageQueries.clearAllMessages()
        database.conversationQueries.deleteAllConversations()
    }

    suspend fun performAutomaticCleanup() {
        val cutoff = Clock.System.now().toEpochMilliseconds() - MAX_MESSAGE_AGE_MS
        database.chatMessageQueries.deleteMessagesOlderThan(cutoff)
    }

    companion object {
        private const val MAX_MESSAGE_AGE_MS = 30L * 24L * 60L * 60L * 1000L // 30 days
    }
}
