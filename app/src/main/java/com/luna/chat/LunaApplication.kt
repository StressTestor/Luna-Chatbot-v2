package com.luna.chat

import android.app.Application
import android.os.Process
import android.widget.Toast
import com.luna.chat.data.repository.ApiKeyInitializer
import com.luna.chat.security.AppIntegrityChecker
import com.luna.chat.security.SecureLogger
import com.luna.chat.security.SecurityConfig
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LunaApplication : Application() {
    
    @Inject
    lateinit var apiKeyInitializer: ApiKeyInitializer
    
    @Inject
    lateinit var appIntegrityChecker: AppIntegrityChecker
    
    @Inject
    lateinit var securityConfig: SecurityConfig
    
    @Inject
    lateinit var secureLogger: SecureLogger
    
    // Application scope for background tasks
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize security configuration
        initializeSecurity()
        
        // Verify app integrity
        if (securityConfig.isIntegrityCheckEnabled() && !verifyAppIntegrity()) {
            // In a real app, you might want to take more drastic measures
            // For now, we'll just show a toast and continue
            Toast.makeText(
                this,
                "Security warning: App integrity check failed",
                Toast.LENGTH_LONG
            ).show()
            
            // Log the security issue
            secureLogger.error("App integrity check failed")
        }
        
        // Initialize API key in background
        applicationScope.launch {
            try {
                apiKeyInitializer.initializeApiKey()
            } catch (e: Exception) {
                // Log error using secure logger
                secureLogger.logException(e, "Failed to initialize API key")
            }
        }
    }
    
    /**
     * Initialize security components
     */
    private fun initializeSecurity() {
        try {
            // Initialize security configuration
            securityConfig.initializeSecurityConfig()
            
            secureLogger.info("Security configuration initialized")
        } catch (e: Exception) {
            // If secure logger isn't initialized yet, fall back to Android Log
            android.util.Log.e("LunaApp", "Failed to initialize security", e)
        }
    }
    
    /**
     * Verifies the integrity of the application
     * @return true if the app passes all integrity checks, false otherwise
     */
    private fun verifyAppIntegrity(): Boolean {
        return try {
            // In production, you might want to exit the app if integrity check fails
            appIntegrityChecker.verifyAppIntegrity()
        } catch (e: Exception) {
            // Log the exception in production
            // For now, we'll handle silently and assume the check failed
            false
        }
    }
}