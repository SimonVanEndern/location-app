package com.example.roomwordsample.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface GPSDataDao {

    @Insert(onConflict = REPLACE)
    fun insert(gpsData: GPSData)

    @Query("SELECT * FROM gps_data_table, gps_location_table WHERE timestamp = :timestamp LIMIT 1")
    fun getByTimestamp(timestamp: Long): GPSLocation
}