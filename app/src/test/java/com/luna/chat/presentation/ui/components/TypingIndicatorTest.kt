package com.luna.chat.presentation.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.luna.chat.presentation.theme.LunaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TypingIndicatorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun typingIndicator_displaysWhenVisible() {
        // When
        composeTestRule.setContent {
            LunaTheme {
                TypingIndicator(isVisible = true)
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("typing_indicator")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("typing_indicator_avatar")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("typing_dots_container")
            .assertIsDisplayed()
    }

    @Test
    fun typingIndicator_hidesWhenNotVisible() {
        // When
        composeTestRule.setContent {
            LunaTheme {
                TypingIndicator(isVisible = false)
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("typing_indicator")
            .assertDoesNotExist()
    }

    @Test
    fun typingIndicator_showsAnimatedDots() {
        // When
        composeTestRule.setContent {
            LunaTheme {
                TypingIndicator(isVisible = true)
            }
        }

        // Then - Check that all three dots are present
        composeTestRule
            .onNodeWithTag("typing_dot_0")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("typing_dot_1")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("typing_dot_2")
            .assertIsDisplayed()
    }

    @Test
    fun typingIndicator_hasAccessibilitySupport() {
        // When
        composeTestRule.setContent {
            LunaTheme {
                TypingIndicator(isVisible = true)
            }
        }

        // Then
        composeTestRule
            .onNode(hasContentDescription("Luna is typing a response"))
            .assertIsDisplayed()
    }

    @Test
    fun typingIndicator_showsLunaAvatar() {
        // When
        composeTestRule.setContent {
            LunaTheme {
                TypingIndicator(isVisible = true)
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("typing_indicator_avatar")
            .assertIsDisplayed()
        
        // Check that the avatar contains the moon emoji
        composeTestRule
            .onNodeWithText("🌙")
            .assertIsDisplayed()
    }

    @Test
    fun pulsingTypingIndicator_displaysCorrectly() {
        // When
        composeTestRule.setContent {
            LunaTheme {
                PulsingTypingIndicator(isVisible = true)
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("pulsing_typing_indicator")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("pulsing_avatar")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("wave_text")
            .assertIsDisplayed()
    }

    @Test
    fun pulsingTypingIndicator_hidesWhenNotVisible() {
        // When
        composeTestRule.setContent {
            LunaTheme {
                PulsingTypingIndicator(isVisible = false)
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("pulsing_typing_indicator")
            .assertDoesNotExist()
    }

    @Test
    fun simpleTypingIndicator_displaysCustomMessage() {
        // Given
        val customMessage = "Luna is thinking of something amazing!"

        // When
        composeTestRule.setContent {
            LunaTheme {
                SimpleTypingIndicator(
                    isVisible = true,
                    message = customMessage
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("simple_typing_indicator")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithTag("simple_typing_text")
            .assertIsDisplayed()
            .assertTextEquals(customMessage)
    }

    @Test
    fun simpleTypingIndicator_hasAccessibilitySupport() {
        // Given
        val customMessage = "Luna is working on your answer"

        // When
        composeTestRule.setContent {
            LunaTheme {
                SimpleTypingIndicator(
                    isVisible = true,
                    message = customMessage
                )
            }
        }

        // Then
        composeTestRule
            .onNode(hasContentDescription(customMessage))
            .assertIsDisplayed()
    }

    @Test
    fun simpleTypingIndicator_hidesWhenNotVisible() {
        // When
        composeTestRule.setContent {
            LunaTheme {
                SimpleTypingIndicator(isVisible = false)
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("simple_typing_indicator")
            .assertDoesNotExist()
    }

    @Test
    fun typingIndicator_hasChildFriendlyDesign() {
        // When
        composeTestRule.setContent {
            LunaTheme {
                TypingIndicator(isVisible = true)
            }
        }

        // Then - Check that the avatar is large enough for child interaction
        composeTestRule
            .onNodeWithTag("typing_indicator_avatar")
            .assertIsDisplayed()
            .assertWidthIsAtLeast(40.dp)
            .assertHeightIsAtLeast(40.dp)
    }

    @Test
    fun typingIndicator_showsTypingText() {
        // When
        composeTestRule.setContent {
            LunaTheme {
                TypingIndicator(isVisible = true)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Luna is thinking")
            .assertIsDisplayed()
    }

    @Test
    fun allTypingIndicators_workWithVisibilityToggle() {
        // Test all three variants with visibility toggle
        val indicators = listOf(
            { visible: Boolean -> TypingIndicator(isVisible = visible) },
            { visible: Boolean -> PulsingTypingIndicator(isVisible = visible) },
            { visible: Boolean -> SimpleTypingIndicator(isVisible = visible) }
        )

        indicators.forEach { indicator ->
            // Test visible state
            composeTestRule.setContent {
                LunaTheme {
                    indicator(true)
                }
            }
            
            // Should be displayed
            composeTestRule.waitForIdle()
            
            // Test hidden state
            composeTestRule.setContent {
                LunaTheme {
                    indicator(false)
                }
            }
            
            // Should be hidden
            composeTestRule.waitForIdle()
        }
    }
}