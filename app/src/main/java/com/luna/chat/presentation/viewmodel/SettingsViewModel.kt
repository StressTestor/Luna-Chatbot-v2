package com.luna.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luna.chat.domain.entity.Theme
import com.luna.chat.domain.usecase.ThemeManagementUseCase
import com.luna.chat.domain.usecase.ChatHistoryUseCase
import com.luna.chat.data.repository.UserPreferencesRepository
import com.luna.chat.data.repository.UserPreferences
import com.luna.chat.data.repository.SecureApiKeyProvider
import com.luna.chat.data.repository.ParentAuthenticationService
import com.luna.chat.data.repository.ParentAuthResult
import com.luna.chat.data.remote.api.ApiConnectivityTester
import com.luna.chat.data.remote.api.ApiConnectivityResult
import com.luna.chat.data.remote.api.ApiKeyValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeManagementUseCase: ThemeManagementUseCase,
    private val chatHistoryUseCase: ChatHistoryUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val secureApiKeyProvider: SecureApiKeyProvider,
    private val parentAuthenticationService: ParentAuthenticationService,
    private val apiConnectivityTester: ApiConnectivityTester
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Expose user preferences as a separate flow for easier observation
    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    // Expose current theme as a separate flow
    val currentTheme: StateFlow<Theme> = themeManagementUseCase.getCurrentTheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Theme.getDefaultTheme()
        )

    // Available themes for selection
    val availableThemes: List<Theme> = themeManagementUseCase.getAvailableThemes()

    init {
        loadInitialData()
    }

    /**
     * Select a new theme
     */
    fun selectTheme(themeId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                val result = themeManagementUseCase.setTheme(themeId)
                
                if (result.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Theme changed successfully! 🎨",
                            error = null
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to change theme"
                        ) 
                    }
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to change theme. Please try again! 🎨"
                    ) 
                }
            }
        }
    }

    /**
     * Clear all chat history
     */
    fun clearChatHistory() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                chatHistoryUseCase.clearChatHistory()
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "Chat history cleared! Starting fresh! 🗑️",
                        error = null
                    ) 
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to clear chat history. Please try again! 🔄"
                    ) 
                }
            }
        }
    }

    /**
     * Update parental controls setting
     */
    fun updateParentalControls(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.updateParentalControls(enabled)
                
                _uiState.update { 
                    it.copy(
                        successMessage = if (enabled) {
                            "Parental controls enabled! 👨‍👩‍👧‍👦"
                        } else {
                            "Parental controls disabled! 🔓"
                        },
                        error = null
                    ) 
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to update parental controls. Please try again! ⚙️"
                    ) 
                }
            }
        }
    }

    /**
     * Update content filter setting
     */
    fun updateContentFilter(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.updateContentFilter(enabled)
                
                _uiState.update { 
                    it.copy(
                        successMessage = if (enabled) {
                            "Content filter enabled for safer chats! 🛡️"
                        } else {
                            "Content filter disabled! 🔓"
                        },
                        error = null
                    ) 
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to update content filter. Please try again! 🔧"
                    ) 
                }
            }
        }
    }

    /**
     * Update voice input setting
     */
    fun updateVoiceInput(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.updateVoiceInput(enabled)
                
                _uiState.update { 
                    it.copy(
                        successMessage = if (enabled) {
                            "Voice input enabled! You can now speak to Luna! 🎤"
                        } else {
                            "Voice input disabled! 🔇"
                        },
                        error = null
                    ) 
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to update voice input setting. Please try again! 🎙️"
                    ) 
                }
            }
        }
    }

    /**
     * Update auto-clear history days
     */
    fun updateAutoClearHistoryDays(days: Int) {
        viewModelScope.launch {
            try {
                if (days < 1 || days > 365) {
                    _uiState.update { 
                        it.copy(
                            error = "Please choose between 1 and 365 days! 📅"
                        ) 
                    }
                    return@launch
                }
                
                userPreferencesRepository.updateAutoClearHistoryDays(days)
                
                _uiState.update { 
                    it.copy(
                        successMessage = "Chat history will auto-clear after $days days! 🗓️",
                        error = null
                    ) 
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Failed to update auto-clear setting. Please try again! ⏰"
                    ) 
                }
            }
        }
    }

    /**
     * Configure API key (parent-only function)
     */
    fun configureApiKey(apiKey: String, parentPassword: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Authenticate parent first
                val authResult = parentAuthenticationService.authenticateParent(parentPassword)
                if (!authResult.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = authResult.message
                        ) 
                    }
                    return@launch
                }
                
                // Validate API key format
                val validationResult = apiConnectivityTester.validateApiKeyFormat(apiKey)
                if (!validationResult.isValid) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = validationResult.errorMessage ?: "Invalid API key format! 🔑"
                        ) 
                    }
                    return@launch
                }
                
                // Test API connectivity
                val connectivityResult = apiConnectivityTester.testApiConnectivity(apiKey)
                if (!connectivityResult.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "API key test failed: ${connectivityResult.message}"
                        ) 
                    }
                    return@launch
                }
                
                // Store API key securely
                secureApiKeyProvider.setApiKey(apiKey)
                userPreferencesRepository.updateApiKeyConfigured(true)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "API key configured and tested successfully! Luna is ready to chat! 🤖",
                        error = null,
                        showApiKeyDialog = false
                    ) 
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to configure API key. Please try again! 🔧"
                    ) 
                }
            }
        }
    }

    /**
     * Test API connection
     */
    fun testApiConnection() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                val apiKey = secureApiKeyProvider.getApiKey()
                
                if (apiKey == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No API key configured. Please ask a grown-up to set it up! 🔑"
                        ) 
                    }
                    return@launch
                }
                
                // Test API connectivity
                val connectivityResult = apiConnectivityTester.testApiConnectivity(apiKey)
                
                if (connectivityResult.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = connectivityResult.message,
                            error = null
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = connectivityResult.message
                        ) 
                    }
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to test API connection. Please check your settings! 🌐"
                    ) 
                }
            }
        }
    }

    /**
     * Reset all settings to defaults
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                userPreferencesRepository.resetToDefaults()
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "All settings reset to defaults! 🔄",
                        error = null
                    ) 
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to reset settings. Please try again! ⚙️"
                    ) 
                }
            }
        }
    }

    /**
     * Show API key configuration dialog
     * If parent password is not set up, show parent setup dialog first
     */
    fun showApiKeyDialog() {
        if (!isParentPasswordSetup()) {
            showParentSetupDialog()
        } else {
            _uiState.update { it.copy(showApiKeyDialog = true) }
        }
    }

    /**
     * Hide API key configuration dialog
     */
    fun hideApiKeyDialog() {
        _uiState.update { it.copy(showApiKeyDialog = false) }
    }

    /**
     * Setup parent password for first-time configuration
     */
    fun setupParentPassword(password: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Check if passwords match
                if (password != confirmPassword) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Passwords don't match. Please try again! 🔒"
                        ) 
                    }
                    return@launch
                }
                
                // Setup parent password
                val result = parentAuthenticationService.setupParentPassword(password)
                
                if (result.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = result.message,
                            error = null,
                            showParentSetupDialog = false
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        ) 
                    }
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to setup parent password. Please try again! 🔧"
                    ) 
                }
            }
        }
    }

    /**
     * Show parent password setup dialog
     */
    fun showParentSetupDialog() {
        _uiState.update { it.copy(showParentSetupDialog = true) }
    }

    /**
     * Hide parent password setup dialog
     */
    fun hideParentSetupDialog() {
        _uiState.update { it.copy(showParentSetupDialog = false) }
    }

    /**
     * Check if parent password is setup
     */
    fun isParentPasswordSetup(): Boolean {
        return parentAuthenticationService.isParentPasswordSetup()
    }

    /**
     * Dismiss success message
     */
    fun dismissSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    /**
     * Dismiss error message
     */
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // Check if API key is configured
                val hasApiKey = secureApiKeyProvider.hasApiKey()
                
                _uiState.update { 
                    it.copy(
                        apiKeyConfigured = hasApiKey,
                        isLoading = false
                    ) 
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load settings. Please restart the app! 🔄"
                    ) 
                }
            }
        }
    }

    private fun isValidParentPassword(password: String): Boolean {
        // Simple validation for demo purposes
        // In a real app, this would be more sophisticated
        return password.length >= 4 && password.any { it.isDigit() }
    }

    private fun isValidApiKey(apiKey: String): Boolean {
        // Basic API key validation
        return apiKey.isNotBlank() && 
               apiKey.length >= 20 && 
               apiKey.startsWith("gsk_") // Groq API keys start with gsk_
    }
}

/**
 * UI state for the settings screen
 */
data class SettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showApiKeyDialog: Boolean = false,
    val showParentSetupDialog: Boolean = false,
    val apiKeyConfigured: Boolean = false
) {
    val hasError: Boolean
        get() = error != null
        
    val hasSuccessMessage: Boolean
        get() = successMessage != null
        
    val canConfigureApi: Boolean
        get() = !isLoading
}