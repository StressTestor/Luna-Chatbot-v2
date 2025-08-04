package com.luna.chat.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class ContentFilteringServiceTest {

    @Mock
    private lateinit var securityConfig: SecurityConfig
    
    @Mock
    private lateinit var secureLogger: SecureLogger
    
    private lateinit var contentFilteringService: ContentFilteringService
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Default mock behavior
        whenever(securityConfig.areParentalControlsEnabled()).thenReturn(true)
        whenever(securityConfig.getTemperatureParameter()).thenReturn(0.5f)
        whenever(securityConfig.getTopPParameter()).thenReturn(0.7f)
        whenever(secureLogger.sanitizeMessage(any())).thenAnswer { it.arguments[0] as String }
        
        contentFilteringService = ContentFilteringService(securityConfig, secureLogger)
    }
    
    @Test
    fun `filterUserInput should filter violent content`() {
        // Given
        val userInput = "How do I kill zombies in Minecraft?"
        
        // When
        val result = contentFilteringService.filterUserInput(userInput)
        
        // Then
        assertTrue(result.wasFiltered)
        assertEquals("[Content filtered]", result.input)
        assertTrue(result.reason?.contains("inappropriate content") == true)
    }
    
    @Test
    fun `filterUserInput should filter adult content`() {
        // Given
        val userInput = "What is sex education?"
        
        // When
        val result = contentFilteringService.filterUserInput(userInput)
        
        // Then
        assertTrue(result.wasFiltered)
        assertEquals("[Content filtered]", result.input)
    }
    
    @Test
    fun `filterUserInput should filter personal information requests`() {
        // Given
        val userInput = "Where do you live?"
        
        // When
        val result = contentFilteringService.filterUserInput(userInput)
        
        // Then
        assertTrue(result.wasFiltered)
        assertEquals("[Content filtered]", result.input)
    }
    
    @Test
    fun `filterUserInput should allow educational content`() {
        // Given
        val userInput = "Tell me about the solar system"
        
        // When
        val result = contentFilteringService.filterUserInput(userInput)
        
        // Then
        assertFalse(result.wasFiltered)
        assertEquals(userInput, result.input)
    }
    
    @Test
    fun `filterAiResponse should filter inappropriate content`() {
        // Given
        val aiResponse = "Guns are weapons used in wars and conflicts."
        
        // When
        val result = contentFilteringService.filterAiResponse(aiResponse)
        
        // Then
        assertTrue(result.wasFiltered)
        assertTrue(result.input.contains("I'm sorry"))
    }
    
    @Test
    fun `filterAiResponse should allow educational content`() {
        // Given
        val aiResponse = "The solar system consists of the Sun and the planets that orbit around it."
        
        // When
        val result = contentFilteringService.filterAiResponse(aiResponse)
        
        // Then
        assertFalse(result.wasFiltered)
        assertEquals(aiResponse, result.input)
    }
    
    @Test
    fun `getModelParameters should return parameters based on security config`() {
        // Given
        whenever(securityConfig.getTemperatureParameter()).thenReturn(0.6f)
        whenever(securityConfig.getTopPParameter()).thenReturn(0.8f)
        
        // When
        val parameters = contentFilteringService.getModelParameters()
        
        // Then
        assertEquals(0.6f, parameters["temperature"])
        assertEquals(0.8f, parameters["top_p"])
        assertEquals(true, parameters["safe_mode"])
    }
    
    @Test
    fun `isEducationalTopic should identify educational topics`() {
        // Given/When/Then
        assertTrue(contentFilteringService.isEducationalTopic("Tell me about science"))
        assertTrue(contentFilteringService.isEducationalTopic("I want to learn about dinosaurs"))
        assertTrue(contentFilteringService.isEducationalTopic("What is the solar system?"))
        assertFalse(contentFilteringService.isEducationalTopic("Tell me a joke"))
    }
    
    @Test
    fun `filtering should be disabled when parental controls are off`() {
        // Given
        whenever(securityConfig.areParentalControlsEnabled()).thenReturn(false)
        val userInput = "What is sex education?"
        
        // When
        val result = contentFilteringService.filterUserInput(userInput)
        
        // Then
        assertFalse(result.wasFiltered)
        assertEquals(userInput, result.input)
    }
}