package com.simonvanendern.tracking


import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.simonvanendern.tracking.database.schemata.GPSData
import com.simonvanendern.tracking.database.schemata.GPSDataDao
import com.simonvanendern.tracking.database.schemata.GPSLocation
import com.simonvanendern.tracking.database.schemata.GPSLocationDao

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
class GPSRepository(
    private val gpsLocationDao: GPSLocationDao,
    private val gpsDataDao: GPSDataDao
) {

    val recentLocations: LiveData<List<GPSData>> = gpsLocationDao.get10MostRecentLocations()


    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(gpsData: GPSData) {
        gpsDataDao.insert(gpsData)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(gpsLocation: GPSLocation) : Long {
        return gpsLocationDao.insert(gpsLocation)
    }
}