package com.example.roomwordsample.logging

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.roomwordsample.ActivityRepository
import com.example.roomwordsample.StepsRepository
import com.example.roomwordsample.database.ActivityTransition
import com.example.roomwordsample.database.LocationRoomDatabase
import com.example.roomwordsample.database.Steps
import com.google.android.gms.location.ActivityTransitionEvent
import com.google.android.gms.location.ActivityTransitionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class ActivityRecognitionReceiver : BroadcastReceiver() {

    private var parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)


    override fun onReceive(context: Context, intent: Intent) {
        val activityDao = LocationRoomDatabase.getDatabase(context, scope).activityDao()
        val activityRepository = ActivityRepository(activityDao)
        val stepsDao = LocationRoomDatabase.getDatabase(context, scope).stepsDao()
        val stepsRepository = StepsRepository(stepsDao)

        Log.d("BroadCastReceiver", "Started onReceive")
        Toast.makeText(context, "Started onReceive", Toast.LENGTH_SHORT).show()
        scope.launch(Dispatchers.IO) {
            stepsRepository.insert(Steps(Date(), 111))
        }

        //Check whether the Intent contains activity recognition data//
        if (ActivityTransitionResult.hasResult(intent)) {

            //If data is available, then extract the ActivityRecognitionResult from the Intent//
            val result = ActivityTransitionResult.extractResult(intent)
            Toast.makeText(context, "Got activity ${result.toString()}", Toast.LENGTH_SHORT).show()

            scope.launch(Dispatchers.IO) {
                stepsRepository.insert(Steps(Date(), 333))
            }

            //Get an array of DetectedActivity objects//
            val detectedActivities = result?.transitionEvents as List<ActivityTransitionEvent>
            for (activity in detectedActivities) {
                Log.d("ACTIVITY", "The activity ${activity.activityType}")
                scope.launch(Dispatchers.IO) {
                    activity.elapsedRealTimeNanos
                    activityRepository.insert(ActivityTransition(0, Date(), activity.activityType, 0, 0))
                }
            }

        } else {
            throw RuntimeException()
        }
    }
}