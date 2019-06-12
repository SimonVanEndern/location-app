package com.simonvanendern.tracking.backgroundService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Receives a Broadcast when the BackgroundService is closed
 * and tries to restart the service.
 * This is especially useful in order to keep the service
 * running when the app is closed
 */
class RestarterReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                Log.d("RESTARTER", "Phone was restarted, starting service ...")
            } else {
                Log.d("RESTARTER", "Service was killed, try restarting ...")
            }

            val serviceIntent = Intent(context, BackgroundService::class.java)

            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e:Exception) {
            Log.e("RESTARTER", "Error: $e")
        }
    }
}
