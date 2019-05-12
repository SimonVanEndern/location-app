package com.simonvanendern.tracking.aggregation

import com.google.android.gms.location.DetectedActivity
import java.util.*
import java.util.concurrent.TimeUnit

class AverageTimeInActivity(private val start: Date, private val end: Date, private val activity: DetectedActivity) :
    Datatype(start, end) {

    override fun getValue(): Float {
        val diffInMilliSeconds = Math.abs(end.time - start.time)
        val diff = TimeUnit.DAYS.convert(diffInMilliSeconds, TimeUnit.MILLISECONDS)
        return (getDb()?.activityTransitionDao()?.getTotalTimeSpentOnActivity(start, end, activity)?.toFloat() ?: 0F) / diff
    }
}