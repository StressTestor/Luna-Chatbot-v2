package com.luna.chat.data.repository

import com.luna.chat.data.remote.api.LunaApiClient
import com.luna.chat.data.remote.dto.GroqChatRequest
import com.luna.chat.data.remote.dto.GroqMessage
import com.luna.chat.db.LunaDatabase
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.domain.repository.ChatRepository
import com.luna.chat.domain.repository.ConversationRepository
import com.luna.chat.domain.repository.UserPreferencesRepository
import com.luna.chat.hrr.NuggetShelf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatRepositoryImpl(
    private val apiClient: LunaApiClient,
    private val apiKeyProvider: ApiKeyProvider,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val nuggetShelf: NuggetShelf,
    private val database: LunaDatabase,
    private val conversationRepository: ConversationRepository,
) : ChatRepository {

    companion object {
        private const val MAX_CONVERSATION_HISTORY = 20
        private val SYSTEM_MESSAGE = """You are Luna, a smart and friendly AI assistant.
            |Your user is a sharp preteen — talk to her like a peer, not a little kid.
            |Be direct, helpful, and genuine. Match her energy.
            |Help with homework, creative projects, coding, art, music, whatever she's into.
            |You can discuss mature topics like history, philosophy, current events, and
            |emotions honestly — she can handle nuance.
            |Don't lecture or be preachy. Don't over-explain obvious things.
            |Keep responses concise unless she asks for detail.
            |If she's venting, listen first — don't immediately try to fix everything.
            |Never share or ask for personal info like addresses, phone numbers, or full names.
            |Never generate explicit sexual content or detailed violence.""".trimMargin()
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun sendMessage(message: String, conversationId: String): Flow<Result<String>> = flow {
        try {
            if (message.isBlank()) {
                emit(Result.failure(IllegalArgumentException("Message cannot be empty")))
                return@flow
            }

            val apiKey = apiKeyProvider.getApiKey()
            if (apiKey.isNullOrBlank()) {
                emit(Result.failure(IllegalStateException("API key not configured")))
                return@flow
            }

            // Load conversation history from SQLite for API context
            val history = getMessagesForConversation(conversationId)
                .takeLast(MAX_CONVERSATION_HISTORY)
                .map { msg ->
                    if (msg.isFromUser) GroqMessage.createUserMessage(msg.content)
                    else GroqMessage.createAssistantMessage(msg.content)
                }

            // Inject promoted nuggets into system prompt
            val promotedFacts = nuggetShelf.getPromotedFacts()
            val systemMessage = if (promotedFacts.isNotEmpty()) {
                val factsBlock = promotedFacts.joinToString("\n") { (_, fact) ->
                    val safeKey = fact.key.take(50).replace("\n", " ").trim()
                    val safeVal = fact.value.take(100).replace("\n", " ").trim()
                    "- $safeKey: $safeVal"
                }
                "$SYSTEM_MESSAGE\n\n<user_facts>\n$factsBlock\n</user_facts>"
            } else {
                SYSTEM_MESSAGE
            }

            val messages = buildList {
                add(GroqMessage.createSystemMessage(systemMessage))
                addAll(history)
                add(GroqMessage.createUserMessage(message.trim()))
            }

            val selectedModel = userPreferencesRepository.userPreferencesFlow.first().selectedModel
            val request = GroqChatRequest.create(messages = messages, model = selectedModel, maxTokens = 1000)

            val response = apiClient.sendChatMessage(apiKey, request)
            val assistantMessage = response.getAssistantMessage()

            if (assistantMessage.isNullOrBlank()) {
                emit(Result.failure(IllegalStateException("Empty response from AI")))
                return@flow
            }

            // Persist AI message to SQLite
            val aiMessage = ChatMessage(
                id = Uuid.random().toString(),
                content = assistantMessage,
                isFromUser = false,
                timestamp = Clock.System.now().toEpochMilliseconds(),
                status = MessageStatus.DELIVERED,
            )
            persistMessage(aiMessage, conversationId)
            conversationRepository.touch(conversationId)

            emit(Result.success(assistantMessage))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getMessagesForConversation(conversationId: String): List<ChatMessage> {
        return database.chatMessageQueries.getMessagesBySession(conversationId)
            .executeAsList()
            .map { row ->
                ChatMessage(
                    id = row.id,
                    content = row.content,
                    isFromUser = row.is_from_user != 0L,
                    timestamp = row.timestamp,
                    status = MessageStatus.valueOf(row.status),
                )
            }
    }

    override suspend fun persistMessage(message: ChatMessage, conversationId: String) {
        database.chatMessageQueries.insertMessage(
            id = message.id,
            content = message.content,
            is_from_user = if (message.isFromUser) 1L else 0L,
            timestamp = message.timestamp,
            session_id = conversationId,
            status = message.status.name,
        )
    }

    override suspend fun clearConversationMessages(conversationId: String) {
        database.chatMessageQueries.clearSessionMessages(conversationId)
    }

    override suspend fun clearAllMessages() {
        database.chatMessageQueries.clearAllMessages()
    }
}
