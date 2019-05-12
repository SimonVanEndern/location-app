package com.simonvanendern.tracking.database.schemata

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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