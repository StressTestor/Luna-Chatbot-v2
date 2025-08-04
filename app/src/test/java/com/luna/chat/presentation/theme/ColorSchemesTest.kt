package com.luna.chat.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class ColorSchemesTest {
    
    @Test
    fun `getColorSchemeForTheme should return correct color scheme for rainbow theme`() {
        // When
        val colorScheme = LunaColorSchemes.getColorSchemeForTheme("rainbow")
        
        // Then
        assertEquals(RainbowPrimary, colorScheme.primary)
        assertEquals(RainbowSecondary, colorScheme.secondary)
        assertEquals(RainbowBackground, colorScheme.background)
        assertEquals(RainbowSurface, colorScheme.surface)
    }
    
    @Test
    fun `getColorSchemeForTheme should return correct color scheme for ocean theme`() {
        // When
        val colorScheme = LunaColorSchemes.getColorSchemeForTheme("ocean")
        
        // Then
        assertEquals(OceanPrimary, colorScheme.primary)
        assertEquals(OceanSecondary, colorScheme.secondary)
        assertEquals(OceanBackground, colorScheme.background)
        assertEquals(OceanSurface, colorScheme.surface)
    }
    
    @Test
    fun `getColorSchemeForTheme should return correct color scheme for forest theme`() {
        // When
        val colorScheme = LunaColorSchemes.getColorSchemeForTheme("forest")
        
        // Then
        assertEquals(ForestPrimary, colorScheme.primary)
        assertEquals(ForestSecondary, colorScheme.secondary)
        assertEquals(ForestBackground, colorScheme.background)
        assertEquals(ForestSurface, colorScheme.surface)
    }
    
    @Test
    fun `getColorSchemeForTheme should return correct color scheme for space theme`() {
        // When
        val colorScheme = LunaColorSchemes.getColorSchemeForTheme("space")
        
        // Then
        assertEquals(SpacePrimary, colorScheme.primary)
        assertEquals(SpaceSecondary, colorScheme.secondary)
        assertEquals(SpaceBackground, colorScheme.background)
        assertEquals(SpaceSurface, colorScheme.surface)
    }
    
    @Test
    fun `getColorSchemeForTheme should return correct color scheme for sunset theme`() {
        // When
        val colorScheme = LunaColorSchemes.getColorSchemeForTheme("sunset")
        
        // Then
        assertEquals(SunsetPrimary, colorScheme.primary)
        assertEquals(SunsetSecondary, colorScheme.secondary)
        assertEquals(SunsetBackground, colorScheme.background)
        assertEquals(SunsetSurface, colorScheme.surface)
    }
    
    @Test
    fun `getColorSchemeForTheme should return rainbow scheme for invalid theme`() {
        // When
        val colorScheme = LunaColorSchemes.getColorSchemeForTheme("invalid")
        
        // Then
        assertEquals(RainbowPrimary, colorScheme.primary)
        assertEquals(RainbowSecondary, colorScheme.secondary)
        assertEquals(RainbowBackground, colorScheme.background)
        assertEquals(RainbowSurface, colorScheme.surface)
    }
    
    @Test
    fun `getColorSchemeForTheme should return rainbow scheme for empty theme`() {
        // When
        val colorScheme = LunaColorSchemes.getColorSchemeForTheme("")
        
        // Then
        assertEquals(RainbowPrimary, colorScheme.primary)
        assertEquals(RainbowSecondary, colorScheme.secondary)
        assertEquals(RainbowBackground, colorScheme.background)
        assertEquals(RainbowSurface, colorScheme.surface)
    }
    
    @Test
    fun `isThemeDark should return true only for space theme`() {
        // Given
        val themes = listOf("rainbow", "ocean", "forest", "space", "sunset")
        
        // When & Then
        themes.forEach { theme ->
            if (theme == "space") {
                assertTrue("Space theme should be dark", LunaColorSchemes.isThemeDark(theme))
            } else {
                assertFalse("$theme theme should not be dark", LunaColorSchemes.isThemeDark(theme))
            }
        }
    }
    
    @Test
    fun `isThemeDark should return false for invalid theme`() {
        // When & Then
        assertFalse(LunaColorSchemes.isThemeDark("invalid"))
        assertFalse(LunaColorSchemes.isThemeDark(""))
    }
    
    @Test
    fun `all color schemes should have proper contrast ratios`() {
        // Given
        val themes = listOf("rainbow", "ocean", "forest", "space", "sunset")
        
        // When & Then
        themes.forEach { themeId ->
            val colorScheme = LunaColorSchemes.getColorSchemeForTheme(themeId)
            
            // Verify that on-colors are different from their base colors
            assertNotEquals("Primary and onPrimary should be different for $themeId", 
                colorScheme.primary, colorScheme.onPrimary)
            assertNotEquals("Secondary and onSecondary should be different for $themeId", 
                colorScheme.secondary, colorScheme.onSecondary)
            assertNotEquals("Background and onBackground should be different for $themeId", 
                colorScheme.background, colorScheme.onBackground)
            assertNotEquals("Surface and onSurface should be different for $themeId", 
                colorScheme.surface, colorScheme.onSurface)
        }
    }
    
    @Test
    fun `rainbow color scheme should have child-friendly bright colors`() {
        // When
        val colorScheme = LunaColorSchemes.RainbowLightColorScheme
        
        // Then
        assertEquals(Color(0xFF6366F1), colorScheme.primary)
        assertEquals(Color(0xFFEC4899), colorScheme.secondary)
        assertEquals(Color(0xFF10B981), colorScheme.tertiary)
        assertEquals(Color(0xFFFEF3C7), colorScheme.background)
    }
    
    @Test
    fun `ocean color scheme should have blue tones`() {
        // When
        val colorScheme = LunaColorSchemes.OceanLightColorScheme
        
        // Then
        assertEquals(Color(0xFF0EA5E9), colorScheme.primary)
        assertEquals(Color(0xFF06B6D4), colorScheme.secondary)
        assertEquals(Color(0xFF3B82F6), colorScheme.tertiary)
        assertEquals(Color(0xFFE0F7FA), colorScheme.background)
    }
    
    @Test
    fun `forest color scheme should have green tones`() {
        // When
        val colorScheme = LunaColorSchemes.ForestLightColorScheme
        
        // Then
        assertEquals(Color(0xFF10B981), colorScheme.primary)
        assertEquals(Color(0xFF34D399), colorScheme.secondary)
        assertEquals(Color(0xFF059669), colorScheme.tertiary)
        assertEquals(Color(0xFFECFDF5), colorScheme.background)
    }
    
    @Test
    fun `space color scheme should have dark background with bright accents`() {
        // When
        val colorScheme = LunaColorSchemes.SpaceDarkColorScheme
        
        // Then
        assertEquals(Color(0xFF8B5CF6), colorScheme.primary)
        assertEquals(Color(0xFFA78BFA), colorScheme.secondary)
        assertEquals(Color(0xFF6366F1), colorScheme.tertiary)
        assertEquals(Color(0xFF1E1B4B), colorScheme.background)
        assertEquals(Color(0xFF312E81), colorScheme.surface)
    }
    
    @Test
    fun `sunset color scheme should have warm tones`() {
        // When
        val colorScheme = LunaColorSchemes.SunsetLightColorScheme
        
        // Then
        assertEquals(Color(0xFFF97316), colorScheme.primary)
        assertEquals(Color(0xFFEC4899), colorScheme.secondary)
        assertEquals(Color(0xFFFB923C), colorScheme.tertiary)
        assertEquals(Color(0xFFFFF7ED), colorScheme.background)
    }
    
    @Test
    fun `all color schemes should have error colors defined`() {
        // Given
        val themes = listOf("rainbow", "ocean", "forest", "space", "sunset")
        
        // When & Then
        themes.forEach { themeId ->
            val colorScheme = LunaColorSchemes.getColorSchemeForTheme(themeId)
            
            // Verify error colors are defined and not transparent
            assertNotEquals("Error color should be defined for $themeId", 
                Color.Transparent, colorScheme.error)
            assertNotEquals("OnError color should be defined for $themeId", 
                Color.Transparent, colorScheme.onError)
            assertNotEquals("ErrorContainer color should be defined for $themeId", 
                Color.Transparent, colorScheme.errorContainer)
            assertNotEquals("OnErrorContainer color should be defined for $themeId", 
                Color.Transparent, colorScheme.onErrorContainer)
        }
    }
    
    @Test
    fun `all color schemes should have container colors defined`() {
        // Given
        val themes = listOf("rainbow", "ocean", "forest", "space", "sunset")
        
        // When & Then
        themes.forEach { themeId ->
            val colorScheme = LunaColorSchemes.getColorSchemeForTheme(themeId)
            
            // Verify container colors are defined
            assertNotEquals("PrimaryContainer should be defined for $themeId", 
                Color.Transparent, colorScheme.primaryContainer)
            assertNotEquals("SecondaryContainer should be defined for $themeId", 
                Color.Transparent, colorScheme.secondaryContainer)
            assertNotEquals("TertiaryContainer should be defined for $themeId", 
                Color.Transparent, colorScheme.tertiaryContainer)
        }
    }
}