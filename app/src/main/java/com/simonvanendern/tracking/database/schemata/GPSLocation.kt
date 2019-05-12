package com.simonvanendern.tracking.database.schemata

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gps_location_table")
data class GPSLocation(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "longitude") val longitude: Float,
    @ColumnInfo(name = "latitude") val latitude: Float,
    @ColumnInfo(name = "speed") val speed: Float
)