package com.simonvanendern.tracking.database.data_model

import androidx.room.*

/**
 * The data access object / class for the aggregation_request_table defined in @see AggregationRequest
 */
@Dao
interface AggregationRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(request: AggregationRequest): Long

    @Query("SELECT * FROM aggregation_request_table WHERE id = :id LIMIT 1")
    fun getById(id: Int): AggregationRequest

    @Query("SELECT * FROM aggregation_request_table WHERE incoming = 1")
    fun getAllIncomingAggregationRequests(): List<AggregationRequest>

    @Query("SELECT * FROM aggregation_request_table WHERE incoming = 0")
    fun getAllPendingOutgoingAggregationRequests(): List<AggregationRequest>

    @Delete
    fun delete(request: AggregationRequest)
}