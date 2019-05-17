package com.simonvanendern.tracking.communication

import retrofit2.Call
import retrofit2.http.*

interface WebService {

    @Headers("Content-Type: application/json")
    @POST("/user")
    fun createUser(@Body user : User) : Call<Response>

    @GET("/requests")
    fun getRequestsForUser(@Query("pk") pk: String): Call<List<AggregationRequest>>

    @Headers("Content-Type: application/json")
    @POST("/forward")
    fun forwardAggregationRequest(@Body request: AggregationRequest) : Call<Response>

    @Headers("Content-Type: application/json")
    @POST("/aggregation")
    fun insertAggregationResult(@Body result: AggregationResult): Call<Response>
}