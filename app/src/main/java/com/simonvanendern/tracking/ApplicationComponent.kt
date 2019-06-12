package com.simonvanendern.tracking

import com.simonvanendern.tracking.aggregation.DatabaseAggregator
import com.simonvanendern.tracking.aggregation.ServerRequestHandler
import com.simonvanendern.tracking.backgroundService.BackgroundService
import com.simonvanendern.tracking.data_collection.ActivityTransitionReceiver
import com.simonvanendern.tracking.data_collection.LocationReceiver
import com.simonvanendern.tracking.data_collection.StepsLogger
import com.simonvanendern.tracking.repository.GPSRepository
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(application: MyApplication)
    fun inject(databaseAggregator: DatabaseAggregator)
    fun inject(activityTransitionReceiver: ActivityTransitionReceiver)
    fun inject(LocationReceiver: LocationReceiver)
    fun inject(stepsLogger: StepsLogger)
    fun inject(allDataViewModel: AllDataViewModel)
    fun inject(serverRequestHandler: ServerRequestHandler)
    fun inject(backgroundService: BackgroundService)
    fun inject(gpsRepository: GPSRepository)
}
