package com.example.roomwordsample.logging

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.roomwordsample.R
import java.lang.Thread.sleep


class LoggingService : Service() {
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {

            // Do GPS Logging here
            val stepsLogger = StepsLogger(applicationContext)
            TransitionRecognition(applicationContext)
            post(stepsLogger)
            Log.d("LOGGING_SERVICE", "Started all services")

            try {
                sleep(5000)
            } catch (e: InterruptedException) {

            }

//            stopSelf(msg.arg1)
        }
    }

    override fun onCreate() {
//        super.onCreate()
//        Toast.makeText(this, "logging service created", Toast.LENGTH_SHORT).show()

        Log.d("FOREGROUND", "created")
        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)


        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        Toast.makeText(this, "logging service starting", Toast.LENGTH_SHORT).show()

        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val intent = Intent()
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)


        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.ticker_text))
            .build()

        startForeground(1, notification)
//        Toast.makeText(this, "Foreground service running", Toast.LENGTH_SHORT).show()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        Toast.makeText(this, "logging service done", Toast.LENGTH_SHORT).show()
    }
}