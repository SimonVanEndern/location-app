package com.simonvanendern.tracking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.simonvanendern.tracking.database.*
import com.simonvanendern.tracking.database.schemata.*
import com.simonvanendern.tracking.repository.ActivityRepository
import com.simonvanendern.tracking.repository.GPSRepository
import com.simonvanendern.tracking.repository.StepsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * View Model to keep a reference to the word wordRepository and
 * an up-to-date list of all words.
 */

class AllDataViewModel(application: Application) : AndroidViewModel(application) {

    private var parentJob = Job()
    // By default all the coroutines launched in this scope should be using the Main dispatcher
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    //Getting the Repositories for the respective data
    private val stepsRepository: StepsRepository
    private val activityRepository: ActivityRepository
    private val locationRepository : GPSRepository

    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val mostRecentSteps: LiveData<List<Steps>>
    val mostRecentActivities: LiveData<List<Activity>>
    val mostRecentLocations: LiveData<List<GPSData>>
    val mostRecentActivityTransitions: LiveData<List<ActivityTransition>>

    init {
        val stepsDao = TrackingDatabase.getDatabase(application, scope).stepsDao()
        val stepsRawDao = TrackingDatabase.getDatabase(application, scope).stepsRawDao()
        val activityDao = TrackingDatabase.getDatabase(application, scope).activityDao()
        val activityTransitionDao = TrackingDatabase.getDatabase(application, scope).activityTransitionDao()
        val locationDao = TrackingDatabase.getDatabase(application, scope).gPSLocationDao()
        val gpsDataDao = TrackingDatabase.getDatabase(application, scope).gPSDataDao()

        stepsRepository = StepsRepository(stepsDao, stepsRawDao)
        activityRepository =
            ActivityRepository(activityTransitionDao, activityDao)
        locationRepository = GPSRepository(locationDao, gpsDataDao)

        mostRecentSteps = stepsRepository.recentSteps
        mostRecentActivities = activityRepository.recentActivities
        mostRecentLocations = locationRepository.recentLocations
        mostRecentActivityTransitions = activityRepository.recentActivityTransitions
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
//    fun insert(word: Word) = scope.launch(Dispatchers.IO) {
//        wordRepository.insert(word)
//    }

    fun insert(activityTransition: ActivityTransition) = scope.launch(Dispatchers.IO) {
        activityRepository.insert(activityTransition)
    }

    fun insert(stepsRaw: StepsRaw) = scope.launch(Dispatchers.IO) {
        stepsRepository.insert(stepsRaw)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}
