package com.simonvanendern.tracking.logging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.simonvanendern.tracking.ActivityRepository
import com.simonvanendern.tracking.database.LocationRoomDatabase
import com.simonvanendern.tracking.database.schemata.ActivityTransition
import com.google.android.gms.location.ActivityTransitionEvent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
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
        val activityTransitionDao = LocationRoomDatabase.getDatabase(context, scope).activityTransitionDao()
        val activityRepository = ActivityRepository(activityTransitionDao, activityDao)

        Log.d("BroadCastReceiver", "Started onReceive")
        Toast.makeText(context, "Started onReceive", Toast.LENGTH_SHORT).show()
//        scope.launch(Dispatchers.IO) {
//            stepsRepository.insert(StepsRaw(Date(), 111))
//        }

        //Check whether the Intent contains activity recognition data//
        if (ActivityTransitionResult.hasResult(intent)) {

            //If data is available, then extract the ActivityRecognitionResult from the Intent//
            val result = ActivityTransitionResult.extractResult(intent)
            Toast.makeText(context, "Got activity ${result.toString()}", Toast.LENGTH_SHORT).show()

//            scope.launch(Dispatchers.IO) {
//                activityRepository.insert(Activity(0, Date(), 10, 1, 2))
//            }

            //Get an array of DetectedActivity objects//
            val detectedActivities = result?.transitionEvents as List<ActivityTransitionEvent>
            for (activity in detectedActivities) {
                Log.d("ACTIVITY", "The activity ${activity.activityType}")
                scope.launch(Dispatchers.IO) {
                    activityRepository.insert(
                        ActivityTransition(
                            0,
                            Date(),
                            activity.activityType,
                            activity.transitionType,
                            Date().time - ((SystemClock.elapsedRealtimeNanos() - activity.elapsedRealTimeNanos) / 1000000),
                            false
                        )
                    )
                }
            }

            val i = Intent(context, LoggingService::class.java)
            val activity = detectedActivities
                .filter { activity -> activity.transitionType == 0 }
                .maxBy { activity -> activity.elapsedRealTimeNanos }
                ?.activityType

            Log.d("ACTIVITY_RECOGNITION", "Got activity for change: $activity")

            when (activity) {
                DetectedActivity.STILL -> {
                    i.putExtra("granularity", 0)
                }

                DetectedActivity.WALKING -> {
                    i.putExtra("granularity", 1)
                }

                else -> {
                    i.putExtra("granularity", 2)
                }
            }
            context.startService(i)

        } else {
            throw RuntimeException()
        }
    }
}