package com.simonvanendern.tracking.server_communication

import retrofit2.Call
import retrofit2.http.*

/**
 * Retrofit WebService for interacting with the server.
 */
interface WebService {

    @Headers("Content-Type: application/json")
    @POST("/user")
    fun createUser(@Body user: User): Call<User>

    @GET("/requests")
    fun getRequestsForUser(
        @Query("publicKey") publicKey: String,
        @Query("password") password: String
    ): Call<List<AggregationRequest>>

    @Headers("Content-Type: application/json")
    @POST("/forward")
    fun forwardAggregationRequest(@Body request: AggregationResponse): Call<Void>

    @Headers("Content-Type: application/json")
    @POST("/forward")
    fun insertAggregationResult(@Body result: AggregationRequest): Call<Void>
}