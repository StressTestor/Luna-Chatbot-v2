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
    
    /**
     * Deprecated static accessor. Use Hilt DI to obtain UpdateManager:
     * - Inject UpdateManager into Android components annotated with @AndroidEntryPoint
     * - Or use Hilt EntryPointAccessors where direct injection isn't supported.
     */
    @Deprecated(
        message = "Use Hilt DI to obtain UpdateManager. Annotate your component with @AndroidEntryPoint and inject UpdateManager, or use EntryPointAccessors.",
        replaceWith = ReplaceWith("/* Obtain via Hilt DI */")
    )
    companion object {
        /**
         * Not supported. UpdateManager is provided by Hilt. Do not call this method.
         */
        fun getInstance(@Suppress("UNUSED_PARAMETER") context: Context): UpdateManager {
            throw UnsupportedOperationException(
                "UpdateManager.getInstance is not supported. Obtain UpdateManager via Hilt DI (@AndroidEntryPoint injection or EntryPointAccessors)."
            )
        }
    }
}