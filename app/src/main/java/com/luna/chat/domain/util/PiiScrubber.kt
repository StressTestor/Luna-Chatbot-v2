package com.luna.chat.domain.util

/**
 * Minimal conservative PII scrubber; do not over-match.
 * Apply upstream before sending prompts to external services.
 */
object PiiScrubber {
    private val emailRegex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
    private val phoneRegex = Regex("\\+?\\d[\\d\\s().-]{7,}")
    private val ssnRegex = Regex("\\b\\d{3}-\\d{2}-\\d{4}\\b")
    private val ccRegex = Regex("\\b\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}\\b")

    fun scrub(text: String): String =
        text
            .replace(emailRegex, "[redacted-email]")
            .replace(phoneRegex, "[redacted-phone]")
            .replace(ssnRegex, "[redacted-ssn]")
            .replace(ccRegex, "[redacted-cc]")
}