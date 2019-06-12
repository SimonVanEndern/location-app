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
import com.simonvanendern.tracking.data_collection.ActivityTransitionRecognition
import com.simonvanendern.tracking.data_collection.LocationUpdates
import com.simonvanendern.tracking.data_collection.StepsLogger
import com.simonvanendern.tracking.repository.RequestRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.KeyPairGenerator
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.inject.Inject

/**
 * This service takes care of the services obtaining data (GPS, activity, steps)
 * running in the background even when the app is closed.
 * Therefore a non-dismissible status notification is displayed by this service.
 * The service also registers periodic work requests for locally aggregating the raw data
 * and polling for new aggregation requests at the server / sending the results back to the server.
 */
class BackgroundService : Service() {
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    private lateinit var locationUpdates: LocationUpdates

    @Inject
    lateinit var requestRepository: RequestRepository

    private var aggregateDataWorkRequest: PeriodicWorkRequest? = null
    private var serveServerRequests: PeriodicWorkRequest? = null
    private lateinit var notification: Notification

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        /**
         * Handles a message (intent) send to this service.
         * The message is either the command to start the service or a command
         * to change the frequency of GPS logging.
         */
        override fun handleMessage(msg: Message) {
            locationUpdates = LocationUpdates(applicationContext)
            if (msg.arg2 == -1) {
                // Do GPS Logging here
                val stepsLogger = StepsLogger(applicationContext)
                ActivityTransitionRecognition(applicationContext)
                post(stepsLogger)

                // Registers the periodic work request for aggregating local data
                if (aggregateDataWorkRequest == null) {
                    aggregateDataWorkRequest = PeriodicWorkRequest.Builder(
                        DatabaseAggregator::class.java,
                        PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                        TimeUnit.MILLISECONDS
                    ).build()
                }

                // Registers the periodic work request for polling the server
                if (serveServerRequests == null) {
                    serveServerRequests = PeriodicWorkRequest.Builder(
                        ServerRequestHandler::class.java,
                        PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                        TimeUnit.MILLISECONDS
                    ).build()
                }

                WorkManager.getInstance().enqueue(
                    listOf(
                        aggregateDataWorkRequest,
                        serveServerRequests
                    )
                )
            } else {
                locationUpdates.setGranularity(msg.arg2)
            }
        }
    }

    /**
     * Called once when the service is initiated. Invokes dependency injection,
     * registers the app installation with a public-private key-pair and
     * starts the handler for messages in a separate thread.
     */
    override fun onCreate() {
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(applicationContext))
            .build()
            .inject(this)

        GlobalScope.launch { setUpApp() }

        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    /**
     * Called whenever an intent is received.
     * On receiving the first intent ever, the status notification is created and displayed.
     * When the notification is already created, only the intent is handled.
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            msg.arg2 = intent.getIntExtra("granularity", -1)
            serviceHandler?.sendMessage(msg)
        }

        if (!this::notification.isInitialized) {
            notification = buildNotification(this)
            startForeground(1, notification)
        }

        return START_STICKY
    }

    /**
     * Builds a status notification
     */
    private fun buildNotification(context: Context): Notification {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val intent = Intent()
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.ticker_text))
            .setOngoing(true)
            .build()
    }

    /**
     * Builds a notification channel
     */
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

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * Either onTaskRemoved or onDestroy is called when the application is closed
     * e.g. through the task manager.
     * The periodic work requests are de-registered and an intent is sent
     * to a service that restarts the BackgroundService.
     */
    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "logging service done", Toast.LENGTH_SHORT).show()
        Log.e(
            "LOGGINGDESTROY",
            "Service unexpectedly destroyed while GPSLogger was running. Will send broadcast to RestarterReceiver."
        )

        if (aggregateDataWorkRequest != null) {
            WorkManager.getInstance().cancelWorkById(aggregateDataWorkRequest!!.id)
            aggregateDataWorkRequest = null
        }
        if (serveServerRequests != null) {
            WorkManager.getInstance().cancelWorkById(serveServerRequests!!.id)
            serveServerRequests = null
        }

        val broadcastIntent = Intent(applicationContext, RestarterReceiver::class.java)
        sendBroadcast(broadcastIntent)
    }

    /**
     * Either onTaskRemoved or onDestroy is called when the application is closed
     * e.g. through the task manager.
     * The periodic work requests are de-registered and an intent is sent
     * to a service that restarts the BackgroundService.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Toast.makeText(this, "logging service done with onTaskRemoved", Toast.LENGTH_SHORT).show()
        Log.e(
            "LOGGINGREMOVED",
            "Service unexpectedly destroyed while GPSLogger was running. Will send broadcast to RestarterReceiver."
        )

        if (aggregateDataWorkRequest != null) {
            WorkManager.getInstance().cancelWorkById(aggregateDataWorkRequest!!.id)
            aggregateDataWorkRequest = null
        }
        if (serveServerRequests != null) {
            WorkManager.getInstance().cancelWorkById(serveServerRequests!!.id)
            serveServerRequests = null
        }

        val broadcastIntent = Intent(applicationContext, RestarterReceiver::class.java)
        sendBroadcast(broadcastIntent)
    }

    // Should be refactored and moved to an encryption module
    /**
     * Creates a public-private key-pair if not present yet,
     * registers with the server and saves the returned password.
     */
    private fun setUpApp() {
        val store = getSharedPreferences(getString(R.string.identifiers), Context.MODE_PRIVATE)
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        val keyPair = generator.generateKeyPair()
        val public = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.encodeToString(keyPair.public.encoded, Base64.DEFAULT) +
                "-----END PUBLIC KEY-----"
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.public)
        if (!store.contains(getString(R.string.public_key))) {
            with(store.edit()) {
                putString(getString(R.string.public_key), Base64.encodeToString(keyPair.public.encoded, Base64.DEFAULT))
                putString("public_key_complete", public)
                putString(
                    getString(R.string.private_key),
                    Base64.encodeToString(keyPair.private.encoded, Base64.DEFAULT)
                )
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