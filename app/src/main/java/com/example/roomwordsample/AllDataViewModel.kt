package com.example.roomwordsample

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.roomwordsample.database.ActivityTransition
import com.example.roomwordsample.database.GPSData
import com.example.roomwordsample.database.LocationRoomDatabase
import com.example.roomwordsample.database.Steps
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
    val mostRecentSteps: LiveData<List<Int>>
    val mostRecentActivity: LiveData<List<ActivityTransition>>
    val mostRecentLocation: LiveData<List<GPSData>>

    init {
        val stepsDao = LocationRoomDatabase.getDatabase(application, scope).stepsDao()
        val activityDao = LocationRoomDatabase.getDatabase(application, scope).activityDao()
        val locationDao = LocationRoomDatabase.getDatabase(application, scope).gPSLocationDao()
        val gpsDataDao = LocationRoomDatabase.getDatabase(application, scope).gPSDataDao()

        stepsRepository = StepsRepository(stepsDao)
        activityRepository = ActivityRepository(activityDao)
        locationRepository = GPSRepository(locationDao, gpsDataDao)

        mostRecentSteps = stepsRepository.recentSteps
        mostRecentActivity = activityRepository.recentActivities
        mostRecentLocation = locationRepository.recentLocations
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

    fun insert(steps: Steps) = scope.launch(Dispatchers.IO) {
        stepsRepository.insert(steps)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}
