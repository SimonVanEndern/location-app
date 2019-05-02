package com.example.roomwordsample.database.schemata

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "gps_data_table",
    foreignKeys = [ForeignKey(
        entity = GPSLocation::class,
        parentColumns = ["id"],
        childColumns = ["location_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class GPSData(
    @ColumnInfo(name = "location_id") val location_id: Long,
    @PrimaryKey @ColumnInfo(name = "timestamp") val timestamp: Long
)