package com.luna.chat.update.manager

import android.content.Context
import com.luna.chat.update.model.UpdatePackage
import com.luna.chat.update.model.UpdateStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for the Update Manager component that coordinates the update process.
 * This manager is responsible for processing update packages, verifying their integrity,
 * and applying updates to the application.
 */
interface UpdateManager {
    /**
     * Current status of the update process as a StateFlow
     */
    val updateStatus: StateFlow<UpdateStatus>
    
    /**
     * Process an update from the given file path
     * 
     * @param updatePath Path to the update package file
     * @return true if the update process was started successfully, false otherwise
     */
    suspend fun processUpdate(updatePath: String): Boolean
    
    /**
     * Check if an update is currently in progress
     * 
     * @return true if an update is in progress, false otherwise
     */
    fun isUpdateInProgress(): Boolean
    
    /**
     * Cancel the current update process if possible
     * 
     * @return true if the update was canceled successfully, false otherwise
     */
    suspend fun cancelUpdate(): Boolean
    
    /**
     * Get information about the last processed update
     * 
     * @return UpdatePackage object containing information about the last update, or null if no update has been processed
     */
    fun getLastUpdate(): UpdatePackage?
    
    companion object {
        /**
         * Get the singleton instance of the UpdateManager
         * 
         * @param context Application context
         * @return UpdateManager instance
         */
        fun getInstance(context: Context): UpdateManager {
            // Implementation will be provided in the concrete class
            throw NotImplementedError("Implementation not provided in interface")
        }
    }
}