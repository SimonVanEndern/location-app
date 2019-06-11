package com.simonvanendern.tracking.database.data_model.aggregated

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * This class saves steps computed from the step counter values saved in step_counter_table
 * timestamp is a UNIX timestamp
 * steps is the number of steps walked in the time between the timestamp and the next highest
 * timestamp in this table.
 * day is the day on which those steps were walked.
 */
@Entity(tableName = "steps_table")
data class Steps(
    @PrimaryKey @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "day") val day: Date,
    @ColumnInfo(name = "steps") val steps: Int
)