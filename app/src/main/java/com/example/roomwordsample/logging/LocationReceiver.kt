package com.example.roomwordsample.logging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.roomwordsample.GPSRepository
import com.example.roomwordsample.database.*
import com.example.roomwordsample.database.schemata.GPSData
import com.example.roomwordsample.database.schemata.GPSDataDao
import com.example.roomwordsample.database.schemata.GPSLocation
import com.example.roomwordsample.database.schemata.GPSLocationDao
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LocationReceiver : BroadcastReceiver() {

    private var parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    override fun onReceive(context: Context, intent: Intent) {
        val locationDao: GPSLocationDao = LocationRoomDatabase.getDatabase(context, scope).gPSLocationDao()
        val gpsDao: GPSDataDao = LocationRoomDatabase.getDatabase(context, scope).gPSDataDao()

        val locationRepository = GPSRepository(locationDao, gpsDao)

        Log.d("LocationReceiver", "Started onReceive")

        if (LocationResult.hasResult(intent)) {
            val result = LocationResult.extractResult(intent)

            Log.d("LocationReceiver", "onReceive with result")


            for (location in result.locations) {
                scope.launch(Dispatchers.IO) {
                    val id = locationRepository.insert(
                        GPSLocation(
                            0,
                            location.longitude.toFloat(),
                            location.latitude.toFloat(),
                            location.speed
                        )
                    )
                    locationRepository.insert(
                        GPSData(
                            id,
                            location.time
                        )
                    )
                }
            }
        }
    }
}