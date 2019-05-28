package com.simonvanendern.tracking.repository

import com.simonvanendern.tracking.communication.AggregationRequest
import com.simonvanendern.tracking.communication.User
import com.simonvanendern.tracking.communication.WebService
import com.simonvanendern.tracking.database.TrackingDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestRepository @Inject constructor(
    db: TrackingDatabase,
    private val webService: WebService
) {
    private val aggregationRequestDao = db.aggregationRequestDao()

    fun createUser(userId: String): User? {
        return webService.createUser(User(userId, "")).execute().body()
    }

    fun getPendingRequests(
        userId: String,
        pw: String
    ): List<com.simonvanendern.tracking.database.schemata.AggregationRequest> {
//        val pk = Base64.encodeToString(Base64.decode(userId, 0), Base64.URL_SAFE)

        val newRequests = webService.getRequestsForUser(userId, pw).execute().body() ?: emptyList()
        for (request in newRequests) {
            aggregationRequestDao.insert(
                com.simonvanendern.tracking.database.schemata.AggregationRequest(
                    0,
                    request.serverId,
                    request.nextUser,
                    request.type,
                    request.n,
                    request.value,
                    request.start,
                    request.end,
                    true,
                    request.valueList
                )
            )
        }

        return aggregationRequestDao.getAllPendingRequests()
    }

    fun getPendingResults(): List<com.simonvanendern.tracking.database.schemata.AggregationRequest> {
        return aggregationRequestDao.getAllPendingResults()
    }

    fun insertRequestResult(res: com.simonvanendern.tracking.database.schemata.AggregationRequest) {
        aggregationRequestDao.insert(res)
    }

    fun deletePendingRequest(req: com.simonvanendern.tracking.database.schemata.AggregationRequest) {
        aggregationRequestDao.delete(req)
    }

    fun sendOutResults() {
        val requests = aggregationRequestDao.getAllPendingResults()
        for (res in requests) {
            webService.forwardAggregationRequest(
                AggregationRequest(
                    res.serverId,
                    res.nextUser,
                    res.type,
                    res.n,
                    res.value,
                    res.start,
                    res.end,
                    res.valueList
                )
            ).execute()
            aggregationRequestDao.delete(res)
        }
    }

    fun postAggregationRequest(userId: String, request: AggregationRequest): Boolean {
        return webService.forwardAggregationRequest(request).execute().body()?.status ?: false
    }

//    fun postAggregationnResult(userId: String, result: AggregationResult): Boolean {
//        return webService.insertAggregationResult(result).execute().body()?.status ?: false
//    }
}