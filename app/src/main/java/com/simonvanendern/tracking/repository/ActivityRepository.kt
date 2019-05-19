package com.simonvanendern.tracking.repository

import android.os.SystemClock.elapsedRealtimeNanos
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.google.android.gms.location.ActivityTransitionEvent
import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.schemata.aggregated.Activity
import com.simonvanendern.tracking.database.schemata.raw.ActivityTransition
import java.util.*
import javax.inject.Inject

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
class ActivityRepository @Inject constructor(db: TrackingDatabase) {
    private val activityTransitionDao = db.activityTransitionDao()
    private val activityDao = db.activityDao()

    val recentActivityTransitions: LiveData<List<ActivityTransition>> =
        activityTransitionDao.get10RecentActivityTransitions()
    val recentActivities: LiveData<List<Activity>> = activityDao.get10RecentActivities()

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(activityTransition: ActivityTransition) {
        activityTransitionDao.insert(activityTransition)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(activity: Activity) {
        activityDao.insert(activity)
    }

    fun aggregateActivities() {
        val lastTimestamp = activityTransitionDao.getLastTimestamp()
        val newActivities = activityTransitionDao.computeNewActivities()
        activityDao.insertAll(newActivities)
        activityTransitionDao.setProcessed(lastTimestamp)
    }

    fun insertDetectedActiviteis(activities: List<ActivityTransitionEvent>) {
        val now = Date()
        val activityTransitions = activities.map {
            ActivityTransition(
                0,
                now,
                it.activityType,
                it.transitionType,
                now.time - ((elapsedRealtimeNanos() - it.elapsedRealTimeNanos) / 1000000),
                false
            )
        }

        activityTransitionDao.insertAll(activityTransitions)
    }
}