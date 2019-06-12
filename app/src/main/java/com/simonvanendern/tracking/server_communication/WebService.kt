package com.simonvanendern.tracking.server_communication

import retrofit2.Call
import retrofit2.http.*

interface WebService {

    @Headers("Content-Type: application/json")
    @POST("/user")
    fun createUser(@Body user: User): Call<User>

    @GET("/requests")
    fun getRequestsForUser(
        @Query("pk") pk: String,
        @Query("pw") pw: String
    ): Call<List<AggregationRequest>>

    @Headers("Content-Type: application/json")
    @POST("/forward")
    fun forwardAggregationRequest(@Body request: AggregationResponse): Call<Response>

    @Headers("Content-Type: application/json")
    @POST("/forward")
    fun insertAggregationResult(@Body result: AggregationRequest): Call<Response>
}