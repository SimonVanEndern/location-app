package com.example.roomwordsample.logging

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.example.roomwordsample.ActivityRepository
import com.example.roomwordsample.StepsRepository
import com.example.roomwordsample.database.Activity
import com.example.roomwordsample.database.LocationRoomDatabase
import com.example.roomwordsample.database.Steps
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext


class ActivityLogger : IntentService("ActivityLogger") {

    private var parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    private val activityDao = LocationRoomDatabase.getDatabase(this, scope).activityDao()
    private val activityRepository = ActivityRepository(activityDao)

    override fun onHandleIntent(intent: Intent?) {
        //Check whether the Intent contains activity recognition data//
        if (ActivityRecognitionResult.hasResult(intent)) {

            //If data is available, then extract the ActivityRecognitionResult from the Intent//
            val result = ActivityRecognitionResult.extractResult(intent)

            //Get an array of DetectedActivity objects//
            val detectedActivities = result.probableActivities as List<DetectedActivity>
            val activity = detectedActivities.maxBy { activity -> activity.confidence }
            Log.d("ACTIVITY", "The activity ${activity.toString()} has confidence ${activity?.confidence}")
            if (activity != null) {
                scope.launch(Dispatchers.IO) {
                    activityRepository.insert(Activity(0, Date(), activity.type, 0, 0))
                }
            }
        }
    }
}