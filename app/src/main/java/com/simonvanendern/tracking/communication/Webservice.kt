package com.simonvanendern.tracking.communication

import retrofit2.Call
import retrofit2.http.*

interface Webservice {

    @Headers("Content-Type: application/json")
    @POST("/user")
    fun createUser(@Body user : User) : Call<Response>

    @GET("/requests?pk={pk}")
    fun getRequestsForUser(@Path("pk") pk: User): Call<List<AggregationRequest>>

    @Headers("Content-Type: application/json")
    @POST("/forward")
    fun forwardAggregationRequest(@Body request: AggregationRequest) : Call<Boolean>

    @Headers("Content-Type: application/json")
    @POST("/aggregation")
    fun insertAggregationResult(@Body result: AggregationResult): Call<Boolean>
}