package com.simonvanendern.tracking.database.schemata

import androidx.room.*

@Dao
interface AggregationRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(request: AggregationRequest): Long

    @Query("SELECT * FROM aggregationRequest WHERE id = :id LIMIT 1")
    fun getById(id: Int): AggregationRequest

    @Query("SELECT * FROM aggregationRequest WHERE incoming = 1")
    fun getAllPendingRequests(): List<AggregationRequest>

    @Query("SELECT * FROM aggregationRequest WHERE incoming = 0")
    fun getAllPendingResults() : List<AggregationRequest>

    @Delete
    fun delete (request : AggregationRequest)
}