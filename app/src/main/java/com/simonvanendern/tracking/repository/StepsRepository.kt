package com.simonvanendern.tracking.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.data_model.aggregated.Steps
import com.simonvanendern.tracking.database.data_model.raw.StepsRaw
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository handling the DAOs corresponding to steps
 */
@Singleton
class StepsRepository @Inject constructor(db: TrackingDatabase) {

    private val stepsDao = db.stepsDao()
    private val stepsRawDao = db.stepsRawDao()

    val recentSteps: LiveData<List<Steps>> = stepsDao.get10RecentSteps()

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(stepsRaw: StepsRaw) {
        stepsRawDao.insert(stepsRaw)
    }

    fun getAllSteps () :List<Steps>{
        return stepsDao.getAll()
    }

    fun getAllStepsRaw () : List<StepsRaw> {
        return stepsRawDao.getAll()
    }

    /**
     * Computes steps walked from not yet processed step counter values
     * reflecting the step counter value since last reboot
     * and then sets those step counter values as processed.
     */
    fun aggregateSteps() {
        val lastTimestamp = stepsRawDao.getLastTimestamp()
        val newSteps = stepsRawDao.computeNewSteps()
        stepsDao.insertAll(newSteps)
        stepsRawDao.setProcessed(lastTimestamp)
    }
}