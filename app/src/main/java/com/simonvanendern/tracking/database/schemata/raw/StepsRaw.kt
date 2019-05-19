package com.simonvanendern.tracking.database.schemata.raw

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "step_counter_table")
data class StepsRaw(
    @PrimaryKey @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "day") val day: Date,
    @ColumnInfo(name = "steps") val steps: Int,
    @ColumnInfo(name = "processed") val processed : Boolean
)
