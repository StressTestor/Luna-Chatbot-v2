package com.luna.chat.presentation.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luna.chat.domain.entity.Theme
import com.luna.chat.presentation.theme.LunaTheme
import com.luna.chat.presentation.viewmodel.SettingsViewModel
import com.luna.chat.presentation.viewmodel.SettingsUiState
import com.luna.chat.data.repository.UserPreferences
import com.luna.chat.presentation.ui.components.ThemeSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    
    // Handle success and error messages
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.dismissSuccessMessage()
        }
    }
    
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            kotlinx.coroutines.delay(5000)
            viewModel.dismissError()
        }
    }
    
    Scaffold(
        modifier = modifier.testTag("settings_screen"),
        topBar = {
            SettingsTopBar(
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SettingsContent(
                uiState = uiState,
                userPreferences = userPreferences,
                currentTheme = currentTheme,
                availableThemes = viewModel.availableThemes,
                onThemeSelect = viewModel::selectTheme,
                onClearChatHistory = viewModel::clearChatHistory,
                onToggleParentalControls = viewModel::updateParentalControls,
                onToggleContentFilter = viewModel::updateContentFilter,
                onToggleVoiceInput = viewModel::updateVoiceInput,
                onUpdateAutoClearDays = viewModel::updateAutoClearHistoryDays,
                onShowApiKeyDialog = viewModel::showApiKeyDialog,
                onTestApiConnection = viewModel::testApiConnection,
                onResetToDefaults = viewModel::resetToDefaults,
                modifier = Modifier.fillMaxSize()
            )
            
            // API Key Configuration Dialog
            if (uiState.showApiKeyDialog) {
                ApiKeyConfigurationDialog(
                    onDismiss = viewModel::hideApiKeyDialog,
                    onConfigureApiKey = viewModel::configureApiKey,
                    isLoading = uiState.isLoading,
                    isParentPasswordSetup = viewModel.isParentPasswordSetup()
                )
            }
            
            // Parent Password Setup Dialog
            if (uiState.showParentSetupDialog) {
                ParentPasswordSetupDialog(
                    onDismiss = viewModel::hideParentSetupDialog,
                    onSetupPassword = viewModel::setupParentPassword,
                    isLoading = uiState.isLoading
                )
            }
            
            // Success/Error Messages
            AnimatedVisibility(
                visible = uiState.hasSuccessMessage || uiState.hasError,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(300)
                ),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                MessageCard(
                    message = uiState.successMessage ?: uiState.error ?: "",
                    isError = uiState.hasError,
                    onDismiss = if (uiState.hasError) viewModel::dismissError else viewModel::dismissSuccessMessage,
                    modifier = Modifier
                        .padding(16.dp)
                        .testTag(if (uiState.hasError) "error_message" else "success_message")
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⚙️",
                    fontSize = 24.sp
                )
                Text(
                    text = "Settings",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .testTag("back_button")
                    .semantics {
                        contentDescription = "Go back to chat"
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.testTag("settings_top_bar")
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    userPreferences: UserPreferences,
    currentTheme: Theme,
    availableThemes: List<Theme>,
    onThemeSelect: (String) -> Unit,
    onClearChatHistory: () -> Unit,
    onToggleParentalControls: (Boolean) -> Unit,
    onToggleContentFilter: (Boolean) -> Unit,
    onToggleVoiceInput: (Boolean) -> Unit,
    onUpdateAutoClearDays: (Int) -> Unit,
    onShowApiKeyDialog: () -> Unit,
    onTestApiConnection: () -> Unit,
    onResetToDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Theme Selection Section
        item(key = "theme_section") {
            SettingsSection(
                title = "Choose Your Theme 🎨",
                description = "Pick colors that make you happy!"
            ) {
                ThemeSelector(
                    currentTheme = currentTheme,
                    availableThemes = availableThemes,
                    onThemeSelect = onThemeSelect,
                    isEnabled = !uiState.isLoading,
                    modifier = Modifier.testTag("theme_selector")
                )
            }
        }
        
        // Chat Settings Section
        item(key = "chat_section") {
            SettingsSection(
                title = "Chat Settings 💬",
                description = "Customize your chat experience"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Clear Chat History
                    SettingsActionCard(
                        title = "Clear Chat History",
                        description = "Start fresh with a clean slate! 🗑️",
                        icon = Icons.Default.Delete,
                        onClick = onClearChatHistory,
                        isEnabled = !uiState.isLoading,
                        modifier = Modifier.testTag("clear_history_button")
                    )
                    
                    // Voice Input Toggle
                    SettingsToggleCard(
                        title = "Voice Input",
                        description = "Talk to Luna with your voice! 🎤",
                        icon = Icons.Default.Mic,
                        isChecked = userPreferences.voiceInputEnabled,
                        onToggle = onToggleVoiceInput,
                        isEnabled = !uiState.isLoading,
                        modifier = Modifier.testTag("voice_input_toggle")
                    )
                    
                    // Auto-clear History Setting
                    AutoClearHistoryCard(
                        currentDays = userPreferences.autoClearHistoryDays,
                        onUpdateDays = onUpdateAutoClearDays,
                        isEnabled = !uiState.isLoading,
                        modifier = Modifier.testTag("auto_clear_setting")
                    )
                }
            }
        }
        
        // Safety Settings Section
        item(key = "safety_section") {
            SettingsSection(
                title = "Safety Settings 🛡️",
                description = "Keep your chats safe and fun"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Content Filter Toggle
                    SettingsToggleCard(
                        title = "Content Filter",
                        description = "Keep conversations appropriate! 🌟",
                        icon = Icons.Default.Shield,
                        isChecked = userPreferences.contentFilterEnabled,
                        onToggle = onToggleContentFilter,
                        isEnabled = !uiState.isLoading,
                        modifier = Modifier.testTag("content_filter_toggle")
                    )
                    
                    // Parental Controls Toggle
                    SettingsToggleCard(
                        title = "Parental Controls",
                        description = "Extra safety for young users! 👨‍👩‍👧‍👦",
                        icon = Icons.Default.FamilyRestroom,
                        isChecked = userPreferences.parentalControlsEnabled,
                        onToggle = onToggleParentalControls,
                        isEnabled = !uiState.isLoading,
                        modifier = Modifier.testTag("parental_controls_toggle")
                    )
                }
            }
        }
        
        // API Configuration Section (Parent-only)
        item(key = "api_section") {
            SettingsSection(
                title = "API Configuration 🔧",
                description = "For grown-ups only!"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // API Key Status
                    ApiKeyStatusCard(
                        isConfigured = userPreferences.apiKeyConfigured,
                        onConfigureClick = onShowApiKeyDialog,
                        onTestClick = onTestApiConnection,
                        isEnabled = !uiState.isLoading,
                        modifier = Modifier.testTag("api_key_status")
                    )
                }
            }
        }
        
        // Advanced Settings Section
        item(key = "advanced_section") {
            SettingsSection(
                title = "Advanced Settings ⚡",
                description = "For power users"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reset to Defaults
                    SettingsActionCard(
                        title = "Reset to Defaults",
                        description = "Start over with original settings! 🔄",
                        icon = Icons.Default.RestartAlt,
                        onClick = onResetToDefaults,
                        isEnabled = !uiState.isLoading,
                        isDestructive = true,
                        modifier = Modifier.testTag("reset_defaults_button")
                    )
                }
            }
        }
        
        // App Info Section
        item(key = "info_section") {
            AppInfoCard(
                totalMessages = userPreferences.totalMessagesSent,
                experienceLevel = userPreferences.getUserExperienceLevel(),
                modifier = Modifier.testTag("app_info")
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

@Composable
private fun ThemeSelector(
    currentTheme: Theme,
    availableThemes: List<Theme>,
    onThemeSelect: (String) -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(
            items = availableThemes,
            key = { theme -> theme.id }
        ) { theme ->
            ThemeOption(
                theme = theme,
                isSelected = theme.id == currentTheme.id,
                onClick = { onThemeSelect(theme.id) },
                isEnabled = isEnabled,
                modifier = Modifier.testTag("theme_${theme.id}")
            )
        }
    }
}

@Composable
private fun ThemeOption(
    theme: Theme,
    isSelected: Boolean,
    onClick: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }
    
    Column(
        modifier = modifier
            .clickable(enabled = isEnabled) { onClick() }
            .semantics {
                contentDescription = "Select ${theme.displayName} theme"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = borderColor,
                    shape = CircleShape
                )
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(theme.primaryColor),
                            Color(theme.secondaryColor)
                        )
                    ),
                    shape = CircleShape
                )
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            }
        }
        
        Text(
            text = theme.displayName,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDestructive) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggleCard(
    title: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = isChecked,
                onCheckedChange = onToggle,
                enabled = isEnabled,
                modifier = Modifier.semantics {
                    contentDescription = "$title toggle"
                }
            )
        }
    }
}

@Composable
private fun AutoClearHistoryCard(
    currentDays: Int,
    onUpdateDays: (Int) -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) { showDialog = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Auto-Clear History",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Clear chats after $currentDays days 🗓️",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
    
    if (showDialog) {
        AutoClearDaysDialog(
            currentDays = currentDays,
            onDismiss = { showDialog = false },
            onConfirm = { days ->
                onUpdateDays(days)
                showDialog = false
            }
        )
    }
}

@Composable
private fun ApiKeyStatusCard(
    isConfigured: Boolean,
    onConfigureClick: () -> Unit,
    onTestClick: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isConfigured) Icons.Default.Key else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isConfigured) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.size(24.dp)
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "API Key Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isConfigured) {
                            "API key is configured! ✅"
                        } else {
                            "API key needs setup! ⚠️"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isConfigured) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onConfigureClick,
                    enabled = isEnabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isConfigured) "Update Key" else "Setup Key",
                        fontSize = 12.sp
                    )
                }
                
                if (isConfigured) {
                    OutlinedButton(
                        onClick = onTestClick,
                        enabled = isEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Test Connection",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppInfoCard(
    totalMessages: Int,
    experienceLevel: com.luna.chat.data.repository.UserExperienceLevel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🌙",
                    fontSize = 32.sp
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Luna Chat Stats",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Your chat journey so far!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Messages",
                    value = totalMessages.toString(),
                    emoji = "💬"
                )
                
                StatItem(
                    label = "Level",
                    value = when (experienceLevel) {
                        com.luna.chat.data.repository.UserExperienceLevel.NEW -> "New"
                        com.luna.chat.data.repository.UserExperienceLevel.BEGINNER -> "Beginner"
                        com.luna.chat.data.repository.UserExperienceLevel.INTERMEDIATE -> "Explorer"
                        com.luna.chat.data.repository.UserExperienceLevel.EXPERIENCED -> "Expert"
                    },
                    emoji = when (experienceLevel) {
                        com.luna.chat.data.repository.UserExperienceLevel.NEW -> "🌱"
                        com.luna.chat.data.repository.UserExperienceLevel.BEGINNER -> "🌿"
                        com.luna.chat.data.repository.UserExperienceLevel.INTERMEDIATE -> "🌳"
                        com.luna.chat.data.repository.UserExperienceLevel.EXPERIENCED -> "🏆"
                    }
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AutoClearDaysDialog(
    currentDays: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDays by remember { mutableStateOf(currentDays) }
    val dayOptions = listOf(1, 7, 14, 30, 60, 90, 180, 365)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Auto-Clear History 🗓️",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Choose how many days to keep your chat history:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(dayOptions) { days ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDays = days }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = selectedDays == days,
                                onClick = { selectedDays = days }
                            )
                            Text(
                                text = when (days) {
                                    1 -> "1 day"
                                    7 -> "1 week"
                                    14 -> "2 weeks"
                                    30 -> "1 month"
                                    60 -> "2 months"
                                    90 -> "3 months"
                                    180 -> "6 months"
                                    365 -> "1 year"
                                    else -> "$days days"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedDays) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ApiKeyConfigurationDialog(
    onDismiss: () -> Unit,
    onConfigureApiKey: (String, String) -> Unit,
    isLoading: Boolean,
    isParentPasswordSetup: Boolean,
    modifier: Modifier = Modifier
) {
    var apiKey by remember { mutableStateOf("") }
    var parentPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "API Configuration 🔧",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "This section is for grown-ups only! Please ask a parent or guardian to help configure the API key.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = parentPassword,
                    onValueChange = { parentPassword = it },
                    label = { Text("Parent Password") },
                    visualTransformation = if (showPassword) {
                        androidx.compose.ui.text.input.VisualTransformation.None
                    } else {
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { showPassword = !showPassword }
                        ) {
                            Icon(
                                imageVector = if (showPassword) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("OpenRouter API Key") },
                    placeholder = { Text("<your OpenRouter API key>") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "💡 Get your API key from openrouter.ai",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfigureApiKey(apiKey, parentPassword) },
                enabled = !isLoading && apiKey.isNotBlank() && parentPassword.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Configure")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ParentPasswordSetupDialog(
    onDismiss: () -> Unit,
    onSetupPassword: (String, String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Setup Parent Password 👨‍👩‍👧‍👦",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create a password to protect sensitive settings. This password will be required for API configuration and other parent-only features.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Parent Password") },
                    visualTransformation = if (showPassword) {
                        androidx.compose.ui.text.input.VisualTransformation.None
                    } else {
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { showPassword = !showPassword }
                        ) {
                            Icon(
                                imageVector = if (showPassword) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = if (showPassword) {
                        androidx.compose.ui.text.input.VisualTransformation.None
                    } else {
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "💡 Password must be at least 4 characters and contain at least one number",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSetupPassword(password, confirmPassword) },
                enabled = !isLoading && password.isNotBlank() && confirmPassword.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Setup")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onDismiss
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = if (isError) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    LunaTheme {
        SettingsScreen(
            onBackClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeSelectorPreview() {
    LunaTheme {
        ThemeSelector(
            currentTheme = Theme.RAINBOW_THEME,
            availableThemes = Theme.getAllThemes(),
            onThemeSelect = { },
            isEnabled = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}