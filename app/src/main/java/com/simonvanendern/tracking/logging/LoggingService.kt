package com.simonvanendern.tracking.logging

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.simonvanendern.tracking.R
import com.simonvanendern.tracking.database.DatabaseAggregator
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit


class LoggingService : Service() {
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    private lateinit var locationUpdates: LocationUpdates

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            Log.d("SERVICE_HANLDER", "got intent")

            if (msg.arg2 == -1) {
                // Do GPS Logging here
                val stepsLogger = StepsLogger(applicationContext)
                TransitionRecognition(applicationContext)
                locationUpdates = LocationUpdates(applicationContext)
                post(stepsLogger)


                // Aggregate the location data from time to time
                val aggregateDataWorkRequest = PeriodicWorkRequest.Builder(
                    DatabaseAggregator::class.java,
                    PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                    .build()
                WorkManager.getInstance().enqueue(aggregateDataWorkRequest)
                Log.d("LOGGING_SERVICE", "Started all services")

                try {
                    sleep(5000)
                } catch (e: InterruptedException) {

                }
            } else {
                Log.d("SERVICE_HANDLER", "intent is change location Updates")
                locationUpdates.setGranularity(msg.arg2)
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
            channelName, NotificationManager.IMPORTANCE_DEFAULT
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
//        Toast.makeText(this, "logging service starting", Toast.LENGTH_SHORT).show()

        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            msg.arg2 = intent.getIntExtra("granularity", -1)
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
            .setOngoing(true)
            .build()

        startForeground(1, notification)
//        Toast.makeText(this, "Foreground service running", Toast.LENGTH_SHORT).show()

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "logging service done", Toast.LENGTH_SHORT).show()
        Log.e("LOGGINGDESTROY", "Service unexpectedly destroyed while GPSLogger was running. Will send broadcast to RestarterReceiver.")
        val broadcastIntent = Intent(applicationContext, RestarterReceiver::class.java)
        sendBroadcast(broadcastIntent)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Toast.makeText(this, "logging service done with onTaskRemoved", Toast.LENGTH_SHORT).show()
        Log.e("LOGGINGDESTROY", "Service unexpectedly destroyed while GPSLogger was running. Will send broadcast to RestarterReceiver.")
        val broadcastIntent = Intent(applicationContext, RestarterReceiver::class.java)
        sendBroadcast(broadcastIntent)
    }
}