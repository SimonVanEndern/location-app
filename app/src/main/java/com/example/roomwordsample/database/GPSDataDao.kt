package com.example.roomwordsample.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface GPSDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert (gpsData : GPSData)

    @Query("SELECT * FROM gps_data_table, gps_location_table WHERE timestamp = :timestamp")
    fun getByTimestamp (timestamp : Int) : GPSLocation
}