package com.luna.chat.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus
// Removed non-existent accessibility imports
import com.luna.chat.presentation.theme.LunaTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    // Removed TextToSpeechService parameter for now
) {
    val context = LocalContext.current
    val isAccessibilityEnabled = false // Simplified for now
    val shouldReduceAnimations = false // Simplified for now
    
    AnimatedVisibility(
        visible = isVisible,
        enter = if (shouldReduceAnimations) {
            fadeIn(animationSpec = tween(100))
        } else {
            fadeIn(animationSpec = tween(300)) + 
            slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(300)
            )
        },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("message_bubble_${message.id}"),
            horizontalArrangement = if (message.isFromUser) {
                Arrangement.End
            } else {
                Arrangement.Start
            }
        ) {
            if (message.isFromUser) {
                UserMessageBubble(
                    message = message,
                    showSpeakButton = isAccessibilityEnabled
                )
            } else {
                AiMessageBubble(
                    message = message,
                    showSpeakButton = isAccessibilityEnabled
                )
            }
        }
    }
}

@Composable
private fun UserMessageBubble(
    message: ChatMessage,
    showSpeakButton: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(max = 280.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showSpeakButton) {
                SpeakButton(
                    onClick = { /* TODO: Implement text-to-speech */ },
                    contentDescription = "Read your message aloud"
                )
            }
            
            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                    .semantics {
                        contentDescription = "Your message: ${message.content}"
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier
                        .padding(16.dp)
                        .testTag("user_message_text"),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }
        
        Row(
            modifier = Modifier.padding(top = 4.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("message_timestamp")
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            MessageStatusIndicator(
                status = message.status,
                modifier = Modifier.testTag("message_status")
            )
        }
    }
}

@Composable
private fun AiMessageBubble(
    message: ChatMessage,
    showSpeakButton: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(max = 280.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Luna avatar/icon placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .testTag("luna_avatar"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌙",
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Card(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                        .semantics {
                            contentDescription = "Luna's message: ${message.content}"
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = message.content,
                        modifier = Modifier
                            .padding(16.dp)
                            .testTag("ai_message_text"),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
                
                if (showSpeakButton) {
                    SpeakButton(
                        onClick = { /* TODO: Implement text-to-speech */ },
                        contentDescription = "Read Luna's message aloud",
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        
        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 4.dp, start = 48.dp)
                .testTag("ai_message_timestamp")
        )
    }
}

@Composable
private fun SpeakButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(32.dp)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            }
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun MessageStatusIndicator(
    status: MessageStatus,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (status) {
        MessageStatus.SENDING -> "⏳" to MaterialTheme.colorScheme.onSurfaceVariant
        MessageStatus.SENT -> "✓" to MaterialTheme.colorScheme.onSurfaceVariant
        MessageStatus.DELIVERED -> "✓✓" to MaterialTheme.colorScheme.primary
        MessageStatus.FAILED -> "❌" to MaterialTheme.colorScheme.error
    }
    
    Text(
        text = icon,
        color = color,
        fontSize = 12.sp,
        modifier = modifier.semantics {
            contentDescription = "Message status: ${status.name.lowercase()}"
        }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
private fun MessageBubblePreview() {
    LunaTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // User message preview
            MessageBubble(
                message = ChatMessage.create(
                    content = "Hi Luna! Can you help me with my math homework?",
                    isFromUser = true,
                    status = MessageStatus.DELIVERED
                )
            )
            
            // AI message preview
            MessageBubble(
                message = ChatMessage.create(
                    content = "Hi there! I'd love to help you with your math homework! What problem are you working on? 📚✨",
                    isFromUser = false,
                    status = MessageStatus.DELIVERED
                )
            )
            
            // Long message preview
            MessageBubble(
                message = ChatMessage.create(
                    content = "This is a longer message to test how the bubble handles text wrapping and maintains good readability for children. The text should wrap nicely within the bubble constraints.",
                    isFromUser = true,
                    status = MessageStatus.SENT
                )
            )
        }
    }
}