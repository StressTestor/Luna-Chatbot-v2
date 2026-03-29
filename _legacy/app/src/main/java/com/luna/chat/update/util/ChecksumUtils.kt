package com.luna.chat.update.util

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Utility class for generating and verifying checksums.
 * Used to ensure the integrity of update packages and components.
 */
object ChecksumUtils {
    /**
     * Generate SHA-256 checksum for a file
     *
     * @param file The file to generate checksum for
     * @return Hex string representation of the checksum
     * @throws IOException If the file cannot be read
     * @throws NoSuchAlgorithmException If SHA-256 algorithm is not available
     */
    @Throws(IOException::class, NoSuchAlgorithmException::class)
    fun generateSHA256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return bytesToHex(digest.digest())
    }
    
    /**
     * Generate SHA-256 checksum for a byte array
     *
     * @param data The data to generate checksum for
     * @return Hex string representation of the checksum
     * @throws NoSuchAlgorithmException If SHA-256 algorithm is not available
     */
    @Throws(NoSuchAlgorithmException::class)
    fun generateSHA256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(data)
        return bytesToHex(digest.digest())
    }
    
    /**
     * Verify that a file matches the expected checksum
     *
     * @param file The file to verify
     * @param expectedChecksum The expected checksum
     * @return True if the file's checksum matches the expected checksum
     */
    fun verifyChecksum(file: File, expectedChecksum: String): Boolean {
        return try {
            val actualChecksum = generateSHA256(file)
            actualChecksum.equals(expectedChecksum, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Verify that a byte array matches the expected checksum
     *
     * @param data The data to verify
     * @param expectedChecksum The expected checksum
     * @return True if the data's checksum matches the expected checksum
     */
    fun verifyChecksum(data: ByteArray, expectedChecksum: String): Boolean {
        return try {
            val actualChecksum = generateSHA256(data)
            actualChecksum.equals(expectedChecksum, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Convert a byte array to a hex string
     *
     * @param bytes The byte array to convert
     * @return Hex string representation of the byte array
     */
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789abcdef"[v ushr 4]
            hexChars[i * 2 + 1] = "0123456789abcdef"[v and 0x0F]
        }
        return String(hexChars)
    }
}