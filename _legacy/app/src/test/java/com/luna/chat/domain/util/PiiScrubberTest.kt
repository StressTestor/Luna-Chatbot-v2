package com.luna.chat.domain.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PiiScrubberTest {

    @Test
    fun scrub_redacts_emails() {
        val input = "Contact me at kid@example.com for details."
        val out = PiiScrubber.scrub(input)
        assertFalse(out.contains("kid@example.com"))
        assertTrue(out.contains("[redacted-email]"))
    }

    @Test
    fun scrub_redacts_phone_numbers() {
        val input = "Call +1 (303) 555-1212 or 303-555-1212."
        val out = PiiScrubber.scrub(input)
        assertFalse(out.contains("303-555-1212"))
        assertTrue(out.contains("[redacted-phone]"))
    }

    @Test
    fun scrub_redacts_ssn() {
        val input = "My SSN is 123-45-6789."
        val out = PiiScrubber.scrub(input)
        assertFalse(out.contains("123-45-6789"))
        assertTrue(out.contains("[redacted-ssn]"))
    }

    @Test
    fun scrub_redacts_credit_cards() {
        val input = "Card: 4242 4242 4242 4242 or 4242-4242-4242-4242."
        val out = PiiScrubber.scrub(input)
        assertFalse(out.contains("4242 4242 4242 4242"))
        assertTrue(out.contains("[redacted-cc]"))
    }
}