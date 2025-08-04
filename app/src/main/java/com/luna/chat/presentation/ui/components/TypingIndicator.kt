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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.chat.presentation.accessibility.AccessibilityUtils
import com.luna.chat.presentation.accessibility.ChildFriendlyDescriptions
import com.luna.chat.presentation.accessibility.getAccessibleTextStyle
import com.luna.chat.presentation.theme.LunaTheme
import kotlinx.coroutines.delay

@Composable
fun TypingIndicator(
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shouldReduceAnimations = remember { AccessibilityUtils.shouldReduceAnimations(context) }
    
    if (isVisible) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("typing_indicator")
                .semantics {
                    contentDescription = ChildFriendlyDescriptions.typingIndicator()
                },
            horizontalArrangement = Arrangement.Start
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Luna avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                        .testTag("typing_indicator_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌙",
                        fontSize = 20.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Typing bubble with animated dots
                Card(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .testTag("typing_dots_container"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Luna is thinking",
                            style = getAccessibleTextStyle(
                                baseStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        )
                        
                        if (!shouldReduceAnimations) {
                            AnimatedTypingDots()
                        } else {
                            StaticTypingDots()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedTypingDots(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_animation")
    
    // Create three dots with staggered animations
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600,
                        delayMillis = index * 200,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_scale_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("typing_dot_$index")
            )
        }
    }
}

@Composable
private fun StaticTypingDots(
    modifier: Modifier = Modifier
) {
    // Static dots for users who prefer reduced motion
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("static_typing_dot_$index")
            )
        }
    }
}

@Composable
fun PulsingTypingIndicator(
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("pulsing_typing_indicator"),
            horizontalArrangement = Arrangement.Start
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                // Luna avatar with pulsing effect
                PulsingAvatar()
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Typing message with wave effect
                Card(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        WaveText(
                            text = "Luna is thinking...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsingAvatar(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_scale"
    )
    
    Box(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .testTag("pulsing_avatar"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🌙",
            fontSize = 20.sp
        )
    }
}

@Composable
private fun WaveText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_text")
    
    Row(
        modifier = modifier.testTag("wave_text"),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        text.forEachIndexed { index, char ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1000,
                        delayMillis = index * 100,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "char_offset_$index"
            )
            
            Text(
                text = char.toString(),
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.offset(y = offsetY.dp)
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
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("simple_typing_indicator"),
            horizontalArrangement = Arrangement.Start
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                // Luna avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌙",
                        fontSize = 20.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Simple typing message
                Card(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                        .semantics {
                            contentDescription = message
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier
                            .padding(16.dp)
                            .testTag("simple_typing_text"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TypingIndicatorPreview() {
    LunaTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Animated Dots Typing Indicator",
                style = MaterialTheme.typography.titleMedium
            )
            TypingIndicator(isVisible = true)
            
            Text(
                text = "Pulsing Typing Indicator",
                style = MaterialTheme.typography.titleMedium
            )
            PulsingTypingIndicator(isVisible = true)
            
            Text(
                text = "Simple Typing Indicator",
                style = MaterialTheme.typography.titleMedium
            )
            SimpleTypingIndicator(
                isVisible = true,
                message = "Luna is thinking of something fun! 🎨"
            )
        }
    }
}