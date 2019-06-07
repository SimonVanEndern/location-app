package com.simonvanendern.tracking.database.schemata.aggregated

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TrajectoryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(trajectory: Trajectory): Long

    @Query("SELECT * FROM trajectory_table WHERE id = :id LIMIT 1")
    fun getById(id: Long): Trajectory

    @Query(
        """SELECT g1.latitude as lat1, g1.longitude as lon1, g2.latitude as lat2, g2.longitude as lon2
        FROM trajectory_table AS t
        JOIN gps_location_table AS g1 ON t.location_start = g1.id
        JOIN gps_location_table AS g2 ON t.location_end = g2.id
        WHERE start BETWEEN :startDayInclusive AND :endDayInclusive"""
    )
    fun getTrajectories(startDayInclusive: Long, endDayInclusive: Long):
            List<TrajectoryData>

    @Query("SELECT * FROM trajectory_table")
    fun getAll(): List<Trajectory>

    @Query(
        """SELECT g1.latitude as lat1, g1.longitude as lon1, g2.latitude as lat2, g2.longitude as lon2
        FROM trajectory_table AS t
        JOIN gps_location_table AS g1 ON t.location_start = g1.id
        JOIN gps_location_table AS g2 ON t.location_end = g2.id"""
    )
    fun getAllFormatted(): List<TrajectoryData>

    class TrajectoryData(
        val lat1: Float,
        val lon1: Float,
        val lat2: Float,
        val lon2: Float
    )
}