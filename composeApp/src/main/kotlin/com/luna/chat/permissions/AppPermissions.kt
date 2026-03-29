package com.luna.chat.permissions

import android.Manifest
import android.os.Build

object AppPermissions {
    const val SPEECH_RECOGNITION = Manifest.permission.RECORD_AUDIO

    val VISION_PERMISSIONS: List<String> = buildList {
        add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}
