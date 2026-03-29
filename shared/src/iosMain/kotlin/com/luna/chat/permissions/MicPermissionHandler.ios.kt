package com.luna.chat.permissions

import androidx.compose.runtime.Composable

@Composable
actual fun rememberMicPermissionHandler(
    onPermissionGranted: () -> Unit,
): MicPermissionHandler = MicPermissionHandler(
    isAvailable = false,
    onClick = {},
)
