package com.simonvanendern.tracking.database.schemata.aggregated

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.simonvanendern.tracking.database.schemata.raw.GPSLocation

/**
 * This class saves trajectories computed from the GPS locations saved in gps_data_table and gps_location_table.
 * start and end are timestamps specifying the start and end of the trajectory.
 * activity specifies the corresponding @see DetectedActivity (not used yet).
 * location_end and location_start link to the gps points in gps_location_table
 */
@Entity(
    tableName = "trajectory_table",
    foreignKeys = [ForeignKey(
        entity = GPSLocation::class,
        parentColumns = ["id"],
        childColumns = ["location_start"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = GPSLocation::class,
        parentColumns = ["id"],
        childColumns = ["location_end"],
        onDelete = ForeignKey.CASCADE
    )]
)
class Trajectory(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "start") val start: Long,
    @ColumnInfo(name = "end") val end: Long,
    @ColumnInfo(name = "location_start") val locationStart: Long,
    @ColumnInfo(name = "location_end") val locationEnd: Long,
    @ColumnInfo(name = "activity") val activity: Int
)