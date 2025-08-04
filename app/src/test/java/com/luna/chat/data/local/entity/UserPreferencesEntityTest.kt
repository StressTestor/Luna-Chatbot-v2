package com.luna.chat.data.local.entity

import org.junit.Test
import org.junit.Assert.*

class UserPreferencesEntityTest {

    @Test
    fun `createDefault should create entity with default values`() {
        val preferences = UserPreferencesEntity.createDefault()
        
        assertEquals(1, preferences.id)
        assertEquals(UserPreferencesEntity.DEFAULT_THEME, preferences.selectedTheme)
        assertTrue(preferences.parentalControlsEnabled)
        assertFalse(preferences.apiKeyConfigured)
        assertTrue(preferences.firstTimeUser)
        assertEquals(UserPreferencesEntity.DEFAULT_AUTO_CLEAR_DAYS, preferences.autoClearHistoryDays)
        assertTrue(preferences.contentFilterEnabled)
        assertTrue(preferences.voiceInputEnabled)
        assertTrue(preferences.createdAt > 0)
        assertTrue(preferences.updatedAt > 0)
    }

    @Test
    fun `updateTheme should update theme and timestamp`() {
        val originalPrefs = UserPreferencesEntity.createDefault()
        val originalUpdatedAt = originalPrefs.updatedAt
        
        // Wait a bit to ensure timestamp difference
        Thread.sleep(1)
        
        val newTheme = "ocean"
        val updatedPrefs = originalPrefs.updateTheme(newTheme)
        
        assertEquals(newTheme, updatedPrefs.selectedTheme)
        assertTrue(updatedPrefs.updatedAt > originalUpdatedAt)
        // Other fields should remain the same
        assertEquals(originalPrefs.id, updatedPrefs.id)
        assertEquals(originalPrefs.parentalControlsEnabled, updatedPrefs.parentalControlsEnabled)
        assertEquals(originalPrefs.apiKeyConfigured, updatedPrefs.apiKeyConfigured)
    }

    @Test
    fun `markApiKeyConfigured should update api key status and timestamp`() {
        val originalPrefs = UserPreferencesEntity.createDefault()
        val originalUpdatedAt = originalPrefs.updatedAt
        
        // Wait a bit to ensure timestamp difference
        Thread.sleep(1)
        
        val updatedPrefs = originalPrefs.markApiKeyConfigured()
        
        assertTrue(updatedPrefs.apiKeyConfigured)
        assertTrue(updatedPrefs.updatedAt > originalUpdatedAt)
        // Other fields should remain the same
        assertEquals(originalPrefs.selectedTheme, updatedPrefs.selectedTheme)
        assertEquals(originalPrefs.parentalControlsEnabled, updatedPrefs.parentalControlsEnabled)
    }

    @Test
    fun `markNotFirstTime should update first time user status and timestamp`() {
        val originalPrefs = UserPreferencesEntity.createDefault()
        val originalUpdatedAt = originalPrefs.updatedAt
        
        // Wait a bit to ensure timestamp difference
        Thread.sleep(1)
        
        val updatedPrefs = originalPrefs.markNotFirstTime()
        
        assertFalse(updatedPrefs.firstTimeUser)
        assertTrue(updatedPrefs.updatedAt > originalUpdatedAt)
        // Other fields should remain the same
        assertEquals(originalPrefs.selectedTheme, updatedPrefs.selectedTheme)
        assertEquals(originalPrefs.apiKeyConfigured, updatedPrefs.apiKeyConfigured)
    }

    @Test
    fun `updateParentalControls should update parental controls and timestamp`() {
        val originalPrefs = UserPreferencesEntity.createDefault()
        val originalUpdatedAt = originalPrefs.updatedAt
        
        // Wait a bit to ensure timestamp difference
        Thread.sleep(1)
        
        val updatedPrefs = originalPrefs.updateParentalControls(false)
        
        assertFalse(updatedPrefs.parentalControlsEnabled)
        assertTrue(updatedPrefs.updatedAt > originalUpdatedAt)
        // Other fields should remain the same
        assertEquals(originalPrefs.selectedTheme, updatedPrefs.selectedTheme)
        assertEquals(originalPrefs.apiKeyConfigured, updatedPrefs.apiKeyConfigured)
    }

    @Test
    fun `multiple updates should chain correctly`() {
        val originalPrefs = UserPreferencesEntity.createDefault()
        
        val updatedPrefs = originalPrefs
            .updateTheme("space")
            .markApiKeyConfigured()
            .markNotFirstTime()
            .updateParentalControls(false)
        
        assertEquals("space", updatedPrefs.selectedTheme)
        assertTrue(updatedPrefs.apiKeyConfigured)
        assertFalse(updatedPrefs.firstTimeUser)
        assertFalse(updatedPrefs.parentalControlsEnabled)
        assertTrue(updatedPrefs.updatedAt > originalPrefs.updatedAt)
    }

    @Test
    fun `default constants should have expected values`() {
        assertEquals("rainbow", UserPreferencesEntity.DEFAULT_THEME)
        assertEquals(30, UserPreferencesEntity.DEFAULT_AUTO_CLEAR_DAYS)
    }

    @Test
    fun `entity should handle all theme options`() {
        val themes = listOf("rainbow", "ocean", "forest", "space", "sunset")
        val originalPrefs = UserPreferencesEntity.createDefault()
        
        themes.forEach { theme ->
            val updatedPrefs = originalPrefs.updateTheme(theme)
            assertEquals(theme, updatedPrefs.selectedTheme)
        }
    }

    @Test
    fun `entity should preserve id across updates`() {
        val originalPrefs = UserPreferencesEntity.createDefault()
        val originalId = originalPrefs.id
        
        val updatedPrefs = originalPrefs
            .updateTheme("ocean")
            .markApiKeyConfigured()
            .markNotFirstTime()
        
        assertEquals(originalId, updatedPrefs.id)
    }

    @Test
    fun `entity should handle edge case values`() {
        val preferences = UserPreferencesEntity(
            id = 1,
            selectedTheme = "",
            parentalControlsEnabled = false,
            apiKeyConfigured = true,
            firstTimeUser = false,
            autoClearHistoryDays = 0,
            contentFilterEnabled = false,
            voiceInputEnabled = false,
            createdAt = 0,
            updatedAt = 0
        )
        
        // Should not throw exceptions
        assertNotNull(preferences)
        assertEquals("", preferences.selectedTheme)
        assertEquals(0, preferences.autoClearHistoryDays)
        assertEquals(0, preferences.createdAt)
    }

    @Test
    fun `copy should work correctly with all fields`() {
        val original = UserPreferencesEntity.createDefault()
        val copied = original.copy(
            selectedTheme = "custom",
            parentalControlsEnabled = false,
            autoClearHistoryDays = 60
        )
        
        assertEquals("custom", copied.selectedTheme)
        assertFalse(copied.parentalControlsEnabled)
        assertEquals(60, copied.autoClearHistoryDays)
        // Other fields should remain the same
        assertEquals(original.id, copied.id)
        assertEquals(original.apiKeyConfigured, copied.apiKeyConfigured)
        assertEquals(original.firstTimeUser, copied.firstTimeUser)
    }
}