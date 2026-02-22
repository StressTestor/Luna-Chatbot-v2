package com.luna.chat.platform

actual object PlatformLogger {
    actual fun debug(tag: String, message: String) { println("DEBUG [$tag]: $message") }
    actual fun info(tag: String, message: String) { println("INFO [$tag]: $message") }
    actual fun error(tag: String, message: String, throwable: Throwable?) {
        println("ERROR [$tag]: $message")
        throwable?.let { println("  Exception: ${it.message}") }
    }
}
