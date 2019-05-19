package com.simonvanendern.tracking.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.schemata.aggregated.Steps
import com.simonvanendern.tracking.database.schemata.raw.StepsRaw
import javax.inject.Inject

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
class StepsRepository @Inject constructor(db: TrackingDatabase) {

    private val stepsDao = db.stepsDao()
    private val stepsRawDao = db.stepsRawDao()

    val recentSteps: LiveData<List<Steps>> = stepsDao.get10RecentSteps()

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
//    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun insert(stepsRaw: StepsRaw) {
        stepsRawDao.insert(stepsRaw)
    }

    fun aggregateSteps() {
        val lastTimestamp = stepsRawDao.getLastTimestamp()
        val newSteps = stepsRawDao.computeNewSteps()
        stepsDao.insertAll(newSteps)
        stepsRawDao.setProcessed(lastTimestamp)
    }
}