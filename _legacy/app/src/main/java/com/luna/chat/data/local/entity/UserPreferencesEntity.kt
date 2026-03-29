package com.luna.chat.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey 
    @ColumnInfo(name = "id")
    val id: Int = 1,
    
    @ColumnInfo(name = "selected_theme")
    val selectedTheme: String,
    
    @ColumnInfo(name = "parental_controls_enabled")
    val parentalControlsEnabled: Boolean,
    
    @ColumnInfo(name = "api_key_configured")
    val apiKeyConfigured: Boolean,
    
    @ColumnInfo(name = "first_time_user")
    val firstTimeUser: Boolean = true,
    
    @ColumnInfo(name = "auto_clear_history_days")
    val autoClearHistoryDays: Int = 30,
    
    @ColumnInfo(name = "content_filter_enabled")
    val contentFilterEnabled: Boolean = true,
    
    @ColumnInfo(name = "voice_input_enabled")
    val voiceInputEnabled: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val DEFAULT_THEME = "rainbow"
        const val DEFAULT_AUTO_CLEAR_DAYS = 30
        
        fun createDefault(): UserPreferencesEntity {
            return UserPreferencesEntity(
                selectedTheme = DEFAULT_THEME,
                parentalControlsEnabled = true,
                apiKeyConfigured = false,
                firstTimeUser = true,
                autoClearHistoryDays = DEFAULT_AUTO_CLEAR_DAYS,
                contentFilterEnabled = true,
                voiceInputEnabled = true
            )
        }
    }
    
    fun updateTheme(theme: String): UserPreferencesEntity {
        return copy(
            selectedTheme = theme,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun markApiKeyConfigured(): UserPreferencesEntity {
        return copy(
            apiKeyConfigured = true,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun markNotFirstTime(): UserPreferencesEntity {
        return copy(
            firstTimeUser = false,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun updateParentalControls(enabled: Boolean): UserPreferencesEntity {
        return copy(
            parentalControlsEnabled = enabled,
            updatedAt = System.currentTimeMillis()
        )
    }
}