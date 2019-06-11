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

/**
 * BroadcastReceiver receiving GPS updates.
 */
class LocationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var locationRepository: GPSRepository

    /**
    * Stores the received GPS data to the database.
     * This function is not called upon every single GPS position received but rather in bundles.
    */
    override fun onReceive(context: Context, intent: Intent) {

        // Trigger dependency injection on first receive.
        // This cannot be done in the constructor because we need the context
        if (!this::locationRepository.isInitialized) {
            DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(context))
                .build()
                .inject(this)
        }

        // An Event might be fired without actual content
        if (LocationResult.hasResult(intent)) {
            val result = LocationResult.extractResult(intent)

            locationRepository.insertLocations(result.locations)
        }
    }
}