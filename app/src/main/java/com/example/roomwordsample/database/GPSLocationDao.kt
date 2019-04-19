package com.example.roomwordsample.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface GPSLocationDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert (location : GPSLocation)

    @Query("SELECT * FROM gps_location_table WHERE id = :id LIMIT 1")
    fun getById (id : Int) : GPSLocation
}