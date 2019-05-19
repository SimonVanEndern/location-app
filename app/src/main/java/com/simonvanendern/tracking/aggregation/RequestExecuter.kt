package com.simonvanendern.tracking.aggregation

import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.schemata.AggregationRequest
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RequestExecuter @Inject constructor(private val db: TrackingDatabase) {

    fun execute(req: AggregationRequest): AggregationRequest {
        val ownValue: Float

        when (if (req.type.startsWith("activity")) {
            "activity"
        } else {
            req.type
        }) {
            "steps" -> {
                ownValue = db.stepsDao().getAverageSteps(req.start, req.end)
            }

            "activity" -> {
                val activity = req.type.substring("activity_".length).toInt()

                val diffInMilliSeconds = Math.abs(req.end.time - req.start.time)
                val diff = TimeUnit.DAYS.convert(diffInMilliSeconds, TimeUnit.MILLISECONDS)
                ownValue = db.activityDao().getTotalTimeSpentOnActivity(
                    req.start,
                    req.end,
                    activity
                ).toFloat() / diff
            }
            else -> throw Exception("Should not happen. Implementation missing")
        }

        val newMean = (ownValue + req.n * req.value) / (req.n + 1)
        val newN = req.n + 1

        return AggregationRequest(
            0,
            req.serverId,
            req.nextUser,
            req.type,
            newN,
            newMean,
            req.start,
            req.end,
            false
        )
    }
}