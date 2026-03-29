package com.luna.chat.presentation.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luna.chat.presentation.accessibility.AccessibilityUtils
import com.luna.chat.presentation.accessibility.ChildFriendlyDescriptions
import com.luna.chat.presentation.accessibility.getAccessibleTextStyle
import com.luna.chat.presentation.performance.PerformanceUtils
import com.luna.chat.presentation.performance.isScrollingUp
import com.luna.chat.presentation.performance.shouldRenderItem
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.ChatSession
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.domain.usecase.ProcessImageUseCase
import com.luna.chat.presentation.theme.LunaTheme
import com.luna.chat.presentation.ui.components.ImageAttachmentButton
import com.luna.chat.presentation.ui.components.ImagePreviewDialog
import com.luna.chat.presentation.ui.components.MessageBubble
import com.luna.chat.presentation.ui.components.MessageInput
import com.luna.chat.presentation.ui.components.TypingIndicator
import com.luna.chat.presentation.ui.components.WelcomeCard
import com.luna.chat.presentation.viewmodel.ChatViewModel
import com.luna.chat.presentation.viewmodel.ChatUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Image attachment local UI state (in-memory only)
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedImageMime by remember { mutableStateOf<String?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // Avoid composable calls inside remember calculation; hoist a nullable reference placeholder.
    // Actual processing is routed via ViewModel methods (processImage/generateReplyFromVisionSummary).
    val processImageUseCase: ProcessImageUseCase? = null
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(currentSession.messages.size) {
        if (currentSession.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(
                    index = maxOf(0, currentSession.messages.size - 1)
                )
            }
        }
    }
    
    // Handle typing indicator
    LaunchedEffect(messageText) {
        viewModel.setTyping(messageText.isNotEmpty())
    }
    
    Scaffold(
        modifier = modifier.testTag("chat_screen"),
        topBar = {
            ChatTopBar(
                onSettingsClick = onSettingsClick,
                onNewChatClick = { 
                    viewModel.startNewChat()
                    messageText = ""
                }
            )
        },
        bottomBar = {
            Column {
                // Attachment row aligned with MessageInput actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .semantics { contentDescription = "Message actions" },
                    horizontalArrangement = Arrangement.End
                ) {
                    // Image attachment button mirrors component patterns
                    ImageAttachmentButton(
                        enabled = uiState.canSendMessage && !uiState.isLoading && !isAnalyzing,
                        onClick = {
                            // Stub image picker: expect bytes/mime from a provided lambda later
                            // For now, use a no-op that toggles preview if bytes are set by external caller
                            if (selectedImageBytes != null && selectedImageMime != null) {
                                showPreview = true
                            } else {
                                // External integration point (Activity/VM) should set selectedImageBytes/mime via callback
                                // No-op to maintain compile safety
                            }
                        },
                        modifier = Modifier
                            .testTag("attach_image_action")
                    )
                }

                MessageInput(
                    message = messageText,
                    onMessageChange = { messageText = it },
                    onSendMessage = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    },
                    onVoiceInput = {
                        // TODO: Implement voice input in future task
                    },
                    isEnabled = uiState.canSendMessage && !isAnalyzing,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.testTag("chat_message_input")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Image preview dialog
            if (showPreview && selectedImageBytes != null && selectedImageMime != null) {
                ImagePreviewDialog(
                    imageBytes = selectedImageBytes!!,
                    mimeType = selectedImageMime!!,
                    onConfirm = {
                        showPreview = false
                        val bytes = selectedImageBytes
                        val mime = selectedImageMime
                        if (bytes != null && mime != null) {
                            isAnalyzing = true
                            coroutineScope.launch {
                                // Governance guard remains disabled by default.
                                val visionToTextEnabled = false // TODO: enable when governance approves combined response
                                // Execute analysis via ViewModel/UseCase.
                                val summary = try {
                                    val vmResult = viewModel.processImage(bytes, mime)
                                    vmResult ?: "Vision analysis unavailable right now."
                                } catch (e: Exception) {
                                    "Vision analysis unavailable right now."
                                }
                                if (visionToTextEnabled) {
                                    // Route through existing send path to preserve filtering/pinning/logging.
                                    viewModel.generateReplyFromVisionSummary(summary)
                                } else {
                                    // Current default behavior: append summary as assistant message.
                                    viewModel.appendAssistantMessage(summary)
                                }
                                isAnalyzing = false
                                // Clear selection after processing
                                selectedImageBytes = null
                                selectedImageMime = null
                            }
                        }
                    },
                    onDismiss = {
                        showPreview = false
                    }
                )
            }

            ChatContent(
                session = currentSession,
                uiState = if (isAnalyzing) uiState.copy(isAiThinking = true) else uiState,
                listState = listState,
                onRetryMessage = { viewModel.retryLastMessage() },
                onDismissError = { viewModel.dismissError() },
                onHideWelcomeCard = { viewModel.hideWelcomeCard() },
                onSendSuggestion = { suggestion ->
                    viewModel.sendMessage(suggestion)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    onSettingsClick: () -> Unit,
    onNewChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🌙",
                    fontSize = 24.sp
                )
                Text(
                    text = "Luna",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            IconButton(
                onClick = onNewChatClick,
                modifier = Modifier
                    .size(AccessibilityUtils.ChildFriendlyTouchTargetSize)
                    .testTag("new_chat_button")
                    .semantics {
                        contentDescription = ChildFriendlyDescriptions.newChatButton()
                        role = Role.Button
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(AccessibilityUtils.ChildFriendlyTouchTargetSize)
                    .testTag("settings_button")
                    .semantics {
                        contentDescription = ChildFriendlyDescriptions.settingsButton()
                        role = Role.Button
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.testTag("chat_top_bar")
    )
}

@Composable
private fun ChatContent(
    session: ChatSession,
    uiState: ChatUiState,
    listState: LazyListState,
    onRetryMessage: () -> Unit,
    onDismissError: () -> Unit,
    onHideWelcomeCard: () -> Unit,
    onSendSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("chat_messages_list"),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Welcome card for first-time users
        if (uiState.showWelcomeCard) {
            item(key = "welcome_card") {
                WelcomeCard(
                    onConversationStarterClick = onSendSuggestion,
                    modifier = Modifier
                        .animateItemPlacement()
                        .testTag("welcome_card")
                )
            }
        }
        
        // Educational prompts that appear during conversation
        if (session.messages.isNotEmpty() && session.messages.size % 5 == 0 && !uiState.showWelcomeCard) {
            item(key = "educational_prompt_${session.messages.size}") {
                EducationalPromptCard(
                    onSendSuggestion = onSendSuggestion,
                    messageCount = session.messages.size,
                    modifier = Modifier
                        .animateItemPlacement()
                        .testTag("educational_prompt")
                )
            }
        }
        
        // Chat messages with performance optimization
        items(
            items = session.messages,
            key = { message -> message.id }
        ) { message ->
            val messageIndex = session.messages.indexOf(message)
            
            // Use performance-optimized rendering
            if (listState.shouldRenderItem(messageIndex)) {
                MessageBubble(
                    message = message,
                    modifier = Modifier
                        .animateItemPlacement()
                        .testTag("message_${message.id}")
                )
            } else {
                // Lightweight placeholder for off-screen items
                MessagePlaceholder(
                    modifier = Modifier
                        .height(PerformanceUtils.DefaultItemHeight)
                        .testTag("message_placeholder_${message.id}")
                )
            }
        }
        
        // Typing indicator
        if (uiState.showTypingIndicator) {
            item(key = "typing_indicator") {
                TypingIndicator(
                    isVisible = true,
                    modifier = Modifier
                        .animateItemPlacement()
                        .testTag("chat_typing_indicator")
                )
            }
        }

        // Image analysis indicator
        if (uiState.showTypingIndicator || (uiState.isAiThinking && !uiState.isLoading)) {
            item(key = "analyzing_indicator") {
                com.luna.chat.presentation.ui.components.SimpleTypingIndicator(
                    isVisible = true,
                    message = "Analyzing image…",
                    modifier = Modifier
                        .animateItemPlacement()
                        .testTag("analyzing_image_indicator")
                )
            }
        }
        
        // Error message
        if (uiState.hasError) {
            item(key = "error_message") {
                ErrorMessage(
                    message = uiState.error ?: "",
                    onRetry = onRetryMessage,
                    onDismiss = onDismissError,
                    modifier = Modifier
                        .animateItemPlacement()
                        .testTag("error_message")
                )
            }
        }
        
        // Add some bottom padding for better UX
        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
private fun EducationalPromptCard(
    onSendSuggestion: (String) -> Unit,
    messageCount: Int,
    modifier: Modifier = Modifier
) {
    val prompts = remember {
        listOf(
            listOf(
                "🧮 Can you help me with a math problem?",
                "🔬 Tell me something cool about science!",
                "📚 Help me understand this better",
                "🎯 Give me a fun challenge!"
            ),
            listOf(
                "🎨 Let's be creative together!",
                "🌍 Tell me about different countries",
                "🦕 What's your favorite dinosaur fact?",
                "🎵 Do you know any fun songs?"
            ),
            listOf(
                "🎲 Let's play 20 questions!",
                "📖 Help me write a short story",
                "🧩 Give me a riddle to solve",
                "🌟 What's something amazing about space?"
            )
        )
    }
    
    val promptSet = prompts[(messageCount / 5 - 1) % prompts.size]
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("educational_prompt_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎉",
                fontSize = 32.sp,
                modifier = Modifier.testTag("prompt_emoji")
            )
            
            Text(
                text = "Great chatting! Want to try something new?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("prompt_title")
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                promptSet.forEach { prompt ->
                    InteractiveSuggestionChip(
                        text = prompt,
                        onClick = { onSendSuggestion(prompt.substringAfter(" ")) },
                        modifier = Modifier.testTag("educational_suggestion_${promptSet.indexOf(prompt)}")
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveSuggestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = modifier.fillMaxWidth(),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = AssistChipDefaults.assistChipBorder(
            borderColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = modifier.fillMaxWidth(),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = AssistChipDefaults.assistChipBorder(
            borderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun MessagePlaceholder(
    modifier: Modifier = Modifier
) {
    // Lightweight placeholder for off-screen messages to maintain scroll position
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    )
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("error_message_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "😅",
                fontSize = 32.sp,
                modifier = Modifier.testTag("error_emoji")
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("error_message_text")
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onRetry,
                    modifier = Modifier.testTag("error_retry_button")
                ) {
                    Text(
                        text = "Try Again 🔄",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("error_dismiss_button")
                ) {
                    Text(
                        text = "Dismiss",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    LunaTheme {
        // Preview with sample data
        val sampleMessages = listOf(
            ChatMessage.create(
                content = "Hi Luna! Can you help me with my math homework?",
                isFromUser = true,
                status = MessageStatus.DELIVERED
            ),
            ChatMessage.create(
                content = "Hi there! I'd love to help you with your math homework! What problem are you working on? 📚✨",
                isFromUser = false,
                status = MessageStatus.DELIVERED
            ),
            ChatMessage.create(
                content = "I need help with fractions. How do I add 1/4 + 1/3?",
                isFromUser = true,
                status = MessageStatus.DELIVERED
            )
        )
        
        val sampleSession = ChatSession.create().copy(messages = sampleMessages)
        val sampleUiState = ChatUiState(
            showWelcomeCard = false,
            isFirstMessage = false
        )
        
        ChatContent(
            session = sampleSession,
            uiState = sampleUiState,
            listState = rememberLazyListState(),
            onRetryMessage = {},
            onDismissError = {},
            onHideWelcomeCard = {},
            onSendSuggestion = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeCardPreview() {
    LunaTheme {
        WelcomeCard(
            onConversationStarterClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorMessagePreview() {
    LunaTheme {
        ErrorMessage(
            message = "Check your internet connection and try again! 🌐",
            onRetry = {},
            onDismiss = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}