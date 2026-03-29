package com.luna.chat.domain.util

/**
 * Minimal in-memory image validator.
 *
 * Security posture:
 * - Conservative MIME and size constraints; callers should enforce stricter policies as needed.
 * - No bitmap decoding in Phase 1 to avoid OOM or EXIF/decoder issues; dimension checks deferred.
 */
object ImageValidator {
    private val allowedMime = setOf("image/jpeg", "image/png", "image/webp")
    private const val MAX_BYTES = 4 * 1024 * 1024 // 4MB cap (adjust if needed, keep conservative)

    fun validate(bytes: ByteArray, mimeType: String): Boolean {
        if (mimeType !in allowedMime) return false
        if (bytes.size > MAX_BYTES) return false
        // Dimension checks deferred (avoid bitmap decode in Phase 1). Add TODO for Phase 2 if required.
        return true
    }
}