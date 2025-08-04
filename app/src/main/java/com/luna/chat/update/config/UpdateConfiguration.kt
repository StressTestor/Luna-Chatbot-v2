package com.luna.chat.update.config

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Class for managing configuration of the update system.
 * This configuration is used to control the behavior of the update system
 * and can be modified by developers for testing purposes.
 */
class UpdateConfiguration private constructor(private val context: Context) {
    /**
     * Current configuration settings
     */
    private var config = UpdateSettings()
    
    /**
     * Get the current configuration settings
     * 
     * @return Current update settings
     */
    fun getSettings(): UpdateSettings {
        return config
    }
    
    /**
     * Update the configuration settings
     * 
     * @param settings New update settings
     */
    fun updateSettings(settings: UpdateSettings) {
        config = settings
        saveSettings()
    }
    
    /**
     * Reset the configuration to default settings
     */
    fun resetToDefaults() {
        config = UpdateSettings()
        saveSettings()
    }
    
    /**
     * Save the current settings to shared preferences
     */
    private fun saveSettings() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Json.encodeToString(UpdateSettings.serializer(), config)
        prefs.edit().putString(SETTINGS_KEY, json).apply()
    }
    
    /**
     * Load settings from shared preferences
     */
    private fun loadSettings() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(SETTINGS_KEY, null)
        if (json != null) {
            try {
                config = Json.decodeFromString(UpdateSettings.serializer(), json)
            } catch (e: Exception) {
                // If there's an error parsing the settings, use defaults
                config = UpdateSettings()
            }
        }
    }
    
    companion object {
        private const val PREFS_NAME = "update_config"
        private const val SETTINGS_KEY = "settings"
        
        @Volatile
        private var instance: UpdateConfiguration? = null
        
        /**
         * Get the singleton instance of the UpdateConfiguration
         * 
         * @param context Application context
         * @return UpdateConfiguration instance
         */
        fun getInstance(context: Context): UpdateConfiguration {
            return instance ?: synchronized(this) {
                instance ?: UpdateConfiguration(context.applicationContext).also {
                    it.loadSettings()
                    instance = it
                }
            }
        }
    }
}

/**
 * Data class representing the settings for the update system
 */
@Serializable
data class UpdateSettings(
    /**
     * Whether to automatically check for updates
     */
    val autoCheckForUpdates: Boolean = true,
    
    /**
     * Whether to automatically download updates when available
     */
    val autoDownloadUpdates: Boolean = false,
    
    /**
     * Whether to automatically install updates when downloaded
     */
    val autoInstallUpdates: Boolean = false,
    
    /**
     * Whether to allow updates over cellular data
     */
    val allowUpdatesOverCellular: Boolean = false,
    
    /**
     * Maximum size of update packages to download automatically (in bytes)
     */
    val maxAutoDownloadSize: Long = 10 * 1024 * 1024, // 10 MB
    
    /**
     * Directory to store update packages
     */
    val updateDirectory: String = "updates",
    
    /**
     * Whether to enable verbose logging
     */
    val verboseLogging: Boolean = false,
    
    /**
     * Whether to simulate slow downloads for testing
     */
    val simulateSlowDownloads: Boolean = false,
    
    /**
     * Whether to simulate download errors for testing
     */
    val simulateDownloadErrors: Boolean = false,
    
    /**
     * Whether to simulate verification errors for testing
     */
    val simulateVerificationErrors: Boolean = false,
    
    /**
     * Whether to simulate installation errors for testing
     */
    val simulateInstallationErrors: Boolean = false
)