package com.luna.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luna.chat.domain.entity.Theme
import com.luna.chat.domain.entity.UserPreferences
import com.luna.chat.domain.repository.UserPreferencesRepository
import com.luna.chat.domain.usecase.ThemeManagementUseCase
import com.luna.chat.domain.usecase.ChatHistoryUseCase
import com.luna.chat.data.repository.ApiKeyProvider
import com.luna.chat.data.repository.ParentAuthenticationService
import com.luna.chat.data.repository.ApiConnectivityTester
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themeManagementUseCase: ThemeManagementUseCase,
    private val chatHistoryUseCase: ChatHistoryUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val apiKeyProvider: ApiKeyProvider,
    private val parentAuthenticationService: ParentAuthenticationService,
    private val apiConnectivityTester: ApiConnectivityTester
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = UserPreferences())

    val currentTheme: StateFlow<Theme> = themeManagementUseCase.getCurrentTheme()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = Theme.getDefaultTheme())

    val availableThemes: List<Theme> = themeManagementUseCase.getAvailableThemes()

    init { loadInitialData() }

    fun selectTheme(themeId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val result = themeManagementUseCase.setTheme(themeId)
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Theme changed successfully!", error = null) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to change theme") }
                }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to change theme. Please try again!") }
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                chatHistoryUseCase.clearChatHistory()
                _uiState.update { it.copy(isLoading = false, successMessage = "Chat history cleared!", error = null) }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to clear chat history. Please try again!") }
            }
        }
    }

    fun updateParentalControls(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.updateParentalControls(enabled)
                _uiState.update { it.copy(successMessage = if (enabled) "Parental controls enabled!" else "Parental controls disabled!", error = null) }
            } catch (exception: Exception) {
                _uiState.update { it.copy(error = "Failed to update parental controls. Please try again!") }
            }
        }
    }

    fun updateContentFilter(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.updateContentFilter(enabled)
                _uiState.update { it.copy(successMessage = if (enabled) "Content filter enabled!" else "Content filter disabled!", error = null) }
            } catch (exception: Exception) {
                _uiState.update { it.copy(error = "Failed to update content filter. Please try again!") }
            }
        }
    }

    fun updateVoiceInput(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.updateVoiceInput(enabled)
                _uiState.update { it.copy(successMessage = if (enabled) "Voice input enabled!" else "Voice input disabled!", error = null) }
            } catch (exception: Exception) {
                _uiState.update { it.copy(error = "Failed to update voice input setting. Please try again!") }
            }
        }
    }

    fun updateAutoClearHistoryDays(days: Int) {
        viewModelScope.launch {
            try {
                if (days !in 1..365) {
                    _uiState.update { it.copy(error = "Please choose between 1 and 365 days!") }
                    return@launch
                }
                userPreferencesRepository.updateAutoClearHistoryDays(days)
                _uiState.update { it.copy(successMessage = "Chat history will auto-clear after $days days!", error = null) }
            } catch (exception: Exception) {
                _uiState.update { it.copy(error = "Failed to update auto-clear setting. Please try again!") }
            }
        }
    }

    fun configureApiKey(apiKey: String, parentPassword: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val authResult = parentAuthenticationService.authenticateParent(parentPassword)
                if (!authResult.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, error = authResult.message) }
                    return@launch
                }
                val validationResult = apiConnectivityTester.validateApiKeyFormat(apiKey)
                if (!validationResult.isValid) {
                    _uiState.update { it.copy(isLoading = false, error = validationResult.errorMessage ?: "Invalid API key format!") }
                    return@launch
                }
                val connectivityResult = apiConnectivityTester.testApiConnectivity(apiKey)
                if (!connectivityResult.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, error = "API key test failed: ${connectivityResult.message}") }
                    return@launch
                }
                apiKeyProvider.setApiKey(apiKey)
                userPreferencesRepository.updateApiKeyConfigured(true)
                _uiState.update { it.copy(isLoading = false, successMessage = "API key configured successfully!", error = null, showApiKeyDialog = false) }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to configure API key. Please try again!") }
            }
        }
    }

    fun testApiConnection() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val key = apiKeyProvider.getApiKey()
                if (key == null) {
                    _uiState.update { it.copy(isLoading = false, error = "No API key configured. Please ask a grown-up to set it up!") }
                    return@launch
                }
                val connectivityResult = apiConnectivityTester.testApiConnectivity(key)
                if (connectivityResult.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, successMessage = connectivityResult.message, error = null) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = connectivityResult.message) }
                }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to test API connection. Please check your settings!") }
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                userPreferencesRepository.resetToDefaults()
                _uiState.update { it.copy(isLoading = false, successMessage = "All settings reset to defaults!", error = null) }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to reset settings. Please try again!") }
            }
        }
    }

    fun showApiKeyDialog() {
        if (!isParentPasswordSetup()) showParentSetupDialog()
        else _uiState.update { it.copy(showApiKeyDialog = true) }
    }
    fun hideApiKeyDialog() { _uiState.update { it.copy(showApiKeyDialog = false) } }

    fun setupParentPassword(password: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                if (password != confirmPassword) {
                    _uiState.update { it.copy(isLoading = false, error = "Passwords don't match. Please try again!") }
                    return@launch
                }
                val result = parentAuthenticationService.setupParentPassword(password)
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, successMessage = result.message, error = null, showParentSetupDialog = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to setup parent password. Please try again!") }
            }
        }
    }

    fun showParentSetupDialog() { _uiState.update { it.copy(showParentSetupDialog = true) } }
    fun hideParentSetupDialog() { _uiState.update { it.copy(showParentSetupDialog = false) } }
    fun isParentPasswordSetup(): Boolean = parentAuthenticationService.isParentPasswordSetup()
    fun dismissSuccessMessage() { _uiState.update { it.copy(successMessage = null) } }
    fun dismissError() { _uiState.update { it.copy(error = null) } }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val hasApiKey = apiKeyProvider.hasApiKey()
                _uiState.update { it.copy(apiKeyConfigured = hasApiKey, isLoading = false) }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load settings. Please restart the app!") }
            }
        }
    }
}

data class SettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showApiKeyDialog: Boolean = false,
    val showParentSetupDialog: Boolean = false,
    val apiKeyConfigured: Boolean = false
) {
    val hasError: Boolean get() = error != null
    val hasSuccessMessage: Boolean get() = successMessage != null
    val canConfigureApi: Boolean get() = !isLoading
}
