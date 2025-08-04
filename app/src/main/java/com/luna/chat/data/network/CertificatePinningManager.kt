package com.luna.chat.data.network

import okhttp3.CertificatePinner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for certificate pinning to prevent man-in-the-middle attacks
 */
@Singleton
class CertificatePinningManager @Inject constructor() {

    /**
     * Creates a CertificatePinner for OkHttp client
     * The pins are SHA-256 hashes of the public key (SubjectPublicKeyInfo) of the certificate
     * 
     * @return CertificatePinner instance
     */
    fun createCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            // Pin the API server certificate
            // Replace "api.example.com" with your actual API domain
            // Replace the hash with the actual hash of your server's certificate
            .add("api.example.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            // Add backup pins for certificate rotation
            .add("api.example.com", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
            .build()
    }
    
    /**
     * Gets the SHA-256 hash of a certificate for pinning
     * This is a helper method to generate pins for your certificates
     * 
     * @param certificatePem The PEM-encoded certificate
     * @return SHA-256 hash of the certificate's public key
     */
    fun getCertificatePin(certificatePem: String): String {
        // Remove PEM headers and newlines
        val cleanedPem = certificatePem
            .replace("-----BEGIN CERTIFICATE-----", "")
            .replace("-----END CERTIFICATE-----", "")
            .replace("\n", "")
        
        // Decode base64
        val certificateBytes = android.util.Base64.decode(
            cleanedPem,
            android.util.Base64.DEFAULT
        )
        
        // Get the public key
        val certificate = java.security.cert.CertificateFactory
            .getInstance("X.509")
            .generateCertificate(certificateBytes.inputStream()) as java.security.cert.X509Certificate
        
        val publicKey = certificate.publicKey.encoded
        
        // Calculate SHA-256 hash
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(publicKey)
        
        // Convert to base64
        return android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
    }
}