package com.luna.chat.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) +
            slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(300)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("message_bubble_${message.id}"),
            horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
        ) {
            if (message.isFromUser) {
                UserMessageBubble(message = message)
            } else {
                AiMessageBubble(message = message)
            }
        }
    }
}

@Composable
private fun UserMessageBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    Column(modifier = modifier.widthIn(max = 280.dp), horizontalAlignment = Alignment.End) {
        Card(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                .semantics { contentDescription = "Your message: ${message.content}" },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(16.dp).testTag("user_message_text"),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal
                )
            )
        }
        Row(modifier = Modifier.padding(top = 4.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("message_timestamp")
            )
            Spacer(modifier = Modifier.width(4.dp))
            MessageStatusIndicator(status = message.status, modifier = Modifier.testTag("message_status"))
        }
    }
}

@Composable
private fun AiMessageBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    Column(modifier = modifier.widthIn(max = 280.dp), horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondary).testTag("luna_avatar"),
                contentAlignment = Alignment.Center
            ) { Text(text = "\uD83C\uDF19", fontSize = 20.sp) }
            Spacer(modifier = Modifier.width(8.dp))
            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                    .semantics { contentDescription = "Luna's message: ${message.content}" },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.animateContentSize()) {
                    // Collapsible reasoning section (like ChatGPT/Claude thinking dropdown)
                    if (!message.reasoning.isNullOrBlank()) {
                        ReasoningDropdown(reasoning = message.reasoning)
                    }

                    Text(
                        text = message.content,
                        modifier = Modifier.padding(16.dp).testTag("ai_message_text"),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }
        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, start = 48.dp).testTag("ai_message_timestamp")
        )
    }
}

@Composable
private fun ReasoningDropdown(reasoning: String) {
    var expanded by remember { mutableStateOf(false) }
    val summary = reasoning.take(60).replace("\n", " ").trim().let {
        if (reasoning.length > 60) "$it..." else it
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
    ) {
        // Header row — tappable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (expanded) "\u25BE" else "\u25B8",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (expanded) "Thought process" else summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = if (expanded) Int.MAX_VALUE else 1,
            )
        }

        // Expanded reasoning content
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                    thickness = 0.5.dp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = reasoning,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun MessageStatusIndicator(status: MessageStatus, modifier: Modifier = Modifier) {
    val (icon, color) = when (status) {
        MessageStatus.SENDING -> "\u23F3" to MaterialTheme.colorScheme.onSurfaceVariant
        MessageStatus.SENT -> "\u2713" to MaterialTheme.colorScheme.onSurfaceVariant
        MessageStatus.DELIVERED -> "\u2713\u2713" to MaterialTheme.colorScheme.primary
        MessageStatus.FAILED -> "\u274C" to MaterialTheme.colorScheme.error
    }
    Text(
        text = icon, color = color, fontSize = 12.sp,
        modifier = modifier.semantics { contentDescription = "Message status: ${status.name.lowercase()}" }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    return try {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        ""
    }
}
