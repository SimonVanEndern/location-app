package com.simonvanendern.tracking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.schemata.aggregated.Activity
import com.simonvanendern.tracking.database.schemata.aggregated.Steps
import com.simonvanendern.tracking.database.schemata.raw.ActivityTransition
import com.simonvanendern.tracking.database.schemata.raw.GPSData
import com.simonvanendern.tracking.database.schemata.raw.StepsRaw
import com.simonvanendern.tracking.repository.ActivityRepository
import com.simonvanendern.tracking.repository.GPSRepository
import com.simonvanendern.tracking.repository.StepsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
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
    private val locationRepository: GPSRepository

    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val mostRecentSteps: LiveData<List<Steps>>
    val mostRecentActivities: LiveData<List<Activity>>
    val mostRecentLocations: LiveData<List<GPSData>>
    val mostRecentActivityTransitions: LiveData<List<ActivityTransition>>

    @Inject
    lateinit var db : TrackingDatabase

    init {
        val component = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(application))
            .build()
        component.inject(this)

        stepsRepository = StepsRepository(db)
        activityRepository = ActivityRepository(db)
        locationRepository = GPSRepository(db)

        mostRecentSteps = stepsRepository.recentSteps
        mostRecentActivities = activityRepository.recentActivities
        mostRecentLocations = locationRepository.recentLocations
        mostRecentActivityTransitions = activityRepository.recentActivityTransitions
    }

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
