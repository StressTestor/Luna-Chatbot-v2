package com.luna.chat.permissions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

enum class PermissionStatus { GRANTED, DENIED, NOT_ASKED }

@Stable
class PermissionState(
    val status: PermissionStatus,
    val launchRequest: () -> Unit,
)

@Composable
fun rememberPermissionState(
    permission: String,
    onResult: (Boolean) -> Unit = {},
): PermissionState {
    var status by remember { mutableStateOf(PermissionStatus.NOT_ASKED) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        status = if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
        onResult(granted)
    }

    return PermissionState(
        status = status,
        launchRequest = { launcher.launch(permission) },
    )
}

@Composable
fun rememberMultiplePermissionsState(
    permissions: List<String>,
    onResult: (Map<String, Boolean>) -> Unit = {},
): List<PermissionState> {
    var statuses by remember {
        mutableStateOf(permissions.associateWith { PermissionStatus.NOT_ASKED })
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        statuses = results.mapValues { (_, granted) ->
            if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
        }
        onResult(results)
    }

    return permissions.map { permission ->
        PermissionState(
            status = statuses[permission] ?: PermissionStatus.NOT_ASKED,
            launchRequest = { launcher.launch(permissions.toTypedArray()) },
        )
    }
}
