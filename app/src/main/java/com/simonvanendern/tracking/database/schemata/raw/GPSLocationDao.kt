package com.simonvanendern.tracking.database.schemata.raw

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.ABORT
import androidx.room.Query

/**
 * The data access object / class for the gps_location_table defined in @see GPSLocation
 */
@Dao
interface GPSLocationDao {

    @Insert(onConflict = ABORT)
    fun insert(location: GPSLocation): Long

    @Insert
    fun insertAll(locations: List<GPSLocation>)

    @Query("SELECT * FROM gps_location_table WHERE id = :id LIMIT 1")
    fun getById(id: Long): GPSLocation
}