package com.example.roomwordsample.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.ABORT
import androidx.room.Query

@Dao
interface GPSLocationDao {

    @Insert(onConflict = ABORT)
    fun insert(location: GPSLocation): Long

    @Query("SELECT * FROM gps_location_table WHERE id = :id LIMIT 1")
    fun getById(id: Long): GPSLocation
}