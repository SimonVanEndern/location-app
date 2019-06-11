package com.simonvanendern.tracking.database.data_model.aggregated

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * This class saves activities computed from a starting and exiting @see ActivityTransitionEvent
 * activityType corresponds to @see DetectedActivity
 * duration is in milliseconds
 * start is a UNIX timestamp
 */
@Entity(tableName = "activity_table")
data class Activity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "day") val day: Date,
    @ColumnInfo(name = "activity_type") val activityType: Int,
    @ColumnInfo(name = "start") val start: Long,
    @ColumnInfo(name = "duration") val duration: Int
)