package com.luna.chat.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
class MicPermissionHandler(
    val isAvailable: Boolean,
    val onClick: () -> Unit,
)

@Composable
expect fun rememberMicPermissionHandler(
    onPermissionGranted: () -> Unit,
): MicPermissionHandler
