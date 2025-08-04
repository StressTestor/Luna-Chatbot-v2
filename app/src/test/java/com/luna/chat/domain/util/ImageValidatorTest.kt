package com.luna.chat.domain.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageValidatorTest {

    @Test
    fun validate_allows_supported_mime_and_under_size() {
        val smallBytes = ByteArray(1024) { 0 }
        assertTrue(ImageValidator.validate(smallBytes, "image/jpeg"))
        assertTrue(ImageValidator.validate(smallBytes, "image/png"))
        assertTrue(ImageValidator.validate(smallBytes, "image/webp"))
    }

    @Test
    fun validate_rejects_unsupported_mime() {
        val smallBytes = ByteArray(1024) { 0 }
        assertFalse(ImageValidator.validate(smallBytes, "image/gif"))
        assertFalse(ImageValidator.validate(smallBytes, "application/octet-stream"))
        assertFalse(ImageValidator.validate(smallBytes, "text/plain"))
    }

    @Test
    fun validate_rejects_over_size_cap() {
        // MAX_BYTES is 4 * 1024 * 1024; craft just-over cap buffer
        val overCap = ByteArray(4 * 1024 * 1024 + 1) { 0 }
        assertFalse(ImageValidator.validate(overCap, "image/png"))
    }
}