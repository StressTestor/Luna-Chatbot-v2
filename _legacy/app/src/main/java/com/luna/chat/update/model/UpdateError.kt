package com.luna.chat.update.model

/**
 * Data class representing an error that occurred during the update process.
 * Used by the UpdateErrorHandler to determine how to respond to different types of errors.
 */
data class UpdateError(
    /**
     * Error code identifying the type of error
     */
    val code: Int,
    
    /**
     * Error message describing what went wrong
     */
    val message: String,
    
    /**
     * Whether the error is critical and requires immediate attention
     */
    val isCritical: Boolean = false,
    
    /**
     * The phase of the update process where the error occurred
     */
    val phase: UpdatePhase,
    
    /**
     * Additional details about the error
     */
    val details: Map<String, String> = emptyMap(),
    
    /**
     * The original exception that caused this error, if any
     */
    val cause: Throwable? = null
) {
    companion object {
        // Error codes
        const val ERROR_DOWNLOAD_FAILED = 1001
        const val ERROR_VERIFICATION_FAILED = 2001
        const val ERROR_COMPATIBILITY_CHECK_FAILED = 2002
        const val ERROR_SIGNATURE_INVALID = 2003
        const val ERROR_INSTALLATION_FAILED = 3001
        const val ERROR_INSUFFICIENT_STORAGE = 3002
        const val ERROR_PERMISSION_DENIED = 4001
        const val ERROR_UNKNOWN = 9999
        
        /**
         * Create a download error
         * 
         * @param message Error message
         * @param isCritical Whether the error is critical
         * @param details Additional details about the error
         * @param cause The original exception that caused this error
         * @return UpdateError for a download failure
         */
        fun downloadError(
            message: String,
            isCritical: Boolean = false,
            details: Map<String, String> = emptyMap(),
            cause: Throwable? = null
        ): UpdateError {
            return UpdateError(
                code = ERROR_DOWNLOAD_FAILED,
                message = message,
                isCritical = isCritical,
                phase = UpdatePhase.DOWNLOAD,
                details = details,
                cause = cause
            )
        }
        
        /**
         * Create a verification error
         * 
         * @param message Error message
         * @param isCritical Whether the error is critical
         * @param details Additional details about the error
         * @param cause The original exception that caused this error
         * @return UpdateError for a verification failure
         */
        fun verificationError(
            message: String,
            isCritical: Boolean = false,
            details: Map<String, String> = emptyMap(),
            cause: Throwable? = null
        ): UpdateError {
            return UpdateError(
                code = ERROR_VERIFICATION_FAILED,
                message = message,
                isCritical = isCritical,
                phase = UpdatePhase.VERIFICATION,
                details = details,
                cause = cause
            )
        }
        
        /**
         * Create an installation error
         * 
         * @param message Error message
         * @param isCritical Whether the error is critical
         * @param details Additional details about the error
         * @param cause The original exception that caused this error
         * @return UpdateError for an installation failure
         */
        fun installationError(
            message: String,
            isCritical: Boolean = false,
            details: Map<String, String> = emptyMap(),
            cause: Throwable? = null
        ): UpdateError {
            return UpdateError(
                code = ERROR_INSTALLATION_FAILED,
                message = message,
                isCritical = isCritical,
                phase = UpdatePhase.INSTALLATION,
                details = details,
                cause = cause
            )
        }
    }
}

/**
 * Enum representing the different phases of the update process
 */
enum class UpdatePhase {
    /**
     * Downloading the update package
     */
    DOWNLOAD,
    
    /**
     * Verifying the update package integrity and authenticity
     */
    VERIFICATION,
    
    /**
     * Installing the update components
     */
    INSTALLATION,
    
    /**
     * Finalizing the update process
     */
    FINALIZATION
}