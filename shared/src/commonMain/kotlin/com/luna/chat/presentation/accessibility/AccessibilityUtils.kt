package com.luna.chat.presentation.accessibility

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

object AccessibilityUtils {
    val MinTouchTargetSize = 48.dp
    val ChildFriendlyTouchTargetSize = 56.dp
}

@Composable
fun getAccessibleTextSize(baseSize: TextUnit): TextUnit = baseSize

@Composable
fun getAccessibleTextStyle(
    baseStyle: TextStyle,
    isImportant: Boolean = false
): TextStyle = baseStyle

fun Modifier.accessibleTouchTarget(
    minSize: Dp = AccessibilityUtils.MinTouchTargetSize
): Modifier = this.size(minSize)

fun Modifier.childFriendlyTouchTarget(): Modifier =
    this.size(AccessibilityUtils.ChildFriendlyTouchTargetSize)

fun Modifier.accessibleSemantics(
    description: String,
    role: Role? = null,
    additionalSemantics: (SemanticsPropertyReceiver.() -> Unit)? = null
): Modifier = this.semantics {
    contentDescription = description
    role?.let { this.role = it }
    additionalSemantics?.invoke(this)
}

object ChildFriendlyDescriptions {
    fun messageFromUser(content: String): String = "You said: $content"
    fun messageFromLuna(content: String): String = "Luna says: $content"
    fun sendButton(isLoading: Boolean): String =
        if (isLoading) "Luna is thinking about your message" else "Send your message to Luna"
    fun voiceButton(): String = "Tap to speak your message to Luna instead of typing"
    fun newChatButton(): String = "Start a new conversation with Luna"
    fun settingsButton(): String = "Open settings to change themes and other options"
    fun typingIndicator(): String = "Luna is thinking and will respond soon"
    fun errorMessage(error: String): String = "Something went wrong: $error. You can try again."
    fun welcomeCard(): String = "Welcome message from Luna with conversation suggestions"
    fun themeSelector(themeName: String): String = "Choose $themeName theme with fun colors"
}
