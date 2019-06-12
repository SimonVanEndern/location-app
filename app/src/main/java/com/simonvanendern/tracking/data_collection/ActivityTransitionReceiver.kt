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
import com.simonvanendern.tracking.backgroundService.BackgroundService
import com.simonvanendern.tracking.repository.ActivityRepository
import javax.inject.Inject

/**
 * BroadcastReceiver receiving updates from the ActivityRecognition framework.
 */
class ActivityTransitionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var activityRepository: ActivityRepository

    /**
     * Stores the received ActivityTransitionEvents to the database and
     * informs the GPS logging service to set the logging frequency accordingly.
     * When the device is still, GPS logging frequency is lower than when it is moving.
     */
    override fun onReceive(context: Context, intent: Intent) {

        // Trigger dependency injection on first receive.
        // This cannot be done in the constructor because we need the context
        if (!this::activityRepository.isInitialized) {
            DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(context))
                .build()
                .inject(this)
        }

        // An Event might be fired without actual content
        if (ActivityTransitionResult.hasResult(intent)) {
            Log.d("ACTIVITY_RECOGNITION", "Got activity")

            val result = ActivityTransitionResult.extractResult(intent)

            val detectedActivities = result?.transitionEvents as List<ActivityTransitionEvent>
            activityRepository.insertDetectedActivities(detectedActivities)

            val activity = detectedActivities
                .filter { it.transitionType == 0 }
                .maxBy { it.elapsedRealTimeNanos }
                ?.activityType

            notifyGPSLoggingServiceAboutActivityChange(context, activity)
        } else {
            Log.d("ACTIVITY_RECOGNITION", "Got something else")
        }
    }

    /**
     * Sends a notification to the @see BackgroundService in order to
     * change the frequency of GPS updates.
     * The frequency itself is set in the GPS logging service, this method sends an extra
     * along the intent specifying the frequency (granularity) as one of 0,1,2.
     */
    private fun notifyGPSLoggingServiceAboutActivityChange(context: Context, activity: Int?) {
        Log.d("ACTIVITY_RECOGNITION", "Got activity for change: $activity")

        val i = Intent(context, BackgroundService::class.java)

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