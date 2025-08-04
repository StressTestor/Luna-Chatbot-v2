package com.luna.chat.presentation.accessibility

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Utility object for accessibility-related functions and constants
 */
object AccessibilityUtils {
    
    // Minimum touch target size for accessibility (48dp as per Material Design guidelines)
    val MinTouchTargetSize = 48.dp
    
    // Enhanced touch target size for children (56dp for easier tapping)
    val ChildFriendlyTouchTargetSize = 56.dp
    
    /**
     * Checks if TalkBack or other accessibility services are enabled
     */
    fun isAccessibilityEnabled(context: Context): Boolean {
        return try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            ) == 1
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Checks if high contrast mode is enabled
     */
    fun isHighContrastEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Settings.Secure.getInt(
                    context.contentResolver,
                    "high_text_contrast_enabled"
                ) == 1
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }
    
    /**
     * Gets the system font scale for large text support
     */
    fun getFontScale(context: Context): Float {
        return context.resources.configuration.fontScale
    }
    
    /**
     * Checks if animations should be reduced based on system settings
     */
    fun shouldReduceAnimations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    1.0f
                ) == 0.0f
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }
}

/**
 * Composable function to get accessibility-aware text size
 */
@Composable
fun getAccessibleTextSize(baseSize: TextUnit): TextUnit {
    val context = LocalContext.current
    val fontScale = remember { AccessibilityUtils.getFontScale(context) }
    
    return when {
        fontScale >= 1.3f -> baseSize * 1.2f // Large text
        fontScale >= 1.15f -> baseSize * 1.1f // Medium-large text
        else -> baseSize // Normal text
    }
}

/**
 * Composable function to get accessibility-aware colors with high contrast support
 */
@Composable
fun getAccessibleColors(
    normalColor: Color,
    highContrastColor: Color = normalColor
): Color {
    val context = LocalContext.current
    val isHighContrast = remember { AccessibilityUtils.isHighContrastEnabled(context) }
    
    return if (isHighContrast) highContrastColor else normalColor
}

/**
 * Modifier extension for enhanced touch targets
 */
fun Modifier.accessibleTouchTarget(
    minSize: Dp = AccessibilityUtils.MinTouchTargetSize
): Modifier = this.size(minSize)

/**
 * Modifier extension for child-friendly touch targets
 */
fun Modifier.childFriendlyTouchTarget(): Modifier = 
    this.size(AccessibilityUtils.ChildFriendlyTouchTargetSize)

/**
 * Semantic modifier for better screen reader support
 */
fun Modifier.accessibleSemantics(
    description: String,
    role: Role? = null,
    additionalSemantics: (SemanticsPropertyReceiver.() -> Unit)? = null
): Modifier = this.semantics {
    contentDescription = description
    role?.let { this.role = it }
    additionalSemantics?.invoke(this)
}

/**
 * Get accessible text style with proper contrast and sizing
 */
@Composable
fun getAccessibleTextStyle(
    baseStyle: TextStyle,
    isImportant: Boolean = false
): TextStyle {
    val accessibleSize = getAccessibleTextSize(baseStyle.fontSize ?: 16.sp)
    val context = LocalContext.current
    val isHighContrast = remember { AccessibilityUtils.isHighContrastEnabled(context) }
    
    return baseStyle.copy(
        fontSize = accessibleSize,
        fontWeight = if (isImportant && isHighContrast) FontWeight.Bold else baseStyle.fontWeight,
        color = if (isHighContrast) {
            // Use high contrast colors
            when (baseStyle.color) {
                MaterialTheme.colorScheme.onSurface -> Color.Black
                MaterialTheme.colorScheme.onBackground -> Color.Black
                MaterialTheme.colorScheme.primary -> Color.Blue
                else -> baseStyle.color
            }
        } else {
            baseStyle.color
        }
    )
}

/**
 * Creates child-friendly content descriptions for screen readers
 */
object ChildFriendlyDescriptions {
    
    fun messageFromUser(content: String): String {
        return "You said: $content"
    }
    
    fun messageFromLuna(content: String): String {
        return "Luna says: $content"
    }
    
    fun sendButton(isLoading: Boolean): String {
        return if (isLoading) {
            "Luna is thinking about your message"
        } else {
            "Send your message to Luna"
        }
    }
    
    fun voiceButton(): String {
        return "Tap to speak your message to Luna instead of typing"
    }
    
    fun newChatButton(): String {
        return "Start a new conversation with Luna"
    }
    
    fun settingsButton(): String {
        return "Open settings to change themes and other options"
    }
    
    fun typingIndicator(): String {
        return "Luna is thinking and will respond soon"
    }
    
    fun errorMessage(error: String): String {
        return "Something went wrong: $error. You can try again."
    }
    
    fun welcomeCard(): String {
        return "Welcome message from Luna with conversation suggestions"
    }
    
    fun themeSelector(themeName: String): String {
        return "Choose $themeName theme with fun colors"
    }
}