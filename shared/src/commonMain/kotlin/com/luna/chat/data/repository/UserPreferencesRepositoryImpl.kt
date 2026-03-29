package com.luna.chat.data.repository

import com.luna.chat.db.LunaDatabase
import com.luna.chat.domain.entity.UserPreferences
import com.luna.chat.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

class UserPreferencesRepositoryImpl(
    private val database: LunaDatabase
) : UserPreferencesRepository {

    private val _prefsFlow = MutableStateFlow(UserPreferences())
    override val userPreferencesFlow: Flow<UserPreferences> = _prefsFlow.asStateFlow()

    init {
        database.userPreferencesQueries.insertDefaultPreferences()
        refreshPrefs()
    }

    private fun now() = Clock.System.now().toEpochMilliseconds()

    private fun refreshPrefs() {
        val row = database.userPreferencesQueries.getUserPreferences().executeAsOneOrNull() ?: return
        _prefsFlow.value = UserPreferences(
            selectedTheme = row.selected_theme,
            parentalControlsEnabled = row.parental_controls_enabled != 0L,
            apiKeyConfigured = row.api_key_configured != 0L,
            firstTimeUser = row.first_time_user != 0L,
            autoClearHistoryDays = row.auto_clear_history_days.toInt(),
            contentFilterEnabled = row.content_filter_enabled != 0L,
            voiceInputEnabled = row.voice_input_enabled != 0L,
            lastUpdated = row.updated_at,
            appVersion = row.app_version,
            totalMessagesSent = row.total_messages_sent.toInt(),
            selectedModel = row.selected_model,
            favoriteThemeColors = emptySet()
        )
    }

    override suspend fun updateTheme(theme: String) {
        database.userPreferencesQueries.updateTheme(theme, now())
        refreshPrefs()
    }

    override suspend fun updateParentalControls(enabled: Boolean) {
        database.userPreferencesQueries.updateParentalControls(if (enabled) 1L else 0L, now())
        refreshPrefs()
    }

    override suspend fun updateApiKeyConfigured(configured: Boolean) {
        database.userPreferencesQueries.updateApiKeyConfigured(if (configured) 1L else 0L, now())
        refreshPrefs()
    }

    override suspend fun markNotFirstTimeUser() {
        database.userPreferencesQueries.markNotFirstTimeUser(now())
        refreshPrefs()
    }

    override suspend fun updateAutoClearHistoryDays(days: Int) {
        database.userPreferencesQueries.updateAutoClearDays(days.toLong(), now())
        refreshPrefs()
    }

    override suspend fun updateContentFilter(enabled: Boolean) {
        database.userPreferencesQueries.updateContentFilter(if (enabled) 1L else 0L, now())
        refreshPrefs()
    }

    override suspend fun updateVoiceInput(enabled: Boolean) {
        database.userPreferencesQueries.updateVoiceInput(if (enabled) 1L else 0L, now())
        refreshPrefs()
    }

    override suspend fun updateAppVersion(version: String) {
        database.userPreferencesQueries.updateAppVersion(version, now())
        refreshPrefs()
    }

    override suspend fun incrementMessagesSent() {
        database.userPreferencesQueries.incrementMessagesSent(now())
        refreshPrefs()
    }

    override suspend fun updateSelectedModel(modelId: String) {
        database.userPreferencesQueries.updateSelectedModel(modelId, now())
        refreshPrefs()
    }

    override suspend fun updateFavoriteThemeColors(colors: Set<String>) {
        // Stored in SecureStorage or separate table — no-op for now
        refreshPrefs()
    }

    override suspend fun resetToDefaults() {
        database.userPreferencesQueries.resetToDefaults(now())
        refreshPrefs()
    }
}
