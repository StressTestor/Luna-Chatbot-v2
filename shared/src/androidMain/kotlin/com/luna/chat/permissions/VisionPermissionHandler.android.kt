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
actual fun rememberVisionPermissionHandler(
    onPermissionsGranted: () -> Unit,
): VisionPermissionHandler {
    val context = LocalContext.current
    val permissions = AppPermissions.VISION_PERMISSIONS

    var statuses by remember {
        mutableStateOf(
            permissions.associateWith { permission ->
                if (ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
                ) PermissionStatus.GRANTED else PermissionStatus.NOT_ASKED
            },
        )
    }
    var showRationaleDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        statuses = results.mapValues { (_, granted) ->
            if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
        }
        if (results.values.all { it }) onPermissionsGranted()
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Camera access needed") },
            text = {
                Text(
                    "Luna needs camera and photo access to analyse images you send. " +
                        "Please allow access in the next prompt.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    launcher.launch(permissions.toTypedArray())
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) { Text("Not now") }
            },
        )
    }

    return VisionPermissionHandler(
        onClick = {
            val allGranted = statuses.values.all { it == PermissionStatus.GRANTED }
            if (allGranted) {
                onPermissionsGranted()
                return@VisionPermissionHandler
            }

            val activity = context.findActivity()
            val anyNeedRationale = permissions.any { permission ->
                statuses[permission] == PermissionStatus.DENIED &&
                    (activity?.let {
                        ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
                    } ?: false)
            }

            if (anyNeedRationale) {
                showRationaleDialog = true
            } else {
                val anyPermanentlyDenied = permissions.any { permission ->
                    statuses[permission] == PermissionStatus.DENIED
                }
                if (anyPermanentlyDenied) {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        },
                    )
                } else {
                    launcher.launch(permissions.toTypedArray())
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
