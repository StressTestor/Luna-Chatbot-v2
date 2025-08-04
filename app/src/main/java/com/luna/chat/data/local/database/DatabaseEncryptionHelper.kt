package com.luna.chat.data.local.database

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import net.sqlcipher.database.SupportFactory
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for database encryption using SQLCipher
 */
@Singleton
class DatabaseEncryptionHelper @Inject constructor(
    private val context: Context
) {
    /**
     * Creates a SupportFactory for Room database encryption
     * @return SupportFactory instance for Room database builder
     */
    fun createSupportFactory(): SupportFactory {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        // Generate a secure passphrase for SQLCipher
        val passphrase = getOrCreateDatabasePassphrase(masterKey)
        
        return SupportFactory(passphrase)
    }
    
    /**
     * Gets or creates a secure passphrase for database encryption
     * @param masterKey The master key to use for encryption
     * @return ByteArray containing the passphrase
     */
    private fun getOrCreateDatabasePassphrase(masterKey: MasterKey): ByteArray {
        val passphraseFile = File(context.filesDir, "db_passphrase.bin")
        
        // If passphrase file doesn't exist, create a new one
        if (!passphraseFile.exists()) {
            val newPassphrase = ByteArray(32) // 256-bit key
            java.security.SecureRandom().nextBytes(newPassphrase)
            
            // Store the passphrase in an encrypted file
            val encryptedFile = EncryptedFile.Builder(
                context,
                passphraseFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            
            encryptedFile.openFileOutput().use { outputStream ->
                outputStream.write(newPassphrase)
            }
            
            return newPassphrase
        }
        
        // Read existing passphrase
        val encryptedFile = EncryptedFile.Builder(
            context,
            passphraseFile,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        
        return encryptedFile.openFileInput().use { inputStream ->
            inputStream.readBytes()
        }
    }
}