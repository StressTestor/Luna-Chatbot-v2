package com.luna.chat.update.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.luna.chat.update.manager.UpdateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver that listens for update notifications.
 * This receiver is triggered when an update is available and initiates the update process.
 */
class UpdateReceiver : BroadcastReceiver() {
    companion object {
        /**
         * Intent action for update availability
         */
        const val ACTION_UPDATE_AVAILABLE = "com.luna.chat.UPDATE_AVAILABLE"
        
        /**
         * Intent extra key for update path
         */
        const val EXTRA_UPDATE_PATH = "update_path"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_UPDATE_AVAILABLE) {
            val updatePath = intent.getStringExtra(EXTRA_UPDATE_PATH)
            if (updatePath != null) {
                // Launch a coroutine to process the update
                CoroutineScope(Dispatchers.IO).launch {
                    UpdateManager.getInstance(context).processUpdate(updatePath)
                }
            }
        }
    }
}