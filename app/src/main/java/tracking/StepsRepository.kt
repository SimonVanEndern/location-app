package tracking

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import tracking.database.schemata.Steps
import tracking.database.schemata.StepsDao
import tracking.database.schemata.StepsRaw
import tracking.database.schemata.StepsRawDao

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
class StepsRepository(
    private val stepsDao : StepsDao,
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