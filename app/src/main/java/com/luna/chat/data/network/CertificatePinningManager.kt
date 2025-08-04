package com.luna.chat.data.network

import okhttp3.CertificatePinner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for certificate pinning to prevent man-in-the-middle attacks
 *
 * Host alignment: Retrofit BASE_URL is set to https://api.openrouter.ai/v1/,
 * so we pin the same host (api.openrouter.ai) to ensure pinning is active.
 */
@Singleton
class CertificatePinningManager @Inject constructor() {

    /**
     * Creates a CertificatePinner for OkHttp client.
     * Pins are SHA-256 hashes of the Subject Public Key Info (SPKI) of the certificate's public key.
     *
     * Rotation notes:
     * - Always maintain at least one backup pin to allow seamless rotation.
     * - Update pins promptly if OpenRouter rotates keys/certs to avoid client lockout.
     */
    fun createCertificatePinner(): CertificatePinner {
        // Certificate pinning for OpenRouter (api.openrouter.ai)
        // Extraction command (documented):
        //   openssl s_client -connect api.openrouter.ai:443 -servername api.openrouter.ai < /dev/null |
        //   openssl x509 -pubkey -noout | openssl pkey -pubin -outform DER |
        //   openssl dgst -sha256 -binary | openssl base64
        //
        // Known SPKI pins:
        // Primary (as provided): faEjnRb/+sG+MnJzfe4TiH581Qe9nEZlNoQdNAdonrY=
        //
        // TODO (backup SPKI pin): Populate a verified secondary pin for api.openrouter.ai before release.
        // Actionable guidance:
        // 1) Run the above openssl extraction against the full cert chain and identify at least one alternate valid SPKI.
        // 2) Validate the resulting base64 against a live connection AND an independent source.
        // 3) Add as: .add("api.openrouter.ai", "sha256/<BACKUP_PIN_BASE64>")
        // 4) Rebuild and perform a live smoke test to ensure both pins work (primary/backup).
        // 5) Document rotation date and source of verification in release notes.
        //
        // Placeholder anchor for secondary pin (no behavior change):
        // .add("api.openrouter.ai", "sha256/<SECONDARY_SPki_PIN_BASE64>") // TODO: Insert verified backup SPKI after governance approval
        return CertificatePinner.Builder()
            .add("api.openrouter.ai", "sha256/faEjnRb/+sG+MnJzfe4TiH581Qe9nEZlNoQdNAdonrY=")
            // .add("api.openrouter.ai", "sha256/<BACKUP_PIN_BASE64>") // TODO: add verified backup SPKI pin prior to enabling production vision
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