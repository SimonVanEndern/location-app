package com.simonvanendern.tracking.aggregation

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simonvanendern.tracking.ApplicationModule
import com.simonvanendern.tracking.DaggerApplicationComponent
import com.simonvanendern.tracking.repository.ActivityRepository
import com.simonvanendern.tracking.repository.GPSRepository
import com.simonvanendern.tracking.repository.StepsRepository
import javax.inject.Inject

class DatabaseAggregator(private val appContext: Context, workParams: WorkerParameters) :
    Worker(appContext, workParams) {

    @Inject
    lateinit var activityRepository: ActivityRepository

    @Inject
    lateinit var stepsRepository: StepsRepository

    @Inject
    lateinit var gpsRepository: GPSRepository

    override fun doWork(): Result {
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(appContext))
            .build()
            .inject(this)

        val stepsRaw = "[" + stepsRepository.getAllStepsRaw().joinToString { step ->
            "{\"stepsRaw\": ${step.steps}, \"timestamp\":${step.timestamp}}"
        } + "]"

        val steps = "[" + stepsRepository.getAllSteps().joinToString { step ->
            "{\"steps\": ${step.steps}, \"timestamp\":${step.timestamp}, \"day\":\"${step.day}\"}"
        } + "]"

        val activityTransitions = "[" + activityRepository.getAllTransitions().joinToString { trans ->
            "{\"start\": ${trans.start}, \"activity_type\":${trans.activityType}, \"transition_type\":${trans.transitionType}}"
        } + "]"

        val activities = "[" + activityRepository.getAllActivities().joinToString { activity ->
            "{\"start\": ${activity.start}, \"activity_type\":${activity.activityType}, \"day\":\"${activity.day}\", \"duration\": ${activity.duration}}"
        } + "]"

        val gpsString = "[" + gpsRepository.getAllGPSDataEntries().joinToString { gps ->
            "{\"timestamp\": ${gps.timestamp}, \"latitude\":${gps.latitude}, \"longitude\":${gps.longitude}}"
        } + "]"

        val trajectoryString = "[" + gpsRepository.getAllTrajectories().joinToString { trajectory ->
            "{\"lat1\": ${trajectory.lat1}, \"lon1\":${trajectory.lon1}, \"lat2\":${trajectory.lat2}, \"lon2\":${trajectory.lon2}}"
        } + "]"



        activityRepository.aggregateActivities()
        stepsRepository.aggregateSteps()
        gpsRepository.aggregateGPSRoutes()
//        val gpsData = gpsRepository.getAllGPSDataEntries()
//        val json = gpsData.map { data ->  "{\"longitude\":${data.longitude}, \"latitude\":${data.latitude}, \"timestamp\":${data.timestamp}}" }
//
//        val jsonString = json.joinToString()

        Log.d("AGGREGATOR", "Successful aggregation of Stuff")

        return Result.success()
    }
}