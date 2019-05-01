package com.example.roomwordsample.logging

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

    init {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {

            Log.i("LOCATION_UPDATES", "location permission available")

            val locationClient = FusedLocationProviderClient(context)
            val request = LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(1000)
                .setMaxWaitTime(1000 * 60)

            val intent = Intent(context, LocationReceiver::class.java)
            mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

            val task = locationClient.requestLocationUpdates(request, mPendingIntent)
            task.addOnSuccessListener {
                //            mPendingIntent.cancel()
//            Toast.makeText(context, "Started Activity Recognition", Toast.LENGTH_SHORT).show()
            }

            task.addOnFailureListener { e: Exception ->
                Toast.makeText(context, "Failed Location Updates", Toast.LENGTH_SHORT).show()
                Log.e("MYCOMPONENT", e.message)
            }
        } else {
            Log.e("LOCATIOIN_UPDATES", "We have no location permission")
        }
    }
}