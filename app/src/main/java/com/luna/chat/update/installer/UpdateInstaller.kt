package com.luna.chat.update.installer

import com.luna.chat.update.model.UpdateComponent
import com.luna.chat.update.model.InstallationResult

/**
 * Interface for the Update Installer component that applies updates to the application.
 * This installer is responsible for applying different types of updates (assets, resources, etc.)
 * to the application while ensuring data integrity and proper error handling.
 */
interface UpdateInstaller {
    /**
     * Install an update component to the application
     * 
     * @param component The update component to install
     * @param data The binary data of the component
     * @return InstallationResult indicating success or failure with details
     */
    suspend fun installComponent(component: UpdateComponent, data: ByteArray): InstallationResult
    
    /**
     * Install an asset file update
     * 
     * @param assetPath The path to the asset file within the app
     * @param data The binary data of the asset
     * @return InstallationResult indicating success or failure with details
     */
    suspend fun installAssetUpdate(assetPath: String, data: ByteArray): InstallationResult
    
    /**
     * Install a resource file update
     * 
     * @param resourcePath The path to the resource file within the app
     * @param data The binary data of the resource
     * @return InstallationResult indicating success or failure with details
     */
    suspend fun installResourceUpdate(resourcePath: String, data: ByteArray): InstallationResult
    
    /**
     * Check if an update requires the application to be restarted
     * 
     * @param component The update component to check
     * @return true if the update requires a restart, false otherwise
     */
    fun requiresRestart(component: UpdateComponent): Boolean
    
    /**
     * Clean up any temporary files created during the update process
     * 
     * @return true if cleanup was successful, false otherwise
     */
    suspend fun cleanupTemporaryFiles(): Boolean
}