package com.simonvanendern.tracking.database.data_model.raw

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * This class saves activity transitions registered via an ActivityTransitionEvent
 * day is the corresponding day of the timestamp specified in start
 * activityType corresponds to @see DetectedActivity
 * transitionType is one of ENTER (0) or EXIT (1)
 * start is a UNIX timestamp
 * processed is a flag whether this entry has already been processed and used to
 * compute aggregated data in the aggregated package
 */
@Entity(tableName = "activity_transition_table")
data class ActivityTransition(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "day") val day: Date?,
    @ColumnInfo(name = "activity_type") val activityType: Int,
    @ColumnInfo(name = "transition_type") val transitionType: Int,
    @ColumnInfo(name = "start") val start: Long,
    @ColumnInfo(name = "processed") val processed: Boolean
)