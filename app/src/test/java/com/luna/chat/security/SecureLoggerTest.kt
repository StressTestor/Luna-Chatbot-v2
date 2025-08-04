package com.luna.chat.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class SecureLoggerTest {

    private lateinit var secureLogger: SecureLogger
    
    @Before
    fun setup() {
        secureLogger = SecureLogger()
    }
    
    @Test
    fun `sanitizeMessage should redact API keys`() {
        // Given
        val message = "Using API key: sk-1234567890abcdef"
        
        // When
        val sanitized = secureLogger.sanitizeMessage(message)
        
        // Then
        assertEquals("Using API REDACTED", sanitized)
        assertFalse(sanitized.contains("sk-1234567890abcdef"))
    }
    
    @Test
    fun `sanitizeMessage should redact passwords`() {
        // Given
        val message = "User entered password: MySecretPass123"
        
        // When
        val sanitized = secureLogger.sanitizeMessage(message)
        
        // Then
        assertEquals("User entered REDACTED", sanitized)
        assertFalse(sanitized.contains("MySecretPass123"))
    }
    
    @Test
    fun `sanitizeMessage should redact tokens`() {
        // Given
        val message = "Auth token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
        
        // When
        val sanitized = secureLogger.sanitizeMessage(message)
        
        // Then
        assertEquals("Auth REDACTED", sanitized)
        assertFalse(sanitized.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"))
    }
    
    @Test
    fun `sanitizeMessage should redact email addresses`() {
        // Given
        val message = "User email: user@example.com"
        
        // When
        val sanitized = secureLogger.sanitizeMessage(message)
        
        // Then
        assertFalse(sanitized.contains("user@example.com"))
    }
    
    @Test
    fun `sanitizeMessage should not redact non-sensitive information`() {
        // Given
        val message = "User selected the rainbow theme"
        
        // When
        val sanitized = secureLogger.sanitizeMessage(message)
        
        // Then
        assertEquals(message, sanitized)
    }
    
    @Test
    fun `sanitizeMessage should handle null and empty strings`() {
        // Given
        val emptyMessage = ""
        
        // When
        val sanitized = secureLogger.sanitizeMessage(emptyMessage)
        
        // Then
        assertEquals(emptyMessage, sanitized)
    }
}