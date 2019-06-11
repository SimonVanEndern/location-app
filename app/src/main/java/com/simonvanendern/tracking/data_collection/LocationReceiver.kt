package com.simonvanendern.tracking.data_collection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationResult
import com.simonvanendern.tracking.ApplicationModule
import com.simonvanendern.tracking.DaggerApplicationComponent
import com.simonvanendern.tracking.repository.GPSRepository
import javax.inject.Inject

class LocationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var locationRepository: GPSRepository

    override fun onReceive(context: Context, intent: Intent) {
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(context))
            .build()
            .inject(this)

        Log.d("LOCATION_RECEIVER", "Started onReceive")

        if (LocationResult.hasResult(intent)) {
            val result = LocationResult.extractResult(intent)

            Log.d("LOCATION_RECEIVER", "onReceive with result")

            locationRepository.insertLocations(result.locations)
        }
    }
}