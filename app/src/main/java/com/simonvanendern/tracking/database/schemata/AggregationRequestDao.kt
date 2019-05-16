package com.simonvanendern.tracking.database.schemata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.ABORT
import androidx.room.Query

@Dao
interface AggregationRequestDao {

    @Insert(onConflict = ABORT)
    fun insert(request: AggregationRequest): Long

    @Query("SELECT * FROM aggregationRequest WHERE id = :id LIMIT 1")
    fun getById(id : Int) : AggregationRequest

    @Query("SELECT * FROM aggregationRequest")
    fun getAll(): List<AggregationRequest>
}