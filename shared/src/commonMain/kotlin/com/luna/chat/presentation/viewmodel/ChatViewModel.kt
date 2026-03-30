package com.luna.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.ChatSession
import com.luna.chat.domain.entity.Conversation
import com.luna.chat.domain.entity.LunaModel
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.domain.entity.ModelCategory
import com.luna.chat.domain.repository.ChatRepository
import com.luna.chat.domain.repository.ConversationRepository
import com.luna.chat.domain.repository.ModelRepository
import com.luna.chat.domain.repository.UserPreferencesRepository
import com.luna.chat.domain.usecase.NuggetExtractionUseCase
import com.luna.chat.domain.usecase.SendMessageUseCase
import com.luna.chat.domain.usecase.ContentFilterException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val modelRepository: ModelRepository,
    private val nuggetExtractionUseCase: NuggetExtractionUseCase,
    private val chatRepository: ChatRepository,
    private val conversationRepository: ConversationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _currentSession = MutableStateFlow(ChatSession.create())
    val currentSession: StateFlow<ChatSession> = _currentSession.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _availableModels = MutableStateFlow<Map<ModelCategory, List<LunaModel>>>(emptyMap())
    val availableModels: StateFlow<Map<ModelCategory, List<LunaModel>>> = _availableModels.asStateFlow()

    private val _modelsLoading = MutableStateFlow(false)
    val modelsLoading: StateFlow<Boolean> = _modelsLoading.asStateFlow()

    init {
        observeUserPreferences()
        observeConversations()
        loadMostRecentOrNew()
    }

    // -- conversation management --

    private fun observeConversations() {
        viewModelScope.launch {
            conversationRepository.conversationsFlow.collect { list ->
                _conversations.value = list
            }
        }
    }

    private fun loadMostRecentOrNew() {
        viewModelScope.launch {
            val convos = conversationRepository.conversationsFlow.first()
            if (convos.isNotEmpty()) {
                loadConversation(convos.first().id)
            } else {
                _uiState.update { it.copy(showWelcomeCard = true, isFirstMessage = true) }
            }
        }
    }

    fun switchConversation(id: String) {
        viewModelScope.launch {
            loadConversation(id)
        }
    }

    private suspend fun loadConversation(id: String) {
        val messages = chatRepository.getMessagesForConversation(id)
        val session = ChatSession.create().copy(messages = messages)
        _currentSession.value = session
        _currentConversationId.value = id
        _uiState.update {
            it.copy(
                isFirstMessage = messages.isEmpty(),
                showWelcomeCard = messages.isEmpty(),
                error = null,
            )
        }
    }

    fun startNewChat() {
        viewModelScope.launch {
            try {
                // Extract nuggets from the ending conversation
                val currentMessages = _currentSession.value.messages
                if (currentMessages.size >= 4) {
                    launch { nuggetExtractionUseCase.extractAndStore(currentMessages) }
                }

                // Create new conversation
                val conversation = conversationRepository.createConversation()
                _currentSession.value = ChatSession.create()
                _currentConversationId.value = conversation.id
                _uiState.update { it.copy(error = null, isFirstMessage = true, showWelcomeCard = true) }
            } catch (e: Exception) {
                showError("Failed to start new chat.")
            }
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            conversationRepository.deleteConversation(id)
            // If we deleted the current conversation, start fresh
            if (_currentConversationId.value == id) {
                val remaining = conversationRepository.conversationsFlow.first()
                if (remaining.isNotEmpty()) {
                    loadConversation(remaining.first().id)
                } else {
                    _currentSession.value = ChatSession.create()
                    _currentConversationId.value = null
                    _uiState.update { it.copy(showWelcomeCard = true, isFirstMessage = true) }
                }
            }
        }
    }

    // -- messaging --

    fun sendMessage(message: String) {
        if (message.isBlank()) return
        val trimmedMessage = message.trim()
        if (trimmedMessage.length > ChatMessage.MAX_MESSAGE_LENGTH) {
            showError("Message is too long! Please keep it under ${ChatMessage.MAX_MESSAGE_LENGTH} characters.")
            return
        }

        viewModelScope.launch {
            try {
                println("Luna:VM: sendMessage start, convId=${_currentConversationId.value}")
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Ensure we have a conversation
                var convId = _currentConversationId.value
                if (convId == null) {
                    val conv = conversationRepository.createConversation(
                        title = trimmedMessage.take(40),
                    )
                    convId = conv.id
                    _currentConversationId.value = convId
                }

                // Set conversation title from first message if untitled
                val conv = conversationRepository.getConversation(convId)
                if (conv != null && conv.title.isBlank()) {
                    conversationRepository.updateTitle(convId, trimmedMessage.take(40))
                }

                // Create and persist user message
                val userMessage = ChatMessage.create(
                    content = trimmedMessage,
                    isFromUser = true,
                    status = MessageStatus.SENDING,
                )
                chatRepository.persistMessage(userMessage, convId)
                addMessageToSession(userMessage.copy(status = MessageStatus.DELIVERED))

                // Send to API
                sendMessageUseCase(trimmedMessage, convId)
                    .catch { exception ->
                        handleSendMessageError(exception)
                        _uiState.update { it.copy(isAiThinking = false) }
                    }
                    .collect { result ->
                        result.fold(
                            onSuccess = { aiMessage ->
                                // aiMessage is already persisted by ChatRepositoryImpl
                                addMessageToSession(aiMessage)
                                userPreferencesRepository.incrementMessagesSent()
                            },
                            onFailure = { exception ->
                                handleSendMessageError(exception)
                                _uiState.update { it.copy(isAiThinking = false) }
                            },
                        )
                    }
            } catch (exception: Exception) {
                println("Luna:VM: EXCEPTION in sendMessage: ${exception::class.simpleName}: ${exception.message}")
                exception.printStackTrace()
                handleSendMessageError(exception)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun appendAssistantMessage(content: String) {
        val message = ChatMessage.create(content = content, isFromUser = false, status = MessageStatus.DELIVERED)
        addMessageToSession(message)
        _uiState.update { it.copy(isAiThinking = false, isLoading = false) }
    }

    suspend fun processImage(imageBytes: ByteArray, mimeType: String): String? {
        return try {
            // ProcessImageUseCase removed from constructor — vision handled separately
            "Vision analysis unavailable right now."
        } catch (e: Exception) {
            "Vision analysis unavailable right now."
        }
    }

    // -- models --

    fun loadModels() {
        if (_availableModels.value.isNotEmpty() || _modelsLoading.value) return
        viewModelScope.launch {
            _modelsLoading.value = true
            try {
                val models = modelRepository.getAvailableModels()
                _availableModels.value = models.groupBy { it.category }
                    .toSortedMap(compareBy { it.ordinal })
            } catch (_: Exception) { }
            finally { _modelsLoading.value = false }
        }
    }

    fun selectModel(model: LunaModel) {
        viewModelScope.launch { userPreferencesRepository.updateSelectedModel(model.id) }
    }

    // -- UI helpers --

    fun retryLastMessage() {
        val lastMessage = _currentSession.value.messages.lastOrNull { it.isFromUser }
        lastMessage?.let { message ->
            if (message.status == MessageStatus.FAILED) {
                sendMessage(message.content)
            }
        }
    }

    fun dismissError() { _uiState.update { it.copy(error = null) } }
    fun hideWelcomeCard() { _uiState.update { it.copy(showWelcomeCard = false) } }
    fun setTyping(isTyping: Boolean) { _uiState.update { it.copy(isUserTyping = isTyping) } }

    fun clearChatHistory() {
        viewModelScope.launch {
            try {
                chatRepository.clearAllMessages()
                conversationRepository.deleteAll()
                _currentSession.value = ChatSession.create()
                _currentConversationId.value = null
                _uiState.update { it.copy(error = null, isFirstMessage = true, showWelcomeCard = true) }
            } catch (_: Exception) {
                showError("Failed to clear chat history.")
            }
        }
    }

    fun sendEducationalPrompt(prompt: String) { sendMessage(prompt) }

    fun getContextualSuggestions(): List<String> {
        val messageCount = _currentSession.value.messages.size
        return when {
            messageCount == 0 -> listOf("Help me with math homework", "Tell me about space and planets", "Help me write a story", "Let's play a word game", "Give me a fun drawing idea")
            else -> listOf("Ask me something new!", "Let's try a different topic", "What would you like to learn?", "Tell me about your day")
        }
    }

    // -- internals --

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow
                .catch { }
                .collect { preferences ->
                    _uiState.update {
                        it.copy(
                            isFirstTimeUser = preferences.firstTimeUser,
                            contentFilterEnabled = preferences.contentFilterEnabled,
                            selectedModel = preferences.selectedModel,
                        )
                    }
                }
        }
    }

    private fun addMessageToSession(message: ChatMessage) {
        try {
            val updatedSession = _currentSession.value.addMessage(message)
            _currentSession.value = updatedSession
            _uiState.update { currentState ->
                currentState.copy(
                    isFirstMessage = false,
                    showWelcomeCard = false,
                    isAiThinking = message.isFromUser && message.status != MessageStatus.FAILED,
                )
            }
            if (!message.isFromUser) {
                _uiState.update { it.copy(isAiThinking = false, isLoading = false) }
            }
        } catch (e: Exception) {
            showError("Failed to add message to chat.")
        }
    }

    private fun handleSendMessageError(exception: Throwable) {
        val errorMessage = when (exception) {
            is ContentFilterException -> exception.message ?: "Let's talk about something else!"
            else -> "Something went wrong. Let's try that again!"
        }
        showError(errorMessage)
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(error = message, isLoading = false, isAiThinking = false) }
    }
}

data class ChatUiState(
    val isLoading: Boolean = false,
    val isAiThinking: Boolean = false,
    val isUserTyping: Boolean = false,
    val error: String? = null,
    val isFirstMessage: Boolean = true,
    val isFirstTimeUser: Boolean = true,
    val showWelcomeCard: Boolean = true,
    val contentFilterEnabled: Boolean = true,
    val selectedModel: String = "meta-llama/llama-3.3-70b-instruct:free",
) {
    val canSendMessage: Boolean get() = !isLoading && !isAiThinking
    val showTypingIndicator: Boolean get() = isAiThinking && !isLoading
    val hasError: Boolean get() = error != null
}
