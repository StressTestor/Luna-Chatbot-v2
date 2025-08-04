package com.luna.chat.update.model

/**
 * Data class representing the result of a verification operation.
 * Used by the UpdateVerifier to report the outcome of verification checks.
 */
data class VerificationResult(
    /**
     * Whether the verification was successful
     */
    val isSuccess: Boolean,
    
    /**
     * Error code if verification failed, 0 if successful
     */
    val errorCode: Int = 0,
    
    /**
     * Message describing the verification result
     */
    val message: String = "",
    
    /**
     * Additional details about the verification result
     */
    val details: Map<String, String> = emptyMap()
) {
    companion object {
        /**
         * Create a successful verification result
         * 
         * @return VerificationResult indicating success
         */
        fun success(): VerificationResult {
            return VerificationResult(
                isSuccess = true,
                message = "Verification successful"
            )
        }
        
        /**
         * Create a failed verification result
         * 
         * @param errorCode Error code
         * @param message Error message
         * @param details Additional details about the error
         * @return VerificationResult indicating failure
         */
        fun failure(errorCode: Int, message: String, details: Map<String, String> = emptyMap()): VerificationResult {
            return VerificationResult(
                isSuccess = false,
                errorCode = errorCode,
                message = message,
                details = details
            )
        }
    }
}