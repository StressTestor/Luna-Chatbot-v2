package com.luna.chat.permissions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
actual fun rememberMicPermissionHandler(
    onPermissionGranted: () -> Unit,
): MicPermissionHandler {
    val context = LocalContext.current

    var status by remember {
        mutableStateOf(
            if (ContextCompat.checkSelfPermission(
                    context,
                    AppPermissions.SPEECH_RECOGNITION,
                ) == PackageManager.PERMISSION_GRANTED
            ) PermissionStatus.GRANTED else PermissionStatus.NOT_ASKED,
        )
    }
    var showRationaleDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        status = if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
        if (granted) onPermissionGranted()
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Microphone needed") },
            text = {
                Text(
                    "Luna needs the microphone to hear you. " +
                        "Please allow access in the next prompt.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    launcher.launch(AppPermissions.SPEECH_RECOGNITION)
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) { Text("Not now") }
            },
        )
    }

    return MicPermissionHandler(
        isAvailable = true,
        onClick = {
            when (status) {
                PermissionStatus.GRANTED -> onPermissionGranted()
                PermissionStatus.NOT_ASKED -> launcher.launch(AppPermissions.SPEECH_RECOGNITION)
                PermissionStatus.DENIED -> {
                    val activity = context.findActivity()
                    val shouldShowRationale = activity?.let {
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            it,
                            AppPermissions.SPEECH_RECOGNITION,
                        )
                    } ?: false

                    if (shouldShowRationale) {
                        showRationaleDialog = true
                    } else {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            },
                        )
                    }
                }
            }
        },
    )
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
