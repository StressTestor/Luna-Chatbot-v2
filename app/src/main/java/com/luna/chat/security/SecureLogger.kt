package com.luna.chat.security

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure logging utility that prevents sensitive information from being logged
 */
@Singleton
class SecureLogger @Inject constructor() {

    companion object {
        private const val TAG = "LunaSecure"
        
        // List of patterns that should be redacted from logs
        private val SENSITIVE_PATTERNS = listOf(
            Regex("api[_-]?key[^\\w].*?([\\w-]{10,})"),
            Regex("password[^\\w].*?([\\w-]{6,})"),
            Regex("token[^\\w].*?([\\w-]{10,})"),
            Regex("secret[^\\w].*?([\\w-]{10,})"),
            Regex("auth[^\\w].*?([\\w-]{10,})"),
            Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b"), // Email pattern
            Regex("\\b\\d{3}-\\d{2}-\\d{4}\\b"), // SSN pattern
            Regex("\\b\\d{16}\\b"), // Credit card pattern (simplified)
            Regex("\\b\\d{10,15}\\b") // Phone number pattern (simplified)
        )
    }
    
    /**
     * Log a debug message
     * @param message The message to log
     * @param tag Optional tag (defaults to LunaSecure)
     */
    fun debug(message: String, tag: String = TAG) {
        Log.d(tag, sanitizeMessage(message))
    }
    
    /**
     * Log an info message
     * @param message The message to log
     * @param tag Optional tag (defaults to LunaSecure)
     */
    fun info(message: String, tag: String = TAG) {
        Log.i(tag, sanitizeMessage(message))
    }
    
    /**
     * Log a warning message
     * @param message The message to log
     * @param tag Optional tag (defaults to LunaSecure)
     */
    fun warn(message: String, tag: String = TAG) {
        Log.w(tag, sanitizeMessage(message))
    }
    
    /**
     * Log an error message
     * @param message The message to log
     * @param throwable Optional throwable
     * @param tag Optional tag (defaults to LunaSecure)
     */
    fun error(message: String, throwable: Throwable? = null, tag: String = TAG) {
        val sanitizedMessage = sanitizeMessage(message)
        if (throwable != null) {
            Log.e(tag, sanitizedMessage, throwable)
        } else {
            Log.e(tag, sanitizedMessage)
        }
    }
    
    /**
     * Sanitizes a message by redacting sensitive information
     * @param message The message to sanitize
     * @return The sanitized message
     */
    fun sanitizeMessage(message: String): String {
        var sanitizedMessage = message
        
        // Replace sensitive patterns with redacted text
        for (pattern in SENSITIVE_PATTERNS) {
            sanitizedMessage = sanitizedMessage.replace(pattern) { matchResult ->
                val fullMatch = matchResult.value
                val sensitivePartIndex = fullMatch.indexOfFirst { it.isLetterOrDigit() }
                
                if (sensitivePartIndex >= 0) {
                    fullMatch.substring(0, sensitivePartIndex) + "REDACTED"
                } else {
                    "REDACTED"
                }
            }
        }
        
        return sanitizedMessage
    }
    
    /**
     * Logs an exception securely
     * @param throwable The throwable to log
     * @param message Optional message to include
     * @param tag Optional tag (defaults to LunaSecure)
     */
    fun logException(throwable: Throwable, message: String? = null, tag: String = TAG) {
        val logMessage = message?.let { sanitizeMessage(it) } ?: "Exception occurred"
        Log.e(tag, logMessage, throwable)
    }
}