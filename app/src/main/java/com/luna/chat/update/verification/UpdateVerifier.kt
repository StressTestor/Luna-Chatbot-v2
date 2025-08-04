package com.luna.chat.update.verification

import com.luna.chat.update.model.UpdateComponent
import com.luna.chat.update.model.UpdatePackage
import com.luna.chat.update.model.VerificationResult

/**
 * Interface for the Update Verifier component that validates update packages.
 * This verifier is responsible for checking the integrity and authenticity of update packages
 * before they are applied to the application.
 */
interface UpdateVerifier {
    /**
     * Verify the integrity of an update package file
     * 
     * @param updatePath Path to the update package file
     * @return VerificationResult indicating success or failure with details
     */
    suspend fun verifyUpdatePackage(updatePath: String): VerificationResult
    
    /**
     * Verify the integrity of an individual update component
     * 
     * @param component The update component to verify
     * @param data The binary data of the component
     * @return VerificationResult indicating success or failure with details
     */
    suspend fun verifyComponent(component: UpdateComponent, data: ByteArray): VerificationResult
    
    /**
     * Verify the compatibility of an update package with the current application version
     * 
     * @param updatePackage The update package to verify
     * @return VerificationResult indicating success or failure with details
     */
    fun verifyCompatibility(updatePackage: UpdatePackage): VerificationResult
    
    /**
     * Verify the signature of an update package to ensure it comes from a trusted source
     * 
     * @param updatePackage The update package to verify
     * @param signature The signature data
     * @return VerificationResult indicating success or failure with details
     */
    suspend fun verifySignature(updatePackage: UpdatePackage, signature: ByteArray): VerificationResult
}