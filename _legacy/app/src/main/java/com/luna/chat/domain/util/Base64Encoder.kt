package com.luna.chat.domain.util

/**
 * In-memory encoder to Base64 data URL for images.
 *
 * In-memory constraints:
 * - Accepts raw bytes and MIME type; returns data:<mime>;base64,... string without touching disk.
 */
object Base64Encoder {
    fun toDataUrl(bytes: ByteArray, mimeType: String): String {
        val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        return "data:$mimeType;base64,$base64"
    }
}