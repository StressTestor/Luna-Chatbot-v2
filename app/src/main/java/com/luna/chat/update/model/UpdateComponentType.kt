package com.luna.chat.update.model

import kotlinx.serialization.Serializable

/**
 * Enum representing the different types of components that can be included in an update package.
 */
@Serializable
enum class UpdateComponentType {
    /**
     * Image asset file (png, jpg, etc.)
     */
    ASSET_IMAGE,
    
    /**
     * Text asset file (txt, json, etc.)
     */
    ASSET_TEXT,
    
    /**
     * Binary asset file (any other format)
     */
    ASSET_BINARY,
    
    /**
     * String resource (for localization)
     */
    RESOURCE_STRING,
    
    /**
     * Layout resource (XML)
     */
    RESOURCE_LAYOUT,
    
    /**
     * Configuration file
     */
    CONFIGURATION,
    
    /**
     * Other type of component
     */
    OTHER;
    
    companion object {
        /**
         * Get the appropriate component type based on file extension
         *
         * @param filePath Path to the file
         * @return The component type that best matches the file
         */
        fun fromFilePath(filePath: String): UpdateComponentType {
            return when {
                filePath.endsWith(".png", ignoreCase = true) ||
                filePath.endsWith(".jpg", ignoreCase = true) ||
                filePath.endsWith(".jpeg", ignoreCase = true) ||
                filePath.endsWith(".gif", ignoreCase = true) ||
                filePath.endsWith(".webp", ignoreCase = true) -> ASSET_IMAGE
                
                filePath.endsWith(".txt", ignoreCase = true) ||
                filePath.endsWith(".json", ignoreCase = true) ||
                filePath.endsWith(".xml", ignoreCase = true) ||
                filePath.endsWith(".html", ignoreCase = true) ||
                filePath.endsWith(".md", ignoreCase = true) -> ASSET_TEXT
                
                filePath.endsWith(".xml", ignoreCase = true) && 
                filePath.contains("res/layout") -> RESOURCE_LAYOUT
                
                filePath.endsWith(".xml", ignoreCase = true) && 
                filePath.contains("res/values") -> RESOURCE_STRING
                
                filePath.endsWith(".properties", ignoreCase = true) ||
                filePath.endsWith(".config", ignoreCase = true) ||
                filePath.endsWith(".ini", ignoreCase = true) -> CONFIGURATION
                
                else -> ASSET_BINARY
            }
        }
    }
}