package com.simonvanendern.tracking.database.schemata.raw

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

/**
 * The data access object / class for the gps_data_table defined in @see GPSData
 */
@Dao
interface GPSDataDao {

    @Insert(onConflict = REPLACE)
    fun insert(gpsData: GPSData)

    @Query("SELECT * FROM gps_data_table, gps_location_table WHERE timestamp = :timestamp LIMIT 1")
    fun getByTimestamp(timestamp: Long): GPSLocation

    /**
     * Sets the processed flag for each entry with a timestamp value below or equal to @param timestamp
     * @param lastTimestamp The timestamp in milliseconds
     */
    @Query(
        """UPDATE gps_data_table SET processed = 1
                WHERE timestamp <= :lastTimestamp"""
    )
    fun setProcessed(lastTimestamp: Long)

    /**
     * Retrieves all saved gps points as a list of @see GPSLocationWithTime.
     * This method is used for exporting data.
     */
    @Query(
        """SELECT id, timestamp, longitude, latitude
        FROM gps_data_table
        INNER JOIN gps_location_table ON gps_data_table.location_id = gps_location_table.id
        WHERE processed = 0
        ORDER BY timestamp ASC"""
    )
    fun getAll(): List<GPSLocationWithTime>

    /**
     * This method is rather for debugging and testing purposes.
     * It retrieves the 10 most recently recorded GPS data entries.
     */
    @Query(
        """SELECT * FROM gps_data_table
        ORDER BY timestamp DESC
        LIMIT 10
    """
    )
    fun get10MostRecentLocationTimestamps(): LiveData<List<GPSData>>

    /**
     * Helper class to transfer gps data through classes.
     * This class is necessary as a gps point is saved in linked tables and not one single table.
     */
    class GPSLocationWithTime(
        val id: Long,
        val timestamp: Long,
        val longitude: Float,
        val latitude: Float
    )
}