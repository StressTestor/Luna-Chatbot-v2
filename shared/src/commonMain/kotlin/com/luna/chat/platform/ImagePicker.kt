package com.luna.chat.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
class ImagePicker(
    val onClick: () -> Unit,
)

@Composable
expect fun rememberImagePicker(
    onImagePicked: (bytes: ByteArray, mimeType: String) -> Unit,
): ImagePicker
