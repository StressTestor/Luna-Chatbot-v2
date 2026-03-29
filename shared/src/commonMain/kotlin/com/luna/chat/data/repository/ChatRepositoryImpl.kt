package com.luna.chat.data.repository

import com.luna.chat.data.remote.api.LunaApiClient
import com.luna.chat.data.remote.dto.GroqChatRequest
import com.luna.chat.data.remote.dto.GroqMessage
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.domain.repository.ChatRepository
import com.luna.chat.domain.repository.UserPreferencesRepository
import com.luna.chat.hrr.NuggetShelf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatRepositoryImpl(
    private val apiClient: LunaApiClient,
    private val apiKeyProvider: ApiKeyProvider,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val nuggetShelf: NuggetShelf,
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

    // In-memory message store (will be backed by SQLDelight in Phase 5)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun sendMessage(message: String): Flow<Result<String>> = flow {
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

            // Build conversation with history
            val conversationHistory = _messages.value
                .takeLast(MAX_CONVERSATION_HISTORY)
                .map { msg ->
                    if (msg.isFromUser) GroqMessage.createUserMessage(msg.content)
                    else GroqMessage.createAssistantMessage(msg.content)
                }

            // Inject promoted nuggets (facts recalled 3+ times) into system prompt
            val promotedFacts = nuggetShelf.getPromotedFacts()
            val systemMessage = if (promotedFacts.isNotEmpty()) {
                val factsBlock = promotedFacts.joinToString("\n") { (_, fact) ->
                    "- ${fact.key}: ${fact.value}"
                }
                "$SYSTEM_MESSAGE\n\nThings you know about the user:\n$factsBlock"
            } else {
                SYSTEM_MESSAGE
            }

            val messages = buildList {
                add(GroqMessage.createSystemMessage(systemMessage))
                addAll(conversationHistory)
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

            // Store AI message in memory
            val aiMessage = ChatMessage(
                id = Uuid.random().toString(),
                content = assistantMessage,
                isFromUser = false,
                timestamp = Clock.System.now().toEpochMilliseconds(),
                status = MessageStatus.DELIVERED
            )
            _messages.value = _messages.value + aiMessage

            emit(Result.success(assistantMessage))
        } catch (e: Exception) {

            emit(Result.failure(e))
        }
    }

    override suspend fun getChatHistory(): Flow<List<ChatMessage>> = _messages

    override suspend fun saveChatHistory(messages: List<ChatMessage>) {
        _messages.value = messages
    }

    override suspend fun clearChatHistory() {
        _messages.value = emptyList()
    }
}
