package com.simonvanendern.tracking.data_collection

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest

/**
 * Registers for GPS / location updates
 */
class LocationUpdates(private val context: Context) {
    private lateinit var mPendingIntent: PendingIntent
    private lateinit var locationProviderClient: FusedLocationProviderClient
    private lateinit var currentRequest: LocationRequest

    private val FREQUENCY_HIGH = 1000
    private val FREQUENCY_MEDIUM = 1000 * 5
    private val FREQUENCY_LOW = 1000 * 60

    init {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationProviderClient = FusedLocationProviderClient(context)
            val intent = Intent(context, LocationReceiver::class.java)
            mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

            setGranularity(0)
        } else {
            Log.e("LOCATION_UPDATES", "We have no location permission")
        }
    }

    /**
     * Sets the LocationRequest to receive updates at a frequency according to
     * @param granularity 0 means low frequency, 1 medium, 2 high
     */
    fun setGranularity(granularity: Int) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            currentRequest = LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(FREQUENCY_HIGH.toLong())

            // Setting the frequency to the requested frequency.
            // If the frequency is already at the requested level, we just return
            when (granularity) {
                0 -> {
                    if (currentRequest.interval.toInt() == FREQUENCY_LOW) {
                        return
                    } else {
                        currentRequest.setInterval(FREQUENCY_LOW.toLong()).maxWaitTime = 1000 * 60 * 10
                    }
                }

                1 -> {
                    if (currentRequest.interval.toInt() == FREQUENCY_MEDIUM) {
                        return
                    } else {
                        currentRequest.setInterval(FREQUENCY_MEDIUM.toLong()).maxWaitTime = 1000 * 60
                    }
                }

                2 -> {
                    if (currentRequest.interval.toInt() == FREQUENCY_HIGH) {
                        return
                    } else {
                        currentRequest.setInterval(FREQUENCY_HIGH.toLong()).maxWaitTime = 1000 * 20
                    }
                }
            }

            // Make sure, no locations are lost, not sure if necessary
            locationProviderClient.flushLocations()

            val task = locationProviderClient.requestLocationUpdates(currentRequest, mPendingIntent)
            task.addOnFailureListener { e: Exception ->
                Log.e("LOCATION_UPDATES", "We have no location permission")
            }
        } else {
            Log.e("LOCATION_UPDATES", "We have no location permission")
        }
    }
}