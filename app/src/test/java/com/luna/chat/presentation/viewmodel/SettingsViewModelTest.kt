package com.luna.chat.presentation.viewmodel

import com.luna.chat.domain.entity.Theme
import com.luna.chat.domain.usecase.ThemeManagementUseCase
import com.luna.chat.domain.usecase.ThemeValidationResult
import com.luna.chat.domain.usecase.ChatHistoryUseCase
import com.luna.chat.data.repository.UserPreferencesRepository
import com.luna.chat.data.repository.UserPreferences
import com.luna.chat.data.repository.SecureApiKeyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @Mock
    private lateinit var themeManagementUseCase: ThemeManagementUseCase

    @Mock
    private lateinit var chatHistoryUseCase: ChatHistoryUseCase

    @Mock
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    @Mock
    private lateinit var secureApiKeyProvider: SecureApiKeyProvider

    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Setup default mock behaviors
        whenever(userPreferencesRepository.userPreferencesFlow)
            .thenReturn(flowOf(UserPreferences()))
        whenever(themeManagementUseCase.getCurrentTheme())
            .thenReturn(flowOf(Theme.getDefaultTheme()))
        whenever(themeManagementUseCase.getAvailableThemes())
            .thenReturn(Theme.getAllThemes())
        whenever(secureApiKeyProvider.hasApiKey())
            .thenReturn(false)

        viewModel = SettingsViewModel(
            themeManagementUseCase = themeManagementUseCase,
            chatHistoryUseCase = chatHistoryUseCase,
            userPreferencesRepository = userPreferencesRepository,
            secureApiKeyProvider = secureApiKeyProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() {
        val initialState = viewModel.uiState.value
        
        assertFalse(initialState.isLoading)
        assertNull(initialState.error)
        assertNull(initialState.successMessage)
        assertFalse(initialState.showApiKeyDialog)
        assertFalse(initialState.apiKeyConfigured)
        assertTrue(initialState.canConfigureApi)
    }

    @Test
    fun `selectTheme should handle successful theme change`() = runTest {
        // Arrange
        val themeId = "ocean"
        val theme = Theme.OCEAN_THEME
        val successResult = ThemeValidationResult.Success(theme, "Theme changed to Ocean!")
        
        whenever(themeManagementUseCase.validateAndSetTheme(themeId))
            .thenReturn(successResult)

        // Act
        viewModel.selectTheme(themeId)

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Theme changed to Ocean!", state.successMessage)
        assertNull(state.error)
        
        verify(themeManagementUseCase).validateAndSetTheme(themeId)
    }

    @Test
    fun `selectTheme should handle validation error`() = runTest {
        // Arrange
        val themeId = "invalid"
        val errorResult = ThemeValidationResult.Error("Invalid theme selected")
        
        whenever(themeManagementUseCase.validateAndSetTheme(themeId))
            .thenReturn(errorResult)

        // Act
        viewModel.selectTheme(themeId)

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Invalid theme selected", state.error)
        assertNull(state.successMessage)
    }

    @Test
    fun `selectTheme should handle exception`() = runTest {
        // Arrange
        val themeId = "ocean"
        
        whenever(themeManagementUseCase.validateAndSetTheme(themeId))
            .thenThrow(RuntimeException("Network error"))

        // Act
        viewModel.selectTheme(themeId)

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Failed to change theme. Please try again! 🎨", state.error)
        assertNull(state.successMessage)
    }

    @Test
    fun `clearChatHistory should handle successful clear`() = runTest {
        // Act
        viewModel.clearChatHistory()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Chat history cleared! Starting fresh! 🗑️", state.successMessage)
        assertNull(state.error)
        
        verify(chatHistoryUseCase).clearChatHistory()
    }

    @Test
    fun `clearChatHistory should handle exception`() = runTest {
        // Arrange
        whenever(chatHistoryUseCase.clearChatHistory())
            .thenThrow(RuntimeException("Database error"))

        // Act
        viewModel.clearChatHistory()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Failed to clear chat history. Please try again! 🔄", state.error)
        assertNull(state.successMessage)
    }

    @Test
    fun `updateParentalControls should handle successful update`() = runTest {
        // Test enabling
        viewModel.updateParentalControls(true)
        
        var state = viewModel.uiState.value
        assertEquals("Parental controls enabled! 👨‍👩‍👧‍👦", state.successMessage)
        assertNull(state.error)
        
        verify(userPreferencesRepository).updateParentalControls(true)

        // Test disabling
        viewModel.updateParentalControls(false)
        
        state = viewModel.uiState.value
        assertEquals("Parental controls disabled! 🔓", state.successMessage)
        
        verify(userPreferencesRepository).updateParentalControls(false)
    }

    @Test
    fun `updateParentalControls should handle exception`() = runTest {
        // Arrange
        whenever(userPreferencesRepository.updateParentalControls(any()))
            .thenThrow(RuntimeException("Storage error"))

        // Act
        viewModel.updateParentalControls(true)

        // Assert
        val state = viewModel.uiState.value
        assertEquals("Failed to update parental controls. Please try again! ⚙️", state.error)
        assertNull(state.successMessage)
    }

    @Test
    fun `updateContentFilter should handle successful update`() = runTest {
        // Test enabling
        viewModel.updateContentFilter(true)
        
        var state = viewModel.uiState.value
        assertEquals("Content filter enabled for safer chats! 🛡️", state.successMessage)
        assertNull(state.error)
        
        verify(userPreferencesRepository).updateContentFilter(true)

        // Test disabling
        viewModel.updateContentFilter(false)
        
        state = viewModel.uiState.value
        assertEquals("Content filter disabled! 🔓", state.successMessage)
        
        verify(userPreferencesRepository).updateContentFilter(false)
    }

    @Test
    fun `updateVoiceInput should handle successful update`() = runTest {
        // Test enabling
        viewModel.updateVoiceInput(true)
        
        var state = viewModel.uiState.value
        assertEquals("Voice input enabled! You can now speak to Luna! 🎤", state.successMessage)
        assertNull(state.error)
        
        verify(userPreferencesRepository).updateVoiceInput(true)

        // Test disabling
        viewModel.updateVoiceInput(false)
        
        state = viewModel.uiState.value
        assertEquals("Voice input disabled! 🔇", state.successMessage)
        
        verify(userPreferencesRepository).updateVoiceInput(false)
    }

    @Test
    fun `updateAutoClearHistoryDays should handle valid input`() = runTest {
        // Act
        viewModel.updateAutoClearHistoryDays(30)

        // Assert
        val state = viewModel.uiState.value
        assertEquals("Chat history will auto-clear after 30 days! 🗓️", state.successMessage)
        assertNull(state.error)
        
        verify(userPreferencesRepository).updateAutoClearHistoryDays(30)
    }

    @Test
    fun `updateAutoClearHistoryDays should handle invalid input`() = runTest {
        // Test too low
        viewModel.updateAutoClearHistoryDays(0)
        
        var state = viewModel.uiState.value
        assertEquals("Please choose between 1 and 365 days! 📅", state.error)
        
        // Test too high
        viewModel.updateAutoClearHistoryDays(400)
        
        state = viewModel.uiState.value
        assertEquals("Please choose between 1 and 365 days! 📅", state.error)
        
        verifyNoInteractions(userPreferencesRepository)
    }

    @Test
    fun `configureApiKey should handle valid input`() = runTest {
        // Arrange
        val validApiKey = "gsk_1234567890abcdefghijklmnop"
        val validPassword = "1234"

        // Act
        viewModel.configureApiKey(validApiKey, validPassword)

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("API key configured successfully! Luna is ready to chat! 🤖", state.successMessage)
        assertNull(state.error)
        assertFalse(state.showApiKeyDialog)
        
        verify(secureApiKeyProvider).setApiKey(validApiKey)
        verify(userPreferencesRepository).updateApiKeyConfigured(true)
    }

    @Test
    fun `configureApiKey should handle invalid parent password`() = runTest {
        // Arrange
        val validApiKey = "gsk_1234567890abcdefghijklmnop"
        val invalidPassword = "abc" // too short, no digits

        // Act
        viewModel.configureApiKey(validApiKey, invalidPassword)

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Invalid parent password. Please ask a grown-up for help! 👨‍👩‍👧‍👦", state.error)
        assertNull(state.successMessage)
        
        verifyNoInteractions(secureApiKeyProvider)
        verify(userPreferencesRepository, never()).updateApiKeyConfigured(any())
    }

    @Test
    fun `configureApiKey should handle invalid API key format`() = runTest {
        // Arrange
        val invalidApiKey = "invalid_key"
        val validPassword = "1234"

        // Act
        viewModel.configureApiKey(invalidApiKey, validPassword)

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("API key format is not valid. Please check and try again! 🔑", state.error)
        assertNull(state.successMessage)
        
        verifyNoInteractions(secureApiKeyProvider)
        verify(userPreferencesRepository, never()).updateApiKeyConfigured(any())
    }

    @Test
    fun `testApiConnection should handle configured API key`() = runTest {
        // Arrange
        whenever(secureApiKeyProvider.hasApiKey()).thenReturn(true)

        // Act
        viewModel.testApiConnection()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("API connection looks good! Luna is ready to chat! ✅", state.successMessage)
        assertNull(state.error)
    }

    @Test
    fun `testApiConnection should handle missing API key`() = runTest {
        // Arrange
        whenever(secureApiKeyProvider.hasApiKey()).thenReturn(false)

        // Act
        viewModel.testApiConnection()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("No API key configured. Please ask a grown-up to set it up! 🔑", state.error)
        assertNull(state.successMessage)
    }

    @Test
    fun `resetToDefaults should handle successful reset`() = runTest {
        // Act
        viewModel.resetToDefaults()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("All settings reset to defaults! 🔄", state.successMessage)
        assertNull(state.error)
        
        verify(userPreferencesRepository).resetToDefaults()
    }

    @Test
    fun `resetToDefaults should handle exception`() = runTest {
        // Arrange
        whenever(userPreferencesRepository.resetToDefaults())
            .thenThrow(RuntimeException("Storage error"))

        // Act
        viewModel.resetToDefaults()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Failed to reset settings. Please try again! ⚙️", state.error)
        assertNull(state.successMessage)
    }

    @Test
    fun `showApiKeyDialog should update state`() = runTest {
        // Act
        viewModel.showApiKeyDialog()

        // Assert
        assertTrue(viewModel.uiState.value.showApiKeyDialog)
    }

    @Test
    fun `hideApiKeyDialog should update state`() = runTest {
        // Arrange
        viewModel.showApiKeyDialog()
        assertTrue(viewModel.uiState.value.showApiKeyDialog)

        // Act
        viewModel.hideApiKeyDialog()

        // Assert
        assertFalse(viewModel.uiState.value.showApiKeyDialog)
    }

    @Test
    fun `dismissSuccessMessage should clear success message`() = runTest {
        // Arrange - trigger a success message
        viewModel.clearChatHistory()
        assertTrue(viewModel.uiState.value.hasSuccessMessage)

        // Act
        viewModel.dismissSuccessMessage()

        // Assert
        assertFalse(viewModel.uiState.value.hasSuccessMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `dismissError should clear error message`() = runTest {
        // Arrange - trigger an error
        whenever(chatHistoryUseCase.clearChatHistory())
            .thenThrow(RuntimeException("Error"))
        
        viewModel.clearChatHistory()
        assertTrue(viewModel.uiState.value.hasError)

        // Act
        viewModel.dismissError()

        // Assert
        assertFalse(viewModel.uiState.value.hasError)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `availableThemes should return all child-friendly themes`() {
        // Assert
        val themes = viewModel.availableThemes
        assertEquals(5, themes.size)
        assertTrue(themes.all { it.isChildFriendly })
        
        verify(themeManagementUseCase).getAvailableThemes()
    }

    @Test
    fun `userPreferences flow should be exposed correctly`() = runTest {
        // Arrange
        val testPreferences = UserPreferences(
            selectedTheme = "ocean",
            parentalControlsEnabled = false,
            contentFilterEnabled = true
        )
        
        whenever(userPreferencesRepository.userPreferencesFlow)
            .thenReturn(flowOf(testPreferences))

        // Create new viewModel to trigger flow subscription
        val newViewModel = SettingsViewModel(
            themeManagementUseCase = themeManagementUseCase,
            chatHistoryUseCase = chatHistoryUseCase,
            userPreferencesRepository = userPreferencesRepository,
            secureApiKeyProvider = secureApiKeyProvider
        )

        // Assert
        assertEquals(testPreferences, newViewModel.userPreferences.value)
    }

    @Test
    fun `currentTheme flow should be exposed correctly`() = runTest {
        // Arrange
        val testTheme = Theme.OCEAN_THEME
        
        whenever(themeManagementUseCase.getCurrentTheme())
            .thenReturn(flowOf(testTheme))

        // Create new viewModel to trigger flow subscription
        val newViewModel = SettingsViewModel(
            themeManagementUseCase = themeManagementUseCase,
            chatHistoryUseCase = chatHistoryUseCase,
            userPreferencesRepository = userPreferencesRepository,
            secureApiKeyProvider = secureApiKeyProvider
        )

        // Assert
        assertEquals(testTheme, newViewModel.currentTheme.value)
    }

    @Test
    fun `SettingsUiState computed properties should work correctly`() {
        // Test hasError
        var state = SettingsUiState(error = null)
        assertFalse(state.hasError)

        state = SettingsUiState(error = "Some error")
        assertTrue(state.hasError)

        // Test hasSuccessMessage
        state = SettingsUiState(successMessage = null)
        assertFalse(state.hasSuccessMessage)

        state = SettingsUiState(successMessage = "Success!")
        assertTrue(state.hasSuccessMessage)

        // Test canConfigureApi
        state = SettingsUiState(isLoading = true)
        assertFalse(state.canConfigureApi)

        state = SettingsUiState(isLoading = false)
        assertTrue(state.canConfigureApi)
    }
}