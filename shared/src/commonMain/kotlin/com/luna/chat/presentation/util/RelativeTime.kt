package com.luna.chat.presentation.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatRelativeTime(timestampMs: Long): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val diffMs = now - timestampMs
    val diffSec = diffMs / 1000
    val diffMin = diffSec / 60
    val diffHour = diffMin / 60
    val diffDay = diffHour / 24

    return when {
        diffMin < 1 -> "Just now"
        diffMin < 60 -> "${diffMin}m ago"
        diffHour < 24 -> "${diffHour}h ago"
        diffDay < 2 -> "Yesterday"
        diffDay < 7 -> "${diffDay}d ago"
        else -> {
            val dt = Instant.fromEpochMilliseconds(timestampMs)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            "${months[dt.monthNumber - 1]} ${dt.dayOfMonth}"
        }
    }
}
