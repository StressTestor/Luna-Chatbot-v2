package com.luna.chat.data.repository

import com.luna.chat.data.remote.api.LunaApiClient
import com.luna.chat.data.remote.dto.GroqChatRequest
import com.luna.chat.data.remote.dto.GroqMessage
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatRepositoryImpl(
    private val apiClient: LunaApiClient,
    private val apiKeyProvider: ApiKeyProvider
) : ChatRepository {

    companion object {
        private const val MAX_CONVERSATION_HISTORY = 20
        private val SYSTEM_MESSAGE = """You are Luna, a helpful and friendly AI assistant designed specifically for children.
            |You should:
            |- Use simple, age-appropriate language
            |- Be encouraging and positive
            |- Help with homework and learning
            |- Suggest fun and educational activities
            |- Always prioritize child safety and appropriate content
            |- Keep responses concise and engaging
            |Remember to be patient, kind, and educational in all your responses.""".trimMargin()
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

            val messages = buildList {
                add(GroqMessage.createSystemMessage(SYSTEM_MESSAGE))
                addAll(conversationHistory)
                add(GroqMessage.createUserMessage(message.trim()))
            }

            val request = GroqChatRequest.create(messages = messages, maxTokens = 1000)

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
