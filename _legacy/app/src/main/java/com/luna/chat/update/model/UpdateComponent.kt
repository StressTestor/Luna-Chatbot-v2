package com.luna.chat.update.model

import kotlinx.serialization.Serializable

/**
 * Represents a single component within an update package.
 * A component can be an asset file, a resource file, or other updateable content.
 */
@Serializable
data class UpdateComponent(
    /**
     * Unique identifier for this component
     */
    val id: String,
    
    /**
     * Type of this component
     */
    val type: UpdateComponentType,
    
    /**
     * Path where this component should be installed
     */
    val path: String,
    
    /**
     * Checksum of this component for integrity verification
     */
    val checksum: String,
    
    /**
     * Size of this component in bytes
     */
    val size: Long,
    
    /**
     * Whether this component is required for the update to be applied
     */
    val isRequired: Boolean = true,
    
    /**
     * Whether this component requires the app to be restarted after installation
     */
    val requiresRestart: Boolean = false,
    
    /**
     * Additional metadata for this component
     */
    val metadata: Map<String, String> = emptyMap()
) {
    /**
     * Check if this component is a child-safe content component that requires validation
     *
     * @return True if this component requires child safety validation
     */
    fun requiresChildSafetyValidation(): Boolean {
        return type == UpdateComponentType.ASSET_IMAGE || 
               type == UpdateComponentType.ASSET_TEXT ||
               type == UpdateComponentType.RESOURCE_STRING
    }
    
    /**
     * Get the installation priority of this component
     * Higher priority components are installed first
     *
     * @return Installation priority (higher number = higher priority)
     */
    fun getInstallationPriority(): Int {
        return when {
            isRequired -> 100
            type == UpdateComponentType.RESOURCE_STRING -> 80
            type == UpdateComponentType.ASSET_IMAGE -> 60
            type == UpdateComponentType.ASSET_TEXT -> 60
            else -> 50
        }
    }
}