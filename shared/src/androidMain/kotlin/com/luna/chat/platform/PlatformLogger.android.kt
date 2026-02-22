package com.luna.chat.platform

import android.util.Log

actual object PlatformLogger {
    actual fun debug(tag: String, message: String) { Log.d(tag, message) }
    actual fun info(tag: String, message: String) { Log.i(tag, message) }
    actual fun error(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) Log.e(tag, message, throwable) else Log.e(tag, message)
    }
}
