package com.simonvanendern.tracking.aggregation

import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.schemata.AggregationRequest
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RequestExecuter @Inject constructor(private val db: TrackingDatabase) {

    fun execute(req: AggregationRequest): AggregationRequest {
        val ownValue: Float
        val newMean: Float
        val newN: Int

        when (if (req.type.startsWith("activity")) {
            "activity"
        } else {
            req.type
        }) {
            "steps" -> {
                ownValue = db.stepsDao().getAverageSteps(req.start, req.end)
                if (ownValue.compareTo(0) == 0) {
                    newMean = req.value
                    newN = req.n
                } else {
                    newMean = (ownValue + req.n * req.value) / (req.n + 1)
                    newN = req.n + 1
                }
            }

            "stepsListing" -> {
                ownValue = db.stepsDao().getAverageSteps(req.start, req.end)
                newMean = req.value
                newN = req.n
                req.valueList.add(ownValue)
            }

            "activity" -> {
                // The activities according to the Android activity framework
                val activity = req.type.substring("activity_".length).toInt()

                val diffInMilliSeconds = Math.abs(req.end.time - req.start.time)
                val diff = TimeUnit.DAYS.convert(diffInMilliSeconds, TimeUnit.MILLISECONDS)
                ownValue = db.activityDao().getTotalTimeSpentOnActivity(
                    req.start,
                    req.end,
                    activity
                ).toFloat() / (1000 * 60) / diff

                newMean = (ownValue + req.n * req.value) / (req.n + 1)
                newN = req.n + 1
            }

            "trajectory" -> {
                val trajectories = db.trajectoryDao().getTrajectories(
                    req.start.time,
                    req.end.time
                )
                trajectories.forEach {
                    req.valueList.add(it.lat1)
                    req.valueList.add(it.lon1)
                    req.valueList.add(it.lat2)
                    req.valueList.add(it.lon2)
                }
                newMean = req.value
                newN = req.n
            }
            else -> throw Exception("Should not happen. Implementation missing")
        }


        return AggregationRequest(
            0,
            req.serverId,
            req.nextUser,
            req.type,
            newN,
            newMean,
            req.start,
            req.end,
            false,
            req.valueList
        )
    }
}