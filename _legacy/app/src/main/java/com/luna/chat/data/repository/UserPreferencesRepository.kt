package com.luna.chat.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Repository for managing user preferences using DataStore
 * Provides type-safe access to user settings and preferences
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val dataStore = context.dataStore

    companion object {
        // Preference keys
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
        val PARENTAL_CONTROLS_ENABLED = booleanPreferencesKey("parental_controls_enabled")
        val API_KEY_CONFIGURED = booleanPreferencesKey("api_key_configured")
        val FIRST_TIME_USER = booleanPreferencesKey("first_time_user")
        val AUTO_CLEAR_HISTORY_DAYS = intPreferencesKey("auto_clear_history_days")
        val CONTENT_FILTER_ENABLED = booleanPreferencesKey("content_filter_enabled")
        val VOICE_INPUT_ENABLED = booleanPreferencesKey("voice_input_enabled")
        val LAST_UPDATED = longPreferencesKey("last_updated")
        val APP_VERSION = stringPreferencesKey("app_version")
        val TOTAL_MESSAGES_SENT = intPreferencesKey("total_messages_sent")
        val FAVORITE_THEME_COLORS = stringSetPreferencesKey("favorite_theme_colors")
        
        // Default values
        const val DEFAULT_THEME = "rainbow"
        const val DEFAULT_AUTO_CLEAR_DAYS = 30
        const val DEFAULT_PARENTAL_CONTROLS = true
        const val DEFAULT_CONTENT_FILTER = true
        const val DEFAULT_VOICE_INPUT = true
    }

    /**
     * Get user preferences as a Flow
     */
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            // Handle IOException and emit default preferences
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                selectedTheme = preferences[SELECTED_THEME] ?: DEFAULT_THEME,
                parentalControlsEnabled = preferences[PARENTAL_CONTROLS_ENABLED] ?: DEFAULT_PARENTAL_CONTROLS,
                apiKeyConfigured = preferences[API_KEY_CONFIGURED] ?: false,
                firstTimeUser = preferences[FIRST_TIME_USER] ?: true,
                autoClearHistoryDays = preferences[AUTO_CLEAR_HISTORY_DAYS] ?: DEFAULT_AUTO_CLEAR_DAYS,
                contentFilterEnabled = preferences[CONTENT_FILTER_ENABLED] ?: DEFAULT_CONTENT_FILTER,
                voiceInputEnabled = preferences[VOICE_INPUT_ENABLED] ?: DEFAULT_VOICE_INPUT,
                lastUpdated = preferences[LAST_UPDATED] ?: System.currentTimeMillis(),
                appVersion = preferences[APP_VERSION] ?: "",
                totalMessagesSent = preferences[TOTAL_MESSAGES_SENT] ?: 0,
                favoriteThemeColors = preferences[FAVORITE_THEME_COLORS] ?: emptySet()
            )
        }

    /**
     * Update selected theme
     */
    suspend fun updateTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[SELECTED_THEME] = theme
            preferences[LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Update parental controls setting
     */
    suspend fun updateParentalControls(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PARENTAL_CONTROLS_ENABLED] = enabled
            preferences[LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Update API key configuration status
     */
    suspend fun updateApiKeyConfigured(configured: Boolean) {
        dataStore.edit { preferences ->
            preferences[API_KEY_CONFIGURED] = configured
            preferences[LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Mark user as no longer first-time user
     */
    suspend fun markNotFirstTimeUser() {
        dataStore.edit { preferences ->
            preferences[FIRST_TIME_USER] = false
            preferences[LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Update auto-clear history setting
     */
    suspend fun updateAutoClearHistoryDays(days: Int) {
        dataStore.edit { preferences ->
            preferences[AUTO_CLEAR_HISTORY_DAYS] = days
            preferences[LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Update content filter setting
     */
    suspend fun updateContentFilter(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[CONTENT_FILTER_ENABLED] = enabled
            preferences[LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Update voice input setting
     */
    suspend fun updateVoiceInput(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[VOICE_INPUT_ENABLED] = enabled
            preferences[LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Update app version (for migration purposes)
     */
    suspend fun updateAppVersion(version: String) {
        dataStore.edit { preferences ->
            preferences[APP_VERSION] = version
            preferences[LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Increment total messages sent counter
     */
    suspend fun incrementMessagesSent() {
        dataStore.edit { preferences ->
            val current = preferences[TOTAL_MESSAGES_SENT] ?: 0
            preferences[TOTAL_MESSAGES_SENT] = current + 1
            preferences[LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Update favorite theme colors
     */
    suspend fun updateFavoriteThemeColors(colors: Set<String>) {
        dataStore.edit { preferences ->
            preferences[FAVORITE_THEME_COLORS] = colors
            preferences[LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Reset all preferences to defaults
     */
    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
            preferences[SELECTED_THEME] = DEFAULT_THEME
            preferences[PARENTAL_CONTROLS_ENABLED] = DEFAULT_PARENTAL_CONTROLS
            preferences[API_KEY_CONFIGURED] = false
            preferences[FIRST_TIME_USER] = true
            preferences[AUTO_CLEAR_HISTORY_DAYS] = DEFAULT_AUTO_CLEAR_DAYS
            preferences[CONTENT_FILTER_ENABLED] = DEFAULT_CONTENT_FILTER
            preferences[VOICE_INPUT_ENABLED] = DEFAULT_VOICE_INPUT
            preferences[LAST_UPDATED] = System.currentTimeMillis()
            preferences[TOTAL_MESSAGES_SENT] = 0
        }
    }

    /**
     * Update multiple preferences atomically
     */
    suspend fun updatePreferences(update: UserPreferences.() -> UserPreferences) {
        dataStore.edit { preferences ->
            val current = UserPreferences(
                selectedTheme = preferences[SELECTED_THEME] ?: DEFAULT_THEME,
                parentalControlsEnabled = preferences[PARENTAL_CONTROLS_ENABLED] ?: DEFAULT_PARENTAL_CONTROLS,
                apiKeyConfigured = preferences[API_KEY_CONFIGURED] ?: false,
                firstTimeUser = preferences[FIRST_TIME_USER] ?: true,
                autoClearHistoryDays = preferences[AUTO_CLEAR_HISTORY_DAYS] ?: DEFAULT_AUTO_CLEAR_DAYS,
                contentFilterEnabled = preferences[CONTENT_FILTER_ENABLED] ?: DEFAULT_CONTENT_FILTER,
                voiceInputEnabled = preferences[VOICE_INPUT_ENABLED] ?: DEFAULT_VOICE_INPUT,
                lastUpdated = preferences[LAST_UPDATED] ?: System.currentTimeMillis(),
                appVersion = preferences[APP_VERSION] ?: "",
                totalMessagesSent = preferences[TOTAL_MESSAGES_SENT] ?: 0,
                favoriteThemeColors = preferences[FAVORITE_THEME_COLORS] ?: emptySet()
            )
            
            val updated = current.update()
            
            preferences[SELECTED_THEME] = updated.selectedTheme
            preferences[PARENTAL_CONTROLS_ENABLED] = updated.parentalControlsEnabled
            preferences[API_KEY_CONFIGURED] = updated.apiKeyConfigured
            preferences[FIRST_TIME_USER] = updated.firstTimeUser
            preferences[AUTO_CLEAR_HISTORY_DAYS] = updated.autoClearHistoryDays
            preferences[CONTENT_FILTER_ENABLED] = updated.contentFilterEnabled
            preferences[VOICE_INPUT_ENABLED] = updated.voiceInputEnabled
            preferences[APP_VERSION] = updated.appVersion
            preferences[TOTAL_MESSAGES_SENT] = updated.totalMessagesSent
            preferences[FAVORITE_THEME_COLORS] = updated.favoriteThemeColors
            preferences[LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Check if preferences need migration based on app version
     */
    suspend fun needsMigration(currentAppVersion: String): Boolean {
        return try {
            val preferences = dataStore.data.map { prefs ->
                prefs[APP_VERSION] ?: ""
            }
            val storedVersion = preferences.catch { emit("") }.map { it }.toString()
            storedVersion != currentAppVersion
        } catch (e: Exception) {
            true // Assume migration needed if we can't determine version
        }
    }
}

/**
 * Data class representing user preferences
 */
data class UserPreferences(
    val selectedTheme: String = UserPreferencesRepository.DEFAULT_THEME,
    val parentalControlsEnabled: Boolean = UserPreferencesRepository.DEFAULT_PARENTAL_CONTROLS,
    val apiKeyConfigured: Boolean = false,
    val firstTimeUser: Boolean = true,
    val autoClearHistoryDays: Int = UserPreferencesRepository.DEFAULT_AUTO_CLEAR_DAYS,
    val contentFilterEnabled: Boolean = UserPreferencesRepository.DEFAULT_CONTENT_FILTER,
    val voiceInputEnabled: Boolean = UserPreferencesRepository.DEFAULT_VOICE_INPUT,
    val lastUpdated: Long = System.currentTimeMillis(),
    val appVersion: String = "",
    val totalMessagesSent: Int = 0,
    val favoriteThemeColors: Set<String> = emptySet()
) {
    /**
     * Available theme options
     */
    companion object {
        val AVAILABLE_THEMES = listOf(
            "rainbow",
            "ocean", 
            "forest",
            "space",
            "sunset"
        )
        
        fun isValidTheme(theme: String): Boolean {
            return theme in AVAILABLE_THEMES
        }
    }
    
    /**
     * Check if theme is valid
     */
    fun hasValidTheme(): Boolean = isValidTheme(selectedTheme)
    
    /**
     * Check if auto-clear setting is reasonable
     */
    fun hasValidAutoClearSetting(): Boolean = autoClearHistoryDays in 1..365
    
    /**
     * Get user experience level based on message count
     */
    fun getUserExperienceLevel(): UserExperienceLevel {
        return when {
            totalMessagesSent == 0 -> UserExperienceLevel.NEW
            totalMessagesSent < 10 -> UserExperienceLevel.BEGINNER
            totalMessagesSent < 50 -> UserExperienceLevel.INTERMEDIATE
            else -> UserExperienceLevel.EXPERIENCED
        }
    }
}

/**
 * Enum representing user experience levels
 */
enum class UserExperienceLevel {
    NEW,
    BEGINNER,
    INTERMEDIATE,
    EXPERIENCED
}