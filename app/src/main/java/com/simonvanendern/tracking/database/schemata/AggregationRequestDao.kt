package com.simonvanendern.tracking.database.schemata

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.ABORT
import androidx.room.Query

@Dao
interface AggregationRequestDao {

    @Insert(onConflict = ABORT)
    fun insert(request: AggregationRequest): Long

    @Query("SELECT * FROM aggregationRequest")
    fun getAll () : LiveData<List<AggregationRequest>>
}