package com.simonvanendern.tracking.aggregation

import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.data_model.AggregationRequest
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * This class is responsible for processing aggregation requests.
 */
class RequestExecuter @Inject constructor(private val db: TrackingDatabase) {

    /**
     * Executes the aggregation request logic depending on the type parameter
     */
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
                // 0 values are excluded in the mean calculation.
                // A 0 value most probably results from the phone having no step sensor
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
                newN = req.n + 1
                req.valueList.add(ownValue)
            }

            "activity" -> {
                // The activities according to the Android activity framework
                val activity = req.type.substring("activity_".length).toInt()

                val diffInMilliSeconds = Math.abs(req.end.time - req.start.time)
                val diff = TimeUnit.DAYS.convert(diffInMilliSeconds, TimeUnit.MILLISECONDS)
                // Calculates the average time in minutes
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
            // Change the error handling
            else -> throw Exception("Should not happen. Implementation missing")
        }

        return AggregationRequest(
            0,
            req.serverId,
            req.nextUser,
            req.start,
            req.end,
            req.type,
            newN,
            newMean,
            req.valueList,
            false
        )
    }
}