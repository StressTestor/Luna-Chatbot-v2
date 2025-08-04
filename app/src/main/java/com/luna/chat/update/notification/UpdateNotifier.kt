package com.luna.chat.update.notification

import com.luna.chat.update.model.UpdatePackage
import com.luna.chat.update.model.UpdateStatus

/**
 * Interface for the Update Notifier component that handles notifying the developer about update status.
 * This notifier is responsible for displaying update notifications and progress information
 * to the developer during the update process.
 */
interface UpdateNotifier {
    /**
     * Show a notification about an available update
     * 
     * @param updatePackage The update package that is available
     */
    fun notifyUpdateAvailable(updatePackage: UpdatePackage)
    
    /**
     * Show a notification about the update status
     * 
     * @param status Current status of the update process
     */
    fun notifyUpdateStatus(status: UpdateStatus)
    
    /**
     * Show a notification about the update completion
     * 
     * @param success Whether the update was successful
     * @param message Message describing the result
     * @param requiresRestart Whether the app needs to be restarted
     */
    fun notifyUpdateComplete(success: Boolean, message: String, requiresRestart: Boolean)
    
    /**
     * Show a notification about an update error
     * 
     * @param code Error code
     * @param message Error message
     * @param isCritical Whether the error is critical
     */
    fun notifyUpdateError(code: Int, message: String, isCritical: Boolean)
    
    /**
     * Clear all update notifications
     */
    fun clearNotifications()
}