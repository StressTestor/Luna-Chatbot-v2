package com.luna.chat.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.chat.presentation.accessibility.ChildFriendlyDescriptions

@Composable
fun TypingIndicator(
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("typing_indicator")
                .semantics { contentDescription = ChildFriendlyDescriptions.typingIndicator() },
            horizontalArrangement = Arrangement.Start
        ) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.secondary).testTag("typing_indicator_avatar"),
                    contentAlignment = Alignment.Center
                ) { Text(text = "\uD83C\uDF19", fontSize = 20.sp) }

                Spacer(modifier = Modifier.width(8.dp))

                Card(
                    modifier = Modifier.clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).testTag("typing_dots_container"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Luna is thinking",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp, fontWeight = FontWeight.Medium
                            )
                        )
                        AnimatedTypingDots()
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedTypingDots(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_animation")
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f, targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, delayMillis = index * 200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "dot_scale_$index"
            )
            Box(
                modifier = Modifier.size(6.dp).scale(scale).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary).testTag("typing_dot_$index")
            )
        }
    }
}

@Composable
fun SimpleTypingIndicator(
    isVisible: Boolean = true,
    message: String = "Luna is typing...",
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Row(
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).testTag("simple_typing_indicator"),
            horizontalArrangement = Arrangement.Start
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) { Text(text = "\uD83C\uDF19", fontSize = 20.sp) }
                Spacer(modifier = Modifier.width(8.dp))
                Card(
                    modifier = Modifier.clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                        .semantics { contentDescription = message },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp).testTag("simple_typing_text"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp, fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
