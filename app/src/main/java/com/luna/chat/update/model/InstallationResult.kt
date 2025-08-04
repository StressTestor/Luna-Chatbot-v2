package com.luna.chat.update.model

/**
 * Data class representing the result of an installation operation.
 * Used by the UpdateInstaller to report the outcome of installation operations.
 */
data class InstallationResult(
    /**
     * Whether the installation was successful
     */
    val isSuccess: Boolean,
    
    /**
     * Error code if installation failed, 0 if successful
     */
    val errorCode: Int = 0,
    
    /**
     * Message describing the installation result
     */
    val message: String = "",
    
    /**
     * Whether the installation requires the app to be restarted
     */
    val requiresRestart: Boolean = false,
    
    /**
     * Additional details about the installation result
     */
    val details: Map<String, String> = emptyMap()
) {
    companion object {
        /**
         * Create a successful installation result
         * 
         * @param requiresRestart Whether the installation requires the app to be restarted
         * @return InstallationResult indicating success
         */
        fun success(requiresRestart: Boolean = false): InstallationResult {
            return InstallationResult(
                isSuccess = true,
                message = "Installation successful",
                requiresRestart = requiresRestart
            )
        }
        
        /**
         * Create a failed installation result
         * 
         * @param errorCode Error code
         * @param message Error message
         * @param details Additional details about the error
         * @return InstallationResult indicating failure
         */
        fun failure(errorCode: Int, message: String, details: Map<String, String> = emptyMap()): InstallationResult {
            return InstallationResult(
                isSuccess = false,
                errorCode = errorCode,
                message = message,
                details = details
            )
        }
    }
}