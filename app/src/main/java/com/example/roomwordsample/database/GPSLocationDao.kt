package com.example.roomwordsample.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GPSLocationDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(location: GPSLocation)

    @Query("SELECT * FROM gps_location_table WHERE id = :id LIMIT 1")
    fun getById(id: Int): GPSLocation
}