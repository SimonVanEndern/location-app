package com.simonvanendern.tracking.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.simonvanendern.tracking.database.schemata.Steps
import com.simonvanendern.tracking.database.schemata.StepsDao
import com.simonvanendern.tracking.database.schemata.StepsRaw
import com.simonvanendern.tracking.database.schemata.StepsRawDao
import javax.inject.Inject

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
class StepsRepository @Inject constructor(
    stepsDao : StepsDao,
    private val stepsRawDao: StepsRawDao) {

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
}