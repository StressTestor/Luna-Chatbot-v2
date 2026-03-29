package com.luna.chat

import android.app.Application
import android.os.Process
import android.widget.Toast
import com.luna.chat.data.repository.ApiKeyInitializer
import com.luna.chat.security.AppIntegrityChecker
import com.luna.chat.security.SecureLogger
import com.luna.chat.security.SecurityConfig
import com.luna.chat.security.IntegrityPolicy
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
            // Determine enforcement behavior from SecurityConfig
            when (securityConfig.getIntegrityPolicy()) {
                IntegrityPolicy.ALLOW_WITH_WARNING -> {
                    // Maintain current behavior: Toast + secureLogger.error
                    Toast.makeText(
                        this,
                        "Security warning: App integrity check failed",
                        Toast.LENGTH_LONG
                    ).show()
                    secureLogger.error("App integrity check failed (policy=ALLOW_WITH_WARNING)")
                }
                IntegrityPolicy.LIMITED_MODE -> {
                    // Enable a global restricted mode flag.
                    // This can be read by feature gates to disable sensitive capabilities.
                    // TODO: Define and enforce which features are disabled in limited mode
                    // e.g., disable local model downloads, restrict network calls, block in-app updates, etc.
                    securityConfig.setLimitedModeEnabled(true)

                    // Optionally also warn the user non-blockingly
                    Toast.makeText(
                        this,
                        "Limited functionality: integrity check failed",
                        Toast.LENGTH_LONG
                    ).show()
                    secureLogger.error("App integrity check failed (policy=LIMITED_MODE) — limited mode enabled")
                }
                IntegrityPolicy.BLOCK_STARTUP -> {
                    // Log securely and terminate the process gracefully.
                    // We avoid throwing exceptions; instead, we kill the process after a short, non-blocking notification.
                    // Future improvement: show a dedicated blocking screen/activity to inform the user and provide support steps.
                    Toast.makeText(
                        this,
                        "App blocked due to failed integrity check",
                        Toast.LENGTH_LONG
                    ).show()
                    secureLogger.error("App integrity check failed (policy=BLOCK_STARTUP) — terminating process")
                    
                    // Terminate gracefully
                    Process.killProcess(Process.myPid())
                    // No return; process is terminated. Avoid throwing exceptions.
                }
            }
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
            // Respect existing AppIntegrityChecker behavior, which allows null installer on debug builds.
            appIntegrityChecker.verifyAppIntegrity()
        } catch (e: Exception) {
            // Handle silently and assume the check failed
            false
        }
    }
}