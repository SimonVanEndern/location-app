package com.simonvanendern.tracking.repository

import com.simonvanendern.tracking.communication.AggregationRequest
import com.simonvanendern.tracking.communication.AggregationResult
import com.simonvanendern.tracking.communication.User
import com.simonvanendern.tracking.communication.Webservice
import com.simonvanendern.tracking.database.schemata.AggregationRequestDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestRepository @Inject constructor(
    private val webservice: Webservice,
    private val aggregationRequestDao: AggregationRequestDao
) {

//    private val pendingRequests = aggregationRequestDao.getAll()

    fun createUser(userId: String): Boolean {
        return webservice.createUser(User(userId)).execute().body()?.status ?: true
    }

    fun getPendingRequests(userId: String): List<com.simonvanendern.tracking.database.schemata.AggregationRequest> {

        val newRequests = webservice.getRequestsForUser((userId)).execute().body() ?: emptyList()
        for (request in newRequests) {
            aggregationRequestDao.insert(
                com.simonvanendern.tracking.database.schemata.AggregationRequest(
                    0, request.id, request.data
                )
            )
        }

        return aggregationRequestDao.getAll()
    }

    fun postAggregationRequest(userId: String, request: AggregationRequest): Boolean {
        return webservice.forwardAggregationRequest(request).execute().body()?.status ?: false
    }

    fun postAggregationnResult(userId: String, result: AggregationResult): Boolean {
        return webservice.insertAggregationResult(result).execute().body()?.status ?: false
    }
}