package com.luna.chat.platform

import platform.Foundation.NSUserDefaults
import platform.Security.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value

@OptIn(ExperimentalForeignApi::class)
actual class SecureStorage {
    // Simplified implementation using NSUserDefaults for now
    // TODO: Replace with Keychain for production
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String): String? = defaults.stringForKey(key)
    actual fun putString(key: String, value: String) { defaults.setObject(value, key) }
    actual fun remove(key: String) { defaults.removeObjectForKey(key) }
    actual fun contains(key: String): Boolean = defaults.objectForKey(key) != null
}
