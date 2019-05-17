package com.simonvanendern.tracking.aggregation

import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.schemata.AggregationRequest
import javax.inject.Inject

class RequestExecuter @Inject constructor(private val db: TrackingDatabase) {

    fun execute(req: AggregationRequest): AggregationRequest {
        var ownValue: Float

        when (req.type) {
            "steps" -> {
                ownValue = db.stepsDao().getAverageSteps(req.start, req.end)
            }
            else -> throw Exception("Should not happen. Implementation missing")
        }

        val newMean = (ownValue + req.n * req.value) / (req.n + 1)
        val newN = req.n + 1

        return AggregationRequest(
            req.id,
            "serverId",
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