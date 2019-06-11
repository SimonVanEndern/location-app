package com.simonvanendern.tracking.database.schemata.raw

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * This class saves GPS locations.
 * longitude is the longitude of the location (angle to equatorial line)
 * latitude is the latitude of the location (anglge to the prime meridian through Greenwich)
 * speed is currently not used.
 */
@Entity(tableName = "gps_location_table")
data class GPSLocation(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "longitude") val longitude: Float,
    @ColumnInfo(name = "latitude") val latitude: Float,
    @ColumnInfo(name = "speed") val speed: Float
)