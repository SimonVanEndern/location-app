package com.simonvanendern.tracking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.simonvanendern.tracking.database.data_model.aggregated.Activity
import com.simonvanendern.tracking.database.data_model.aggregated.Steps
import com.simonvanendern.tracking.database.data_model.raw.ActivityTransition
import com.simonvanendern.tracking.database.data_model.raw.GPSData
import com.simonvanendern.tracking.database.data_model.raw.StepsRaw
import com.simonvanendern.tracking.repository.ActivityRepository
import com.simonvanendern.tracking.repository.GPSRepository
import com.simonvanendern.tracking.repository.StepsRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * View Model to keep a reference to some repositories and
 * an up-to-date list of all data used for displaying for debugging purposes.
 */
class AllDataViewModel(application: Application) : AndroidViewModel(application) {

    // Using LiveData in order to automatically update the view when the database changes
    val mostRecentSteps: LiveData<List<Steps>>
    val mostRecentActivities: LiveData<List<Activity>>
    val mostRecentLocations: LiveData<List<GPSData>>
    val mostRecentActivityTransitions: LiveData<List<ActivityTransition>>

    @Inject
    lateinit var stepsRepository: StepsRepository

    @Inject
    lateinit var activityRepository: ActivityRepository

    @Inject
    lateinit var locationRepository: GPSRepository

    /**
     * Invoking dependency injection and getting the data to be used in the debug screen
     */
    init {
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(application))
            .build()
            .inject(this)

        mostRecentSteps = stepsRepository.recentSteps
        mostRecentActivities = activityRepository.recentActivities
        mostRecentLocations = locationRepository.recentLocations
        mostRecentActivityTransitions = activityRepository.recentActivityTransitions
    }

    fun insert(activityTransition: ActivityTransition) = GlobalScope.launch {
        activityRepository.insert(activityTransition)
    }

    fun insert(stepsRaw: StepsRaw) = GlobalScope.launch {
        stepsRepository.insert(stepsRaw)
    }
}
