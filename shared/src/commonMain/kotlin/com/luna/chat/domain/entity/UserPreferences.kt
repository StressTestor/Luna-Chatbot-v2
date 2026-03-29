package com.luna.chat.domain.entity

data class UserPreferences(
    val selectedTheme: String = DEFAULT_THEME,
    val parentalControlsEnabled: Boolean = DEFAULT_PARENTAL_CONTROLS,
    val apiKeyConfigured: Boolean = false,
    val firstTimeUser: Boolean = true,
    val autoClearHistoryDays: Int = DEFAULT_AUTO_CLEAR_DAYS,
    val contentFilterEnabled: Boolean = DEFAULT_CONTENT_FILTER,
    val voiceInputEnabled: Boolean = DEFAULT_VOICE_INPUT,
    val lastUpdated: Long = 0L,
    val appVersion: String = "",
    val totalMessagesSent: Int = 0,
    val selectedModel: String = DEFAULT_MODEL,
    val favoriteThemeColors: Set<String> = emptySet()
) {
    companion object {
        const val DEFAULT_THEME = "rainbow"
        const val DEFAULT_MODEL = "nvidia/nemotron-3-super-120b-a12b:free"
        const val DEFAULT_AUTO_CLEAR_DAYS = 30
        const val DEFAULT_PARENTAL_CONTROLS = true
        const val DEFAULT_CONTENT_FILTER = true
        const val DEFAULT_VOICE_INPUT = true

        val AVAILABLE_THEMES = listOf("rainbow", "ocean", "forest", "space", "sunset")

        fun isValidTheme(theme: String): Boolean = theme in AVAILABLE_THEMES
    }

    fun hasValidTheme(): Boolean = isValidTheme(selectedTheme)
    fun hasValidAutoClearSetting(): Boolean = autoClearHistoryDays in 1..365

    fun getUserExperienceLevel(): UserExperienceLevel = when {
        totalMessagesSent == 0 -> UserExperienceLevel.NEW
        totalMessagesSent < 10 -> UserExperienceLevel.BEGINNER
        totalMessagesSent < 50 -> UserExperienceLevel.INTERMEDIATE
        else -> UserExperienceLevel.EXPERIENCED
    }
}

enum class UserExperienceLevel {
    NEW, BEGINNER, INTERMEDIATE, EXPERIENCED
}
