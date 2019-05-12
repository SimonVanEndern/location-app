package tracking

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import tracking.database.schemata.Activity
import tracking.database.schemata.ActivityDao
import tracking.database.schemata.ActivityTransition
import tracking.database.schemata.ActivityTransitionDao

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
class ActivityRepository(private val activityTransitionDao: ActivityTransitionDao,
                         private val activityDao: ActivityDao
) {

    val recentActivityTransitions: LiveData<List<ActivityTransition>> = activityTransitionDao.get10RecentActivityTransitions()
    val recentActivities : LiveData<List<Activity>> = activityDao.get10RecentActivities()


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
}