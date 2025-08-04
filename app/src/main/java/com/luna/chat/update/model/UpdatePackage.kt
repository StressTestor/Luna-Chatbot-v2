package com.luna.chat.update.model

import kotlinx.serialization.Serializable
import java.io.File

/**
 * Represents a complete update package that can be downloaded and installed.
 * An update package contains multiple components and a manifest describing the update.
 */
@Serializable
data class UpdatePackage(
    /**
     * Unique identifier for this update package
     */
    val id: String,
    
    /**
     * Version code of this update package
     */
    val versionCode: Int,
    
    /**
     * Version name of this update package (human-readable)
     */
    val versionName: String,
    
    /**
     * Minimum app version required to install this update
     */
    val minAppVersion: Int,
    
    /**
     * Maximum app version this update can be applied to
     */
    val maxAppVersion: Int,
    
    /**
     * Release notes for this update
     */
    val releaseNotes: String,
    
    /**
     * Whether this update is critical and should be installed immediately
     */
    val isCritical: Boolean = false,
    
    /**
     * Whether this update requires a restart of the app
     */
    val requiresRestart: Boolean = false,
    
    /**
     * The manifest describing the components in this update
     */
    val manifest: UpdateManifest,
    
    /**
     * Timestamp when this update was created (milliseconds since epoch)
     */
    val createdAt: Long,
    
    /**
     * Digital signature for this update package
     */
    val signature: String
) {
    /**
     * Local file containing the update package data
     * This is not serialized as part of the package
     */
    @kotlinx.serialization.Transient
    var packageFile: File? = null
    
    /**
     * Check if this update is compatible with the current app version
     *
     * @param currentAppVersion The current app version code
     * @return True if the update is compatible, false otherwise
     */
    fun isCompatibleWith(currentAppVersion: Int): Boolean {
        return currentAppVersion in minAppVersion..maxAppVersion
    }
    
    /**
     * Get the total size of all components in this update
     *
     * @return Total size in bytes
     */
    fun getTotalSize(): Long {
        return manifest.components.sumOf { it.size }
    }
}