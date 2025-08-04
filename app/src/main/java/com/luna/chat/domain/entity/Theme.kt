package com.luna.chat.domain.entity

import androidx.compose.ui.graphics.Color

/**
 * Represents a theme configuration for the Luna chat app
 */
data class Theme(
    val id: String,
    val name: String,
    val displayName: String,
    val description: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val backgroundColor: Long,
    val surfaceColor: Long,
    val onPrimaryColor: Long,
    val onSecondaryColor: Long,
    val onBackgroundColor: Long,
    val onSurfaceColor: Long,
    val isChildFriendly: Boolean = true
) {
    
    fun validate(): Boolean {
        return id.isNotBlank() && 
               name.isNotBlank() && 
               displayName.isNotBlank() &&
               description.isNotBlank()
    }
    
    companion object {
        
        // Predefined child-friendly themes
        val RAINBOW_THEME = Theme(
            id = "rainbow",
            name = "rainbow",
            displayName = "Rainbow 🌈",
            description = "Bright and colorful like a rainbow!",
            primaryColor = 0xFF6366F1,
            secondaryColor = 0xFFEC4899,
            backgroundColor = 0xFFFEF3C7,
            surfaceColor = 0xFFFFFFFF,
            onPrimaryColor = 0xFFFFFFFF,
            onSecondaryColor = 0xFFFFFFFF,
            onBackgroundColor = 0xFF1F2937,
            onSurfaceColor = 0xFF1F2937
        )
        
        val OCEAN_THEME = Theme(
            id = "ocean",
            name = "ocean",
            displayName = "Ocean 🌊",
            description = "Cool blues like the deep ocean!",
            primaryColor = 0xFF0EA5E9,
            secondaryColor = 0xFF06B6D4,
            backgroundColor = 0xFFE0F7FA,
            surfaceColor = 0xFFFFFFFF,
            onPrimaryColor = 0xFFFFFFFF,
            onSecondaryColor = 0xFFFFFFFF,
            onBackgroundColor = 0xFF0F172A,
            onSurfaceColor = 0xFF0F172A
        )
        
        val FOREST_THEME = Theme(
            id = "forest",
            name = "forest",
            displayName = "Forest 🌲",
            description = "Fresh greens like a magical forest!",
            primaryColor = 0xFF10B981,
            secondaryColor = 0xFF34D399,
            backgroundColor = 0xFFECFDF5,
            surfaceColor = 0xFFFFFFFF,
            onPrimaryColor = 0xFFFFFFFF,
            onSecondaryColor = 0xFF000000,
            onBackgroundColor = 0xFF064E3B,
            onSurfaceColor = 0xFF064E3B
        )
        
        val SPACE_THEME = Theme(
            id = "space",
            name = "space",
            displayName = "Space 🚀",
            description = "Dark and mysterious like outer space!",
            primaryColor = 0xFF8B5CF6,
            secondaryColor = 0xFFA78BFA,
            backgroundColor = 0xFF1E1B4B,
            surfaceColor = 0xFF312E81,
            onPrimaryColor = 0xFFFFFFFF,
            onSecondaryColor = 0xFFFFFFFF,
            onBackgroundColor = 0xFFE0E7FF,
            onSurfaceColor = 0xFFE0E7FF
        )
        
        val SUNSET_THEME = Theme(
            id = "sunset",
            name = "sunset",
            displayName = "Sunset 🌅",
            description = "Warm oranges and pinks like a beautiful sunset!",
            primaryColor = 0xFFF97316,
            secondaryColor = 0xFFEC4899,
            backgroundColor = 0xFFFFF7ED,
            surfaceColor = 0xFFFFFFFF,
            onPrimaryColor = 0xFFFFFFFF,
            onSecondaryColor = 0xFFFFFFFF,
            onBackgroundColor = 0xFF9A3412,
            onSurfaceColor = 0xFF9A3412
        )
        
        /**
         * Gets all available themes
         */
        fun getAllThemes(): List<Theme> {
            return listOf(
                RAINBOW_THEME,
                OCEAN_THEME,
                FOREST_THEME,
                SPACE_THEME,
                SUNSET_THEME
            )
        }
        
        /**
         * Gets a theme by its ID
         */
        fun getThemeById(id: String): Theme? {
            return getAllThemes().find { it.id == id }
        }
        
        /**
         * Gets the default theme
         */
        fun getDefaultTheme(): Theme = RAINBOW_THEME
        
        /**
         * Validates if a theme ID is valid
         */
        fun isValidThemeId(id: String): Boolean {
            return getAllThemes().any { it.id == id }
        }
    }
}