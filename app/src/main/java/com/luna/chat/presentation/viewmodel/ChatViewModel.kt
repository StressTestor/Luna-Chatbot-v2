package com.luna.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.ChatSession
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.domain.usecase.SendMessageUseCase
import com.luna.chat.domain.usecase.ChatHistoryUseCase
import com.luna.chat.domain.usecase.ContentFilterException
import com.luna.chat.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val chatHistoryUseCase: ChatHistoryUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _currentSession = MutableStateFlow(ChatSession.create())
    val currentSession: StateFlow<ChatSession> = _currentSession.asStateFlow()

    init {
        loadChatHistory()
        observeUserPreferences()
    }

    /**
     * Send a message from the user
     */
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        val trimmedMessage = message.trim()
        if (trimmedMessage.length > ChatMessage.MAX_MESSAGE_LENGTH) {
            showError("Message is too long! Please keep it under ${ChatMessage.MAX_MESSAGE_LENGTH} characters. 📝")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                sendMessageUseCase(trimmedMessage)
                    .catch { exception ->
                        handleSendMessageError(exception)
                        // Ensure we reset the thinking state if there's an error
                        _uiState.update { it.copy(isAiThinking = false) }
                    }
                    .collect { result ->
                        result.fold(
                            onSuccess = { chatMessage ->
                                addMessageToSession(chatMessage)
                                
                                // Save message to history if it's delivered
                                if (chatMessage.status == MessageStatus.DELIVERED) {
                                    chatHistoryUseCase.saveMessage(chatMessage)
                                    
                                    // Increment message counter for user messages
                                    if (chatMessage.isFromUser) {
                                        userPreferencesRepository.incrementMessagesSent()
                                    }
                                    
                                    // If this is an AI response, explicitly turn off thinking indicator
                                    if (!chatMessage.isFromUser) {
                                        _uiState.update { it.copy(isAiThinking = false) }
                                    }
                                }
                            },
                            onFailure = { exception ->
                                handleSendMessageError(exception)
                                // Ensure we reset the thinking state if there's an error
                                _uiState.update { it.copy(isAiThinking = false) }
                            }
                        )
                    }
            } catch (exception: Exception) {
                handleSendMessageError(exception)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Start a new chat session
     */
    fun startNewChat() {
        viewModelScope.launch {
            try {
                // Save current session messages to history before clearing
                val currentMessages = _currentSession.value.messages
                if (currentMessages.isNotEmpty()) {
                    chatHistoryUseCase.saveMessages(currentMessages)
                }
                
                // Create new session
                _currentSession.value = ChatSession.create()
                _uiState.update { 
                    it.copy(
                        error = null,
                        isFirstMessage = true,
                        showWelcomeCard = true
                    ) 
                }
            } catch (exception: Exception) {
                showError("Failed to start new chat. Please try again! 🔄")
            }
        }
    }

    /**
     * Clear all chat history
     */
    fun clearChatHistory() {
        viewModelScope.launch {
            try {
                chatHistoryUseCase.clearChatHistory()
                _currentSession.value = ChatSession.create()
                _uiState.update { 
                    it.copy(
                        error = null,
                        isFirstMessage = true,
                        showWelcomeCard = true
                    ) 
                }
            } catch (exception: Exception) {
                showError("Failed to clear chat history. Please try again! 🗑️")
            }
        }
    }

    /**
     * Retry sending the last failed message
     */
    fun retryLastMessage() {
        val lastMessage = _currentSession.value.messages.lastOrNull { it.isFromUser }
        lastMessage?.let { message ->
            if (message.status == MessageStatus.FAILED) {
                sendMessage(message.content)
            }
        }
    }

    /**
     * Dismiss the current error
     */
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Hide the welcome card
     */
    fun hideWelcomeCard() {
        _uiState.update { it.copy(showWelcomeCard = false) }
    }

    /**
     * Handle typing indicator for better UX
     */
    fun setTyping(isTyping: Boolean) {
        _uiState.update { it.copy(isUserTyping = isTyping) }
    }

    /**
     * Send an educational prompt message
     */
    fun sendEducationalPrompt(prompt: String) {
        val educationalContext = when {
            prompt.contains("math", ignoreCase = true) -> "Please explain this in a way that's easy for an 11-year-old to understand, with step-by-step examples."
            prompt.contains("science", ignoreCase = true) -> "Please explain this science topic in a fun and engaging way for a curious 11-year-old, using simple examples."
            prompt.contains("story", ignoreCase = true) -> "Help me create an age-appropriate, fun story suitable for an 11-year-old."
            prompt.contains("game", ignoreCase = true) -> "Let's play a fun, educational game that's perfect for an 11-year-old."
            prompt.contains("creative", ignoreCase = true) -> "Let's do something creative and fun that an 11-year-old would enjoy!"
            else -> "Please respond in a way that's fun and appropriate for an 11-year-old."
        }
        
        val contextualPrompt = "$prompt $educationalContext"
        sendMessage(contextualPrompt)
    }

    /**
     * Get conversation suggestions based on current context
     */
    fun getContextualSuggestions(): List<String> {
        val messageCount = _currentSession.value.messages.size
        val lastMessages = _currentSession.value.messages.takeLast(3)
        
        return when {
            messageCount == 0 -> listOf(
                "Help me with math homework",
                "Tell me about space and planets", 
                "Help me write a story",
                "Let's play a word game",
                "Give me a fun drawing idea"
            )
            lastMessages.any { it.content.contains("math", ignoreCase = true) } -> listOf(
                "Show me another math problem",
                "Explain fractions with pizza slices",
                "Help me with multiplication tables",
                "What's a fun math game?"
            )
            lastMessages.any { it.content.contains("science", ignoreCase = true) } -> listOf(
                "Tell me about dinosaurs",
                "How do volcanoes work?",
                "What makes rainbows?",
                "Fun facts about animals"
            )
            lastMessages.any { it.content.contains("story", ignoreCase = true) } -> listOf(
                "Help me write another story",
                "Give me story ideas",
                "What makes a good character?",
                "Let's create a funny ending"
            )
            else -> listOf(
                "Ask me something new!",
                "Let's try a different topic",
                "What would you like to learn?",
                "Tell me about your day"
            )
        }
    }

    private fun loadChatHistory() {
        viewModelScope.launch {
            try {
                chatHistoryUseCase.getChatHistory()
                    .catch { exception ->
                        // Don't show error for history loading failure, just start fresh
                        _uiState.update { it.copy(showWelcomeCard = true) }
                    }
                    .collect { messages ->
                        if (messages.isNotEmpty()) {
                            // Recreate session with loaded messages
                            val session = ChatSession.create().copy(messages = messages)
                            _currentSession.value = session
                            _uiState.update { 
                                it.copy(
                                    isFirstMessage = false,
                                    showWelcomeCard = false
                                ) 
                            }
                        } else {
                            _uiState.update { it.copy(showWelcomeCard = true) }
                        }
                    }
            } catch (exception: Exception) {
                // Start with fresh session if history loading fails
                _uiState.update { it.copy(showWelcomeCard = true) }
            }
        }
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow
                .catch { 
                    // Continue with defaults if preferences loading fails
                }
                .collect { preferences ->
                    _uiState.update { 
                        it.copy(
                            isFirstTimeUser = preferences.firstTimeUser,
                            contentFilterEnabled = preferences.contentFilterEnabled
                        ) 
                    }
                }
        }
    }

    private fun addMessageToSession(message: ChatMessage) {
        try {
            val updatedSession = _currentSession.value.addMessage(message)
            _currentSession.value = updatedSession
            
            // Update UI state based on message
            _uiState.update { currentState ->
                currentState.copy(
                    isFirstMessage = false,
                    showWelcomeCard = false,
                    // Only show thinking indicator for user messages, turn it off for AI responses
                    isAiThinking = message.isFromUser && message.status != MessageStatus.FAILED
                )
            }
            
            // If this is an AI response, ensure we turn off the thinking indicator
            if (!message.isFromUser) {
                _uiState.update { it.copy(isAiThinking = false, isLoading = false) }
            }
        } catch (exception: Exception) {
            showError("Failed to add message to chat. Please try again! 💬")
        }
    }

    private fun handleSendMessageError(exception: Throwable) {
        val errorMessage = when (exception) {
            is ContentFilterException -> exception.message ?: "Let's talk about something else! 🌟"
            is java.net.UnknownHostException -> "Check your internet connection and try again! 🌐"
            is java.net.SocketTimeoutException -> "The request took too long. Please try again! ⏰"
            is java.io.IOException -> "Network error. Please check your connection! 📶"
            else -> "Something went wrong. Let's try that again! 🤖"
        }
        
        showError(errorMessage)
        
        // Mark last user message as failed if it exists
        val messages = _currentSession.value.messages.toMutableList()
        val lastUserMessageIndex = messages.indexOfLast { it.isFromUser }
        if (lastUserMessageIndex >= 0) {
            messages[lastUserMessageIndex] = messages[lastUserMessageIndex].copy(status = MessageStatus.FAILED)
            _currentSession.value = _currentSession.value.copy(messages = messages)
        }
    }

    private fun showError(message: String) {
        _uiState.update { 
            it.copy(
                error = message,
                isLoading = false,
                isAiThinking = false
            ) 
        }
    }
}

/**
 * UI state for the chat screen
 */
data class ChatUiState(
    val isLoading: Boolean = false,
    val isAiThinking: Boolean = false,
    val isUserTyping: Boolean = false,
    val error: String? = null,
    val isFirstMessage: Boolean = true,
    val isFirstTimeUser: Boolean = true,
    val showWelcomeCard: Boolean = true,
    val contentFilterEnabled: Boolean = true
) {
    val canSendMessage: Boolean
        get() = !isLoading && !isAiThinking
        
    val showTypingIndicator: Boolean
        get() = isAiThinking && !isLoading
        
    val hasError: Boolean
        get() = error != null
}