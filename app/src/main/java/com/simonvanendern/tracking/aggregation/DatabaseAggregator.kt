package com.simonvanendern.tracking.aggregation

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simonvanendern.tracking.ApplicationModule
import com.simonvanendern.tracking.DaggerApplicationComponent
import com.simonvanendern.tracking.repository.ActivityRepository
import com.simonvanendern.tracking.repository.StepsRepository
import javax.inject.Inject

class DatabaseAggregator(private val appContext: Context, workParams: WorkerParameters) :
    Worker(appContext, workParams) {

    @Inject
    lateinit var activityRepository: ActivityRepository

    @Inject
    lateinit var stepsRepository: StepsRepository

    override fun doWork(): Result {
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(appContext))
            .build()
            .inject(this)
        activityRepository.aggregateActivities()
        stepsRepository.aggregateSteps()

        Log.d("AGGREGATOR", "Successful aggregation of Stuff")

        return Result.success()
    }
}