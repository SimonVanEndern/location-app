package com.simonvanendern.tracking.backgroundService

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.simonvanendern.tracking.ApplicationModule
import com.simonvanendern.tracking.DaggerApplicationComponent
import com.simonvanendern.tracking.R
import com.simonvanendern.tracking.aggregation.DatabaseAggregator
import com.simonvanendern.tracking.aggregation.ServerRequestHandler
import com.simonvanendern.tracking.logging.LocationUpdates
import com.simonvanendern.tracking.logging.StepsLogger
import com.simonvanendern.tracking.logging.TransitionRecognition
import com.simonvanendern.tracking.repository.RequestRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okio.ByteString
import java.io.FileOutputStream
import java.lang.Thread.sleep
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator
import java.security.spec.X509EncodedKeySpec
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.inject.Inject

class BackgroundLoggingService : Service() {
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    private lateinit var locationUpdates: LocationUpdates

    @Inject
    lateinit var requestRepository: RequestRepository

    private lateinit var aggregateDataWorkRequest: PeriodicWorkRequest
    private lateinit var serveServerRequests: PeriodicWorkRequest

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            Log.d("SERVICE_HANLDER", "got intent")

            locationUpdates = LocationUpdates(applicationContext)
            if (msg.arg2 == -1) {
                // Do GPS Logging here
                val stepsLogger = StepsLogger(applicationContext)
                TransitionRecognition(applicationContext)
                post(stepsLogger)


                // Aggregate the location data from time to time
                aggregateDataWorkRequest = PeriodicWorkRequest.Builder(
                    DatabaseAggregator::class.java,
                    PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                    TimeUnit.MILLISECONDS
                ).build()

                serveServerRequests = PeriodicWorkRequest.Builder(
                    ServerRequestHandler::class.java,
                    PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                    TimeUnit.MILLISECONDS
                ).build()

                WorkManager.getInstance().enqueue(
                    listOf(
                        aggregateDataWorkRequest,
                        serveServerRequests
                    )
                )
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
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(applicationContext))
            .build()
            .inject(this)

        GlobalScope.launch { setUpApp() }

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
        Log.e(
            "LOGGINGDESTROY",
            "Service unexpectedly destroyed while GPSLogger was running. Will send broadcast to RestarterReceiver."
        )

        WorkManager.getInstance().cancelWorkById(aggregateDataWorkRequest.id)
        WorkManager.getInstance().cancelWorkById(serveServerRequests.id)

        val broadcastIntent = Intent(applicationContext, RestarterReceiver::class.java)
        sendBroadcast(broadcastIntent)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Toast.makeText(this, "logging service done with onTaskRemoved", Toast.LENGTH_SHORT).show()
        Log.e(
            "LOGGINGDESTROY",
            "Service unexpectedly destroyed while GPSLogger was running. Will send broadcast to RestarterReceiver."
        )

        WorkManager.getInstance().cancelWorkById(aggregateDataWorkRequest.id)
        WorkManager.getInstance().cancelWorkById(serveServerRequests.id)

        val broadcastIntent = Intent(applicationContext, RestarterReceiver::class.java)
        sendBroadcast(broadcastIntent)
    }

    private fun setUpApp() {
        val store = getSharedPreferences(getString(R.string.identifiers), Context.MODE_PRIVATE)
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        val keyPair = generator.generateKeyPair()
        val private = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.encodeToString(keyPair.private.encoded, 0) +
                "-----END PRIVATE KEY-----"
        val format_privat = keyPair.private.format
        val format_public = keyPair.public.format
        val public = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.encodeToString(keyPair.public.encoded, Base64.DEFAULT) +
                "-----END PUBLIC KEY-----"
//        val cipher = Cipher.getInstance("RSA/None/PKCS1Padding")
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.public)
        val encrypted = cipher.doFinal("testIng".toByteArray())
        val encryptedUtf8 = String(encrypted, StandardCharsets.UTF_8)
        val encryptedString = Base64.encodeToString(encrypted, 0)
        val spec = X509EncodedKeySpec(keyPair.public.encoded).encoded
        val specs = Base64.encodeToString(spec, 0)
        if (!store.contains(getString(R.string.public_key))) {
            with(store.edit()) {
                putString(getString(R.string.public_key), Base64.encodeToString(keyPair.public.encoded, Base64.DEFAULT))
                putString("public_key_complete", public)
                putString(getString(R.string.private_key), Base64.encodeToString(keyPair.private.encoded, Base64.DEFAULT))
                apply()
            }
        }
        if (!store.contains("password")) {
            val user = requestRepository.createUser(
                store.getString("public_key_complete", null)!!
            )
            if (user != null) {
                with(store.edit()) {
                    putString(getString(R.string.password), user.pw)
                    apply()
                }
            }
        }
    }
}