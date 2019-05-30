package com.simonvanendern.tracking.database.schemata.raw

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

    @Query(
        """UPDATE gps_data_table SET processed = 1
                WHERE timestamp <= :lastTimestamp"""
    )
    fun setProcessed(lastTimestamp: Long)

    @Query(
        """SELECT id, timestamp, longitude, latitude
        FROM gps_data_table
        INNER JOIN gps_location_table ON gps_data_table.location_id = gps_location_table.id
        WHERE processed = 0
        ORDER BY timestamp ASC"""
    )
    fun getAll(): List<GPSLocationWithTime>

    class GPSLocationWithTime(
        val id: Long,
        val timestamp: Long,
        val longitude: Float,
        val latitude: Float
    )
}