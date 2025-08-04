package com.luna.chat.data.repository

import com.luna.chat.data.local.dao.ChatDao
import com.luna.chat.data.local.entity.ChatMessageEntity
import com.luna.chat.data.remote.api.ApiResponseHandler
import com.luna.chat.data.remote.api.ApiResult
import com.luna.chat.data.remote.api.GroqApiService
import com.luna.chat.data.remote.dto.GroqChatRequest
import com.luna.chat.data.remote.dto.GroqMessage
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.domain.repository.ChatRepository
import com.luna.chat.presentation.performance.DatabaseOptimizationUtils
import com.luna.chat.presentation.performance.ItemComplexity
import com.luna.chat.presentation.performance.MessageMemoryManager
import com.luna.chat.presentation.performance.QueryFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ChatRepository that combines local database storage
 * with remote Groq API service for AI chat functionality
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val groqApiService: GroqApiService,
    private val chatDao: ChatDao,
    private val apiKeyProvider: ApiKeyProvider,
    private val contentFilteringService: com.luna.chat.security.ContentFilteringService
) : ChatRepository {

    companion object {
        private const val DEFAULT_SESSION_ID = "default_session"
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

    /**
     * Send a message to the AI and return the response as a Flow
     * Handles both local storage and remote API communication
     */
    override suspend fun sendMessage(message: String): Flow<Result<String>> = flow {
        try {
            // Validate input
            if (message.isBlank()) {
                emit(Result.failure(IllegalArgumentException("Message cannot be empty")))
                return@flow
            }

            // Get API key
            val apiKey = apiKeyProvider.getApiKey()
            if (apiKey.isNullOrBlank()) {
                emit(Result.failure(IllegalStateException("API key not configured")))
                return@flow
            }
            
            // Filter user input for inappropriate content
            val filteredInput = contentFilteringService.filterUserInput(message.trim())
            
            // If content was filtered, return the filtered message
            if (filteredInput.wasFiltered) {
                val filteredMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    content = "I'm sorry, but I can't respond to that. Let's talk about something else!",
                    isFromUser = false,
                    timestamp = System.currentTimeMillis(),
                    status = MessageStatus.DELIVERED
                )
                
                // Save filtered message to database
                saveMessageToDatabase(filteredMessage, DEFAULT_SESSION_ID)
                
                // Emit filtered response
                emit(Result.success(filteredMessage.content))
                return@flow
            }

            // Create and save user message
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = filteredInput.input,
                isFromUser = true,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENT
            )

            // Save user message to local database
            saveMessageToDatabase(userMessage, DEFAULT_SESSION_ID)

            // Get conversation history for context
            val conversationHistory = getConversationHistoryForApi()

            // Add current user message to conversation
            val messages = mutableListOf<GroqMessage>().apply {
                add(GroqMessage.createSystemMessage(SYSTEM_MESSAGE))
                addAll(conversationHistory)
                add(GroqMessage.createUserMessage(message.trim()))
            }

            // Get model parameters from content filtering service
            val modelParams = contentFilteringService.getModelParameters()
            
            // Create API request with security parameters
            val request = GroqChatRequest.create(
                messages = messages,
                temperature = (modelParams["temperature"] as Float).toDouble(),
                maxTokens = 1000
            )

            // Make API call
            val apiResult = ApiResponseHandler.safeApiCallWithRetry(
                maxRetries = 3,
                delayMs = 1000
            ) {
                groqApiService.sendChatMessage(
                    authorization = GroqApiService.formatAuthHeader(apiKey),
                    request = request
                )
            }

            when (apiResult) {
                is ApiResult.Success -> {
                    val response = apiResult.data
                    val assistantMessage = response.getAssistantMessage()

                    if (assistantMessage.isNullOrBlank()) {
                        emit(Result.failure(IllegalStateException("Empty response from AI")))
                        return@flow
                    }
                    
                    // Filter AI response for inappropriate content
                    val filteredResponse = contentFilteringService.filterAiResponse(assistantMessage)
                    
                    // Use either the filtered or original response
                    val finalContent = if (filteredResponse.wasFiltered) {
                        filteredResponse.input
                    } else {
                        assistantMessage
                    }

                    // Create and save AI response message
                    val aiMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        content = finalContent,
                        isFromUser = false,
                        timestamp = System.currentTimeMillis(),
                        status = MessageStatus.DELIVERED
                    )

                    // Save AI message to local database
                    saveMessageToDatabase(aiMessage, DEFAULT_SESSION_ID)

                    // Emit successful response
                    emit(Result.success(finalContent))
                }

                is ApiResult.Error -> {
                    // Create error message for local storage
                    val errorMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        content = apiResult.exception.getChildFriendlyMessage(),
                        isFromUser = false,
                        timestamp = System.currentTimeMillis(),
                        status = MessageStatus.FAILED
                    )

                    // Save error message to local database
                    saveMessageToDatabase(errorMessage, DEFAULT_SESSION_ID)

                    // Emit error result
                    emit(Result.failure(apiResult.exception))
                }

                is ApiResult.Loading -> {
                    // This shouldn't happen in our current implementation
                    // but handle it gracefully
                    emit(Result.failure(IllegalStateException("Unexpected loading state")))
                }
            }

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Get chat history from local database with memory management
     */
    override suspend fun getChatHistory(): Flow<List<ChatMessage>> {
        return chatDao.getMessagesBySession(DEFAULT_SESSION_ID)
            .map { entities ->
                // Apply memory management - limit messages in memory
                val limitedEntities = if (MessageMemoryManager.shouldCleanupMemory(entities.size)) {
                    // Keep only the most recent messages
                    entities.takeLast(MessageMemoryManager.calculateCleanupCount(entities.size).coerceAtLeast(50))
                } else {
                    entities
                }
                
                limitedEntities.map { it.toDomain() }
            }
    }

    /**
     * Save chat history to local database
     */
    override suspend fun saveChatHistory(messages: List<ChatMessage>) {
        val entities = messages.map { message ->
            ChatMessageEntity.fromDomain(message, DEFAULT_SESSION_ID)
        }
        chatDao.insertMessages(entities)
    }

    /**
     * Clear all chat history from local database
     */
    override suspend fun clearChatHistory() {
        chatDao.clearSessionMessages(DEFAULT_SESSION_ID)
    }

    /**
     * Get recent conversation history for API context
     * Limits the number of messages to avoid token limits
     */
    private suspend fun getConversationHistoryForApi(): List<GroqMessage> {
        val recentMessages = chatDao.getRecentMessages(MAX_CONVERSATION_HISTORY)
        
        // Convert to Flow and collect the first emission
        var messages: List<ChatMessageEntity> = emptyList()
        recentMessages.collect { messageList ->
            messages = messageList.reversed() // Reverse to get chronological order
            return@collect // Exit after first emission
        }

        return messages
            .filter { it.sessionId == DEFAULT_SESSION_ID }
            .filter { it.status != MessageStatus.FAILED.name } // Exclude error messages
            .map { entity ->
                if (entity.isFromUser) {
                    GroqMessage.createUserMessage(entity.content)
                } else {
                    GroqMessage.createAssistantMessage(entity.content)
                }
            }
    }

    /**
     * Save a single message to the database
     */
    private suspend fun saveMessageToDatabase(message: ChatMessage, sessionId: String) {
        val entity = ChatMessageEntity.fromDomain(message, sessionId)
        chatDao.insertMessage(entity)
    }

    /**
     * Clean up old messages to prevent database from growing too large
     */
    suspend fun cleanupOldMessages(maxAgeMillis: Long = 30L * 24 * 60 * 60 * 1000) { // 30 days
        val cutoffTime = System.currentTimeMillis() - maxAgeMillis
        chatDao.deleteMessagesOlderThan(cutoffTime)
    }

    /**
     * Get chat statistics for analytics
     */
    suspend fun getChatStatistics() = chatDao.getChatStatistics()

    /**
     * Update message status (e.g., mark as read, failed, etc.)
     */
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        chatDao.updateMessageStatus(messageId, status.name)
    }

    /**
     * Get messages by type (user or AI messages)
     */
    fun getMessagesByType(isFromUser: Boolean): Flow<List<ChatMessage>> {
        return chatDao.getMessagesByType(isFromUser)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    /**
     * Check if API is configured and available
     */
    suspend fun isApiConfigured(): Boolean {
        val apiKey = apiKeyProvider.getApiKey()
        return !apiKey.isNullOrBlank() && GroqApiService.isValidApiKeyFormat(apiKey)
    }

    /**
     * Test API connectivity
     */
    suspend fun testApiConnection(): ApiResult<String> {
        val apiKey = apiKeyProvider.getApiKey()
        if (apiKey.isNullOrBlank()) {
            return ApiResult.Error(
                com.luna.chat.data.remote.api.ApiException.ApiKeyException("API key not configured")
            )
        }

        val testRequest = GroqChatRequest.create(
            messages = listOf(
                GroqMessage.createSystemMessage("You are a test assistant."),
                GroqMessage.createUserMessage("Hello, this is a test message.")
            ),
            maxTokens = 50
        )

        return ApiResponseHandler.safeApiCall {
            groqApiService.sendChatMessage(
                authorization = GroqApiService.formatAuthHeader(apiKey),
                request = testRequest
            )
        }.map { response ->
            response.getAssistantMessage() ?: "Test successful"
        }
    }
}

// ApiKeyProvider interface is defined in ApiKeyProvider.kt