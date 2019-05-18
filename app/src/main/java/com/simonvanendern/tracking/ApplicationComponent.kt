package com.simonvanendern.tracking

import com.simonvanendern.tracking.aggregation.DatabaseAggregator
import com.simonvanendern.tracking.repository.ActivityRepository
import com.simonvanendern.tracking.repository.RequestRepository
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(application: MyApplication)
//    fun inject(activityRepository: ActivityRepository)
    fun inject(databaseAggregator: DatabaseAggregator)

//        fun activityRepository() : ActivityRepository
//    fun databaseAggregator(activityRepository : ActivityRepository)

//    fun inject(mainActivity: MainActivity)
}
