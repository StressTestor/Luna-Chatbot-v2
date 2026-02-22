package com.luna.chat.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeCard(
    onConversationStarterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val infiniteTransition = rememberInfiniteTransition(label = "welcome_animation")
    val lunaFloat by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "luna_float"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        ) + fadeIn(animationSpec = tween(800)),
        modifier = modifier.testTag("welcome_card")
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.offset(y = lunaFloat.dp), contentAlignment = Alignment.Center) {
                    Text(text = "\uD83C\uDF19", fontSize = 48.sp, modifier = Modifier.testTag("luna_emoji"))
                }
                Text(
                    text = "Hi there! I'm Luna! \uD83D\uDC4B",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "I'm your friendly AI companion, here to help you learn, explore, and have fun! What would you like to talk about today?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                Text(
                    text = "Try asking me about:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(conversationStarters) { starter ->
                        ConversationStarterChip(
                            text = starter.text, emoji = starter.emoji,
                            onClick = { onConversationStarterClick(starter.prompt) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationStarterChip(
    text: String, emoji: String, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chip_scale"
    )

    AssistChip(
        onClick = { isPressed = true; onClick() },
        label = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = emoji, fontSize = 16.sp)
                Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier.scale(scale).testTag("conversation_starter_$text")
    )

    LaunchedEffect(isPressed) {
        if (isPressed) { kotlinx.coroutines.delay(100); isPressed = false }
    }
}

private data class ConversationStarter(val text: String, val emoji: String, val prompt: String)

private val conversationStarters = listOf(
    ConversationStarter("Math Help", "\uD83D\uDD22", "Can you help me with a math problem?"),
    ConversationStarter("Science Facts", "\uD83D\uDD2C", "Tell me something cool about science!"),
    ConversationStarter("Story Time", "\uD83D\uDCDA", "Can you tell me an interesting story?"),
    ConversationStarter("Fun Games", "\uD83C\uDFAE", "Let's play a word game!"),
    ConversationStarter("Animals", "\uD83D\uDC3E", "Tell me about my favorite animal!"),
    ConversationStarter("Space", "\uD83D\uDE80", "What's amazing about space?")
)
