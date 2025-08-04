package com.luna.chat.update.manager

import com.luna.chat.update.model.UpdateError

/**
 * Interface for handling errors that occur during the update process.
 * This handler is responsible for determining how to respond to different types of errors.
 */
interface UpdateErrorHandler {
    /**
     * Handle an update error and determine the appropriate resolution
     * 
     * @param error The error that occurred
     * @return ErrorResolution indicating how to proceed
     */
    fun handleError(error: UpdateError): ErrorResolution
    
    /**
     * Log an error for developer debugging
     * 
     * @param error The error to log
     */
    fun logError(error: UpdateError)
}

/**
 * Enum representing the possible resolutions for update errors
 */
enum class ErrorResolution {
    /**
     * Abort the update process
     */
    ABORT,
    
    /**
     * Retry the operation that failed
     */
    RETRY,
    
    /**
     * Roll back to the previous version
     */
    ROLLBACK,
    
    /**
     * Notify the developer about the error
     */
    NOTIFY_DEVELOPER
}