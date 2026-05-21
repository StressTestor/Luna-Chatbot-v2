package com.luna.chat.presentation.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.ChatSession
import com.luna.chat.domain.entity.MessageStatus
import com.luna.chat.presentation.accessibility.AccessibilityUtils
import com.luna.chat.presentation.accessibility.ChildFriendlyDescriptions
import com.luna.chat.presentation.performance.PerformanceUtils
import com.luna.chat.presentation.performance.shouldRenderItem
import com.luna.chat.platform.rememberImagePicker
import com.luna.chat.presentation.ui.components.ConversationDrawer
import com.luna.chat.presentation.ui.components.ImageAttachmentButton
import com.luna.chat.presentation.ui.components.ImagePreviewDialog
import com.luna.chat.presentation.ui.components.MessageBubble
import com.luna.chat.presentation.ui.components.MessageInput
import com.luna.chat.presentation.ui.components.ModelSelectorSheet
import com.luna.chat.presentation.ui.components.SimpleTypingIndicator
import com.luna.chat.presentation.ui.components.TypingIndicator
import com.luna.chat.presentation.ui.components.WelcomeCard
import com.luna.chat.presentation.viewmodel.ChatViewModel
import com.luna.chat.presentation.viewmodel.ChatUiState
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val modelsLoading by viewModel.modelsLoading.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val currentConversationId by viewModel.currentConversationId.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var showModelSheet by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedImageMime by remember { mutableStateOf<String?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }

    val imagePicker = rememberImagePicker { bytes, mime ->
        selectedImageBytes = bytes
        selectedImageMime = mime
        showPreview = true
    }

    LaunchedEffect(currentSession.messages.size) {
        if (currentSession.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(index = maxOf(0, currentSession.messages.size - 1))
            }
        }
    }

    LaunchedEffect(messageText) {
        viewModel.setTyping(messageText.isNotEmpty())
    }

    if (showModelSheet) {
        ModelSelectorSheet(
            models = availableModels,
            selectedModelId = uiState.selectedModel,
            isLoading = modelsLoading,
            onModelSelected = { viewModel.selectModel(it) },
            onDismiss = { showModelSheet = false },
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.75f),
            ) {
                ConversationDrawer(
                    conversations = conversations,
                    currentConversationId = currentConversationId,
                    onNewChat = {
                        viewModel.startNewChat(); messageText = ""
                        coroutineScope.launch { drawerState.close() }
                    },
                    onSelectConversation = { id ->
                        viewModel.switchConversation(id)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onDeleteConversation = { viewModel.deleteConversation(it) },
                )
            }
        },
    ) {
    Scaffold(
        modifier = modifier.testTag("chat_screen"),
        topBar = {
            ChatTopBar(
                selectedModel = uiState.selectedModel,
                onModelClick = { viewModel.loadModels(); showModelSheet = true },
                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                onSettingsClick = onSettingsClick,
            )
        },
        bottomBar = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .semantics { contentDescription = "Message actions" },
                    horizontalArrangement = Arrangement.End
                ) {
                    ImageAttachmentButton(
                        enabled = uiState.canSendMessage && !uiState.isLoading && !isAnalyzing,
                        onClick = imagePicker.onClick,
                        modifier = Modifier.testTag("attach_image_action")
                    )
                }
                MessageInput(
                    message = messageText,
                    onMessageChange = { messageText = it },
                    onSendMessage = {
                        if (messageText.isNotBlank()) { viewModel.sendMessage(messageText); messageText = "" }
                    },
                    onVoiceInput = { },
                    isEnabled = uiState.canSendMessage && !isAnalyzing,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.testTag("chat_message_input")
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (showPreview && selectedImageBytes != null && selectedImageMime != null) {
                ImagePreviewDialog(
                    imageBytes = selectedImageBytes!!,
                    mimeType = selectedImageMime!!,
                    onConfirm = {
                        showPreview = false
                        val bytes = selectedImageBytes; val mime = selectedImageMime
                        if (bytes != null && mime != null) {
                            isAnalyzing = true
                            coroutineScope.launch {
                                val summary = try {
                                    viewModel.processImage(bytes, mime) ?: "Vision analysis unavailable right now."
                                } catch (e: Exception) { "Vision analysis unavailable right now." }
                                viewModel.appendAssistantMessage(summary)
                                isAnalyzing = false
                                selectedImageBytes = null; selectedImageMime = null
                            }
                        }
                    },
                    onDismiss = { showPreview = false }
                )
            }

            ChatContent(
                session = currentSession,
                uiState = if (isAnalyzing) uiState.copy(isAiThinking = true) else uiState,
                listState = listState,
                onRetryMessage = { viewModel.retryLastMessage() },
                onDismissError = { viewModel.dismissError() },
                onHideWelcomeCard = { viewModel.hideWelcomeCard() },
                onSendSuggestion = { viewModel.sendMessage(it) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    } // ModalNavigationDrawer
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    selectedModel: String,
    onModelClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shortModelName = selectedModel
        .substringBefore(":free")
        .substringAfter("/")

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open conversations",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "\uD83C\uDF19", fontSize = 24.sp)
                    Text(text = "Luna", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = "$shortModelName \u25BE",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onModelClick)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        },
        actions = {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(AccessibilityUtils.ChildFriendlyTouchTargetSize).testTag("settings_button")
                    .semantics { contentDescription = ChildFriendlyDescriptions.settingsButton(); role = Role.Button }
            ) { Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier.testTag("chat_top_bar"),
    )
}

@Composable
private fun ChatContent(
    session: ChatSession, uiState: ChatUiState, listState: LazyListState,
    onRetryMessage: () -> Unit, onDismissError: () -> Unit,
    onHideWelcomeCard: () -> Unit, onSendSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().testTag("chat_messages_list"),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (uiState.showWelcomeCard) {
            item(key = "welcome_card") {
                WelcomeCard(onConversationStarterClick = onSendSuggestion, modifier = Modifier.testTag("welcome_card"))
            }
        }

        if (session.messages.isNotEmpty() && session.messages.size % 5 == 0 && !uiState.showWelcomeCard) {
            item(key = "educational_prompt_${session.messages.size}") {
                EducationalPromptCard(onSendSuggestion = onSendSuggestion, messageCount = session.messages.size)
            }
        }

        items(items = session.messages, key = { it.id }) { message ->
            val messageIndex = session.messages.indexOf(message)
            if (listState.shouldRenderItem(messageIndex)) {
                MessageBubble(message = message, modifier = Modifier.testTag("message_${message.id}"))
            } else {
                Box(modifier = Modifier.height(PerformanceUtils.DefaultItemHeight).fillMaxWidth().background(Color.Transparent))
            }
        }

        if (uiState.showTypingIndicator) {
            item(key = "typing_indicator") { TypingIndicator(isVisible = true, modifier = Modifier.testTag("chat_typing_indicator")) }
        }

        if (uiState.isAiThinking && !uiState.isLoading) {
            item(key = "analyzing_indicator") {
                SimpleTypingIndicator(isVisible = true, message = "Analyzing image\u2026", modifier = Modifier.testTag("analyzing_image_indicator"))
            }
        }

        if (uiState.hasError) {
            item(key = "error_message") {
                ErrorMessage(message = uiState.error ?: "", onRetry = onRetryMessage, onDismiss = onDismissError, modifier = Modifier.testTag("error_message"))
            }
        }

        item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun EducationalPromptCard(onSendSuggestion: (String) -> Unit, messageCount: Int, modifier: Modifier = Modifier) {
    val prompts = remember {
        listOf(
            listOf("\uD83E\uDDEE Can you help me with a math problem?", "\uD83D\uDD2C Tell me something cool about science!", "\uD83D\uDCDA Help me understand this better", "\uD83C\uDFAF Give me a fun challenge!"),
            listOf("\uD83C\uDFA8 Let's be creative together!", "\uD83C\uDF0D Tell me about different countries", "\uD83E\uDD95 What's your favorite dinosaur fact?", "\uD83C\uDFB5 Do you know any fun songs?"),
            listOf("\uD83C\uDFB2 Let's play 20 questions!", "\uD83D\uDCD6 Help me write a short story", "\uD83E\uDDE9 Give me a riddle to solve", "\uD83C\uDF1F What's something amazing about space?")
        )
    }
    val promptSet = prompts[(messageCount / 5 - 1) % prompts.size]

    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "\uD83C\uDF89", fontSize = 32.sp)
            Text(
                text = "Great chatting! Want to try something new?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                promptSet.forEach { prompt ->
                    AssistChip(
                        onClick = { onSendSuggestion(prompt.substringAfter(" ")) },
                        label = { Text(text = prompt, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(message: String, onRetry: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "\uD83D\uDE05", fontSize = 32.sp)
            Text(
                text = message, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer, textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onRetry) { Text(text = "Try Again \uD83D\uDD04", color = MaterialTheme.colorScheme.primary) }
                TextButton(onClick = onDismiss) { Text(text = "Dismiss", color = MaterialTheme.colorScheme.onErrorContainer) }
            }
        }
    }
}
