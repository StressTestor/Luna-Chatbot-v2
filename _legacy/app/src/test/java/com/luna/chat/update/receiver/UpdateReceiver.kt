package com.luna.chat.update.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Unit-test-only no-op stub for UpdateReceiver to avoid Hilt ASM instrumentation during unit tests.
 * This class is intentionally minimal and does NOT use @AndroidEntryPoint.
 * It prevents instrumentation from trying to transform a non-existent generated Hilt class in unit tests.
 */
class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // no-op for unit tests
    }
}