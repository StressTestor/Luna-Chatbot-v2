package com.luna.chat.presentation.accessibility

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.provider.Settings
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class AccessibilityUtilsTest {

    private lateinit var context: Context
    private lateinit var resources: Resources
    private lateinit var configuration: Configuration

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        resources = mockk(relaxed = true)
        configuration = mockk(relaxed = true)
        
        every { context.resources } returns resources
        every { resources.configuration } returns configuration
        
        // Mock Settings static methods
        mockkStatic(Settings.Secure::class)
        mockkStatic(Settings.Global::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `isAccessibilityEnabled should return true when accessibility is enabled`() {
        every { 
            Settings.Secure.getInt(
                any(), 
                Settings.Secure.ACCESSIBILITY_ENABLED
            ) 
        } returns 1
        
        val result = AccessibilityUtils.isAccessibilityEnabled(context)
        
        assertTrue(result)
    }

    @Test
    fun `isAccessibilityEnabled should return false when accessibility is disabled`() {
        every { 
            Settings.Secure.getInt(
                any(), 
                Settings.Secure.ACCESSIBILITY_ENABLED
            ) 
        } returns 0
        
        val result = AccessibilityUtils.isAccessibilityEnabled(context)
        
        assertFalse(result)
    }

    @Test
    fun `isAccessibilityEnabled should handle exceptions gracefully`() {
        every { 
            Settings.Secure.getInt(
                any(), 
                Settings.Secure.ACCESSIBILITY_ENABLED
            ) 
        } throws SecurityException("Permission denied")
        
        val result = AccessibilityUtils.isAccessibilityEnabled(context)
        
        assertFalse(result)
    }

    @Test
    fun `isHighContrastEnabled should return true when high contrast is enabled`() {
        every { 
            Settings.Secure.getInt(
                any(), 
                "high_text_contrast_enabled"
            ) 
        } returns 1
        
        val result = AccessibilityUtils.isHighContrastEnabled(context)
        
        assertTrue(result)
    }

    @Test
    fun `isHighContrastEnabled should return false when high contrast is disabled`() {
        every { 
            Settings.Secure.getInt(
                any(), 
                "high_text_contrast_enabled"
            ) 
        } returns 0
        
        val result = AccessibilityUtils.isHighContrastEnabled(context)
        
        assertFalse(result)
    }

    @Test
    fun `getFontScale should return configuration font scale`() {
        val expectedScale = 1.5f
        every { configuration.fontScale } returns expectedScale
        
        val result = AccessibilityUtils.getFontScale(context)
        
        assertEquals(expectedScale, result, 0.01f)
    }

    @Test
    fun `shouldReduceAnimations should return true when animations are disabled`() {
        every { 
            Settings.Global.getFloat(
                any(), 
                Settings.Global.ANIMATOR_DURATION_SCALE, 
                1.0f
            ) 
        } returns 0.0f
        
        val result = AccessibilityUtils.shouldReduceAnimations(context)
        
        assertTrue(result)
    }

    @Test
    fun `shouldReduceAnimations should return false when animations are enabled`() {
        every { 
            Settings.Global.getFloat(
                any(), 
                Settings.Global.ANIMATOR_DURATION_SCALE, 
                1.0f
            ) 
        } returns 1.0f
        
        val result = AccessibilityUtils.shouldReduceAnimations(context)
        
        assertFalse(result)
    }

    @Test
    fun `touch target sizes should meet accessibility guidelines`() {
        val minTouchTarget = AccessibilityUtils.MinTouchTargetSize
        val childFriendlyTouchTarget = AccessibilityUtils.ChildFriendlyTouchTargetSize
        
        // Minimum touch target should be at least 48dp
        assertTrue("Minimum touch target should be at least 48dp", 
                  minTouchTarget.value >= 48f)
        
        // Child-friendly touch target should be larger than minimum
        assertTrue("Child-friendly touch target should be larger than minimum",
                  childFriendlyTouchTarget.value > minTouchTarget.value)
    }

    @Test
    fun `ChildFriendlyDescriptions should provide appropriate descriptions`() {
        val messageContent = "Hello Luna!"
        
        val userDescription = ChildFriendlyDescriptions.messageFromUser(messageContent)
        val lunaDescription = ChildFriendlyDescriptions.messageFromLuna(messageContent)
        
        assertTrue("User message description should contain content", 
                  userDescription.contains(messageContent))
        assertTrue("Luna message description should contain content", 
                  lunaDescription.contains(messageContent))
        
        assertTrue("User message description should indicate it's from user", 
                  userDescription.contains("You"))
        assertTrue("Luna message description should indicate it's from Luna", 
                  lunaDescription.contains("Luna"))
    }

    @Test
    fun `ChildFriendlyDescriptions should provide context-appropriate button descriptions`() {
        val sendButtonNormal = ChildFriendlyDescriptions.sendButton(false)
        val sendButtonLoading = ChildFriendlyDescriptions.sendButton(true)
        
        assertNotEquals("Send button descriptions should differ based on loading state",
                       sendButtonNormal, sendButtonLoading)
        
        assertTrue("Loading description should indicate Luna is thinking",
                  sendButtonLoading.contains("thinking"))
    }

    @Test
    fun `ChildFriendlyDescriptions should provide helpful voice button description`() {
        val voiceDescription = ChildFriendlyDescriptions.voiceButton()
        
        assertTrue("Voice description should mention speaking",
                  voiceDescription.toLowerCase().contains("speak"))
        assertTrue("Voice description should mention Luna",
                  voiceDescription.contains("Luna"))
    }

    @Test
    fun `ChildFriendlyDescriptions should provide clear navigation descriptions`() {
        val newChatDescription = ChildFriendlyDescriptions.newChatButton()
        val settingsDescription = ChildFriendlyDescriptions.settingsButton()
        
        assertTrue("New chat description should mention conversation",
                  newChatDescription.toLowerCase().contains("conversation") ||
                  newChatDescription.toLowerCase().contains("chat"))
        
        assertTrue("Settings description should mention settings or options",
                  settingsDescription.toLowerCase().contains("settings") ||
                  settingsDescription.toLowerCase().contains("options"))
    }

    @Test
    fun `ChildFriendlyDescriptions should provide informative status descriptions`() {
        val typingDescription = ChildFriendlyDescriptions.typingIndicator()
        val errorDescription = ChildFriendlyDescriptions.errorMessage("Network error")
        
        assertTrue("Typing description should mention Luna",
                  typingDescription.contains("Luna"))
        assertTrue("Typing description should indicate waiting",
                  typingDescription.toLowerCase().contains("thinking") ||
                  typingDescription.toLowerCase().contains("respond"))
        
        assertTrue("Error description should contain the error",
                  errorDescription.contains("Network error"))
        assertTrue("Error description should suggest action",
                  errorDescription.toLowerCase().contains("try"))
    }

    @Test
    fun `theme descriptions should be child friendly`() {
        val themes = listOf("Rainbow", "Ocean", "Forest", "Space", "Sunset")
        
        themes.forEach { theme ->
            val description = ChildFriendlyDescriptions.themeSelector(theme)
            
            assertTrue("Theme description should contain theme name",
                      description.contains(theme))
            assertTrue("Theme description should mention colors or fun",
                      description.toLowerCase().contains("color") ||
                      description.toLowerCase().contains("fun"))
        }
    }
}