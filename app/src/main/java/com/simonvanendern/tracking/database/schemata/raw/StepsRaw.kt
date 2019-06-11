package com.simonvanendern.tracking.database.schemata.raw

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * This class saves values received from the phone internal step counter sensor.
 * Those values correspond to the number of registered steps at the specified time since last reboot!
 * timestamp is a UNIX timestamp
 * day is the day corresponding to the timestamp
 * steps is the total number of steps at the specified timestamp since last reboot
 * processed is a flag whether this entry has already been processed and used to
 * compute aggregated data in the aggregated package
 */
@Entity(tableName = "step_counter_table")
data class StepsRaw(
    @PrimaryKey @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "day") val day: Date,
    @ColumnInfo(name = "steps") val steps: Int,
    @ColumnInfo(name = "processed") val processed : Boolean
)