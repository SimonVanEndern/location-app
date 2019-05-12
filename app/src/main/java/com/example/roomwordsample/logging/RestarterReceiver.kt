package com.example.roomwordsample.logging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class RestarterReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("RESTARTER", "Service was killed, try restarting ...")

        val serviceIntent = Intent(context, LoggingService::class.java)

        ContextCompat.startForegroundService(context, serviceIntent)

    }
}
