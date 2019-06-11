package com.simonvanendern.tracking.data_collection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityTransitionEvent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import com.simonvanendern.tracking.ApplicationModule
import com.simonvanendern.tracking.DaggerApplicationComponent
import com.simonvanendern.tracking.backgroundService.BackgroundLoggingService
import com.simonvanendern.tracking.repository.ActivityRepository
import javax.inject.Inject

class ActivityRecognitionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var activityRepository: ActivityRepository

    override fun onReceive(context: Context, intent: Intent) {
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(context))
            .build()
            .inject(this)

        if (ActivityTransitionResult.hasResult(intent)) {
            Log.d("ACTIVITY_RECOGNITION", "Got activity")

            val result = ActivityTransitionResult.extractResult(intent)

            val detectedActivities = result?.transitionEvents as List<ActivityTransitionEvent>
            activityRepository.insertDetectedActivities(detectedActivities)

            val activity = detectedActivities
                .filter { it.transitionType == 0 }
                .maxBy { it.elapsedRealTimeNanos }
                ?.activityType

            notifyAboutActivityChange(context, activity)
        } else {
            Log.d("ACTIVITY_RECOGNITION", "Got something else")
        }
    }

    private fun notifyAboutActivityChange(context: Context, activity: Int?) {
        Log.d("ACTIVITY_RECOGNITION", "Got activity for change: $activity")

        val i = Intent(context, BackgroundLoggingService::class.java)

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
    }
}