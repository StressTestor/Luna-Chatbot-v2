package com.luna.chat.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.chat.domain.entity.Theme
import com.luna.chat.presentation.theme.LunaTheme

/**
 * Theme selector component that displays available themes in a horizontal scrollable row
 */
@Composable
fun ThemeSelector(
    currentTheme: Theme,
    availableThemes: List<Theme>,
    onThemeSelect: (String) -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.testTag("theme_selector"),
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

/**
 * Individual theme option component with visual preview and selection state
 */
@Composable
fun ThemeOption(
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
    
    // Animate selection state
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "theme_scale"
    )
    
    val checkIconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 300),
        label = "check_scale"
    )
    
    Column(
        modifier = modifier
            .scale(scale)
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
                        .scale(checkIconScale)
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

/**
 * Theme preview card that shows a larger preview of a theme with its colors
 */
@Composable
fun ThemePreviewCard(
    theme: Theme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Color preview circles
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(theme.primaryColor))
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(theme.secondaryColor))
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(theme.backgroundColor))
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = theme.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = theme.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
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

@Preview(showBackground = true)
@Composable
private fun ThemeOptionPreview() {
    LunaTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            ThemeOption(
                theme = Theme.OCEAN_THEME,
                isSelected = false,
                onClick = { },
                isEnabled = true
            )
            ThemeOption(
                theme = Theme.FOREST_THEME,
                isSelected = true,
                onClick = { },
                isEnabled = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemePreviewCardPreview() {
    LunaTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            ThemePreviewCard(
                theme = Theme.SPACE_THEME,
                isSelected = false,
                onClick = { }
            )
            ThemePreviewCard(
                theme = Theme.SUNSET_THEME,
                isSelected = true,
                onClick = { }
            )
        }
    }
}