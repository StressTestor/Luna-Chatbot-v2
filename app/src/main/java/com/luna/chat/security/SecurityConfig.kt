package com.luna.chat.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Security configuration for the application
 * Centralizes security settings and provides secure storage for configuration
 */
@Singleton
class SecurityConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Security configuration keys
        private const val SECURITY_PREFS_NAME = "luna_security_config"
        private const val KEY_CERTIFICATE_PINNING_ENABLED = "certificate_pinning_enabled"
        private const val KEY_INTEGRITY_CHECK_ENABLED = "integrity_check_enabled"
        private const val KEY_SECURE_LOGGING_ENABLED = "secure_logging_enabled"
        private const val KEY_CONTENT_FILTER_LEVEL = "content_filter_level"
        private const val KEY_PARENTAL_CONTROLS_ENABLED = "parental_controls_enabled"
        // New: integrity policy and limited mode gate
        private const val KEY_INTEGRITY_POLICY = "integrity_policy"
        private const val KEY_LIMITED_MODE_ENABLED = "limited_mode_enabled"
        // New: governance-controlled vision model approval toggle
        private const val KEY_VISION_MODEL_APPROVED = "vision_model_approved"
        
        // Default values
        private const val DEFAULT_CERTIFICATE_PINNING_ENABLED = true
        private const val DEFAULT_INTEGRITY_CHECK_ENABLED = true
        private const val DEFAULT_SECURE_LOGGING_ENABLED = true
        private const val DEFAULT_CONTENT_FILTER_LEVEL = "medium" // low, medium, high
        private const val DEFAULT_PARENTAL_CONTROLS_ENABLED = true
        // New: default policy is allow-with-warning for dev ergonomics
        private val DEFAULT_INTEGRITY_POLICY = IntegrityPolicy.ALLOW_WITH_WARNING
        private const val DEFAULT_LIMITED_MODE_ENABLED = false
        // New: governance default must remain disabled until explicitly approved
        private const val DEFAULT_VISION_MODEL_APPROVED = false
        
        // Child safety parameters
        const val MAX_TEMPERATURE = 0.7f
        const val MAX_TOP_P = 0.9f
    }
    
    // Encrypted shared preferences for secure storage of security configuration
    private val securityPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        EncryptedSharedPreferences.create(
            context,
            SECURITY_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Initialize security configuration with default values if not already set
     */
    fun initializeSecurityConfig() {
        // Only set default values if they don't already exist
        if (!securityPrefs.contains(KEY_CERTIFICATE_PINNING_ENABLED)) {
            securityPrefs.edit()
                .putBoolean(KEY_CERTIFICATE_PINNING_ENABLED, DEFAULT_CERTIFICATE_PINNING_ENABLED)
                .apply()
        }
        
        if (!securityPrefs.contains(KEY_INTEGRITY_CHECK_ENABLED)) {
            securityPrefs.edit()
                .putBoolean(KEY_INTEGRITY_CHECK_ENABLED, DEFAULT_INTEGRITY_CHECK_ENABLED)
                .apply()
        }
        
        if (!securityPrefs.contains(KEY_SECURE_LOGGING_ENABLED)) {
            securityPrefs.edit()
                .putBoolean(KEY_SECURE_LOGGING_ENABLED, DEFAULT_SECURE_LOGGING_ENABLED)
                .apply()
        }
        
        if (!securityPrefs.contains(KEY_CONTENT_FILTER_LEVEL)) {
            securityPrefs.edit()
                .putString(KEY_CONTENT_FILTER_LEVEL, DEFAULT_CONTENT_FILTER_LEVEL)
                .apply()
        }
        
        if (!securityPrefs.contains(KEY_PARENTAL_CONTROLS_ENABLED)) {
            securityPrefs.edit()
                .putBoolean(KEY_PARENTAL_CONTROLS_ENABLED, DEFAULT_PARENTAL_CONTROLS_ENABLED)
                .apply()
        }

        // New default initialization for integrity policy (string) and limited mode (boolean)
        if (!securityPrefs.contains(KEY_INTEGRITY_POLICY)) {
            securityPrefs.edit()
                .putString(KEY_INTEGRITY_POLICY, DEFAULT_INTEGRITY_POLICY.name)
                .apply()
        }
        if (!securityPrefs.contains(KEY_LIMITED_MODE_ENABLED)) {
            securityPrefs.edit()
                .putBoolean(KEY_LIMITED_MODE_ENABLED, DEFAULT_LIMITED_MODE_ENABLED)
                .apply()
        }
        // Governance-controlled toggle default: MUST remain false until explicitly approved
        if (!securityPrefs.contains(KEY_VISION_MODEL_APPROVED)) {
            securityPrefs.edit()
                .putBoolean(KEY_VISION_MODEL_APPROVED, DEFAULT_VISION_MODEL_APPROVED)
                .apply()
        }
    }
    
    /**
     * Check if certificate pinning is enabled
     * @return true if certificate pinning is enabled, false otherwise
     */
    fun isCertificatePinningEnabled(): Boolean {
        return securityPrefs.getBoolean(KEY_CERTIFICATE_PINNING_ENABLED, DEFAULT_CERTIFICATE_PINNING_ENABLED)
    }
    
    /**
     * Set certificate pinning enabled/disabled
     * @param enabled true to enable certificate pinning, false to disable
     */
    fun setCertificatePinningEnabled(enabled: Boolean) {
        securityPrefs.edit()
            .putBoolean(KEY_CERTIFICATE_PINNING_ENABLED, enabled)
            .apply()
    }
    
    /**
     * Check if integrity check is enabled
     * @return true if integrity check is enabled, false otherwise
     */
    fun isIntegrityCheckEnabled(): Boolean {
        return securityPrefs.getBoolean(KEY_INTEGRITY_CHECK_ENABLED, DEFAULT_INTEGRITY_CHECK_ENABLED)
    }
    
    /**
     * Set integrity check enabled/disabled
     * @param enabled true to enable integrity check, false to disable
     */
    fun setIntegrityCheckEnabled(enabled: Boolean) {
        securityPrefs.edit()
            .putBoolean(KEY_INTEGRITY_CHECK_ENABLED, enabled)
            .apply()
    }
    
    /**
     * Check if secure logging is enabled
     * @return true if secure logging is enabled, false otherwise
     */
    fun isSecureLoggingEnabled(): Boolean {
        return securityPrefs.getBoolean(KEY_SECURE_LOGGING_ENABLED, DEFAULT_SECURE_LOGGING_ENABLED)
    }
    
    /**
     * Set secure logging enabled/disabled
     * @param enabled true to enable secure logging, false to disable
     */
    fun setSecureLoggingEnabled(enabled: Boolean) {
        securityPrefs.edit()
            .putBoolean(KEY_SECURE_LOGGING_ENABLED, enabled)
            .apply()
    }
    
    /**
     * Get content filter level
     * @return content filter level (low, medium, high)
     */
    fun getContentFilterLevel(): String {
        return securityPrefs.getString(KEY_CONTENT_FILTER_LEVEL, DEFAULT_CONTENT_FILTER_LEVEL) ?: DEFAULT_CONTENT_FILTER_LEVEL
    }
    
    /**
     * Set content filter level
     * @param level content filter level (low, medium, high)
     */
    fun setContentFilterLevel(level: String) {
        if (level in listOf("low", "medium", "high")) {
            securityPrefs.edit()
                .putString(KEY_CONTENT_FILTER_LEVEL, level)
                .apply()
        }
    }
    
    /**
     * Check if parental controls are enabled
     * @return true if parental controls are enabled, false otherwise
     */
    fun areParentalControlsEnabled(): Boolean {
        return securityPrefs.getBoolean(KEY_PARENTAL_CONTROLS_ENABLED, DEFAULT_PARENTAL_CONTROLS_ENABLED)
    }
    
    /**
     * Set parental controls enabled/disabled
     * @param enabled true to enable parental controls, false to disable
     */
    fun setParentalControlsEnabled(enabled: Boolean) {
        securityPrefs.edit()
            .putBoolean(KEY_PARENTAL_CONTROLS_ENABLED, enabled)
            .apply()
    }

    // New: Integrity policy getters/setters
    fun getIntegrityPolicy(): IntegrityPolicy {
        val stored = securityPrefs.getString(KEY_INTEGRITY_POLICY, DEFAULT_INTEGRITY_POLICY.name)
        return runCatching { IntegrityPolicy.valueOf(stored ?: DEFAULT_INTEGRITY_POLICY.name) }
            .getOrDefault(DEFAULT_INTEGRITY_POLICY)
    }

    fun setIntegrityPolicy(policy: IntegrityPolicy) {
        securityPrefs.edit()
            .putString(KEY_INTEGRITY_POLICY, policy.name)
            .apply()
    }

    // New: Limited mode feature gate for policy LIMITED_MODE
    fun isLimitedModeEnabled(): Boolean {
        return securityPrefs.getBoolean(KEY_LIMITED_MODE_ENABLED, DEFAULT_LIMITED_MODE_ENABLED)
    }

    fun setLimitedModeEnabled(enabled: Boolean) {
        securityPrefs.edit()
            .putBoolean(KEY_LIMITED_MODE_ENABLED, enabled)
            .apply()
    }
    
    /**
     * Governance-controlled toggle for enabling vision model usage.
     * This is a governance-controlled toggle. Default must remain false until explicitly approved.
     */
    fun isVisionModelApproved(): Boolean {
        return securityPrefs.getBoolean(KEY_VISION_MODEL_APPROVED, DEFAULT_VISION_MODEL_APPROVED)
    }

    /**
     * Governance-controlled setter for vision model approval.
     * This is a governance-controlled toggle. Default must remain false until explicitly approved.
     */
    fun setVisionModelApproved(enabled: Boolean) {
        securityPrefs.edit()
            .putBoolean(KEY_VISION_MODEL_APPROVED, enabled)
            .apply()
    }
    
    /**
     * Get temperature parameter based on content filter level
     * @return temperature parameter (0.0 - MAX_TEMPERATURE)
     */
    fun getTemperatureParameter(): Float {
        return when (getContentFilterLevel()) {
            "low" -> MAX_TEMPERATURE
            "medium" -> MAX_TEMPERATURE * 0.8f
            "high" -> MAX_TEMPERATURE * 0.6f
            else -> MAX_TEMPERATURE * 0.8f // Default to medium
        }
    }
    
    /**
     * Get top_p parameter based on content filter level
     * @return top_p parameter (0.0 - MAX_TOP_P)
     */
    fun getTopPParameter(): Float {
        return when (getContentFilterLevel()) {
            "low" -> MAX_TOP_P
            "medium" -> MAX_TOP_P * 0.8f
            "high" -> MAX_TOP_P * 0.6f
            else -> MAX_TOP_P * 0.8f // Default to medium
        }
    }
}