package com.luna.chat.domain.usecase

import com.luna.chat.data.repository.UserPreferences
import com.luna.chat.data.repository.UserPreferencesRepository
import com.luna.chat.domain.entity.Theme
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ThemeManagementUseCaseTest {
    
    private lateinit var themeManagementUseCase: ThemeManagementUseCase
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    
    @Before
    fun setUp() {
        userPreferencesRepository = mockk(relaxed = true)
        themeManagementUseCase = ThemeManagementUseCase(userPreferencesRepository)
    }
    
    @Test
    fun `getAvailableThemes should return all child-friendly themes`() {
        // When
        val themes = themeManagementUseCase.getAvailableThemes()
        
        // Then
        assertEquals(5, themes.size)
        assertTrue(themes.all { it.isChildFriendly })
        
        val themeIds = themes.map { it.id }
        assertTrue(themeIds.contains("rainbow"))
        assertTrue(themeIds.contains("ocean"))
        assertTrue(themeIds.contains("forest"))
        assertTrue(themeIds.contains("space"))
        assertTrue(themeIds.contains("sunset"))
    }
    
    @Test
    fun `getCurrentTheme should return theme from preferences`() = runTest {
        // Given
        val preferences = UserPreferences(selectedTheme = "ocean")
        every { userPreferencesRepository.userPreferencesFlow } returns flowOf(preferences)
        
        // When
        val result = themeManagementUseCase.getCurrentTheme().toList()
        
        // Then
        assertEquals(1, result.size)
        assertEquals("ocean", result[0].id)
        assertEquals("Ocean 🌊", result[0].displayName)
    }
    
    @Test
    fun `getCurrentTheme should return default theme for invalid theme ID`() = runTest {
        // Given
        val preferences = UserPreferences(selectedTheme = "invalid_theme")
        every { userPreferencesRepository.userPreferencesFlow } returns flowOf(preferences)
        
        // When
        val result = themeManagementUseCase.getCurrentTheme().toList()
        
        // Then
        assertEquals(1, result.size)
        assertEquals(Theme.getDefaultTheme().id, result[0].id)
    }
    
    @Test
    fun `setTheme with valid theme ID should succeed`() = runTest {
        // Given
        val themeId = "forest"
        coEvery { userPreferencesRepository.updateTheme(themeId) } returns Unit
        
        // When
        val result = themeManagementUseCase.setTheme(themeId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { userPreferencesRepository.updateTheme(themeId) }
    }
    
    @Test
    fun `setTheme with invalid theme ID should fail`() = runTest {
        // Given
        val invalidThemeId = "invalid_theme"
        
        // When
        val result = themeManagementUseCase.setTheme(invalidThemeId)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidThemeException)
        assertEquals("Theme ID 'invalid_theme' is not valid", result.exceptionOrNull()?.message)
        
        coVerify(exactly = 0) { userPreferencesRepository.updateTheme(any()) }
    }
    
    @Test
    fun `setTheme with Theme object should use theme ID`() = runTest {
        // Given
        val theme = Theme.OCEAN_THEME
        coEvery { userPreferencesRepository.updateTheme(theme.id) } returns Unit
        
        // When
        val result = themeManagementUseCase.setTheme(theme)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { userPreferencesRepository.updateTheme(theme.id) }
    }
    
    @Test
    fun `resetToDefaultTheme should set default theme`() = runTest {
        // Given
        val defaultThemeId = Theme.getDefaultTheme().id
        coEvery { userPreferencesRepository.updateTheme(defaultThemeId) } returns Unit
        
        // When
        val result = themeManagementUseCase.resetToDefaultTheme()
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { userPreferencesRepository.updateTheme(defaultThemeId) }
    }
    
    @Test
    fun `getThemeById should return correct theme`() {
        // When
        val theme = themeManagementUseCase.getThemeById("space")
        
        // Then
        assertNotNull(theme)
        assertEquals("space", theme?.id)
        assertEquals("Space 🚀", theme?.displayName)
    }
    
    @Test
    fun `getThemeById should return null for invalid ID`() {
        // When
        val theme = themeManagementUseCase.getThemeById("invalid_theme")
        
        // Then
        assertNull(theme)
    }
    
    @Test
    fun `isValidTheme should return true for valid child-friendly themes`() {
        // Given
        val validThemes = listOf("rainbow", "ocean", "forest", "space", "sunset")
        
        // When & Then
        validThemes.forEach { themeId ->
            assertTrue("Theme '$themeId' should be valid", themeManagementUseCase.isValidTheme(themeId))
        }
    }
    
    @Test
    fun `isValidTheme should return false for invalid themes`() {
        // Given
        val invalidThemes = listOf("invalid", "", "dark_theme", "adult_theme")
        
        // When & Then
        invalidThemes.forEach { themeId ->
            assertFalse("Theme '$themeId' should be invalid", themeManagementUseCase.isValidTheme(themeId))
        }
    }
    
    @Test
    fun `getThemePreferences should return current theme and available themes`() = runTest {
        // Given
        val preferences = UserPreferences(selectedTheme = "sunset")
        every { userPreferencesRepository.userPreferencesFlow } returns flowOf(preferences)
        
        // When
        val result = themeManagementUseCase.getThemePreferences().toList()
        
        // Then
        assertEquals(1, result.size)
        val themePrefs = result[0]
        assertEquals("sunset", themePrefs.currentTheme.id)
        assertEquals(5, themePrefs.availableThemes.size)
        assertTrue(themePrefs.canChangeTheme)
    }
    
    @Test
    fun `validateAndSetTheme should return error for blank theme ID`() = runTest {
        // When
        val result = themeManagementUseCase.validateAndSetTheme("")
        
        // Then
        assertTrue(result is ThemeValidationResult.Error)
        assertEquals("Please select a theme", (result as ThemeValidationResult.Error).message)
    }
    
    @Test
    fun `validateAndSetTheme should return error for invalid theme ID`() = runTest {
        // When
        val result = themeManagementUseCase.validateAndSetTheme("invalid_theme")
        
        // Then
        assertTrue(result is ThemeValidationResult.Error)
        assertEquals("The selected theme is not available", (result as ThemeValidationResult.Error).message)
    }
    
    @Test
    fun `validateAndSetTheme should return success for valid theme`() = runTest {
        // Given
        val themeId = "ocean"
        coEvery { userPreferencesRepository.updateTheme(themeId) } returns Unit
        
        // When
        val result = themeManagementUseCase.validateAndSetTheme(themeId)
        
        // Then
        assertTrue(result is ThemeValidationResult.Success)
        val success = result as ThemeValidationResult.Success
        assertEquals("ocean", success.theme.id)
        assertEquals("Theme changed to Ocean 🌊!", success.message)
        
        coVerify { userPreferencesRepository.updateTheme(themeId) }
    }
    
    @Test
    fun `validateAndSetTheme should handle repository exceptions`() = runTest {
        // Given
        val themeId = "forest"
        coEvery { userPreferencesRepository.updateTheme(themeId) } throws RuntimeException("Database error")
        
        // When
        val result = themeManagementUseCase.validateAndSetTheme(themeId)
        
        // Then
        assertTrue(result is ThemeValidationResult.Error)
        assertEquals("Failed to change theme. Please try again.", (result as ThemeValidationResult.Error).message)
    }
    
    @Test
    fun `setTheme should handle repository exceptions`() = runTest {
        // Given
        val themeId = "rainbow"
        coEvery { userPreferencesRepository.updateTheme(themeId) } throws RuntimeException("Database error")
        
        // When
        val result = themeManagementUseCase.setTheme(themeId)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }
}