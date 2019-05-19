package com.simonvanendern.tracking

import com.simonvanendern.tracking.aggregation.DatabaseAggregator
import com.simonvanendern.tracking.aggregation.ServerRequestHandler
import com.simonvanendern.tracking.logging.ActivityRecognitionReceiver
import com.simonvanendern.tracking.logging.LocationReceiver
import com.simonvanendern.tracking.logging.StepsLogger
import com.simonvanendern.tracking.repository.ActivityRepository
import com.simonvanendern.tracking.repository.RequestRepository
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(application: MyApplication)
    fun inject(databaseAggregator: DatabaseAggregator)
    fun inject(activityRecognitionReceiver : ActivityRecognitionReceiver)
    fun inject(LocationReceiver : LocationReceiver)
    fun inject(stepsLogger: StepsLogger)
    fun inject(allDataViewModel: AllDataViewModel)
    fun inject(serverRequestHandler: ServerRequestHandler)
}
