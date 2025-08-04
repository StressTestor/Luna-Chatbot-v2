package com.luna.chat.update.model

import kotlinx.serialization.Serializable

/**
 * Represents the manifest of an update package, containing metadata and a list of components.
 * The manifest is used to verify the integrity of the update package and to guide the installation process.
 */
@Serializable
data class UpdateManifest(
    /**
     * List of components included in this update
     */
    val components: List<UpdateComponent>,
    
    /**
     * Format version of this manifest
     */
    val formatVersion: Int = 1,
    
    /**
     * Checksum of the entire update package for integrity verification
     */
    val packageChecksum: String,
    
    /**
     * Additional metadata for this update
     */
    val metadata: Map<String, String> = emptyMap()
) {
    /**
     * Check if the manifest contains components of a specific type
     *
     * @param type The component type to check for
     * @return True if the manifest contains components of the specified type
     */
    fun hasComponentType(type: UpdateComponentType): Boolean {
        return components.any { it.type == type }
    }
    
    /**
     * Get all components of a specific type
     *
     * @param type The component type to filter by
     * @return List of components of the specified type
     */
    fun getComponentsByType(type: UpdateComponentType): List<UpdateComponent> {
        return components.filter { it.type == type }
    }
    
    /**
     * Validate that all required components are present and have valid checksums
     *
     * @return True if the manifest is valid
     */
    fun isValid(): Boolean {
        // Basic validation - ensure we have at least one component
        if (components.isEmpty()) {
            return false
        }
        
        // Check that all components have non-empty paths and checksums
        return components.all { 
            it.path.isNotEmpty() && it.checksum.isNotEmpty() && it.size > 0 
        }
    }
}