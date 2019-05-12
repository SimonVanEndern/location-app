package tracking.logging

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest

class LocationUpdates(private val context: Context) {
    private lateinit var mPendingIntent: PendingIntent
    private lateinit var locationProviderClient: FusedLocationProviderClient
    private lateinit var currentRequest: LocationRequest

    init {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {

            Log.i("LOCATION_UPDATES", "location permission available")

            locationProviderClient = FusedLocationProviderClient(context)
            currentRequest = LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(60000)
                .setFastestInterval(1000)
                .setMaxWaitTime(1000 * 60)

            val intent = Intent(context, LocationReceiver::class.java)
            mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

            val task = locationProviderClient.requestLocationUpdates(currentRequest, mPendingIntent)
            task.addOnSuccessListener {
                //            mPendingIntent.cancel()
//            Toast.makeText(context, "Started Activity Recognition", Toast.LENGTH_SHORT).show()
            }

            task.addOnFailureListener { e: Exception ->
                Toast.makeText(context, "Failed Location Updates", Toast.LENGTH_SHORT).show()
                Log.e("MYCOMPONENT", e.message)
            }
        } else {
            Log.e("LOCATION_UPDATES", "We have no location permission")
        }
    }


    fun setGranularity(granularity: Int) {
        Log.d("LOCATION_UPDATES", "start of granularity set function")
        Log.d("LOCATION_UPDATES", "current interval : ${currentRequest.interval}")
        Log.d("LOCATION_UPDATES", "requested granularity: $granularity")
        when (granularity) {
            0 -> {
                if (currentRequest.interval.toInt() == 1000 * 60) {
                    Log.d("LOCATION_UPDATES", "listener was already at one per minute")
                    return
                }
            }

            1 -> {

                if (currentRequest.interval.toInt() == 1000 * 5) {
                    Log.d("LOCATION_UPDATES", "Listener was already at every 5 seconds")
                    return
                }
            }

            2 -> {
                if (currentRequest.interval.toInt() == 1000) {
                    Log.d("LOCATION_UPDATES", "Listener was already at one per second")
                    return
                }
            }
        }

        Log.d("LOCATION_UPDATES", "Sending change location update frequency update")

        Toast.makeText(context, "Changed location listener update frequency", Toast.LENGTH_LONG).show()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            currentRequest = LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

            when (granularity) {
                0 -> {
                    // Device is idle
                    currentRequest.setInterval(1000 * 60).maxWaitTime = 1000 * 60 * 10
                    Log.d("LOCATION_UPDATES", "Setting interval to one per minute")
                }

                1 -> {
                    // Device is in state walking
                    currentRequest.setInterval(1000 * 5).maxWaitTime = 1000 * 60
                    Log.d("LOCATION_UPDATES", "Setting interval to one per 5 seconds")
                }

                2 -> {
                    // Device is in any other state
                    currentRequest.interval = 1000
                    currentRequest.maxWaitTime = 1000 * 20
                    Log.d("LOCATION_UPDATES", "Setting interval to one every ${currentRequest.interval/1000}")
                }
            }

            // Make sure, no locations are lost, not sure if necessary
            locationProviderClient.flushLocations()

            val task = locationProviderClient.requestLocationUpdates(currentRequest, mPendingIntent)
            task.addOnSuccessListener {
                Log.i("LOCATIONUPDATES", "Successfully Changed accuracy to every ${currentRequest.interval / 1000} seconds")
            }
            task.addOnFailureListener { e: Exception ->
                Toast.makeText(context, "Failed Location Updates", Toast.LENGTH_SHORT).show()
                Log.e("MYCOMPONENT", e.message)
            }
        } else {
            Log.e("LOCATION_UPDATES", "We have no location permission")
        }
    }
}