package com.luna.chat.platform

expect object PlatformLogger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}
