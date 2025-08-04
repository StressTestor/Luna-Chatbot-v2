package com.luna.chat.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class UserPreferencesRepositoryTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var dataStore: DataStore<Preferences>

    private lateinit var userPreferencesRepository: UserPreferencesRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        userPreferencesRepository = UserPreferencesRepository(context)
    }

    @Test
    fun `UserPreferences should have correct default values`() {
        // Given
        val defaultPrefs = UserPreferences()

        // Then
        assertEquals("rainbow", defaultPrefs.selectedTheme)
        assertTrue(defaultPrefs.parentalControlsEnabled)
        assertFalse(defaultPrefs.apiKeyConfigured)
        assertTrue(defaultPrefs.firstTimeUser)
        assertEquals(30, defaultPrefs.autoClearHistoryDays)
        assertTrue(defaultPrefs.contentFilterEnabled)
        assertTrue(defaultPrefs.voiceInputEnabled)
        assertEquals("", defaultPrefs.appVersion)
        assertEquals(0, defaultPrefs.totalMessagesSent)
        assertTrue(defaultPrefs.favoriteThemeColors.isEmpty())
    }

    @Test
    fun `isValidTheme should validate theme names correctly`() {
        // Valid themes
        assertTrue(UserPreferences.isValidTheme("rainbow"))
        assertTrue(UserPreferences.isValidTheme("ocean"))
        assertTrue(UserPreferences.isValidTheme("forest"))
        assertTrue(UserPreferences.isValidTheme("space"))
        assertTrue(UserPreferences.isValidTheme("sunset"))

        // Invalid themes
        assertFalse(UserPreferences.isValidTheme("invalid"))
        assertFalse(UserPreferences.isValidTheme(""))
        assertFalse(UserPreferences.isValidTheme("RAINBOW"))
        assertFalse(UserPreferences.isValidTheme("dark"))
    }

    @Test
    fun `hasValidTheme should check theme validity`() {
        // Valid theme
        val validPrefs = UserPreferences(selectedTheme = "ocean")
        assertTrue(validPrefs.hasValidTheme())

        // Invalid theme
        val invalidPrefs = UserPreferences(selectedTheme = "invalid")
        assertFalse(invalidPrefs.hasValidTheme())
    }

    @Test
    fun `hasValidAutoClearSetting should validate auto-clear days`() {
        // Valid settings
        val validPrefs1 = UserPreferences(autoClearHistoryDays = 1)
        assertTrue(validPrefs1.hasValidAutoClearSetting())

        val validPrefs2 = UserPreferences(autoClearHistoryDays = 30)
        assertTrue(validPrefs2.hasValidAutoClearSetting())

        val validPrefs3 = UserPreferences(autoClearHistoryDays = 365)
        assertTrue(validPrefs3.hasValidAutoClearSetting())

        // Invalid settings
        val invalidPrefs1 = UserPreferences(autoClearHistoryDays = 0)
        assertFalse(invalidPrefs1.hasValidAutoClearSetting())

        val invalidPrefs2 = UserPreferences(autoClearHistoryDays = -1)
        assertFalse(invalidPrefs2.hasValidAutoClearSetting())

        val invalidPrefs3 = UserPreferences(autoClearHistoryDays = 366)
        assertFalse(invalidPrefs3.hasValidAutoClearSetting())
    }

    @Test
    fun `getUserExperienceLevel should return correct levels`() {
        // New user
        val newUser = UserPreferences(totalMessagesSent = 0)
        assertEquals(UserExperienceLevel.NEW, newUser.getUserExperienceLevel())

        // Beginner user
        val beginnerUser = UserPreferences(totalMessagesSent = 5)
        assertEquals(UserExperienceLevel.BEGINNER, beginnerUser.getUserExperienceLevel())

        // Intermediate user
        val intermediateUser = UserPreferences(totalMessagesSent = 25)
        assertEquals(UserExperienceLevel.INTERMEDIATE, intermediateUser.getUserExperienceLevel())

        // Experienced user
        val experiencedUser = UserPreferences(totalMessagesSent = 100)
        assertEquals(UserExperienceLevel.EXPERIENCED, experiencedUser.getUserExperienceLevel())
    }

    @Test
    fun `AVAILABLE_THEMES should contain all expected themes`() {
        val expectedThemes = listOf("rainbow", "ocean", "forest", "space", "sunset")
        assertEquals(expectedThemes, UserPreferences.AVAILABLE_THEMES)
        assertEquals(5, UserPreferences.AVAILABLE_THEMES.size)
    }

    @Test
    fun `default constants should have correct values`() {
        assertEquals("rainbow", UserPreferencesRepository.DEFAULT_THEME)
        assertEquals(30, UserPreferencesRepository.DEFAULT_AUTO_CLEAR_DAYS)
        assertTrue(UserPreferencesRepository.DEFAULT_PARENTAL_CONTROLS)
        assertTrue(UserPreferencesRepository.DEFAULT_CONTENT_FILTER)
        assertTrue(UserPreferencesRepository.DEFAULT_VOICE_INPUT)
    }

    @Test
    fun `UserExperienceLevel enum should have all expected values`() {
        val expectedLevels = arrayOf(
            UserExperienceLevel.NEW,
            UserExperienceLevel.BEGINNER,
            UserExperienceLevel.INTERMEDIATE,
            UserExperienceLevel.EXPERIENCED
        )
        
        assertArrayEquals(expectedLevels, UserExperienceLevel.values())
        assertEquals(4, UserExperienceLevel.values().size)
    }

    @Test
    fun `preference keys should be properly defined`() {
        // Test that preference keys are not null and have expected names
        assertNotNull(UserPreferencesRepository.SELECTED_THEME)
        assertNotNull(UserPreferencesRepository.PARENTAL_CONTROLS_ENABLED)
        assertNotNull(UserPreferencesRepository.API_KEY_CONFIGURED)
        assertNotNull(UserPreferencesRepository.FIRST_TIME_USER)
        assertNotNull(UserPreferencesRepository.AUTO_CLEAR_HISTORY_DAYS)
        assertNotNull(UserPreferencesRepository.CONTENT_FILTER_ENABLED)
        assertNotNull(UserPreferencesRepository.VOICE_INPUT_ENABLED)
        assertNotNull(UserPreferencesRepository.LAST_UPDATED)
        assertNotNull(UserPreferencesRepository.APP_VERSION)
        assertNotNull(UserPreferencesRepository.TOTAL_MESSAGES_SENT)
        assertNotNull(UserPreferencesRepository.FAVORITE_THEME_COLORS)

        // Test key names (these are the actual string values used in DataStore)
        assertEquals("selected_theme", UserPreferencesRepository.SELECTED_THEME.name)
        assertEquals("parental_controls_enabled", UserPreferencesRepository.PARENTAL_CONTROLS_ENABLED.name)
        assertEquals("api_key_configured", UserPreferencesRepository.API_KEY_CONFIGURED.name)
        assertEquals("first_time_user", UserPreferencesRepository.FIRST_TIME_USER.name)
        assertEquals("auto_clear_history_days", UserPreferencesRepository.AUTO_CLEAR_HISTORY_DAYS.name)
        assertEquals("content_filter_enabled", UserPreferencesRepository.CONTENT_FILTER_ENABLED.name)
        assertEquals("voice_input_enabled", UserPreferencesRepository.VOICE_INPUT_ENABLED.name)
        assertEquals("last_updated", UserPreferencesRepository.LAST_UPDATED.name)
        assertEquals("app_version", UserPreferencesRepository.APP_VERSION.name)
        assertEquals("total_messages_sent", UserPreferencesRepository.TOTAL_MESSAGES_SENT.name)
        assertEquals("favorite_theme_colors", UserPreferencesRepository.FAVORITE_THEME_COLORS.name)
    }

    @Test
    fun `UserPreferences copy methods should work correctly`() {
        // Given
        val originalPrefs = UserPreferences(
            selectedTheme = "ocean",
            parentalControlsEnabled = false,
            totalMessagesSent = 10
        )

        // When - Test that we can create modified copies
        val modifiedTheme = originalPrefs.copy(selectedTheme = "forest")
        val modifiedControls = originalPrefs.copy(parentalControlsEnabled = true)
        val modifiedMessages = originalPrefs.copy(totalMessagesSent = 20)

        // Then
        assertEquals("forest", modifiedTheme.selectedTheme)
        assertEquals(false, modifiedTheme.parentalControlsEnabled) // Should remain unchanged
        assertEquals(10, modifiedTheme.totalMessagesSent) // Should remain unchanged

        assertEquals("ocean", modifiedControls.selectedTheme) // Should remain unchanged
        assertEquals(true, modifiedControls.parentalControlsEnabled)
        assertEquals(10, modifiedControls.totalMessagesSent) // Should remain unchanged

        assertEquals("ocean", modifiedMessages.selectedTheme) // Should remain unchanged
        assertEquals(false, modifiedMessages.parentalControlsEnabled) // Should remain unchanged
        assertEquals(20, modifiedMessages.totalMessagesSent)
    }

    @Test
    fun `favorite theme colors should handle empty and populated sets`() {
        // Empty set
        val emptyPrefs = UserPreferences(favoriteThemeColors = emptySet())
        assertTrue(emptyPrefs.favoriteThemeColors.isEmpty())

        // Populated set
        val colors = setOf("blue", "green", "purple")
        val populatedPrefs = UserPreferences(favoriteThemeColors = colors)
        assertEquals(3, populatedPrefs.favoriteThemeColors.size)
        assertTrue(populatedPrefs.favoriteThemeColors.contains("blue"))
        assertTrue(populatedPrefs.favoriteThemeColors.contains("green"))
        assertTrue(populatedPrefs.favoriteThemeColors.contains("purple"))
    }
}