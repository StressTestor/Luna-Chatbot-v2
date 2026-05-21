package com.luna.chat.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberImagePicker(
    onImagePicked: (bytes: ByteArray, mimeType: String) -> Unit,
): ImagePicker = ImagePicker(onClick = {})
