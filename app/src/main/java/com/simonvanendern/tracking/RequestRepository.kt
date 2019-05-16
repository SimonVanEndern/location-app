package com.simonvanendern.tracking

import com.simonvanendern.tracking.communication.AggregationRequest
import com.simonvanendern.tracking.communication.AggregationResult
import com.simonvanendern.tracking.communication.User
import com.simonvanendern.tracking.communication.Webservice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestRepository @Inject constructor(private val webservice: Webservice) {

    fun createUser(userId: String): Boolean {
        return webservice.createUser(User(userId)).execute().body() ?: false
    }

    fun getPendingRequests(userId: String): List<AggregationRequest> {
        return webservice.getRequestsForUser(User(userId)).execute().body() ?: emptyList()
    }

    fun postAggregationRequest(userId: String, request: AggregationRequest): Boolean {
        return webservice.forwardAggregationRequest(request).execute().body() ?: false
    }

    fun postAggregationnResult(userId: String, result: AggregationResult): Boolean {
        return webservice.insertAggregationResult(result).execute().body() ?: false
    }
}