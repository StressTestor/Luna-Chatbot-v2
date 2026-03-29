package com.luna.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.ChatSession
import com.luna.chat.domain.entity.LunaModel
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.domain.entity.ModelCategory
import com.luna.chat.domain.repository.ModelRepository
import com.luna.chat.domain.repository.UserPreferencesRepository
import com.luna.chat.domain.usecase.NuggetExtractionUseCase
import com.luna.chat.domain.usecase.SendMessageUseCase
import com.luna.chat.domain.usecase.ChatHistoryUseCase
import com.luna.chat.domain.usecase.ContentFilterException
import com.luna.chat.domain.usecase.ProcessImageUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val chatHistoryUseCase: ChatHistoryUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val processImageUseCase: ProcessImageUseCase,
    private val modelRepository: ModelRepository,
    private val nuggetExtractionUseCase: NuggetExtractionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _currentSession = MutableStateFlow(ChatSession.create())
    val currentSession: StateFlow<ChatSession> = _currentSession.asStateFlow()

    private val _availableModels = MutableStateFlow<Map<ModelCategory, List<LunaModel>>>(emptyMap())
    val availableModels: StateFlow<Map<ModelCategory, List<LunaModel>>> = _availableModels.asStateFlow()

    private val _modelsLoading = MutableStateFlow(false)
    val modelsLoading: StateFlow<Boolean> = _modelsLoading.asStateFlow()

    init {
        loadChatHistory()
        observeUserPreferences()
    }

    fun loadModels() {
        if (_availableModels.value.isNotEmpty() || _modelsLoading.value) return
        viewModelScope.launch {
            _modelsLoading.value = true
            try {
                val models = modelRepository.getAvailableModels()
                _availableModels.value = models.groupBy { it.category }
                    .toSortedMap(compareBy { it.ordinal })
            } catch (_: Exception) {
                // Silently fail — the sheet will show empty state
            } finally {
                _modelsLoading.value = false
            }
        }
    }

    fun selectModel(model: LunaModel) {
        viewModelScope.launch {
            userPreferencesRepository.updateSelectedModel(model.id)
        }
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return
        val trimmedMessage = message.trim()
        if (trimmedMessage.length > ChatMessage.MAX_MESSAGE_LENGTH) {
            showError("Message is too long! Please keep it under ${ChatMessage.MAX_MESSAGE_LENGTH} characters.")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                sendMessageUseCase(trimmedMessage)
                    .catch { exception ->
                        handleSendMessageError(exception)
                        _uiState.update { it.copy(isAiThinking = false) }
                    }
                    .collect { result ->
                        result.fold(
                            onSuccess = { chatMessage ->
                                addMessageToSession(chatMessage)
                                if (chatMessage.status == MessageStatus.DELIVERED) {
                                    chatHistoryUseCase.saveMessage(chatMessage)
                                    if (chatMessage.isFromUser) {
                                        userPreferencesRepository.incrementMessagesSent()
                                    }
                                    if (!chatMessage.isFromUser) {
                                        _uiState.update { it.copy(isAiThinking = false) }
                                    }
                                }
                            },
                            onFailure = { exception ->
                                handleSendMessageError(exception)
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

    fun generateReplyFromVisionSummary(summary: String) {
        val prompt = "Using the following summary of an image, respond kindly and helpfully for a child.\n\nImage summary: $summary"
        sendMessage(prompt)
    }

    fun startNewChat() {
        viewModelScope.launch {
            try {
                val currentMessages = _currentSession.value.messages
                if (currentMessages.isNotEmpty()) {
                    chatHistoryUseCase.saveMessages(currentMessages)
                    // Extract persistent facts from the ending conversation (async, best-effort)
                    launch { nuggetExtractionUseCase.extractAndStore(currentMessages) }
                }
                _currentSession.value = ChatSession.create()
                _uiState.update { it.copy(error = null, isFirstMessage = true, showWelcomeCard = true) }
            } catch (exception: Exception) {
                showError("Failed to start new chat. Please try again!")
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            try {
                chatHistoryUseCase.clearChatHistory()
                _currentSession.value = ChatSession.create()
                _uiState.update { it.copy(error = null, isFirstMessage = true, showWelcomeCard = true) }
            } catch (exception: Exception) {
                showError("Failed to clear chat history. Please try again!")
            }
        }
    }

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

    fun sendEducationalPrompt(prompt: String) {
        val educationalContext = when {
            prompt.contains("math", ignoreCase = true) -> "Please explain this in a way that's easy for an 11-year-old to understand, with step-by-step examples."
            prompt.contains("science", ignoreCase = true) -> "Please explain this science topic in a fun and engaging way for a curious 11-year-old, using simple examples."
            prompt.contains("story", ignoreCase = true) -> "Help me create an age-appropriate, fun story suitable for an 11-year-old."
            prompt.contains("game", ignoreCase = true) -> "Let's play a fun, educational game that's perfect for an 11-year-old."
            prompt.contains("creative", ignoreCase = true) -> "Let's do something creative and fun that an 11-year-old would enjoy!"
            else -> "Please respond in a way that's fun and appropriate for an 11-year-old."
        }
        sendMessage("$prompt $educationalContext")
    }

    fun getContextualSuggestions(): List<String> {
        val messageCount = _currentSession.value.messages.size
        val lastMessages = _currentSession.value.messages.takeLast(3)
        return when {
            messageCount == 0 -> listOf("Help me with math homework", "Tell me about space and planets", "Help me write a story", "Let's play a word game", "Give me a fun drawing idea")
            lastMessages.any { it.content.contains("math", ignoreCase = true) } -> listOf("Show me another math problem", "Explain fractions with pizza slices", "Help me with multiplication tables", "What's a fun math game?")
            lastMessages.any { it.content.contains("science", ignoreCase = true) } -> listOf("Tell me about dinosaurs", "How do volcanoes work?", "What makes rainbows?", "Fun facts about animals")
            lastMessages.any { it.content.contains("story", ignoreCase = true) } -> listOf("Help me write another story", "Give me story ideas", "What makes a good character?", "Let's create a funny ending")
            else -> listOf("Ask me something new!", "Let's try a different topic", "What would you like to learn?", "Tell me about your day")
        }
    }

    fun appendAssistantMessage(content: String) {
        val message = ChatMessage.create(content = content, isFromUser = false, status = MessageStatus.DELIVERED)
        addMessageToSession(message)
        _uiState.update { it.copy(isAiThinking = false, isLoading = false) }
    }

    suspend fun processImage(imageBytes: ByteArray, mimeType: String): String? {
        return try {
            processImageUseCase(imageBytes, mimeType, userPrompt = null).getOrNull()
        } catch (e: Exception) {
            "Vision analysis unavailable right now."
        }
    }

    private fun loadChatHistory() {
        viewModelScope.launch {
            try {
                chatHistoryUseCase.getChatHistory()
                    .catch { _uiState.update { it.copy(showWelcomeCard = true) } }
                    .collect { messages ->
                        if (messages.isNotEmpty()) {
                            val session = ChatSession.create().copy(messages = messages)
                            _currentSession.value = session
                            _uiState.update { it.copy(isFirstMessage = false, showWelcomeCard = false) }
                        } else {
                            _uiState.update { it.copy(showWelcomeCard = true) }
                        }
                    }
            } catch (exception: Exception) {
                _uiState.update { it.copy(showWelcomeCard = true) }
            }
        }
    }

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
                    isAiThinking = message.isFromUser && message.status != MessageStatus.FAILED
                )
            }
            if (!message.isFromUser) {
                _uiState.update { it.copy(isAiThinking = false, isLoading = false) }
            }
        } catch (exception: Exception) {
            showError("Failed to add message to chat. Please try again!")
        }
    }

    private fun handleSendMessageError(exception: Throwable) {
        val errorMessage = when (exception) {
            is ContentFilterException -> exception.message ?: "Let's talk about something else!"
            else -> "Something went wrong. Let's try that again!"
        }
        showError(errorMessage)
        val messages = _currentSession.value.messages.toMutableList()
        val lastUserMessageIndex = messages.indexOfLast { it.isFromUser }
        if (lastUserMessageIndex >= 0) {
            messages[lastUserMessageIndex] = messages[lastUserMessageIndex].copy(status = MessageStatus.FAILED)
            _currentSession.value = _currentSession.value.copy(messages = messages)
        }
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
    val selectedModel: String = "nvidia/nemotron-3-super-120b-a12b:free",
) {
    val canSendMessage: Boolean get() = !isLoading && !isAiThinking
    val showTypingIndicator: Boolean get() = isAiThinking && !isLoading
    val hasError: Boolean get() = error != null
}
