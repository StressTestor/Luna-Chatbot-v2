package com.luna.chat.update.logging

import com.luna.chat.update.model.UpdateError
import com.luna.chat.update.model.UpdateStatus

/**
 * Interface for the Update Logger component that handles logging of update events.
 * This logger is responsible for recording detailed information about the update process
 * for debugging and monitoring purposes.
 */
interface UpdateLogger {
    /**
     * Log an informational message about the update process
     * 
     * @param message The message to log
     * @param details Additional details about the event
     */
    fun logInfo(message: String, details: Map<String, String> = emptyMap())
    
    /**
     * Log a warning message about the update process
     * 
     * @param message The message to log
     * @param details Additional details about the event
     */
    fun logWarning(message: String, details: Map<String, String> = emptyMap())
    
    /**
     * Log an error that occurred during the update process
     * 
     * @param error The error to log
     */
    fun logError(error: UpdateError)
    
    /**
     * Log a status change in the update process
     * 
     * @param status The new status
     */
    fun logStatusChange(status: UpdateStatus)
    
    /**
     * Get the log entries for the current update process
     * 
     * @return List of log entries
     */
    fun getUpdateLogs(): List<UpdateLogEntry>
    
    /**
     * Clear all log entries
     */
    fun clearLogs()
}

/**
 * Data class representing a log entry in the update process
 */
data class UpdateLogEntry(
    /**
     * Timestamp of the log entry
     */
    val timestamp: Long,
    
    /**
     * Log level (INFO, WARNING, ERROR)
     */
    val level: LogLevel,
    
    /**
     * Log message
     */
    val message: String,
    
    /**
     * Additional details about the log entry
     */
    val details: Map<String, String> = emptyMap()
)

/**
 * Enum representing the possible log levels
 */
enum class LogLevel {
    INFO,
    WARNING,
    ERROR
}