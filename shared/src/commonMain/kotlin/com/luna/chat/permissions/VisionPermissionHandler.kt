package com.luna.chat.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
class VisionPermissionHandler(
    val onClick: () -> Unit,
)

@Composable
expect fun rememberVisionPermissionHandler(
    onPermissionsGranted: () -> Unit,
): VisionPermissionHandler
