package com.luna.chat.presentation.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// Removed non-existent accessibility imports
import com.luna.chat.presentation.theme.LunaTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onVoiceInput: () -> Unit,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    // Removed SpeechToTextService parameter for now
) {
    val context = LocalContext.current
    var hasRecordPermission by remember { mutableStateOf(false) }
    
    // Speech-to-text state (simplified for now)
    var isListening by remember { mutableStateOf(false) }
    
    // Permission launcher for voice input
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordPermission = isGranted
        if (isGranted) {
            isListening = true
            onVoiceInput()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("message_input_card"),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Voice input button
            VoiceInputButton(
                onClick = {
                    if (isListening) {
                        isListening = false
                    } else if (hasRecordPermission) {
                        isListening = true
                        onVoiceInput()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                isEnabled = isEnabled && !isLoading,
                isListening = isListening,
                modifier = Modifier.testTag("voice_input_button")
            )
            
            // Text input field
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("message_text_field")
                    .semantics {
                        contentDescription = "Type your message to Luna"
                    },
                placeholder = {
                    Text(
                        text = if (isLoading) "Luna is thinking..." else "Ask Luna anything! 🌟",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                enabled = isEnabled && !isLoading,
                singleLine = false,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (message.isNotBlank() && isEnabled && !isLoading) {
                            onSendMessage()
                        }
                    }
                ),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            // Send button
            SendButton(
                onClick = onSendMessage,
                isEnabled = isEnabled && !isLoading && message.isNotBlank(),
                isLoading = isLoading,
                modifier = Modifier.testTag("send_button")
            )
        }
    }
}

@Composable
private fun VoiceInputButton(
    onClick: () -> Unit,
    isEnabled: Boolean,
    isListening: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val scale by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "voice_button_scale"
    )
    
    // Pulsing animation when listening
    val infiniteTransition = rememberInfiniteTransition(label = "listening_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val backgroundColor = when {
        isListening -> MaterialTheme.colorScheme.primary
        isEnabled -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    IconButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
            .size(48.dp)
            .scale(scale * pulseScale)
            .clip(CircleShape)
            .background(backgroundColor)
            .semantics {
                contentDescription = if (isListening) {
                    "Listening... Tap to stop recording"
                } else {
                    "Tap to speak to Luna! 🎤"
                }
                role = Role.Button
            }
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = null,
            tint = when {
                isListening -> MaterialTheme.colorScheme.onPrimary
                isEnabled -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SendButton(
    onClick: () -> Unit,
    isEnabled: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "send_button_scale"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isLoading) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "send_button_rotation"
    )

    IconButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isEnabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .semantics {
                contentDescription = if (isLoading) "Sending message..." else "Send message"
            }
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
            },
            label = "send_button_content"
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = if (isEnabled) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PulsingButton(
    onClick: () -> Unit,
    isEnabled: Boolean,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    IconButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.scale(if (isEnabled) scale else 1f)
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageInputPreview() {
    LunaTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Normal state
            MessageInput(
                message = "",
                onMessageChange = {},
                onSendMessage = {},
                onVoiceInput = {},
                isEnabled = true,
                isLoading = false
            )
            
            // With text
            MessageInput(
                message = "Hello Luna! Can you help me?",
                onMessageChange = {},
                onSendMessage = {},
                onVoiceInput = {},
                isEnabled = true,
                isLoading = false
            )
            
            // Loading state
            MessageInput(
                message = "",
                onMessageChange = {},
                onSendMessage = {},
                onVoiceInput = {},
                isEnabled = false,
                isLoading = true
            )
        }
    }
}