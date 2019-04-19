package com.example.roomwordsample.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

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
    @ColumnInfo(name = "location_id") val location: Int,
    @PrimaryKey @ColumnInfo(name = "timestamp") val timestamp: Long
)