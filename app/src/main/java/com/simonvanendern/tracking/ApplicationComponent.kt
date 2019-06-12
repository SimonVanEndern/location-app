package com.simonvanendern.tracking

import com.simonvanendern.tracking.aggregation.DatabaseAggregator
import com.simonvanendern.tracking.aggregation.ServerRequestHandler
import com.simonvanendern.tracking.backgroundService.BackgroundService
import com.simonvanendern.tracking.data_collection.ActivityTransitionReceiver
import com.simonvanendern.tracking.data_collection.LocationReceiver
import com.simonvanendern.tracking.data_collection.StepsLogger
import com.simonvanendern.tracking.repository.ActivityRepository
import com.simonvanendern.tracking.repository.GPSRepository
import com.simonvanendern.tracking.repository.StepsRepository
import dagger.Component
import javax.inject.Singleton

/**
 * Component handling dependency injection.
 */
@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(application: MyApplication)
    fun inject(activityTransitionReceiver: ActivityTransitionReceiver)
    fun inject(LocationReceiver: LocationReceiver)
    fun inject(stepsLogger: StepsLogger)
    fun inject(backgroundService: BackgroundService)
    fun inject(serverRequestHandler: ServerRequestHandler)
    fun inject(databaseAggregator: DatabaseAggregator)
    fun inject(allDataViewModel: AllDataViewModel)
    fun inject(gpsRepository: GPSRepository)
    fun inject(activityRepository: ActivityRepository)
    fun inject(stepsRepository: StepsRepository)
}
