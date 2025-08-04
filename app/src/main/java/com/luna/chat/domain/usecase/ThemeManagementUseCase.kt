package com.luna.chat.domain.usecase

import com.luna.chat.domain.entity.Theme
import com.luna.chat.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManagementUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    /**
     * Gets all available themes for the user to choose from
     */
    fun getAvailableThemes(): List<Theme> {
        return Theme.getAllThemes().filter { it.isChildFriendly }
    }
    
    /**
     * Gets the currently selected theme
     */
    fun getCurrentTheme(): Flow<Theme> {
        return userPreferencesRepository.userPreferencesFlow
            .map { preferences ->
                Theme.getThemeById(preferences.selectedTheme) ?: Theme.getDefaultTheme()
            }
    }
    
    /**
     * Sets the selected theme
     */
    suspend fun setTheme(themeId: String): Result<Unit> {
        return try {
            // Validate theme ID
            if (!isValidTheme(themeId)) {
                Result.failure(InvalidThemeException("Theme ID '$themeId' is not valid"))
            } else {
                userPreferencesRepository.updateTheme(themeId)
                Result.success(Unit)
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
    
    /**
     * Sets the theme using a Theme object
     */
    suspend fun setTheme(theme: Theme): Result<Unit> {
        return setTheme(theme.id)
    }
    
    /**
     * Resets to the default theme
     */
    suspend fun resetToDefaultTheme(): Result<Unit> {
        return setTheme(Theme.getDefaultTheme().id)
    }
    
    /**
     * Gets a specific theme by ID
     */
    fun getThemeById(themeId: String): Theme? {
        return Theme.getThemeById(themeId)
    }
    
    /**
     * Checks if a theme is valid and child-friendly
     */
    fun isValidTheme(themeId: String): Boolean {
        val theme = Theme.getThemeById(themeId)
        return theme != null && theme.isChildFriendly
    }
    
    /**
     * Gets theme preferences for display in settings
     */
    fun getThemePreferences(): Flow<ThemePreferences> {
        return getCurrentTheme().map { currentTheme ->
            ThemePreferences(
                currentTheme = currentTheme,
                availableThemes = getAvailableThemes(),
                canChangeTheme = true
            )
        }
    }
    
    /**
     * Validates theme selection and provides user-friendly error messages
     */
    suspend fun validateAndSetTheme(themeId: String): ThemeValidationResult {
        return when {
            themeId.isBlank() -> {
                ThemeValidationResult.Error("Please select a theme")
            }
            !Theme.isValidThemeId(themeId) -> {
                ThemeValidationResult.Error("The selected theme is not available")
            }
            !isValidTheme(themeId) -> {
                ThemeValidationResult.Error("This theme is not suitable for children")
            }
            else -> {
                try {
                    setTheme(themeId)
                    val theme = Theme.getThemeById(themeId)!!
                    ThemeValidationResult.Success(theme, "Theme changed to ${theme.displayName}!")
                } catch (exception: Exception) {
                    ThemeValidationResult.Error("Failed to change theme. Please try again.")
                }
            }
        }
    }
}

/**
 * Represents theme preferences for the UI
 */
data class ThemePreferences(
    val currentTheme: Theme,
    val availableThemes: List<Theme>,
    val canChangeTheme: Boolean
)

/**
 * Result of theme validation and setting
 */
sealed class ThemeValidationResult {
    data class Success(val theme: Theme, val message: String) : ThemeValidationResult()
    data class Error(val message: String) : ThemeValidationResult()
}

/**
 * Exception thrown when an invalid theme is selected
 */
class InvalidThemeException(message: String) : Exception(message)