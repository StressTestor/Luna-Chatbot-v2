package com.luna.chat.security

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class to check the integrity of the application at runtime
 * Detects if the app has been tampered with or is running in an insecure environment
 */
@Singleton
class AppIntegrityChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        /**
         * SHA-256 hash of the app's signing certificate.
         *
         * TODO(team): Set this per build variant/keystore.
         * - For each signing config (debug, release, internal, etc.), compute the SHA-256 of the
         *   signing certificate and set the expected value via build-time constants or variant-
         *   specific sources.
         *
         * How to compute:
         * 1) Using keytool (JDK):
         *    keytool -list -v -keystore path/to/keystore.jks -alias your_alias -storepass your_storepass -keypass your_keypass
         *    Then take the "SHA256" of the certificate (hex, lowercase) without colons.
         *
         * 2) From a signed APK/AAB:
         *    Use apksigner or aapt to extract the signing cert and compute SHA-256.
         *
         * 3) Programmatically in a debug build:
         *    Run getAppSignatures() on the installed build and log getSignatureHash(signature).
         *
         * Recommended:
         * - Inject via BuildConfig or flavors to avoid hardcoding different values here.
         * - Keep current behavior if unset (empty string): signature check will fail, allowing
         *   policy to decide response (warning/limited/block).
         */
        private const val EXPECTED_SIGNATURE_HASH: String = ""
        
        // Package name of the app
        private const val EXPECTED_PACKAGE_NAME = "com.luna.chat"
    }
    
    /**
     * Performs all integrity checks
     * @return true if all checks pass, false otherwise
     */
    fun verifyAppIntegrity(): Boolean {
        return verifySignature() && 
               verifyPackageName() && 
               verifyInstallerStore() &&
               !isRunningOnEmulator() &&
               !isDeviceRooted()
    }
    
    /**
     * Verifies the app's signature matches the expected signature
     * @return true if the signature matches, false otherwise
     */
    fun verifySignature(): Boolean {
        try {
            val signatures = getAppSignatures()
            if (signatures.isEmpty()) {
                return false
            }
            
            // Check if any signature matches the expected hash.
            // If EXPECTED_SIGNATURE_HASH is empty, treat as unset and force failure so policy handles it.
            if (EXPECTED_SIGNATURE_HASH.isBlank()) return false
            return signatures.any { signature ->
                val signatureHash = getSignatureHash(signature)
                signatureHash == EXPECTED_SIGNATURE_HASH
            }
        } catch (e: Exception) {
            // If there's an exception, assume the signature check failed
            return false
        }
    }
    
    /**
     * Gets the app's signatures
     * @return List of signatures
     */
    private fun getAppSignatures(): List<Signature> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                packageInfo.signingInfo.apkContentsSigners.toList()
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                packageInfo.signatures.toList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Calculates the SHA-256 hash of a signature
     * @param signature The signature to hash
     * @return The hash as a hex string
     */
    private fun getSignatureHash(signature: Signature): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(signature.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Verifies the app's package name matches the expected package name
     * @return true if the package name matches, false otherwise
     */
    fun verifyPackageName(): Boolean {
        return context.packageName == EXPECTED_PACKAGE_NAME
    }
    
    /**
     * Verifies the app was installed from an official store
     * @return true if the app was installed from an official store, false otherwise
     */
    fun verifyInstallerStore(): Boolean {
        val validInstallers = listOf(
            "com.android.vending",  // Google Play Store
            "com.amazon.venezia",   // Amazon App Store
            "com.sec.android.app.samsungapps"  // Samsung Galaxy Store
        )
        
        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName)
        }
        
        // If the installer is null, the app was likely sideloaded
        // In development, this is expected, so we can return true for debug builds
        return if (installer == null) {
            // Allow null installer in debug builds
            context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
        } else {
            validInstallers.contains(installer)
        }
    }
    
    /**
     * Checks if the app is running on an emulator
     * @return true if running on an emulator, false otherwise
     */
    fun isRunningOnEmulator(): Boolean {
        // Check various properties that indicate an emulator
        return (Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk" == Build.PRODUCT)
    }
    
    /**
     * Checks if the device is rooted
     * @return true if the device is rooted, false otherwise
     */
    fun isDeviceRooted(): Boolean {
        // Check for common root management apps
        val rootApps = arrayOf(
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
            "eu.chainfire.supersu",
            "com.topjohnwu.magisk"
        )
        
        // Check for su binary
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk"
        )
        
        // Check if any root app is installed
        try {
            for (app in rootApps) {
                try {
                    context.packageManager.getPackageInfo(app, 0)
                    return true
                } catch (e: PackageManager.NameNotFoundException) {
                    // Package not found, continue checking
                }
            }
        } catch (e: Exception) {
            // Ignore exceptions
        }
        
        // Check if su binary exists
        for (path in paths) {
            if (java.io.File(path).exists()) {
                return true
            }
        }
        
        return false
    }
}