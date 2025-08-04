package com.luna.chat.domain.entity

import org.junit.Assert.*
import org.junit.Test

class ThemeTest {
    
    @Test
    fun `validate should return true for valid theme`() {
        // Given
        val validTheme = Theme(
            id = "test",
            name = "test",
            displayName = "Test Theme",
            description = "A test theme",
            primaryColor = 0xFF6366F1,
            secondaryColor = 0xFFEC4899,
            backgroundColor = 0xFFFEF3C7,
            surfaceColor = 0xFFFFFFFF,
            onPrimaryColor = 0xFFFFFFFF,
            onSecondaryColor = 0xFFFFFFFF,
            onBackgroundColor = 0xFF1F2937,
            onSurfaceColor = 0xFF1F2937
        )
        
        // When & Then
        assertTrue(validTheme.validate())
    }
    
    @Test
    fun `validate should return false for theme with blank id`() {
        // Given
        val invalidTheme = Theme(
            id = "",
            name = "test",
            displayName = "Test Theme",
            description = "A test theme",
            primaryColor = 0xFF6366F1,
            secondaryColor = 0xFFEC4899,
            backgroundColor = 0xFFFEF3C7,
            surfaceColor = 0xFFFFFFFF,
            onPrimaryColor = 0xFFFFFFFF,
            onSecondaryColor = 0xFFFFFFFF,
            onBackgroundColor = 0xFF1F2937,
            onSurfaceColor = 0xFF1F2937
        )
        
        // When & Then
        assertFalse(invalidTheme.validate())
    }
    
    @Test
    fun `validate should return false for theme with blank name`() {
        // Given
        val invalidTheme = Theme(
            id = "test",
            name = "",
            displayName = "Test Theme",
            description = "A test theme",
            primaryColor = 0xFF6366F1,
            secondaryColor = 0xFFEC4899,
            backgroundColor = 0xFFFEF3C7,
            surfaceColor = 0xFFFFFFFF,
            onPrimaryColor = 0xFFFFFFFF,
            onSecondaryColor = 0xFFFFFFFF,
            onBackgroundColor = 0xFF1F2937,
            onSurfaceColor = 0xFF1F2937
        )
        
        // When & Then
        assertFalse(invalidTheme.validate())
    }
    
    @Test
    fun `getAllThemes should return all predefined themes`() {
        // When
        val themes = Theme.getAllThemes()
        
        // Then
        assertEquals(5, themes.size)
        
        val themeIds = themes.map { it.id }
        assertTrue(themeIds.contains("rainbow"))
        assertTrue(themeIds.contains("ocean"))
        assertTrue(themeIds.contains("forest"))
        assertTrue(themeIds.contains("space"))
        assertTrue(themeIds.contains("sunset"))
    }
    
    @Test
    fun `getThemeById should return correct theme`() {
        // When
        val rainbowTheme = Theme.getThemeById("rainbow")
        val oceanTheme = Theme.getThemeById("ocean")
        val invalidTheme = Theme.getThemeById("invalid")
        
        // Then
        assertNotNull(rainbowTheme)
        assertEquals("rainbow", rainbowTheme?.id)
        assertEquals("Rainbow 🌈", rainbowTheme?.displayName)
        
        assertNotNull(oceanTheme)
        assertEquals("ocean", oceanTheme?.id)
        assertEquals("Ocean 🌊", oceanTheme?.displayName)
        
        assertNull(invalidTheme)
    }
    
    @Test
    fun `getDefaultTheme should return rainbow theme`() {
        // When
        val defaultTheme = Theme.getDefaultTheme()
        
        // Then
        assertEquals("rainbow", defaultTheme.id)
        assertEquals("Rainbow 🌈", defaultTheme.displayName)
    }
    
    @Test
    fun `isValidThemeId should return true for valid theme IDs`() {
        // Given
        val validIds = listOf("rainbow", "ocean", "forest", "space", "sunset")
        
        // When & Then
        validIds.forEach { id ->
            assertTrue("Theme ID '$id' should be valid", Theme.isValidThemeId(id))
        }
    }
    
    @Test
    fun `isValidThemeId should return false for invalid theme IDs`() {
        // Given
        val invalidIds = listOf("invalid", "", "dark", "adult", "custom")
        
        // When & Then
        invalidIds.forEach { id ->
            assertFalse("Theme ID '$id' should be invalid", Theme.isValidThemeId(id))
        }
    }
    
    @Test
    fun `all predefined themes should be child-friendly`() {
        // When
        val themes = Theme.getAllThemes()
        
        // Then
        themes.forEach { theme ->
            assertTrue("Theme '${theme.id}' should be child-friendly", theme.isChildFriendly)
        }
    }
    
    @Test
    fun `all predefined themes should be valid`() {
        // When
        val themes = Theme.getAllThemes()
        
        // Then
        themes.forEach { theme ->
            assertTrue("Theme '${theme.id}' should be valid", theme.validate())
        }
    }
    
    @Test
    fun `rainbow theme should have correct properties`() {
        // When
        val theme = Theme.RAINBOW_THEME
        
        // Then
        assertEquals("rainbow", theme.id)
        assertEquals("rainbow", theme.name)
        assertEquals("Rainbow 🌈", theme.displayName)
        assertEquals("Bright and colorful like a rainbow!", theme.description)
        assertTrue(theme.isChildFriendly)
        assertTrue(theme.validate())
    }
    
    @Test
    fun `ocean theme should have correct properties`() {
        // When
        val theme = Theme.OCEAN_THEME
        
        // Then
        assertEquals("ocean", theme.id)
        assertEquals("ocean", theme.name)
        assertEquals("Ocean 🌊", theme.displayName)
        assertEquals("Cool blues like the deep ocean!", theme.description)
        assertTrue(theme.isChildFriendly)
        assertTrue(theme.validate())
    }
    
    @Test
    fun `forest theme should have correct properties`() {
        // When
        val theme = Theme.FOREST_THEME
        
        // Then
        assertEquals("forest", theme.id)
        assertEquals("forest", theme.name)
        assertEquals("Forest 🌲", theme.displayName)
        assertEquals("Fresh greens like a magical forest!", theme.description)
        assertTrue(theme.isChildFriendly)
        assertTrue(theme.validate())
    }
    
    @Test
    fun `space theme should have correct properties`() {
        // When
        val theme = Theme.SPACE_THEME
        
        // Then
        assertEquals("space", theme.id)
        assertEquals("space", theme.name)
        assertEquals("Space 🚀", theme.displayName)
        assertEquals("Dark and mysterious like outer space!", theme.description)
        assertTrue(theme.isChildFriendly)
        assertTrue(theme.validate())
    }
    
    @Test
    fun `sunset theme should have correct properties`() {
        // When
        val theme = Theme.SUNSET_THEME
        
        // Then
        assertEquals("sunset", theme.id)
        assertEquals("sunset", theme.name)
        assertEquals("Sunset 🌅", theme.displayName)
        assertEquals("Warm oranges and pinks like a beautiful sunset!", theme.description)
        assertTrue(theme.isChildFriendly)
        assertTrue(theme.validate())
    }
}