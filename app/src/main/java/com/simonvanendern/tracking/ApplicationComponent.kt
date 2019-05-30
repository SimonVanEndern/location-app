package com.simonvanendern.tracking

import com.simonvanendern.tracking.aggregation.DatabaseAggregator
import com.simonvanendern.tracking.aggregation.ServerRequestHandler
import com.simonvanendern.tracking.backgroundService.BackgroundLoggingService
import com.simonvanendern.tracking.logging.ActivityRecognitionReceiver
import com.simonvanendern.tracking.logging.LocationReceiver
import com.simonvanendern.tracking.logging.StepsLogger
import com.simonvanendern.tracking.repository.GPSRepository
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(application: MyApplication)
    fun inject(databaseAggregator: DatabaseAggregator)
    fun inject(activityRecognitionReceiver: ActivityRecognitionReceiver)
    fun inject(LocationReceiver: LocationReceiver)
    fun inject(stepsLogger: StepsLogger)
    fun inject(allDataViewModel: AllDataViewModel)
    fun inject(serverRequestHandler: ServerRequestHandler)
    fun inject(backgroundLoggingService: BackgroundLoggingService)
    fun inject(gpsRepository: GPSRepository)
}
