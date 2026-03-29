package com.luna.chat.permissions

import androidx.compose.runtime.Composable

@Composable
actual fun rememberVisionPermissionHandler(
    onPermissionsGranted: () -> Unit,
): VisionPermissionHandler = VisionPermissionHandler(onClick = {})
