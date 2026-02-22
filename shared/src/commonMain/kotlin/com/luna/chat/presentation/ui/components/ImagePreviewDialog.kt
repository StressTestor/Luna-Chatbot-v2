package com.luna.chat.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.luna.chat.domain.util.ImageValidator

@Composable
fun ImagePreviewDialog(
    imageBytes: ByteArray,
    mimeType: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isValid = remember(imageBytes, mimeType) {
        try {
            ImageValidator.validate(imageBytes, mimeType)
        } catch (e: Exception) {
            false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("image_preview_dialog"),
        title = {
            Text(
                text = "Preview image",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Image selected (${imageBytes.size / 1024} KB)",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (!isValid) {
                    Text(
                        text = "Image not accepted. Please choose a smaller JPG, PNG, or WEBP.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = isValid) {
                Text(text = "Use this image")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
