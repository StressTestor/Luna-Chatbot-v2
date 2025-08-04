package com.luna.chat.update.model

/**
 * Sealed class representing the various states of the update process.
 * This provides a type-safe way to handle different update states in the UI and business logic.
 */
sealed class UpdateStatus {
    /**
     * Initial state when no update is in progress
     */
    object Idle : UpdateStatus()
    
    /**
     * State when an update is being downloaded
     * 
     * @param progress Download progress from 0.0 to 1.0
     */
    data class Downloading(val progress: Float) : UpdateStatus()
    
    /**
     * State when an update package is being verified
     * 
     * @param progress Verification progress from 0.0 to 1.0
     */
    data class Verifying(val progress: Float) : UpdateStatus()
    
    /**
     * State when an update is being installed
     * 
     * @param progress Installation progress from 0.0 to 1.0
     * @param currentComponent The component currently being installed
     * @param totalComponents Total number of components to install
     */
    data class Installing(
        val progress: Float,
        val currentComponent: Int = 0,
        val totalComponents: Int = 0
    ) : UpdateStatus()
    
    /**
     * State when an update has been completed
     * 
     * @param success Whether the update was successful
     * @param message Message describing the result
     * @param requiresRestart Whether the app needs to be restarted
     */
    data class Complete(
        val success: Boolean,
        val message: String,
        val requiresRestart: Boolean = false
    ) : UpdateStatus()
    
    /**
     * State when an error occurred during the update process
     * 
     * @param code Error code
     * @param message Error message
     * @param isCritical Whether the error is critical
     */
    data class Error(
        val code: Int,
        val message: String,
        val isCritical: Boolean = false
    ) : UpdateStatus()
}