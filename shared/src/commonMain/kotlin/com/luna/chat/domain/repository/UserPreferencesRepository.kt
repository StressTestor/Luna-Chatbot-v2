package com.luna.chat.domain.repository

import com.luna.chat.domain.entity.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userPreferencesFlow: Flow<UserPreferences>
    suspend fun updateTheme(theme: String)
    suspend fun updateParentalControls(enabled: Boolean)
    suspend fun updateApiKeyConfigured(configured: Boolean)
    suspend fun markNotFirstTimeUser()
    suspend fun updateAutoClearHistoryDays(days: Int)
    suspend fun updateContentFilter(enabled: Boolean)
    suspend fun updateVoiceInput(enabled: Boolean)
    suspend fun updateAppVersion(version: String)
    suspend fun incrementMessagesSent()
    suspend fun updateSelectedModel(modelId: String)
    suspend fun updateFavoriteThemeColors(colors: Set<String>)
    suspend fun resetToDefaults()
}
