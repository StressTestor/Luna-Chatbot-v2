package com.luna.chat.domain.usecase

import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatHistoryUseCase constructor(
    private val chatRepository: ChatRepository
) {
    
    /**
     * Retrieves chat history with privacy-preserving logic
     * Automatically filters out any messages that might contain personal information
     */
    suspend fun getChatHistory(): Flow<List<ChatMessage>> {
        return chatRepository.getChatHistory()
            .map { messages ->
                messages.filter { message ->
                    // Filter out messages that are too old (privacy protection)
                    val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                    val messageAge = currentTime - message.timestamp
                    messageAge <= MAX_MESSAGE_AGE_MS
                }.take(MAX_MESSAGES_TO_KEEP) // Limit number of messages for performance
            }
    }
    
    /**
     * Saves a single message to chat history
     */
    suspend fun saveMessage(message: ChatMessage) {
        // Only save if message doesn't contain personal information
        if (isMessageSafeToStore(message)) {
            chatRepository.saveChatHistory(listOf(message))
        }
    }
    
    /**
     * Saves multiple messages to chat history
     */
    suspend fun saveMessages(messages: List<ChatMessage>) {
        // Filter out messages that contain personal information
        val safeMessages = messages.filter { isMessageSafeToStore(it) }
        if (safeMessages.isNotEmpty()) {
            chatRepository.saveChatHistory(safeMessages)
        }
    }
    
    /**
     * Clears all chat history
     */
    suspend fun clearChatHistory() {
        chatRepository.clearChatHistory()
    }
    
    /**
     * Performs automatic cleanup of old messages
     * Should be called periodically to maintain privacy and performance
     */
    suspend fun performAutomaticCleanup() {
        val currentMessages = chatRepository.getChatHistory()
        
        currentMessages.collect { messages ->
            val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            val messagesToKeep = messages.filter { message ->
                val messageAge = currentTime - message.timestamp
                messageAge <= MAX_MESSAGE_AGE_MS
            }.take(MAX_MESSAGES_TO_KEEP)
            
            // If we filtered out any messages, update the storage
            if (messagesToKeep.size < messages.size) {
                chatRepository.clearChatHistory()
                if (messagesToKeep.isNotEmpty()) {
                    chatRepository.saveChatHistory(messagesToKeep)
                }
            }
        }
    }
    
    /**
     * Gets the count of messages in chat history
     */
    suspend fun getMessageCount(): Flow<Int> {
        return chatRepository.getChatHistory().map { it.size }
    }
    
    /**
     * Checks if a message is safe to store based on privacy guidelines
     */
    private fun isMessageSafeToStore(message: ChatMessage): Boolean {
        val content = message.content.lowercase()
        
        // Check for personal information patterns
        val personalInfoPatterns = listOf(
            // Names and personal identifiers
            "my name is", "i am", "i'm called", "call me",
            
            // Location information
            "i live", "my address", "my house", "my home is",
            "my school is", "i go to school at",
            
            // Contact information
            "my phone", "my number", "my email",
            
            // Family information
            "my mom", "my dad", "my parent", "my family",
            "my brother", "my sister",
            
            // Age and birthday
            "i am [0-9]+ years old", "my birthday", "i was born"
        )
        
        // Don't store messages that contain personal information
        val containsPersonalInfo = personalInfoPatterns.any { pattern ->
            content.contains(pattern, ignoreCase = true)
        }
        
        if (containsPersonalInfo) {
            return false
        }
        
        // Don't store very long messages (might contain too much personal context)
        if (message.content.length > MAX_MESSAGE_LENGTH_TO_STORE) {
            return false
        }
        
        return true
    }
    
    companion object {
        // Keep messages for maximum 7 days for privacy
        private const val MAX_MESSAGE_AGE_MS = 7L * 24L * 60L * 60L * 1000L // 7 days
        
        // Keep maximum 100 messages for performance
        private const val MAX_MESSAGES_TO_KEEP = 100
        
        // Don't store messages longer than 500 characters
        private const val MAX_MESSAGE_LENGTH_TO_STORE = 500
    }
}