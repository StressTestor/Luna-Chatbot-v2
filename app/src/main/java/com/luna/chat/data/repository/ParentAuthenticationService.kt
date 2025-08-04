package com.luna.chat.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling parent authentication for sensitive settings
 * Uses secure storage and proper password hashing
 */
@Singleton
class ParentAuthenticationService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val ENCRYPTED_PREFS_NAME = "luna_parent_auth"
        private const val KEY_PASSWORD_HASH = "parent_password_hash"
        private const val KEY_PASSWORD_SALT = "parent_password_salt"
        private const val KEY_SETUP_COMPLETED = "parent_setup_completed"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LAST_ATTEMPT_TIME = "last_attempt_time"
        private const val MASTER_KEY_ALIAS = "luna_parent_auth_key"
        
        // Security constants
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 300000L // 5 minutes
        private const val MIN_PASSWORD_LENGTH = 4
        private const val SALT_LENGTH = 32
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context, MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedSharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Set up parent password for the first time
     */
    suspend fun setupParentPassword(password: String): ParentAuthResult = withContext(Dispatchers.IO) {
        try {
            val validationResult = validatePasswordStrength(password)
            if (!validationResult.isValid) {
                return@withContext ParentAuthResult.WeakPassword(validationResult.message)
            }

            if (isParentPasswordSetup()) {
                return@withContext ParentAuthResult.AlreadySetup
            }

            val salt = generateSalt()
            val hashedPassword = hashPassword(password, salt)

            encryptedSharedPreferences.edit()
                .putString(KEY_PASSWORD_HASH, hashedPassword)
                .putString(KEY_PASSWORD_SALT, salt)
                .putBoolean(KEY_SETUP_COMPLETED, true)
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .apply()

            ParentAuthResult.Success("Parent password set up successfully! 👨‍👩‍👧‍👦")
        } catch (e: Exception) {
            ParentAuthResult.Error("Failed to set up parent password. Please try again! 🔧")
        }
    }

    /**
     * Authenticate parent with password
     */
    suspend fun authenticateParent(password: String): ParentAuthResult = withContext(Dispatchers.IO) {
        try {
            if (!isParentPasswordSetup()) {
                return@withContext ParentAuthResult.NotSetup
            }

            if (isLockedOut()) {
                val remainingTime = getRemainingLockoutTime()
                return@withContext ParentAuthResult.LockedOut(
                    "Too many failed attempts. Try again in ${remainingTime / 60000} minutes! ⏰"
                )
            }

            val storedHash = encryptedSharedPreferences.getString(KEY_PASSWORD_HASH, null)
            val storedSalt = encryptedSharedPreferences.getString(KEY_PASSWORD_SALT, null)

            if (storedHash == null || storedSalt == null) {
                return@withContext ParentAuthResult.Error("Authentication data corrupted. Please reset! 🔧")
            }

            val inputHash = hashPassword(password, storedSalt)
            
            if (inputHash == storedHash) {
                // Reset failed attempts on successful authentication
                encryptedSharedPreferences.edit()
                    .putInt(KEY_FAILED_ATTEMPTS, 0)
                    .apply()
                
                ParentAuthResult.Success("Authentication successful! ✅")
            } else {
                recordFailedAttempt()
                val failedAttempts = getFailedAttempts()
                val remainingAttempts = MAX_FAILED_ATTEMPTS - failedAttempts
                
                if (remainingAttempts <= 0) {
                    ParentAuthResult.LockedOut(
                        "Too many failed attempts. Account locked for 5 minutes! 🔒"
                    )
                } else {
                    ParentAuthResult.InvalidPassword(
                        "Incorrect password. $remainingAttempts attempts remaining! ❌"
                    )
                }
            }
        } catch (e: Exception) {
            ParentAuthResult.Error("Authentication failed. Please try again! 🔧")
        }
    }

    /**
     * Change parent password (requires current password)
     */
    suspend fun changeParentPassword(
        currentPassword: String,
        newPassword: String
    ): ParentAuthResult = withContext(Dispatchers.IO) {
        try {
            // First authenticate with current password
            val authResult = authenticateParent(currentPassword)
            if (authResult !is ParentAuthResult.Success) {
                return@withContext authResult
            }

            // Validate new password strength
            val validationResult = validatePasswordStrength(newPassword)
            if (!validationResult.isValid) {
                return@withContext ParentAuthResult.WeakPassword(validationResult.message)
            }

            // Set new password
            val salt = generateSalt()
            val hashedPassword = hashPassword(newPassword, salt)

            encryptedSharedPreferences.edit()
                .putString(KEY_PASSWORD_HASH, hashedPassword)
                .putString(KEY_PASSWORD_SALT, salt)
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .apply()

            ParentAuthResult.Success("Password changed successfully! 🔄")
        } catch (e: Exception) {
            ParentAuthResult.Error("Failed to change password. Please try again! 🔧")
        }
    }

    /**
     * Reset parent authentication (for emergency recovery)
     */
    suspend fun resetParentAuth(): ParentAuthResult = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.edit()
                .remove(KEY_PASSWORD_HASH)
                .remove(KEY_PASSWORD_SALT)
                .putBoolean(KEY_SETUP_COMPLETED, false)
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .apply()

            ParentAuthResult.Success("Parent authentication reset. Please set up a new password! 🔄")
        } catch (e: Exception) {
            ParentAuthResult.Error("Failed to reset authentication. Please contact support! 🆘")
        }
    }

    /**
     * Check if parent password is set up
     */
    fun isParentPasswordSetup(): Boolean {
        return encryptedSharedPreferences.getBoolean(KEY_SETUP_COMPLETED, false)
    }

    /**
     * Check if account is currently locked out
     */
    private fun isLockedOut(): Boolean {
        val failedAttempts = getFailedAttempts()
        val lastAttemptTime = encryptedSharedPreferences.getLong(KEY_LAST_ATTEMPT_TIME, 0)
        val currentTime = System.currentTimeMillis()
        
        return failedAttempts >= MAX_FAILED_ATTEMPTS && 
               (currentTime - lastAttemptTime) < LOCKOUT_DURATION_MS
    }

    /**
     * Get remaining lockout time in milliseconds
     */
    private fun getRemainingLockoutTime(): Long {
        val lastAttemptTime = encryptedSharedPreferences.getLong(KEY_LAST_ATTEMPT_TIME, 0)
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - lastAttemptTime
        
        return maxOf(0, LOCKOUT_DURATION_MS - elapsedTime)
    }

    /**
     * Record a failed authentication attempt
     */
    private fun recordFailedAttempt() {
        val currentAttempts = getFailedAttempts()
        encryptedSharedPreferences.edit()
            .putInt(KEY_FAILED_ATTEMPTS, currentAttempts + 1)
            .putLong(KEY_LAST_ATTEMPT_TIME, System.currentTimeMillis())
            .apply()
    }

    /**
     * Get number of failed attempts
     */
    private fun getFailedAttempts(): Int {
        return encryptedSharedPreferences.getInt(KEY_FAILED_ATTEMPTS, 0)
    }

    /**
     * Generate a random salt for password hashing
     */
    private fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    /**
     * Hash password with salt using SHA-256
     */
    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val saltedPassword = password + salt
        val hashBytes = digest.digest(saltedPassword.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Validate password strength
     */
    private fun validatePasswordStrength(password: String): PasswordValidationResult {
        return when {
            password.length < MIN_PASSWORD_LENGTH -> PasswordValidationResult(
                false, 
                "Password must be at least $MIN_PASSWORD_LENGTH characters long! 📏"
            )
            !password.any { it.isDigit() } -> PasswordValidationResult(
                false, 
                "Password must contain at least one number! 🔢"
            )
            password.isBlank() -> PasswordValidationResult(
                false, 
                "Password cannot be empty! ❌"
            )
            else -> PasswordValidationResult(true, "Password is strong! ✅")
        }
    }

    /**
     * Get authentication status for UI
     */
    fun getAuthStatus(): ParentAuthStatus {
        return ParentAuthStatus(
            isSetup = isParentPasswordSetup(),
            isLockedOut = isLockedOut(),
            failedAttempts = getFailedAttempts(),
            remainingLockoutTime = if (isLockedOut()) getRemainingLockoutTime() else 0L
        )
    }
}

/**
 * Result of parent authentication operations
 */
sealed class ParentAuthResult {
    data class Success(override val message: String) : ParentAuthResult()
    data class InvalidPassword(override val message: String) : ParentAuthResult()
    data class WeakPassword(override val message: String) : ParentAuthResult()
    data class LockedOut(override val message: String) : ParentAuthResult()
    data class Error(override val message: String) : ParentAuthResult()
    object NotSetup : ParentAuthResult()
    object AlreadySetup : ParentAuthResult()
    
    val isSuccess: Boolean
        get() = this is Success
        
    open val message: String
        get() = when (this) {
            is Success -> this.message
            is InvalidPassword -> this.message
            is WeakPassword -> this.message
            is LockedOut -> this.message
            is Error -> this.message
            is NotSetup -> "Parent password not set up. Please set up first! 🔧"
            is AlreadySetup -> "Parent password already set up! ✅"
        }
}

/**
 * Password validation result
 */
data class PasswordValidationResult(
    val isValid: Boolean,
    val message: String
)

/**
 * Parent authentication status
 */
data class ParentAuthStatus(
    val isSetup: Boolean,
    val isLockedOut: Boolean,
    val failedAttempts: Int,
    val remainingLockoutTime: Long
)