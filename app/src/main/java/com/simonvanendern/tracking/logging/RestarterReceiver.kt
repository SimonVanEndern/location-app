package com.simonvanendern.tracking.logging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat

class RestarterReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        println("RESTARTER ACTIVE")
        Toast.makeText(context, "Received boradccaast", Toast.LENGTH_SHORT).show()
        try {
            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                Log.d("RESTARTER", "Phone was restarted, starting service ...")
            } else {
                Log.d("RESTARTER", "Service was killed, try restarting ...")
            }

            val serviceIntent = Intent(context, LoggingService::class.java)

            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e:Exception) {
            Log.e("RESTARTER", "Error: $e")
        }
    }
}
